/**
 * Created on 23-Jun-2020
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
package transcraft.myaccountant.ui;

import static org.apache.commons.lang3.StringUtils.capitalize;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.myaccountant.meta.AllocationRuleMeta;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.ui.vc.AllocationRulePopup;
import transcraft.myaccountant.ui.vc.MemorisedEntryPopup;
import transcraft.myaccountant.ui.vc.Messages;
import transcraft.myaccountant.utils.DateUtil;

public abstract class TableRowCellEditor<T extends AllocationRule> extends  Composite 
	implements KeyListener, ControlListener, FocusListener, MouseListener, TraverseListener {
	static private final Logger LOG = getLogger(TableRowCellEditor.class);

	protected final TableRowEditor<T> rowEditor;
    protected final MetaColumn<?> metaColumn;
    
    private Text	textControl;
    private MemorisedEntryPopup popup;
    private boolean activated;
    private CalendarCellDialog calPopup;
    
    /**
     * popup editor for a particular table cell
     * 
     * @param parent
     * @param rowEditor
     * @param style
     */
    public TableRowCellEditor(TableRowEditor<T> rowEditor, MetaColumn<?> metaColumn) {
        super(rowEditor, SWT.NONE);
		this.rowEditor = rowEditor;
        this.metaColumn = metaColumn;
        
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.horizontalSpacing = 0;
        ly.marginHeight = 0;
        ly.marginWidth = 0;
        ly.verticalSpacing = 0;
        
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        
        boolean createTextControl = true;
        /*
         * these popups need to be created first to ensure their icons are left aligned
         */
        if (this.metaColumn.getPrototype().equals(Date.class)) {
        	calPopup = new CalendarCellDialog(this.getShell());
            calPopup.setCellEditorWindow(this);
            final Label button = new Label(this, SWT.NONE);
            button.setImage(ImageCache.get("calendar")); //$NON-NLS-1$
            button.setLayoutData(new GridData(GridData.FILL_VERTICAL));
            button.setToolTipText(Messages.getString("LedgerEntryEditor.0")); //$NON-NLS-1$
            button.addListener(SWT.MouseDown, new Listener() {
            	@Override
                public void	handleEvent(Event evt) {
                    popupCalendar();
                }
            });
        } else if (this.metaColumn.getName().equals(LedgerEntryMeta.ALLOC_PROP)) {
            final Label button = new Label(this, SWT.NONE);
            button.setImage(ImageCache.get("allocate")); //$NON-NLS-1$
            button.setLayoutData(new GridData(GridData.FILL_VERTICAL));
            button.setToolTipText(Messages.getString("LedgerEntryEditor.5")); //$NON-NLS-1$
            button.addListener(SWT.MouseDown, new Listener() {
            	@Override
                public void	handleEvent(Event evt) {
                    popupAllocator();
                }
            });
            gridData.horizontalAlignment = GridData.BEGINNING;
            gridData.verticalAlignment = GridData.BEGINNING;
            createTextControl = false;
        } else if (this.metaColumn.getName().equals(LedgerEntryMeta.BAL_PROP)) {
        	createTextControl = false;
        }
        
        if (createTextControl) {
        	this.textControl = new Text(this, SWT.NONE);
        	this.popup = new MemorisedEntryPopup(this);
            this.textControl.addFocusListener(this);
            this.textControl.addMouseListener(this);
            this.textControl.setLayoutData(gridData);
        }
        
        for (int i = 0; i < this.rowEditor.table.getColumnCount(); i++) {
            TableColumn tc = this.rowEditor.table.getColumn(i);
            if (tc.getData().equals(this.metaColumn.getName())) {
                // make sure we resize when the column is resized
                tc.addControlListener(this);
                break;
            }
        }

        this.setLayout(ly);
    }
    
    protected void popupAllocator() {
        AllocationRulePopup rulePopup = new AllocationRulePopup(this, 
        		this.rowEditor.bomService, 
        		new AllocationRuleMeta(this.rowEditor.getModel()), 
        		this.rowEditor.getModel()
        );
        int returnCode = rulePopup.open();
        if (returnCode == SWT.OK) {
            this.rowEditor.setModified(true);
            this.rowEditor.getModel().copyRule(rulePopup.getAllocationRule());
            this.rowEditor.saveRowData();
        }
    }
    
    protected void	popupCalendar() {
    	if (calPopup.isOpened()) {
    		calPopup.close();
    		return;
    	}
    	
        calPopup.setValue(this.getText(false));
        calPopup.open();
        Calendar cal = calPopup.getValue();
        try {
            String value = DateUtil.format(cal.getTime());
            if (! value.equals(this.getText(false))) {
                this.rowEditor.setModified(true);
                this.setText(value);
                this.modifyCell();
            }
        } catch (Exception e) {
            LOG.error("popupCalendar():", e); //$NON-NLS-1$
        }
    }
    
    protected void	activate() {
    	// start listening to keystrokes
    	if (this.textControl != null) {
    		if (!this.activated) {
    			LOG.debug("{}.activate({})", this, this.textControl.getText());
    			this.textControl.addKeyListener(this);
    			this.textControl.addTraverseListener(this);
    			this.activated = true;
    		}
    	}
    	this.setEnabled(true);
    }
    
    public void insert(String ch) {
    	if (this.textControl != null) {
    		this.textControl.insert(ch);
    	}
    }
    
    public void	deactivate() {
    	if (this.textControl != null) {
            // stop listening to keystrokes
    		this.textControl.removeKeyListener(this);
    		this.textControl.removeTraverseListener(this);
    	}
        this.activated = false;
    }
    
    @Override
    public void	keyTraversed(TraverseEvent evt) {
    	LOG.debug(String.format("keyTraversed(char=%c,code=0x%x,mask=0x%x)", evt.character, evt.keyCode, evt.stateMask));
        switch(evt.keyCode) {
        case SWT.TAB:
        	goToNextField(evt.stateMask);
        	break;
        case SWT.ARROW_UP:
        	if (! this.metaColumn.getPrototype().equals(Date.class) && this.popup != null) {
                String matched = this.popup.advance(-1);
                if (matched != null) {
                    this.updateValueFromPopup(matched, 0);
                    evt.doit = false;
                    break;
                }
        	}
            if (! this.rowEditor.isModified() && this.rowEditor.table.getSelectionIndex() > 0) {
                this.gotoRow(this.rowEditor.table.getSelectionIndex() - 1);
            }
            break;
        case SWT.ARROW_DOWN:
            if ((evt.stateMask & SWT.CONTROL) != 0 && 
            this.metaColumn.getPrototype().equals(Date.class) && this.popup != null) {
                this.popupCalendar();
                evt.doit = false;
            } else {
            	if (this.popup != null) {
            		String matched = this.popup.advance(1);
            		if (matched != null) {
            			this.updateValueFromPopup(matched, 0);
            			evt.doit = false;
            			break;
            		}
            	}
            }
        	if (! this.rowEditor.isModified() && this.rowEditor.table.getSelectionIndex() < (this.rowEditor.table.getItemCount() - 1)) {
                this.gotoRow(this.rowEditor.table.getSelectionIndex() + 1);
            }
        	break;
        case SWT.CR:
        case SWT.KEYPAD_CR:
        case SWT.TRAVERSE_RETURN:
            if (! this.rowEditor.isModified()) {
                if (this.rowEditor.table.getSelectionIndex() < (this.rowEditor.table.getItems().length - 1)) {
                	int idx = this.rowEditor.table.getSelectionIndex() + 1;
                	LOG.debug("go to next row {}", idx);
                    this.gotoRow(idx);
                }
            } else {
                save();
            }
        	break;
    	default:
    		break;
        }
        if (ignoreKeyEvent(evt)) {
        	evt.doit = false;
        }
    }
    
    protected abstract boolean ignoreKeyEvent(KeyEvent evt);

	@Override
	public void	keyReleased(KeyEvent evt) {
    	LOG.debug(String.format("keyReleased(char=%c,code=0x%x,mask=0x%x)", evt.character, evt.keyCode, evt.stateMask));
        switch (evt.keyCode) {
        case SWT.ESC:
            this.processEscape();
        	break;
        case SWT.HOME:
            if (! this.rowEditor.isModified()) {
                this.gotoRow(0);
            }
        	break;
        case SWT.END:
            if (! this.rowEditor.isModified()) {
                this.gotoRow(this.rowEditor.table.getItemCount() - 1);
            }
        	break;
        case SWT.PAGE_UP:
            if (! this.rowEditor.isModified()) {
                int page = this.rowEditor.table.getSize().y / this.rowEditor.table.getItemHeight();
                int idx = Math.max(this.rowEditor.table.getSelectionIndex() - page, 0);
                this.gotoRow(idx);
            }
        	break;
        case SWT.PAGE_DOWN:
            if (! this.rowEditor.isModified()) {
                int page = this.rowEditor.table.getSize().y / this.rowEditor.table.getItemHeight();
                int idx = Math.min(this.rowEditor.table.getSelectionIndex() + page, this.rowEditor.table.getItemCount() - 1);
                this.gotoRow(idx);
            }
        	break;
        default:
            if ((evt.stateMask & SWT.ALT) != 0) {
                switch(GUIUtil.keyCodeToChar(evt)) {
                case 'L':
                    popupAllocator();
                    break;
                }
                return;
            }
            if (!Character.isISOControl(evt.character) || evt.keyCode == SWT.BS) {
                this.rowEditor.setModified(true);
                if (metaColumn.getList().isPresent() && metaColumn.getList().get().size() > 0) {
                    int advanceCount = evt.keyCode == SWT.BS ? -1 : 0;
                    String str = this.getText(true);
                    if (this.popup != null) {
	                    String acStr = buildAutoCompletedString(str, advanceCount, 
	                    		this.popup.match(str, advanceCount));
	                    if (acStr != null) {
	                        this.updateValueFromPopup(acStr, advanceCount);
	                    }
                    }
                }
            }
        	break;    
        }
    }

	private String buildAutoCompletedString(String str, int advanceCount, String matched) {
        if (matched != null) {
        	StringBuilder builder = new StringBuilder();
        	builder.append(str.substring(0, str.length() + advanceCount));
        	builder.append(matched.substring(str.length() + advanceCount));
        	LOG.debug("'{}'+'{}'=>'{}'", str, matched, builder);
        	return capitalize(builder.toString());
        }
        return null;
	}
	
	protected abstract void	goToNextField(int mask);

	
    private void gotoRow(int idx) {
	    this.rowEditor.table.deselectAll();
        this.rowEditor.table.select(idx);
        this.rowEditor.table.showSelection();
        this.refresh();
    }
    
    public void	processEscape() {
        this.rowEditor.setModified(false);
    	this.refresh();
    	this.getParent().setVisible(false);
        Control [] items = this.getParent().getTabList();
        for (int i = 0; i < items.length; i++) {
        	if (items[i] instanceof TableRowCellEditor) {
        		TableRowCellEditor<?> ce = (TableRowCellEditor<?>)items[i];
                ce.deactivate();
                ce.modifyCell();
                ce.dismissPopup();
        	}
        }
    	this.rowEditor.table.setFocus();
    }
    
    @Override
	public void mouseDoubleClick(MouseEvent arg0) {
	}

	@Override
	public void mouseDown(MouseEvent arg0) {
	}

	@Override
	public void mouseUp(MouseEvent evt) {
		if (this.textControl != null) {
			this.textControl.selectAll();
		}
	}

	@Override
	public void	controlMoved(ControlEvent evt) {
    }
	
	@Override
    public void	controlResized(ControlEvent evt) {
        this.rowEditor.doInternalLayout();
    }

	@Override
	public void	keyPressed(KeyEvent evt) {
    }
	
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
     */
	@Override
    public void focusGained(FocusEvent e) {
        Control [] items = this.getParent().getTabList();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != this) {
            	if (items[i] instanceof TableRowCellEditor) {
            		TableRowCellEditor<?> ce = (TableRowCellEditor<?>)items[i];
                    ce.deactivate();
                    ce.modifyCell();
                    ce.dismissPopup();
            	}
            }
        }

        Object value = this.rowEditor.metaProvider
        		.getValue(this.rowEditor.getModel(), metaColumn.getName(), this.rowEditor.getMetaSelector());
        LOG.debug("focusGained({}) on {} => {}", e, this.metaColumn.getName(), value);
        if (this.textControl != null) {
        	this.textControl.setText(value != null ? value.toString() : ""); //$NON-NLS-1$
        }
		if (this.popup != null && !this.popup.isVisible()) {
			showMemorisedPopup();
		}
		this.activate();
		if (this.textControl != null) {
			this.textControl.selectAll();
			this.textControl.setFocus();
		}
    }
    
    @Override
    public void focusLost(FocusEvent arg0) {
    	Object oldValue = this.rowEditor.metaProvider
    			.getValue(this.rowEditor.getModel(), this.metaColumn.getName(), 
    					this.rowEditor.getMetaSelector());
    	if (this.textControl != null) {
    		if (!this.textControl.getText().equals(oldValue)) {
    			this.rowEditor.setModified(true);
    			modifyCell();
    			/*
    			 * now re-read the value from the meta layer, just in case it has been auto-formatted
    			 */
    			Object value = this.rowEditor.metaProvider.getValue(this.rowEditor.getModel(),
    					this.metaColumn.getName(), this.rowEditor.getMetaSelector());
    			if (!this.textControl.getText().equals(value)) {
    				this.textControl.setText(value != null ? value.toString() : "");
    			}
    			LOG.debug("focusLost({}):{}=>{}", this.metaColumn.getName(), oldValue, this.textControl.getText());
    		}
    	}
    }

	protected void	showMemorisedPopup() {
        if (this.metaColumn.getList().isPresent() && this.metaColumn.getList().get().size() > 0 &&
        		this.popup != null && !this.popup.isVisible()) {
        	this.activate();
            this.popup.open();
        	this.popup.setValue(this.getText(false));
        }
    }
    
	public void	updateValueFromPopup(String value, int advanceCount) {
    	if (this.textControl != null) {
            int pos = this.textControl.getSelection().x + advanceCount;
            this.rowEditor.setModified(true);
            this.setText(value);
            this.modifyCell();
            this.textControl.setSelection(pos, value.length());
    	}
    }
    
    protected void	modifyCell() {
        try {
            Object oldValue = this.rowEditor.metaProvider.getValue(this.rowEditor.getModel(),
            		this.metaColumn.getName(), this.rowEditor.getMetaSelector());
            if (oldValue == null || (this.textControl != null && !this.getText(false).equals(oldValue))) {
            	this.rowEditor.metaProvider.setValue(this.rowEditor.getModel(),
            			this.rowEditor.getMetaSelector(), this.metaColumn.getName(), this.getText(false));                	
            }                
        } catch (ParseException e) {
            LOG.error(String.format("modifyCell(%s)", this.getText(false), e));
        }
    }
    
    protected void	save() {
        this.deactivate();
        this.modifyCell();
        this.rowEditor.saveRowData();
    }
    
    protected void	refresh() {
        TableItem [] items = this.rowEditor.table.getSelection();
        if (items.length > 0) {
            this.rowEditor.loadModelForTableItem(items[0]);
        }
    }
    
    /**
     * @return Returns the property.
     */
    public void	setText(String text) {
    	if (this.textControl != null) {
            this.textControl.setText(text);
    	}
    }
    
    public String	getText(boolean upToCaret) {
    	if (this.textControl != null) {
            String str = this.textControl.getText();
            if (upToCaret) {
                int idx = this.textControl.getSelection().x;
                LOG.debug("getText({},{})=>{}", str, idx, str.substring(0,idx));
                str = str.substring(0, idx);
            }
            return str;
    	}
    	return null;
    }
    
    public void	selectAll() {
    	if (this.textControl != null) {
    		this.textControl.selectAll();
    	}
    }
    
    public void	setEnabled(boolean flag) {
    	if (this.textControl != null) {
    		this.textControl.setEnabled(flag);
    	}
        super.setEnabled(flag);
    }
    
    protected void refreshPopupContents(List<String> list) {
    	if (this.popup != null) {
    		this.popup.setList(list);
    	}
    }
    
    public void dismissPopup() {
    	if (this.popup != null) {
    		this.popup.close();
    	}
    	if (this.calPopup != null) {
    		this.calPopup.close();
    	}
    }
    
	@Override
	public String toString() {
		return String.format("CellEditor(%s)", metaColumn.getName());
	}        
}