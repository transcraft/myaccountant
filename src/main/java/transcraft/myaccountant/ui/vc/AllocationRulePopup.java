/**
 * Created on 08-Jul-2005
 *
 * Copyrights (c) Transcraft Trading Limited 2003-2005. All rights reserved.
 * 
 * Permission to use, copy, modify, and distribute this software and its
 * documentation for any purpose, without fee, and without a written
 * agreement, is hereby granted, provided that the above copyright notice, 
 * this paragraph and the following two paragraphs appear in all copies, 
 * modifications, and distributions.
 * 
 * IN NO EVENT SHALL WE BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
 * SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
 * ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
 * WE HAVE BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * WE SPECIFICALLY DISCLAIM ANY WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE. THE SOFTWARE AND ACCOMPANYING DOCUMENTATION, IF
 * ANY, PROVIDED HEREUNDER IS PROVIDED "AS IS". WE HAVE NO OBLIGATION
 * TO PROVIDE MAINTENANCE, SUPPORT, UPDATES, ENHANCEMENTS, OR
 * MODIFICATIONS.
 *
 * Unless otherwise specified below by individual copyright and usage information,
 * source code on this page is covered by the above Copyrights Notice.
 * 
 */
package transcraft.myaccountant.ui.vc;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.AllocationRuleMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.ui.TableRowCellEditor;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AllocationRulePopup extends Composite implements BOMListener {
	
	private static final int DEF_SIZE = 480;

    private TableRowCellEditor<?> cellEditorWindow;
    private Shell popup;
    private Combo combo;
    private AllocationRuleViewer viewer;
    private BOMService bomService;
    private AllocationRuleMeta metaProvider;
    private AllocationRule allocationRule;
    private int returnCode = SWT.NONE;
    
    /**
     * @param parent
     */
    public AllocationRulePopup(TableRowCellEditor<?> parent, BOMService bomService, AllocationRuleMeta provider, AllocationRule rule) {
        super(parent, SWT.BORDER);
        this.cellEditorWindow = parent;
        this.bomService = bomService;
        this.metaProvider = provider;
        this.allocationRule = rule;
        // we want to be able to enter the amount too in this mode
        MetaColumn<?> metaColumn = this.metaProvider.getColumn(AllocationRuleMeta.AMT_PROP);
        metaColumn.setVisible(true);
        metaColumn.setEnabled(true);
    }
    
    private static final int MAX_COLS = 10;
    
    protected void createContents() {
        if (this.popup != null && ! this.popup.isDisposed()) {
            return;
        }
	    Shell shell = this.cellEditorWindow.getShell();
	    this.popup = new Shell(shell, SWT.APPLICATION_MODAL);
	    GridLayout ly = new GridLayout();
	    ly.numColumns = MAX_COLS;
	    ly.horizontalSpacing = 2;
	    ly.verticalSpacing = 2;
	    ly.marginHeight = 2;
	    ly.marginWidth = 2;
	    this.popup.setLayout(ly);
        this.combo = new Combo(this.popup, SWT.DROP_DOWN|SWT.READ_ONLY);
        GridData grd = new GridData(GridData.FILL_HORIZONTAL);
        grd.horizontalSpan = MAX_COLS;
        this.combo.setLayoutData(grd);
        this.viewer = new AllocationRuleViewer(this.popup, this.bomService, this.metaProvider, this.allocationRule);
        // make sure the Viewer does not modify our rules in the DB, as we will be making a copy of the final allocation for our entry
        this.viewer.setAutoSave(false);
        grd = new GridData(GridData.FILL_BOTH);
        grd.horizontalSpan = MAX_COLS;
        this.viewer.setLayoutData(grd);
        this.combo.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                String sel = combo.getItem(combo.getSelectionIndex());
                AllocationRule rule = isBlank(sel) ? null : (AllocationRule)bomService.getGeneric(AllocationRule.class, "name", sel); //$NON-NLS-1$
                viewer.setRule(rule);
            }
        });
        final Button okButton = new Button(this.popup, SWT.PUSH);
        okButton.setImage(ImageCache.get("save")); //$NON-NLS-1$
        okButton.setToolTipText(Messages.getString("AllocationRulePopup.2")); //$NON-NLS-1$
        okButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                returnCode = SWT.OK;
                allocationRule = viewer.getRule();
            }
        });      
        final Button clearButton = new Button(this.popup, SWT.PUSH);
        clearButton.setImage(ImageCache.get("trash")); //$NON-NLS-1$
        clearButton.setToolTipText(Messages.getString("AllocationRulePopup.4")); //$NON-NLS-1$
        clearButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                if (! GUIUtil.confirm(Messages.getString("AllocationRulePopup.5"), Messages.getString("AllocationRulePopup.6"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                viewer.reset();
            }
        });      
        final Button undoButton = new Button(this.popup, SWT.PUSH);
        undoButton.setImage(ImageCache.get("undo_edit")); //$NON-NLS-1$
        undoButton.setToolTipText(Messages.getString("AllocationRulePopup.8")); //$NON-NLS-1$
        undoButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                if (! GUIUtil.confirm(Messages.getString("AllocationRulePopup.9"), Messages.getString("AllocationRulePopup.10"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                viewer.setRule(allocationRule);
            }
        });      
        final Button dismissButton = new Button(this.popup, SWT.PUSH);
        dismissButton.setImage(ImageCache.get("close")); //$NON-NLS-1$
        dismissButton.setToolTipText(Messages.getString("AllocationRulePopup.12")); //$NON-NLS-1$
        dismissButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                popup.close();
            }
        });      
                
        // initialise the dropdown list
        this.dataChanged(null);
        this.popup.addShellListener(new ShellAdapter() {
        	@Override
            public void	shellActivated(ShellEvent evt) {
                TableViewer tv = AllocationRulePopup.this.viewer.getTableViewer();
                tv.getTable().select(0);
                Object obj = ((StructuredSelection)tv.getSelection()).getFirstElement();
                if (obj != null) {
                	tv.editElement(obj, 0);
                }
            }
        });
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
	@Override
    public void dataChanged(BOMEvent event) {
        List<String> list = this.bomService.getMemorisedList("allocationRule"); //$NON-NLS-1$
        this.combo.removeAll();this.combo.add(""); //$NON-NLS-1$
        for (String item : list) {
            this.combo.add(item);
        }
    }
        
	public int open() {
	    this.createContents();
        Rectangle cellRect = this.cellEditorWindow.getBounds();
        cellRect.x = 0; cellRect.y = 0;
        Composite parent = this.cellEditorWindow.getParent().getParent();
        Rectangle parentRect = Display.getCurrent().map(parent, null, parent.getBounds());
        int width = Math.max((int)(parentRect.width * 0.65), DEF_SIZE);
        int height = Math.min((int)(parentRect.height * 0.5), DEF_SIZE);
        Rectangle rect = Display.getCurrent().map(this.cellEditorWindow, null, cellRect);
        int xoffset = rect.x > parentRect.x + (parentRect.width / 4) ? (rect.x + rect.width - width) : rect.x;
        Rectangle hintRect = new Rectangle(xoffset, rect.y + rect.height, width, height);
        if (! parentRect.contains(hintRect.x + hintRect.width, hintRect.y + hintRect.height)) {
            hintRect.y -= rect.height + height;
        }
        this.popup.setBounds(hintRect);
        this.viewer.setFocus();
        this.popup.open();
	    Display display = this.popup.getDisplay();
	    while (! this.popup.isDisposed()) {
	        if (!display.readAndDispatch()) {
	            display.sleep();
	        }
	        if (returnCode != SWT.NONE) {
		        if (! this.popup.isDisposed()) {
		   	        this.popup.dispose();
		        }
	        }
	    }
        return this.returnCode;
	}
	
    /**
     * @return Returns the allocationRule.
     */
    public AllocationRule getAllocationRule() {
        return allocationRule;
    }
    
	public void close() {
	    if (! this.popup.isDisposed()) {
	        this.popup.setVisible(false);
	    }
	}
	
	public boolean isVisible() {
	    return ! this.popup.isDisposed() && this.popup.isVisible();
	}
}
