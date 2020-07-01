/**
 * Created on 13-Oct-2005
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



import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
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
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMHelper;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.BookKeeper.service.InvoiceHelper;
import transcraft.myaccountant.meta.InvoiceListMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.InvoiceListReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.action.SearchParameter;
import transcraft.myaccountant.ui.action.SearchParametersDialog;
import transcraft.myaccountant.ui.pref.TaxBandHelper;
import transcraft.myaccountant.utils.DateUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * Invoices list
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceListViewer extends TableMetaViewer<Invoice, Invoice> implements BOMListener, KeyListener {
    private TableViewer tableViewer;
    List<Invoice> cachedEntries = Lists.newArrayList();
    
    /**
     * @param parent
     * @param bomService
     * @param provider
     * @param obj
     */
    public InvoiceListViewer(Composite parent, BOMService bomService,
            MetaProvider<Invoice> provider, Object obj) {
        super(parent, bomService, provider, obj);
        this.createContents();
    }
    
	public void	createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 1;
        ly.verticalSpacing = 10;
        this.setLayout(ly);

        Label title = new Label(this, SWT.SHADOW_IN);
        title.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        title.setFont(GUIUtil.titleFont);
        title.setText(Messages.getString("InvoiceListViewer.0")); //$NON-NLS-1$

        this.tableViewer = new TableViewer(this, SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI);
        this.tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final Table table = this.tableViewer.getTable();
        table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // set up the Table control using the MetaProvider
        this.setupTable(this.tableViewer);

        this.tableViewer.setContentProvider(new InvoiceListContentProvider(this));
        this.tableViewer.setLabelProvider(new InvoiceListLabelProvider(this));
        
        this.installContextMenu(this.tableViewer.getTable());

        /*
         * keyboard handler
         */
        table.addKeyListener(this);
        
        /*
         * double click to select an entry
         */
        this.tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetDefaultSelected(SelectionEvent evt) {
                gotoEntry();
            }
        });
        
        // initialise the dropdown list
        this.bomService.addListener(this);
        this.tableViewer.setInput(this.model);
        // auto-scroll to the last entry
        if (this.cachedEntries.size() > 2) {
        	int n = this.cachedEntries.size() - 2;
        	Invoice o = this.cachedEntries.get(n);
        	ArrayList<Invoice> list = Lists.newArrayList();
        	list.add(o);
        	this.tableViewer.setSelection(new StructuredSelection(list), true);
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.tableViewer.getTable().setFocus();
        }
    }
    /**
     * accessor for use by the ContentProvider and LabelProvider
     * @return meta provider
     */
    MetaProvider<Invoice>	getMetaProvider() {
        return this.metaProvider;
    }
    /**
     * go to the entry associated with an invoice. This involves asking the TreeViewer to open
     * the relevant Account tab and then ask the LedgerEntryViewer to zoom to the relevant
     * row for us
     */
    protected void	gotoEntry() {
        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
        if (sel.isEmpty()) {
            return;
        }
        Invoice invoice = (Invoice)sel.getFirstElement();
        this.gotoEntry(invoice);
    }
    /**
     * go to the entry associated with an invoice. This involves asking the TreeViewer to open
     * the relevant Account tab and then ask the LedgerEntryViewer to zoom to the relevant
     * row for us
     */
    protected void	gotoEntry(Invoice invoice) {
        if (invoice.getAccountReference() == null) {
            return;
        }
        SearchParameter param = new SearchParameter(invoice.getAccountReference(), LedgerEntryViewer.class);
        param.addCriterium(this.metaProvider.getPrimaryKey(), invoice.getReference());
        LedgerTreeViewer treeViewer = GUIUtil.findTreeViewer(this);
        if (treeViewer != null) {
            treeViewer.doFind(param);
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    @Override
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.op == BOMEvent.OP_BROADCAST || evt.entity instanceof Invoice) {
            this.tableViewer.refresh();
        }
    }
    
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        final MenuItem zoomMi = new MenuItem(menu, SWT.PUSH);
        zoomMi.setText(Messages.getString("InvoiceListViewer.1")); //$NON-NLS-1$
        zoomMi.setImage(ImageCache.get("invoice")); //$NON-NLS-1$
        zoomMi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                LedgerTreeViewer treeViewer = GUIUtil.findTreeViewer(table);
                if (treeViewer != null) {
                    TableItem [] items = tableViewer.getTable().getSelection();
                    if (items.length == 1) {
                        treeViewer.selectTab(items[0].getData());
                    }
                }
            }
        });
        final MenuItem cloneMi = new MenuItem(menu, SWT.PUSH);
        cloneMi.setText(Messages.getString("InvoiceListViewer.3")); //$NON-NLS-1$
        cloneMi.setImage(ImageCache.get("clone")); //$NON-NLS-1$
        cloneMi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                templateClone();
            }
        });
        MenuItem unpaidMi = new MenuItem(menu, SWT.PUSH);
        unpaidMi.setText(Messages.getString("InvoiceListViewer.5")); //$NON-NLS-1$
        unpaidMi.setImage(ImageCache.get("unpaid")); //$NON-NLS-1$
        unpaidMi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		List<Invoice> list = Lists.newArrayList();
        		for (Invoice invoice : cachedEntries) {
        			if (invoice.getEntryId() == null) {
        				list.add(invoice);
        			}
        		}
        		tableViewer.getTable().deselectAll();
        		tableViewer.setSelection(new StructuredSelection(list));
        	}
        });
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("InvoiceListViewer.7")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("search")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		doFind(new SearchParameter());
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("InvoiceListViewer.9")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		tableViewer.getTable().selectAll();
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("InvoiceListViewer.11")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.getTable().deselectAll();
            }
         });
        new MenuItem(menu, SWT.SEPARATOR);
        MenuItem selActionMi = new MenuItem(menu, SWT.CASCADE);
        selActionMi.setText(Messages.getString("InvoiceListViewer.13")); //$NON-NLS-1$
        Menu selActionMenu = new Menu(menu);
        selActionMi.setMenu(selActionMenu);
        final MenuItem miDel = new MenuItem(selActionMenu, SWT.PUSH);
        miDel.setText(Messages.getString("InvoiceListViewer.14")); //$NON-NLS-1$
        miDel.setImage(ImageCache.get("trash")); //$NON-NLS-1$
        miDel.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (! GUIUtil.confirm(Messages.getString("InvoiceListViewer.16"),Messages.getString("InvoiceListViewer.17"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
                    return;
                }
                performGenericOp(OP_DELETE);
            }
        });
        final MenuItem miGenLedger = new MenuItem(selActionMenu, SWT.PUSH);
        miGenLedger.setText(Messages.getString("InvoiceListViewer.18")); //$NON-NLS-1$
        miGenLedger.setImage(ImageCache.get("ledger")); //$NON-NLS-1$
        miGenLedger.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (!GUIUtil.confirm(Messages.getString("InvoiceListViewer.20"), //$NON-NLS-1$
                        Messages.getString("InvoiceListViewer.21"), true)) { //$NON-NLS-1$
                    return;
                }
                performGenericOp(OP_UPD_LEDGER);
            }
        });
        final MenuItem miClrPmt = new MenuItem(selActionMenu, SWT.PUSH);
        miClrPmt.setText(Messages.getString("InvoiceListViewer.22")); //$NON-NLS-1$
        miClrPmt.setImage(ImageCache.get("unpaid")); //$NON-NLS-1$
        miClrPmt.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (!GUIUtil.confirm(Messages.getString("InvoiceListViewer.24"), //$NON-NLS-1$
                        Messages.getString("InvoiceListViewer.25"), true)) { //$NON-NLS-1$
                    return;
                }
                performGenericOp(OP_MARK_UNPAID);
            }
        });
        final MenuItem miMakePmt = new MenuItem(selActionMenu, SWT.PUSH);
        miMakePmt.setText(Messages.getString("InvoiceListViewer.26")); //$NON-NLS-1$
        miMakePmt.setImage(ImageCache.get("paid")); //$NON-NLS-1$
        miMakePmt.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (!GUIUtil.confirm(Messages.getString("InvoiceListViewer.28"), //$NON-NLS-1$
                        Messages.getString("InvoiceListViewer.29"), true)) { //$NON-NLS-1$
                    return;
                }
                performGenericOp(OP_MARK_PAID);
            }
        });
        MenuItem taxBandMi = new MenuItem(menu, SWT.CASCADE);
        taxBandMi.setImage(ImageCache.get("bands")); //$NON-NLS-1$
        taxBandMi.setText(Messages.getString("InvoiceListViewer.31")); //$NON-NLS-1$
        Menu taxBandMenu = new Menu(menu);
        taxBandMi.setMenu(taxBandMenu);
        String [] taxBands = new TaxBandHelper().getTaxCodes();
        for (int i = 0; i < taxBands.length; i++) {
            final String taxBand = taxBands[i];
            mi = new MenuItem(taxBandMenu, SWT.PUSH);
            mi.setText(taxBand);
            mi.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    setTaxBand(taxBand);
                }
            });
        }
        table.setMenu(menu);
        table.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseDown(MouseEvent evt) {
                switch (evt.button) {
                case 3:
                    miDel.setEnabled(tableViewer.getTable().getSelection().length > 0);
                    zoomMi.setEnabled(tableViewer.getTable().getSelection().length == 1);
                    cloneMi.setEnabled(tableViewer.getTable().getSelection().length == 1);
                    break;
                }
            }
        });
    }

    @Override
    public void	keyPressed(KeyEvent evt) { /* not used */ }
    
    @Override
    public void	keyReleased(KeyEvent evt) {
        if ((evt.stateMask & SWT.CONTROL) == 0) {
            return;
        }
        switch(GUIUtil.keyCodeToChar(evt)) {
        case 'A':
          	this.tableViewer.getTable().selectAll();
	        break;
        case 'N':
       		this.tableViewer.getTable().deselectAll();
	        break;
        case 'F':
       		this.doFind(new SearchParameter());
	        break;
        case 'R':
       		this.doFind(new SearchParameter(false));
	        break;
        }
    }

    protected void	templateClone() {
        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
        if (sel.isEmpty()) {
            return;
        }
        Invoice invoice = (Invoice)sel.getFirstElement();
        String newRef = new InvoiceHelper().getNextReference(this.bomService);
        InvoiceCloneDialog dialog = new InvoiceCloneDialog(this.getShell(),
                Messages.getString("InvoiceListViewer.32"), Messages.getString("InvoiceListViewer.33") + invoice.getReference() + Messages.getString("InvoiceListViewer.34"), newRef); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        if (dialog.open() == Dialog.CANCEL) {
            return;
        }
        newRef = dialog.getReference();
        Date newDate = dialog.getInvoiceDate();
        if (newDate == null) {
            newDate = DateUtil.getTodayStart().getTime();
        }
        Invoice newInvoice = invoice.templateClone(newRef, newDate);
        this.bomService.storeGeneric(newInvoice, this.metaProvider.getPrimaryKey(), newInvoice.getReference());
        if (newInvoice.getAccountReference() != null) {
            new BOMHelper(this.bomService).generateEntries(new Invoice [] { newInvoice }, newInvoice.getAccountReference(), false);
        }
        this.gotoEntry(newInvoice);
    }
    
    protected static final String OP_DELETE = "delete"; //$NON-NLS-1$
    protected static final String OP_MARK_UNPAID = "markUnpaid"; //$NON-NLS-1$
    protected static final String OP_MARK_PAID = "markPaid"; //$NON-NLS-1$
    protected static final String OP_UPD_LEDGER = "updateLedger"; //$NON-NLS-1$
    
    /**
     * common method for generic ops on a number of selected rows
     * @param op
     */
    protected void	performGenericOp(String op) {
        try {
            setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
            setEnabled(false);
	        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
	        if (op.equals(OP_MARK_PAID) || op.equals(OP_UPD_LEDGER)) {
	            InvoiceLedgerParamDialog dialog = new InvoiceLedgerParamDialog(this.getShell(), this.bomService, false);
	            // select the first account if there is one
		        Invoice o = (Invoice)sel.getFirstElement();
		        if (o != null) {
		        	dialog.setAccountReference(((Invoice)o).getAccountReference());
		        }
	            if (dialog.open() != Dialog.OK) {
	                return;
	            }
	            String accountReference = dialog.getAccountReference();
	            if (accountReference == null) {
	                GUIUtil.showError(Messages.getString("InvoiceListViewer.39"), Messages.getString("InvoiceListViewer.40"), null); //$NON-NLS-1$ //$NON-NLS-2$
	                return;
	            }
	            Invoice [] invoices = Arrays.stream(sel.toArray())
	            		.map(invoice -> (Invoice)invoice)
	            		.toArray(Invoice[]::new);
	            this.bomService.setPublishEvent(false);
	            new BOMHelper(this.bomService).generateEntries(invoices, accountReference, op.equals(OP_MARK_PAID));
	        } else {
		        for (Object o : sel) {
		            Invoice invoice = (Invoice)o;
		            if (op.equals(OP_DELETE)) {
	                    bomService.deleteGeneric(invoice, metaProvider.getPrimaryKey(), invoice.getReference());
		            } else if (op.equals(OP_MARK_UNPAID)) {
		                invoice.setEntryId(null);
		                this.bomService.storeGeneric(invoice, this.metaProvider.getPrimaryKey(), invoice.getReference());
		            }
		        }
	        }
        } finally {
            this.bomService.setPublishEvent(true);
            setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
            setEnabled(true);
            this.tableViewer.refresh();
        }
    }
    protected void	setTaxBand(String taxBand) {
        if (! GUIUtil.confirm(Messages.getString("InvoiceListViewer.41"), //$NON-NLS-1$
                Messages.getString("InvoiceListViewer.42") + taxBand + Messages.getString("InvoiceListViewer.43"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
        for (Object o : sel) {
            Invoice invoice = (Invoice)o;
            invoice.setTaxBand(taxBand);
            this.bomService.storeGeneric(invoice, this.metaProvider.getPrimaryKey(), invoice.getReference());
        }
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#canSearch()
     */
    @Override
    public boolean canSearch() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGenerateHTML()
     */
    @Override
    public boolean canGenerateHTML() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGeneratePDF()
     */
    @Override
    public boolean canGeneratePDF() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGenerateXML()
     */
    @Override
    public boolean canGenerateXML() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doHTML()
     */
    @Override
    public void doHTML() {
        this._doGenerate(GenericReport.OF_HTML);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doPDF()
     */
    @Override
    public void doPDF() {
        this._doGenerate(GenericReport.OF_PDF);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doXML()
     */
    @Override
    public void doXML() {
        this._doGenerate(GenericReport.OF_XML);
    }
    protected void	_doGenerate(String format) {
        Object [] elements = ((StructuredSelection)this.tableViewer.getSelection()).toArray();
        String title = ((elements == null || elements.length == 0) ? Messages.getString("InvoiceListViewer.44") : Messages.getString("InvoiceListViewer.45")) + Messages.getString("InvoiceListViewer.46"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
       List<Invoice> reportData = (elements == null || elements.length == 0) ? this.cachedEntries : Arrays.stream(elements)
    		   .map(o -> (Invoice)o)
    		   .collect(Collectors.toList());
        if (this.sortByColumn != null) {
            title += Messages.getString("InvoiceListViewer.47") + this.sortByColumn.getText(); //$NON-NLS-1$
        }
        InvoiceListReport report = new InvoiceListReport(title, reportData);
        ReportUtil.generateOnlineReport(this, this.bomService, report, format);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#doFind(java.lang.Object)
     */
    @Override
    public List<Invoice> doFind(SearchParameter param) {
        int idx = 0;
        String columnTitle = null;
        String columnValue = null;
        MetaColumn<?> column = null;
        if (param == null) {
            param = new SearchParameter();
        }
        if (param.getPrototype() == null) {
            // support for GUI search
	        SearchParametersDialog dialog = new SearchParametersDialog(this.getShell(),
	                Messages.getString("InvoiceListViewer.48"), Messages.getString("InvoiceListViewer.49"), this.metaProvider.getColumnTitles()); //$NON-NLS-1$ //$NON-NLS-2$
	        dialog.setName(this.metaProvider.getColumn(InvoiceListMeta.CLNTNAME_PROP).getTitle());
	        dialog.setForwardSearch(param != null ? param.isForward() : true);
	        int ok = dialog.open();
	        if (ok == Dialog.CANCEL) {
	            return super.doFind(param);
	        }
	        columnTitle = dialog.getName();
	        columnValue = dialog.getValue();
	        param.setForward(dialog.isForwardSearch());
	        param.setMatchAll(dialog.isMatchAllSearch());
	        param.setCaseSensitive(dialog.isCaseSensitive());
	        idx = this.tableViewer.getTable().getSelectionIndex();
            column = this.metaProvider.getColumnForTitle(columnTitle);
        } else {
            // support for programmatical search
            Object key = param.getCriteria()[0][0];
            if (key != null) {
                columnTitle = key.toString();
                columnValue = Formatters.emptyIfNull(param.getCriteria()[0][1]);
            }
            // we are searching by name, not title, in non-GUI mode
            column = this.metaProvider.getColumn(columnTitle);
        }

        if (columnTitle == null || columnValue == null || column == null) {
            return super.doFind(param);
        }
        if (idx < 0) {
            idx = param.isForward() ? 0 : this.cachedEntries.size() - 1;
        }
        List<Invoice> foundList = Lists.newArrayList();
        for (int i = idx; (param.isForward() && i < this.cachedEntries.size()) ||
        	(! param.isForward() && i >= 0); i += param.isForward() ? 1 : -1) {
            Invoice re = this.cachedEntries.get(i);
            Object value = this.metaProvider.getValue(re, column.getName(), null);
            if (value == null) {
                continue;
            }
            if ((param.isCaseSensitive() && value.toString().indexOf(columnValue) >= 0) ||
                    (! param.isCaseSensitive() && value.toString().toLowerCase().indexOf(columnValue.toLowerCase()) >= 0)) {
                foundList.add(re);
                if (! param.isMatchAll()) {
                    // stop after first match
                    break;
                }
            }
        }
        if (foundList.size() > 0) {
            this.tableViewer.setSelection(new StructuredSelection(foundList), true);
            return foundList;
        }
        return super.doFind(param);
    }
    
    protected void cache() {
        this.cachedEntries.clear();
        this.cachedEntries.addAll(bomService.getRefData(Invoice.class));
        this.cachedEntries = this.cachedEntries.stream().sorted((o1, o2) -> {
                Invoice i1 = (Invoice)o1;
                Invoice i2 = (Invoice)o2;
                if (sortByColumn != null) {
                    if (sortByColumn.getData().equals(InvoiceListMeta.RECDATE_PROP)) {
                        return i1.getReceivedDate() != null && i2.getReceivedDate() != null &&
                        i1.getReceivedDate().before(i2.getReceivedDate()) ? -1 : 1;
                    } else if (sortByColumn.getData().equals(InvoiceListMeta.INVDATE_PROP)) {
                        return i1.getInvoicedDate() != null && i2.getInvoicedDate() != null &&
                        i1.getInvoicedDate().before(i2.getInvoicedDate()) ? -1 : 1;
                    } else if (sortByColumn.getData().equals(InvoiceListMeta.CLNTNAME_PROP)) {
                        try {
                            return i1.getClientName().compareTo(i2.getClientName());
                        } catch (Exception e) {
                            return -1;
                        }
                    } else if (sortByColumn.getData().equals(InvoiceListMeta.ACCTREF_PROP)) {
                        try {
                            return i1.getAccountReference().compareTo(i2.getAccountReference());
                        } catch (Exception e) {
                            return -1;
                        }
                    }
                }
                return i1.getReference().compareTo(i2.getReference());
        }).collect(Collectors.toList());
    }
}
