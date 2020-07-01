/**
 * Created on 15-Oct-2005
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
package transcraft.myaccountant.ui.schema;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;

import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * DB4O schema explorer. This explorer can operate in two modes:
 * 
 * 1. independent mode, which allows you to open a DB file and defrag it. To activate
 * this mode you need to pass a null value as the container parameter
 * 2. dependent mode, which is passed in an Existing ObjectContainer. In this
 * mode you can not do a defrag because the file is being opened by the calling object
 * 
 * @param parent
 * @param container
 * @param obj
 * @author david.tran@transcraft.co.uk
 */
public class SchemaViewer extends Composite {

    private SchemaTreeViewer treeViewer;
    @SuppressWarnings("unused")
	private SchemaEntryViewer entryViewer;
    private Object model;
    private ObjectContainer container;
    private Composite toolbar;
    private File currentFile;
    private StatusLineManager slm;
    private static final Logger log = getLogger(SchemaViewer.class);
    private Label schemaName;
    private Button defragButton;
    
    public SchemaViewer(Composite parent, ObjectContainer container, Object obj) {
        super(parent, SWT.BORDER);
        this.model = obj;
        this.container = container;
        this.createContents();
    }
    
    protected void	createContents() {
        this.setLayout(new GridLayout());
        if (this.container == null) {
		    this.toolbar = new Composite(this, SWT.SHADOW_ETCHED_IN);
		    GridLayout ly = new GridLayout();
		    ly.numColumns = 10;
		    this.toolbar.setLayout(ly);
		    this.toolbar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		    
		    final Button openButton = new Button(this.toolbar, SWT.PUSH);
		    openButton.setImage(ImageCache.get("opendb")); //$NON-NLS-1$
		    openButton.setToolTipText(Messages.getString("SchemaViewer.1")); //$NON-NLS-1$
		    openButton.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void	widgetSelected(SelectionEvent evt) {
		    		openDB();
		    	}
		    });
		
		    defragButton = new Button(this.toolbar, SWT.PUSH);
		    defragButton.setImage(ImageCache.get("defrag")); //$NON-NLS-1$
		    defragButton.setToolTipText(Messages.getString("SchemaViewer.3")); //$NON-NLS-1$
		    defragButton.addSelectionListener(new SelectionAdapter() {
		    	@Override
		    	public void	widgetSelected(SelectionEvent evt) {
		    		defrag();
		    	}
		    });
		    defragButton.setEnabled(false);
		    
		    schemaName = new Label(this, SWT.SHADOW_ETCHED_OUT);
        }
        
        SashForm sash = new SashForm(this, SWT.HORIZONTAL);
        sash.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.treeViewer = new SchemaTreeViewer(sash, this.container, this.model);
        ScrolledComposite scroll = new ScrolledComposite(sash, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
        scroll.setExpandHorizontal(true);
        scroll.setExpandVertical(true);
        scroll.setAlwaysShowScrollBars(false);
        this.entryViewer = new SchemaEntryViewer(scroll, this.treeViewer);
        sash.setWeights(new int[] { 40, 60 });
        sash.SASH_WIDTH = 5;
        
        if (this.container == null) {
	        this.slm = new StatusLineManager();
	        Control slmControl = this.slm.createControl(this);
	        slmControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	        slmControl.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_BACKGROUND));
	        slmControl.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
	        
	        this.getShell().addDisposeListener(new DisposeListener() {
	        	@Override
	        	public void	widgetDisposed(DisposeEvent evt) {
	        		closeDB();
	        	}
	        });
        }
    }
    
    /**
     * @return Returns the container.
     */
    public ObjectContainer getContainer() {
        return this.container;
    }
    /**
     * @param container The container to set.
     */
    public void setContainer(ObjectContainer container) {
        this.container = container;
        this.treeViewer.setContainer(this.container);
    }
    
    protected void	openDB() {
        FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
        fileDialog.setText(Messages.getString("SchemaViewer.4")); //$NON-NLS-1$
        fileDialog.setFilterPath(GUIUtil.getMemorisedPath(this.getClass()));
        String [] fileExt = new String[] { "*.tdb" }; //$NON-NLS-1$
        fileDialog.setFilterExtensions(fileExt);
        String selected = fileDialog.open();
        if (isBlank(selected)) {
            return;
        }
        File fp = new File(selected);
        this.openDB(fp);
    }
    
    protected void	openDB(File fp) {
        try {
            if (this.isOpenedDB()) {
                if (! GUIUtil.confirm(Messages.getString("SchemaViewer.7"), Messages.getString("SchemaViewer.8"))) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
            }
            
            this.closeDB();
            this.container = Db4oEmbedded.openFile(fp.getAbsolutePath());
            this.currentFile = fp;
            this.treeViewer.setContainer(this.container);
            this.schemaName.setText(fp.getAbsolutePath());
            // re-calculate the dimensions of the schemaName widget
	        int w = getClientArea().width;
            Point pt = schemaName.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		    schemaName.setSize(w, pt.y);
		    defragButton.setEnabled(true);
            GUIUtil.setMemorisedPath(this.getClass(), fp.getParent());
        } catch (Exception e) {
            GUIUtil.showError(Messages.getString("SchemaViewer.9"), Messages.getString("SchemaViewer.10") + fp.getName() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    protected boolean isOpenedDB() {
        return this.currentFile != null && this.container != null && ! this.container.ext().isClosed();
    }
    protected void	closeDB() {
        try {
	        if (this.isOpenedDB()) {
	            this.container.commit();
	            this.container.close();
	        }
	        this.container = null;
	        this.currentFile = null;
		    defragButton.setEnabled(false);
		    schemaName.setText("");
        } catch (Exception e) {
            log.error("closeDB():", e); //$NON-NLS-1$
        }
        try {
	        this.treeViewer.setContainer(this.container);
        } catch (Exception e) {}
    }
    protected void	defrag() {
        if (this.currentFile == null) {
            GUIUtil.showError(Messages.getString("SchemaViewer.13"), Messages.getString("SchemaViewer.14"), null); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        if (! GUIUtil.confirm(Messages.getString("SchemaViewer.15"), Messages.getString("SchemaViewer.16") + this.currentFile + "' ?")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return;
        }
        
        try {
	        this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
            this.setEnabled(false);

            new ProgressMonitorDialog(this.getShell()).run(false, false, new IRunnableWithProgress() {
                public void	run(IProgressMonitor monitor) {
	    	        File fp = currentFile;
	    	        closeDB();

	    	        new Defragment().run(fp.getAbsolutePath(), false, monitor);
	    	        openDB(fp);
                }
            });
	    } catch (Exception e) {
	        GUIUtil.showError(Messages.getString("SchemaViewer.18"), Messages.getString("SchemaViewer.19"), e); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
            this.setEnabled(true);
        }
    }
}
