/**
 * Created on 04-Oct-2005
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

import java.io.File;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.BatchReportMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.BatchReport;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * Viewer for BatchReport
 * 
 * @author david.tran@transcraft.co.uk
 */
public class BatchReportViewer extends Composite implements BOMListener, ICheckStateListener {

    protected BOMService bomService;
    protected BatchReportMeta metaProvider; 
    private BatchReport batch;
    private CheckboxTableViewer tableViewer;
    protected Composite actionBar;
    protected Text outputPath;
    
    /**
     * 
     */
    public BatchReportViewer(Composite parent, BOMService bomService, BatchReportMeta provider, BatchReport batch) {
        super(parent, SWT.FLAT);
        this.bomService = bomService;
        this.metaProvider = provider;
        this.batch = batch;
        this.createContents();
    }
    
    protected void createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.verticalSpacing = 10;
        ly.marginWidth = 10;
        this.setLayout(ly);

        Label title = new Label(this, SWT.SHADOW_IN);
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_CENTER);
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        title.setLayoutData(gd);
        title.setFont(GUIUtil.titleFont);
        title.setText(this.batch.getName());
        
        Label label = new Label(this, SWT.SHADOW_IN);
        label.setText(Messages.getString("BatchReportViewer.0")); //$NON-NLS-1$
        final Text desc = new Text(this, SWT.BORDER);
        desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        desc.setText(this.batch.getDescription());
        desc.addModifyListener(new ModifyListener() {
        	@Override
            public void	modifyText(ModifyEvent evt) {
                batch.setDescription(desc.getText());
            }
        });

        this.tableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI);
        gd = new GridData(GridData.FILL_VERTICAL|GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        gd.grabExcessHorizontalSpace = true;
        this.tableViewer.getTable().setLayoutData(gd);
        
        final Table table = this.tableViewer.getTable();
        table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        this.tableViewer.setContentProvider(new BatchReportContentProvider());
        this.tableViewer.setLabelProvider(new BatchReportLabelProvider());
        this.installContextMenu(table);
        
        TableColumn tc = new TableColumn(table, SWT.LEFT);
        tc.setWidth(480);
        tc.setText(Messages.getString("BatchReportViewer.1")); //$NON-NLS-1$

        Composite fileBar = new Composite(this, SWT.SHADOW_ETCHED_IN);
        fileBar.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        GridLayout ly1 = new GridLayout();
        ly1.numColumns = 3;
        fileBar.setLayout(ly1);
        label = new Label(fileBar, SWT.NONE);
        label.setText(Messages.getString("BatchReportViewer.2")); //$NON-NLS-1$
        this.outputPath = new Text(fileBar, SWT.BORDER);
        this.outputPath.setText(GUIUtil.getMemorisedPath(BatchReportViewer.class));
        
        Button browseButton = new Button(fileBar, SWT.PUSH);
        browseButton.setImage(ImageCache.get("folder")); //$NON-NLS-1$
        browseButton.setToolTipText(Messages.getString("BatchReportViewer.4")); //$NON-NLS-1$
        browseButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                DirectoryDialog dirDialog = new DirectoryDialog(getShell(), SWT.OPEN);
                dirDialog.setText(Messages.getString("BatchReportViewer.5")); //$NON-NLS-1$
                dirDialog.setFilterPath(GUIUtil.getMemorisedPath(BatchReportViewer.class));
	            
	            String path = dirDialog.open();
	            if (path == null) {
	                return;
	            }
	            
	            GUIUtil.setMemorisedPath(BatchReportViewer.class, new File(path).getAbsolutePath());
	            outputPath.setText(path);
            }
        });
        this.actionBar = new Composite(this, SWT.SHADOW_OUT);
        //this.actionBar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        GridLayout ly2 = new GridLayout();
        ly2.numColumns = 10;
        this.actionBar.setLayout(ly2);
        final Button button = new Button(this.actionBar, SWT.SHADOW_ETCHED_OUT);
        button.setImage(ImageCache.get("report")); //$NON-NLS-1$
        button.setToolTipText(Messages.getString("BatchReportViewer.7")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                runBatch();
            }
        });
        final Button saveButton = new Button(this.actionBar, SWT.SHADOW_ETCHED_OUT);
        saveButton.setImage(ImageCache.get("save")); //$NON-NLS-1$
        saveButton.setToolTipText(Messages.getString("BatchReportViewer.9")); //$NON-NLS-1$
        saveButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                save();
            }
        });

        this.tableViewer.setInput(this.batch);

        this.tableViewer.addCheckStateListener(this);
        
        // refresh the checked states
        this.dataChanged(null);
        this.bomService.addListener(this);

        GUIUtil.setupTableAutoSized(table);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent event) {
        if (event == null || event.entity.equals(this.batch)) {
            this.tableViewer.setAllChecked(false);
            GenericReport<?> [] reports = this.batch.getReports();
            for (int i = 0; i < reports.length; i++) {
                this.tableViewer.setChecked(reports[i], true);
            }
            this.tableViewer.refresh();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ICheckStateListener#checkStateChanged(org.eclipse.jface.viewers.CheckStateChangedEvent)
     */
    public void checkStateChanged(CheckStateChangedEvent evt) {
        Object elem = evt.getElement();
        if (elem instanceof GenericReport) {
            GenericReport<?> report = (GenericReport<?>)elem;
            this.updateReport(report, evt.getChecked());
        }
    }
    
    protected void	updateReport(GenericReport<?> report, boolean flag) {
        this.reportBatchUpdate(Arrays.asList(new GenericReport<?>[] { report }).iterator(), flag);
    }
    protected void	reportBatchUpdate(Iterator<?> iter, boolean flag) {
        for (; iter.hasNext(); ) {
            GenericReport<?> report = (GenericReport<?>)iter.next();
            if (flag) {
                this.batch.addReport(report);
            } else {
                this.batch.removeReport(report);
            }
        }
        this.dataChanged(null);
    }
    public void	runBatch() {
        this.checkPath();
    }
    public void save() {
        this.batch.setDestination(this.outputPath.getText());
        this.bomService.storeGeneric(this.batch, "name", this.batch.getName()); //$NON-NLS-1$
    }
    protected void	checkPath() {
        String path = this.outputPath.getText();
        File fp = new File(path);
        if (fp.exists()) {
	        if (! fp.isDirectory()) {
	            throw new RuntimeException(path + Messages.getString("BatchReportViewer.11")); //$NON-NLS-1$
	        }
        } else {
            if (! GUIUtil.confirm(Messages.getString("BatchReportViewer.12"), Messages.getString("BatchReportViewer.13") + path + " ?")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                throw new RuntimeException(path + Messages.getString("BatchReportViewer.15")); //$NON-NLS-1$
            }
        }
    }
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("BatchReportViewer.16")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		tableViewer.setAllChecked(true);
        		reportBatchUpdate(Arrays.asList(tableViewer.getCheckedElements()).iterator(), true);
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("BatchReportViewer.18")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		BatchReportContentProvider provider = (BatchReportContentProvider)tableViewer.getContentProvider();
        		Object [] elems = provider.getElements(batch);
        		reportBatchUpdate(Arrays.asList(elems).iterator(), false);
        		tableViewer.refresh();
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("BatchReportViewer.20")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
                reportBatchUpdate(sel.iterator(), true);
            }
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("BatchReportViewer.21")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
                reportBatchUpdate(sel.iterator(), false);
            }
        });
        final MenuItem reportMI = new MenuItem(menu, SWT.PUSH);
        reportMI.setText(Messages.getString("BatchReportViewer.22")); //$NON-NLS-1$
        reportMI.setImage(ImageCache.get("report")); //$NON-NLS-1$
        reportMI.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                editReport();
            }
         });
        table.setMenu(menu);
        
        table.addMouseListener(new MouseAdapter() {
        	@Override
            public void	mouseDown(MouseEvent evt) {
                switch (evt.button) {
                case 3:
                    break;
                default:
                    return;
                }
                
                StructuredSelection sel = (StructuredSelection)tableViewer.getSelection();
                if (! sel.isEmpty()) {
                    Object elem = sel.getFirstElement();
                    reportMI.setEnabled(tableViewer.getChecked(elem));
                }
            }
        });
    }
    
    public void	editReport() {
        
    }
    
    public class BatchReportContentProvider implements IStructuredContentProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
            return ReportUtil.getAvailableReports(bomService, false);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    public class BatchReportLabelProvider implements ITableLabelProvider, IColorProvider {
        private List<ILabelProviderListener> listeners = Lists.newArrayList();

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        @Override
        public Color getBackground(Object element) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        @Override
        public Color getForeground(Object element) {
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return ImageCache.get("report"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(final Object element, int columnIndex) {
            BaseGenericReport<?> report = null;
            if (element instanceof TableItem) {
                report = (BaseGenericReport<?>)((TableItem)element).getData();
            } else {
            	report = (BaseGenericReport<?>)element;
            }
            return report.getName();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void addListener(ILabelProviderListener listener) {
            this.listeners.add(listener);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean isLabelProperty(Object element, String property) {
            MetaColumn<?> [] columns = metaProvider.getColumns();
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].getName().equals(property)) {
                    return ! columns[i].isEnabled();
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void removeListener(ILabelProviderListener listener) {
            this.listeners.remove(listener);
        }
    }    
}
