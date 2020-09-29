/**
 * Created on 15-Jul-2005
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.BookKeeper.bom.TaxBandEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.InvoiceMeta1;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.pref.TaxBandHelper;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceItemViewer extends TableMetaViewer<Invoice, InvoiceItem> 
	implements BOMListener, ICellEditorValidator {
    
    private CheckboxTableViewer tableViewer;
    private Invoice invoice;
    private MenuItem miDel;
    private Label title;
    private boolean modified;
    
    // for stand alone mode. In embedded mode, this value should be set to false
    private boolean autoSave = true;
    
    /**
     * @param parent
     * @param obj
     * @param provider
     */
    public InvoiceItemViewer(Composite parent, BOMService bomService, MetaProvider<InvoiceItem> provider, Invoice invoice) {
        super(parent, bomService, provider, invoice);
        this.createContents();
    }
    
	protected void	createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 1;
        ly.verticalSpacing = 10;
        ly.horizontalSpacing = 0;
        ly.marginWidth = 0;
        this.setLayout(ly);

        this.title = new Label(this, SWT.BORDER);
        this.title.setFont(GUIUtil.boldFont);
        title.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        this.tableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER|SWT.FULL_SELECTION);
        this.tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final Table table = this.tableViewer.getTable();
        table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // set up the Table control using the MetaProvider
        this.setupTable(this.tableViewer);

        // set up validate for allocation percentage
        for (int i = 0; i < this.metaProvider.getColumns().length; i++) {
            if (this.tableViewer.getCellEditors()[i] != null) {
                this.tableViewer.getCellEditors()[i].setValidator(this);
            }
        }
        
        this.tableViewer.setContentProvider(new InvoiceItemContentProvider());
        this.tableViewer.setLabelProvider(new InvoiceItemLabelProvider());
        this.tableViewer.setCellModifier(new InvoiceItemCellModifier());
        this.installContextMenu(table);
        
        this.bomService.addListener(this);

        // load the list for the Account dropdown
        this.dataChanged(null);
        // now set up the items
        this.setInvoice((Invoice)this.model);
        GUIUtil.setupTableAutoSized(table);
    }
    
    /**
     * implements the ICellEditorValidator
     * 
     * @param obj
     */
	@Override
    public String isValid(Object obj) {
        return null;
    }
    
    /**
     * @return Returns the autoSave.
     */
    protected boolean isAutoSave() {
        return autoSave;
    }
    
    /**
     * @param autoSave The autoSave to set.
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
    
    /**
     * @return Returns the rule.
     */
    public Invoice getInvoice() {
        return this.invoice;
    }
    
    /**
     * @param rule The rule to set.
     */
    public void setInvoice(Invoice invoice) {
        if (this.invoice == null) {
            this.invoice = invoice;
        } else {
            this.invoice.copy(invoice);
        }
        if (invoice != null) {
            this.title.setText(invoice.getReference());
        }
        this.tableViewer.setInput(this.invoice);
        this.setModified(false);
        this.tableViewer.refresh();
    }
    
    public void	reset() {
        this.invoice.reset();
        this.setModified(true);
        if (isAutoSave()) {
            // auto save this value to the DB. In embedded mode leave it to the parent to save
            this.bomService.storeGeneric(this.invoice, InvoiceMeta1.REF_PROP, invoice.getReference());
        }
        this.tableViewer.refresh();
    }
    
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("InvoiceItemViewer.0")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		tableViewer.setAllChecked(true);
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("InvoiceItemViewer.1")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.setAllChecked(false);
            }
         });
        miDel = new MenuItem(menu, SWT.PUSH);
        miDel.setText(Messages.getString("InvoiceItemViewer.2")); //$NON-NLS-1$
        miDel.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                Object [] selectedItems = tableViewer.getCheckedElements();
                for (int i = 0; i < selectedItems.length; i++) {
                    InvoiceItem item = (InvoiceItem)selectedItems[i];
                    if (item.getDescription().trim().length() > 0) {
                        invoice.removeItem(item);
                    }
                }
                if (isAutoSave()) {
                    store();
                } else {
                    setModified(true);
                }
                tableViewer.refresh();
            }
        });
        table.setMenu(menu);
        table.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseDown(MouseEvent evt) {
                switch (evt.button) {
                case 3:
                    miDel.setEnabled(tableViewer.getCheckedElements().length > 0);
                    break;
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.op == BOMEvent.OP_BROADCAST || evt.entity instanceof Invoice) {
            if (evt != null && this.invoice.equals(evt.entity)) {
                this.invoice = (Invoice)evt.entity;
            }
            this.tableViewer.refresh();
        }
    }
    
    /**
     * @return Returns the modified.
     */
    protected boolean isModified() {
        return modified;
    }
    
    /**
     * @param modified The modified to set.
     */
    protected void setModified(boolean modified) {
        this.modified = modified;
    }
    
    public void	store() {
        if (this.isAutoSave()) {
            // auto save this value to the DB. In embedded mode leave it to the parent to save
            bomService.storeGeneric(invoice, InvoiceMeta1.REF_PROP, invoice.getReference());
        }
        setModified(false);
    }
    
    public class InvoiceItemContentProvider implements IStructuredContentProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
        	// dummy new empty row
            InvoiceItem item = new InvoiceItem();
            TaxBandEntry tb = new TaxBandHelper().getDefaultBand();
            item.setTaxBand(tb != null ? tb.getCode() : "S");
            return Stream.concat(Arrays.stream(invoice.getItems()), Stream.of(item)).toArray();
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
    
    public class InvoiceItemLabelProvider implements ITableLabelProvider, IColorProvider {
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
        public Image getColumnImage(final Object element, int columnIndex) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(final Object element, int columnIndex) {
        	InvoiceItem item = null;
            if (element instanceof TableItem) {
                item = (InvoiceItem)((TableItem)element).getData();
            } else {
            	item = (InvoiceItem)element;
            }
            Object value = metaProvider.getValue(item, metaProvider.getColumns()[columnIndex].getName(), null);
            return value != null ? value.toString() : ""; //$NON-NLS-1$
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

    public class InvoiceItemCellModifier implements ICellModifier {
        
            /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
         */
        @Override
        public Object getValue(final Object element, String property) {
        	InvoiceItem item = null;
            if (element instanceof TableItem) {
                item = (InvoiceItem)((TableItem)element).getData();
            } else {
            	item = (InvoiceItem)element;
            }
            Object value = metaProvider.getValue(item, property, null);
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList().isPresent()) {
                return Integer.valueOf(column.getListSelectionIndex(value));
            }
            return value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
         */
        @Override
        public void modify(final Object element, String property, final Object value) {
            if (value == null) {
                // failed validation
                return;
            }
            InvoiceItem item = null;
            if (element instanceof TableItem) {
                item = (InvoiceItem)((TableItem)element).getData();
            } else {
            	item = (InvoiceItem)element;
            }
            
            Object newValue = value;
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList().isPresent()) {
                newValue = column.getList().map(l -> l.get(Integer.parseInt(value.toString()))).orElse(null);
            }
            try {
                metaProvider.setValue(item, null, property, newValue);
                invoice.addItem(item);
                if (isAutoSave()) {
                    store();
                } else {
                    setModified(true);
                }
            } catch (Exception e) {
                GUIUtil.showError(Messages.getString("InvoiceItemViewer.4"), Messages.getString("InvoiceItemViewer.5") + property + Messages.getString("InvoiceItemViewer.6") + value, e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            tableViewer.refresh();
        }
    }
}
