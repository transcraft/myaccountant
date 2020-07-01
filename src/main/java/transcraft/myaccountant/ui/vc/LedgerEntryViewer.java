/**
 * Created on 15-Jun-2005
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
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMHelper;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.comms.Quicken2000Export;
import transcraft.myaccountant.comms.Quicken2000Import;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.report.AccountRunningReport;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.action.ActionableTarget;
import transcraft.myaccountant.ui.action.Clipboardable;
import transcraft.myaccountant.ui.action.SearchParameter;
import transcraft.myaccountant.ui.action.SearchParametersDialog;
import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.utils.DateUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * Ledger entries and editing facility
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryViewer extends ActionableTarget<RunningEntry> 
	implements BOMListener, Clipboardable, KeyListener {
    private static final Logger LOG = LoggerFactory.getLogger(LedgerEntryViewer.class);
    
    public static final String POSTING_DATE = Messages.getString("LedgerEntryViewer.0"); //$NON-NLS-1$
    public static final String VALUE_DATE = Messages.getString("LedgerEntryViewer.1"); //$NON-NLS-1$
    
    // <!-- scoped members accessible by LedgerEntryLabelProvider and other supporting classes
    protected String ledgerType = BOMService.VD_LEDGER;
    protected TableViewer tableViewer;
    protected LedgerEntryEditor editor;
    protected Button pdButton;
    protected Button vdButton;
    protected BOMService bomService;
    protected Account account;
    protected MetaProvider<RunningEntry> metaProvider;
    protected Entry closingBalanceEntry = null;
    // -->
    
    private Label titleLabel;
    
    private boolean busyFlag;
    private List<RunningEntry> cachedEntries;
    private boolean supportInvoice = false;
    
    public LedgerEntryViewer(Composite parent, BOMService bomService, MetaProvider<RunningEntry> provider, Account account) {
        super(parent, SWT.FLAT);
        this.bomService = bomService;
        this.metaProvider = provider;
        this.bomService.addListener(this);
        this.account = account;
        this.createContents();
    }
    
	protected void	createContents() {
        this.setLayout(new LedgerEntryLayout(this));
        
        titleLabel = new Label(this, SWT.CENTER|SWT.BORDER|SWT.SHADOW_ETCHED_OUT);
        titleLabel.setBackground(this.getDisplay().getSystemColor(SWT.COLOR_TITLE_BACKGROUND_GRADIENT));
        titleLabel.setForeground(this.getDisplay().getSystemColor(SWT.COLOR_TITLE_FOREGROUND));
        titleLabel.setAlignment(SWT.CENTER);
        titleLabel.setFont(GUIUtil.titleFont);
        titleLabel.setToolTipText(Messages.getString("LedgerEntryViewer.2")); //$NON-NLS-1$
        
        Group group = new Group(this, SWT.SHADOW_ETCHED_IN);
        group.setText(Messages.getString("LedgerEntryViewer.3")); //$NON-NLS-1$
        group.setLayout(new RowLayout(SWT.HORIZONTAL));
        this.pdButton = new Button(group, SWT.RADIO);
        this.pdButton.setText(POSTING_DATE);
        this.pdButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                if (pdButton.getSelection()) {
	                setLedgerType(POSTING_DATE);
	                LedgerEntryViewer.this.cachedEntries = null;
	                getTableViewer().refresh(true);
                }
            }
        });
        this.vdButton = new Button(group, SWT.RADIO);
        this.vdButton.setText(VALUE_DATE);
        this.vdButton.setSelection(true);
        this.vdButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                if (vdButton.getSelection()) {
	                setLedgerType(VALUE_DATE);
		            LedgerEntryViewer.this.cachedEntries = null;
		            getTableViewer().refresh(true);
                }
            }
        });
        
        RowLayout ly = (RowLayout)group.getLayout();
        ly.type = SWT.HORIZONTAL;
        ly.spacing = 15;
        ly.fill = true;
        
        this.tableViewer = new TableViewer(this, SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI);
        final Table table = this.tableViewer.getTable();
        //table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        if (account instanceof LedgerAccount) {
            List<String> invoiceAccounts = PreferenceFactory.getAsList(PreferenceFactory.INVOICE_ACCTS);
            if (invoiceAccounts.contains(this.account.getReference())) {
                this.supportInvoice = true;
            }
            table.addSelectionListener(new SelectionAdapter() {
            	@Override
            	public void	widgetDefaultSelected(SelectionEvent evt) {
            		gotoEntry();
            	}
            });
        }
        
        MetaColumn<?> [] metaColumns = this.metaProvider.getColumns();
        for (int i = 0; i < metaColumns.length; i++) {
            if (account instanceof LedgerAccount) {
                if (metaColumns[i].getName().equals(LedgerEntryMeta.ALLOC_PROP)) {
                    continue;
                }
            }
            if (! metaColumns[i].isVisible()) {
                continue;
            }
            
            TableColumn tc = new TableColumn(table, metaColumns[i].getAlignment());
            tc.setText(metaColumns[i].getTitle());
            tc.setWidth(metaColumns[i].getWidth());
            tc.setData(metaColumns[i].getName());
            if (metaColumns[i].getImage() != null) {
                tc.setImage(ImageCache.get(metaColumns[i].getImage()));
                tc.setText(""); //$NON-NLS-1$
            }
        }
        
        this.tableViewer.setContentProvider(new LedgerEntryContentProvider(this));
        this.tableViewer.setLabelProvider(new LedgerEntryLabelProvider(this));
        if (! (this.account instanceof LedgerAccount)) {
            this.editor = new LedgerEntryEditor(this, this.tableViewer.getTable(), bomService, account);
        }
        
        /*
         * Context Menu
         */
        this.installContextMenu(table);
        
        this.pack();

        int ops = DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT;
        this.tableViewer.addDragSupport(ops, new Transfer[] { TextTransfer.getInstance() }, new LedgerEntryDragAdapter(this));

        /*
         * keyboard handler
         */
        table.addKeyListener(this);
        
        this.tableViewer.setInput(this.account);

        // auto-scroll to the last entry
        if (this.cachedEntries.size() > 2) {
        	int n = this.cachedEntries.size() - 2;
        	RunningEntry re = this.cachedEntries.get(n);
        	List<RunningEntry> list = Lists.newArrayList();
        	list.add(re);
        	this.tableViewer.setSelection(new StructuredSelection(list), true);
        }

        /*
         * 
        ops = DND.DROP_COPY | DND.DROP_DEFAULT;
        this.tableViewer.addDropSupport(ops, new Transfer[] { FileTransfer.getInstance(), TextTransfer.getInstance() },
                new LedgerEntryDropAdapter(this));
         */
    }

    @Override
    public void	keyPressed(KeyEvent evt) { 
    	LOG.debug(String.format("keyPressed(char=%c,code=0x%x,mask=0x%x)", evt.character, evt.keyCode, evt.stateMask)); //$NON-NLS-1$
        Table table = this.tableViewer.getTable();
        if ((evt.stateMask & SWT.CONTROL) == 0) {
            return;
        }

        // CONTROL+char
        switch(GUIUtil.keyCodeToChar(evt)) {
        case 'E':
            editor.loadModelForTableItem(table.getSelection().length > 0 ? table.getSelection()[0] : table.getItem(0));
            break;
        case 'C':
            this.doCopy();
            break;
        case 'X':
            this.doCut();
            break;
        case 'V':
            this.doPaste();
            break;
        case 'A':
	        {
            	this.tableViewer.getTable().selectAll();
	        }
	        break;
        case 'N':
	        {
        		this.tableViewer.getTable().deselectAll();
	        }
	        break;
	    case 'F':
	        this.doFind(null);
	        break;
	    case 'R':
	        this.doFind(new SearchParameter(false));
	        break;
        }
    }
    
    @Override
    public void	keyReleased(KeyEvent evt) {
    	LOG.debug(String.format("keyReleased(char=%c,code=0x%x,mask=0x%x)", evt.character, evt.keyCode, evt.stateMask)); //$NON-NLS-1$
        Table table = this.tableViewer.getTable();
        if (evt.stateMask  == 0) {
            switch(evt.character) {
            case '\r':
            case '\n':
                editor.loadModelForTableItem(table.getSelection().length > 0 ? table.getSelection()[0] : table.getItem(0));
                break;
            }
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
    
    private String [] propagatableColumns = new String[] {
            LedgerEntryMeta.ALLOC_PROP,
            LedgerEntryMeta.ACCT_PROP,
    };
    
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem selectAllItem = new MenuItem(menu, SWT.CASCADE);
        selectAllItem.setText(Messages.getString("LedgerEntryViewer.5")); //$NON-NLS-1$
        selectAllItem.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        Menu selectAllMenu = new Menu(menu);
        selectAllItem.setMenu(selectAllMenu);
        MenuItem mi = new MenuItem(selectAllMenu, SWT.PUSH);
        mi.setText(Messages.getString("LedgerEntryViewer.7")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.getTable().selectAll();
            }
        });
        mi = new MenuItem(selectAllMenu, SWT.PUSH);
        mi.setImage(ImageCache.get("unpaid")); //$NON-NLS-1$
        mi.setText(Messages.getString("LedgerEntryViewer.9")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.getTable().deselectAll();
                List<RunningEntry> list = cachedEntries
                		.stream()
                		.filter(re -> !re.isReconciled())
                		.collect(Collectors.toList());
                tableViewer.setSelection(new StructuredSelection(list));
            }
        });
        MetaColumn<?> [] columns = this.metaProvider.getColumns();
        for (int i = 0; i < columns.length; i++) {
            final MetaColumn<?> column = columns[i];
            if (! column.isVisible() || ! Character.isLetter(column.getTitle().charAt(0))) {
                continue;
            }
            mi = new MenuItem(selectAllMenu, SWT.PUSH);
            mi.setText(Messages.getString("LedgerEntryViewer.10") + column.getTitle()); //$NON-NLS-1$
            mi.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    selectEntries(column.getName(), true);
                }
            });
        }
        MenuItem selectNoneItem = new MenuItem(menu, SWT.CASCADE);
        selectNoneItem.setText(Messages.getString("LedgerEntryViewer.11")); //$NON-NLS-1$
        selectNoneItem.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        Menu selectNoneMenu = new Menu(menu);
        selectNoneItem.setMenu(selectNoneMenu);
        mi = new MenuItem(selectNoneMenu, SWT.PUSH);
        mi.setText(Messages.getString("LedgerEntryViewer.13")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.getTable().deselectAll();
            }
        });
        for (int i = 0; i < columns.length; i++) {
            final MetaColumn<?> column = columns[i];
            if (! column.isVisible() || ! Character.isLetter(column.getTitle().charAt(0))) {
                continue;
            }
            mi = new MenuItem(selectNoneMenu, SWT.PUSH);
            mi.setText(Messages.getString("LedgerEntryViewer.14") + column.getTitle()); //$NON-NLS-1$
            mi.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    selectEntries(column.getName(), false);
                }
            });
        }
        new MenuItem(menu, SWT.SEPARATOR);
        final MenuItem miCopy = new MenuItem(menu, SWT.PUSH);
        miCopy.setText(Messages.getString("LedgerEntryViewer.15")); //$NON-NLS-1$
        miCopy.setImage(ImageCache.get("clip_copy")); //$NON-NLS-1$
        miCopy.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                doCopy();
            }
        });
        if (! (this.account instanceof LedgerAccount)) {
            final MenuItem miCut = new MenuItem(menu, SWT.PUSH);
            miCut.setText(Messages.getString("LedgerEntryViewer.17")); //$NON-NLS-1$
            miCut.setImage(ImageCache.get("clip_cut")); //$NON-NLS-1$
            miCut.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void	widgetSelected(SelectionEvent evt) {
                    doCut();
                }
            });
            final MenuItem miPaste = new MenuItem(menu, SWT.PUSH);
            miPaste.setText(Messages.getString("LedgerEntryViewer.19")); //$NON-NLS-1$
            miPaste.setImage(ImageCache.get("clip_paste")); //$NON-NLS-1$
            miPaste.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    doPaste();
                }
            });
            new MenuItem(menu, SWT.SEPARATOR);

            // actions for selected rows
            final MenuItem selectedMi = new MenuItem(menu, SWT.CASCADE);
            selectedMi.setText(Messages.getString("LedgerEntryViewer.21")); //$NON-NLS-1$
            Menu selectedActionMenu = new Menu(menu);
            selectedMi.setMenu(selectedActionMenu);
            MenuItem reconciledMi = new MenuItem(selectedActionMenu, SWT.PUSH);
            reconciledMi.setImage(ImageCache.get("paid")); //$NON-NLS-1$
            reconciledMi.setText(Messages.getString("LedgerEntryViewer.23")); //$NON-NLS-1$
            reconciledMi.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    setReconciled(true);
                }
            });
            MenuItem unreconciledMi = new MenuItem(selectedActionMenu, SWT.PUSH);
            unreconciledMi.setImage(ImageCache.get("unpaid")); //$NON-NLS-1$
            unreconciledMi.setText(Messages.getString("LedgerEntryViewer.25")); //$NON-NLS-1$
            unreconciledMi.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    setReconciled(false);
                }
            });
            final MenuItem propagateMi = new MenuItem(menu, SWT.CASCADE);
            propagateMi.setText(Messages.getString("LedgerEntryViewer.26")); //$NON-NLS-1$
            Menu propagateMenu = new Menu(menu);
            propagateMi.setMenu(propagateMenu);
            columns = this.metaProvider.getColumns();
            for (int i = 0; i < this.propagatableColumns.length; i++) {
                final MetaColumn<?> column = this.metaProvider.getColumn(this.propagatableColumns[i]);
                if (column == null || ! column.isVisible()) {
                    continue;
                }
	            mi = new MenuItem(propagateMenu, SWT.PUSH);
	            mi.setText(column.getTitle());
	            mi.addSelectionListener(new SelectionAdapter() {
	            	@Override
	                public void widgetSelected(SelectionEvent evt) {
	                    propagateEntry(column);
	                }
	            });
            }
            table.addMouseListener(new MouseAdapter() {
            	@Override
                public void mouseDown(MouseEvent evt) {
                    switch (evt.button) {
                    case 3:
                        miCopy.setEnabled(canCopy() && table.getSelectionCount() > 0);
                        miCut.setEnabled(canCut() && table.getSelectionCount() > 0);
                        miPaste.setEnabled(canPaste());
                        selectedMi.setEnabled(canCopy() && table.getSelectionCount() > 0);
                        propagateMi.setEnabled(canCopy() && table.getSelectionCount() == 1);
                        break;
                    }
                }
            });
        } else {
            if (this.supportInvoice) {
                mi = new MenuItem(menu, SWT.PUSH);
                mi.setText(Messages.getString("LedgerEntryViewer.27")); //$NON-NLS-1$
                mi.setImage(ImageCache.get("invoice")); //$NON-NLS-1$
                mi.addSelectionListener(new SelectionAdapter() {
                	@Override
                    public void widgetSelected(SelectionEvent evt) {
                        generateInvoices();
                    }
                });
            }
            table.addMouseListener(new MouseAdapter() {
            	@Override
                public void mouseDown(MouseEvent evt) {
                    switch (evt.button) {
                    case 3:
                        miCopy.setEnabled(table.getSelection().length > 0);
                        break;
                    }
                }
            });
        }
        
        table.setMenu(menu);
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
	@Override
    public void dataChanged(BOMEvent event) {
        if (this.busyFlag) {
            return;
        }
        if (event.op == BOMEvent.OP_BROADCAST) {
            this.reset();
            return;
        }
        if (event.op != BOMEvent.OP_DEL && event.entity instanceof Entry) {
            Entry entry = (Entry)event.entity;
            if ((entry.getFromAccount() != null && entry.getFromAccount().equals(this.account.getReference())) ||
                    (entry.getToAccount() != null && entry.getToAccount().equals(this.account.getReference()))) {
                        LOG.debug("Refresh view {},{}", this.account.getReference(), entry.getEntryId()); //$NON-NLS-1$
                        this.reset();
            }
        } else if (event.entity.equals(this.account)) {
            this.tableViewer.setInput(this.account);
            this.refreshValues();
        }
    }
    
    protected void	refreshValues() {
        String title = this.account.getReference();
        if (! (this.account instanceof LedgerAccount)) {
            title += Messages.getString("LedgerEntryViewer.31") + Formatters.format(account.getOpeningBalance()) + //$NON-NLS-1$
                Messages.getString("LedgerEntryViewer.32") + DateUtil.format(account.getOpeningDate()) + //$NON-NLS-1$
                (this.cachedEntries != null ? " (" + this.cachedEntries.size() + Messages.getString("LedgerEntryViewer.34") : ""); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
        this.titleLabel.setText(title);
    }
    
    protected void	generateInvoices() {
        RunningEntry [] selectedEntries = this.getSelectedEntries();
        if (selectedEntries == null) {
            return;
        }
        try {
            this.setBusy(true);
            new BOMHelper(this.bomService).generateInvoices(selectedEntries);
        } finally {
            this.setBusy(false);            
        }
    }
    
    /**
     * @return Returns the account.
     */
    public Account getAccount() {
        return account;
    }
    /**
     * @return Returns the bomService.
     */
    public BOMService getBomService() {
        return bomService;
    }
    /**
     * @return Returns the tableViewer.
     */
    public TableViewer getTableViewer() {
        return tableViewer;
    }
    
    /**
     * @return Returns the editor.
     */
    public LedgerEntryEditor getEditor() {
        return editor;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#canCopy()
     */
	@Override
    public boolean canCopy() {
        return true;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#doCopy()
     */
	@Override
    public void doCopy() {
        if (! this.canCopy()) {
            return;
        }
        try {
	        Quicken2000Export qex = new Quicken2000Export(this.bomService);
	        StringBuffer buffer = new StringBuffer();
	        RunningEntry [] selectedEntries = this.getSelectedEntries();
	        if (selectedEntries == null) {
	            return;
	        }
            for (int i = 0; i < selectedEntries.length; i++) {
	               RunningEntry selectedEntry = selectedEntries[i];
	               if (selectedEntry.getEntryId() != null) {
	                   String data = qex.processRow(selectedEntry, this.account);
	                   buffer.append(data);
	               }
	        }
            GUIUtil.getClipboard().setContents(new Object [] { buffer.toString() }, new Transfer [] { TextTransfer.getInstance() });
        } finally {}
    }

    /**
     * mark entries as reconciled or not
     * @param flag
     */
    protected void	setReconciled(boolean flag) {
        try {
            setBusy(true);
            bomService.setPublishEvent(false);
            RunningEntry [] selectedEntries = this.getSelectedEntries();
            for (int i = 0; i < selectedEntries.length; i++) {
                Entry entry = this.bomService.getEntry(selectedEntries[i].getEntryId());
                entry.setReconciled(flag);
                /*
                 * we are only changing a reconciled flag, which does not affect any of the
                 * Ledger postings, so no need to re-generate Ledger entries. Otherwise, we
                 * will have to call the BOMService.store(Entry) method instead
                 * 
                 */
                this.bomService.storeGeneric(entry, this.metaProvider.getPrimaryKey(), entry.getEntryId());
            }
        } finally {
            setBusy(false);
            bomService.setPublishEvent(true);
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#canCut()
     */
	@Override
    public boolean canCut() {
        return ! (this.account instanceof LedgerAccount);
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#doCut()
     */
	@Override
    public void doCut() {
        if (! this.canCut()) {
            return;
        }
        if (this.account instanceof LedgerAccount) {
            // can not cut from a view only account
            return;
        }
        if (! GUIUtil.confirm(Messages.getString("LedgerEntryViewer.36"), Messages.getString("LedgerEntryViewer.37"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        setBusy(true);
        this.doCopy();
        try {
            bomService.setPublishEvent(false);
            RunningEntry [] selectedEntries = this.getSelectedEntries();
            for (int i = 0; i < selectedEntries.length; i++) {
                RunningEntry selectedEntry = selectedEntries[i];
                if (selectedEntry.getEntryId() != null) {
                    bomService.delete(selectedEntry.getEntryId());
                }
            }
            Table table = this.tableViewer.getTable();
            editor.loadModelForTableItem(table.getItem(table.getItemCount() - 1));
        } finally {
            setBusy(false);
            bomService.setPublishEvent(true);
        }
        
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
        RunningEntry re = (RunningEntry)sel.getFirstElement();
        Entry entry = this.bomService.getEntry(re.getEntryId());
        if (entry == null) {
            return;
        }
        SearchParameter param = new SearchParameter(entry.getFromAccount(), LedgerEntryViewer.class);
        param.addCriterium(LedgerEntryMeta.ID_PROP, re.getEntryId());
        LedgerTreeViewer treeViewer = GUIUtil.findTreeViewer(this);
        if (treeViewer != null) {
            treeViewer.doFind(param);
        }
    }
        
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#canPaste()
     */
	@Override
    public boolean canPaste() {
        return ! (this.account instanceof LedgerAccount);
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.Clipboardable#doPaste()
     */
	@Override
    public void doPaste(final Object data) {
        if (! this.canPaste()) {
            return;
        }
        if (this.account instanceof LedgerAccount) {
            GUIUtil.showError(Messages.getString("LedgerEntryViewer.38"), Messages.getString("LedgerEntryViewer.39"), null); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        if (! GUIUtil.confirm(Messages.getString("LedgerEntryViewer.40"), Messages.getString("LedgerEntryViewer.41"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        this.setBusy(true);
        try {
            new ProgressMonitorDialog(this.getShell()).run(false, false, new IRunnableWithProgress() {
                public void	run(IProgressMonitor monitor) {
	    	        new Quicken2000Import(bomService).run(data, account, monitor);
                }
            });
        } catch (Exception e) {
        } finally {
            this.setBusy(false);
        }
    }
    
	public void doPaste() {
        Object data = GUIUtil.getClipboard().getContents(TextTransfer.getInstance());
        doPaste(data);
    }
    
    public void	setBusy(boolean flag) {
        this.busyFlag = flag;
        this.setCursor(Display.getCurrent().getSystemCursor(flag ? SWT.CURSOR_WAIT : SWT.CURSOR_ARROW));
        this.setEnabled(!flag);
        if (! flag) {
            this.reset();
        }
        if (this.editor != null) {
            this.editor.setVisible(false);
        }
    }
    
    /**
     * 
     * @param columnName
     */
    private void	propagateEntry(MetaColumn<?> column) {
        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
        if (sel.isEmpty()) {
            return;
        }
        try {
            this.setBusy(true);
            bomService.setPublishEvent(false);
	        RunningEntry re = (RunningEntry)sel.getFirstElement();
	        if (! GUIUtil.confirm(Messages.getString("LedgerEntryViewer.42"), Messages.getString("LedgerEntryViewer.43") + column.getTitle() + Messages.getString("LedgerEntryViewer.44") + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                re.getDescription() + Messages.getString("LedgerEntryViewer.45"), true)) { //$NON-NLS-1$
	            return;
	        }
	        for (RunningEntry currentRE : this.cachedEntries) {
	            if (re == currentRE || re.equals(currentRE) || ! currentRE.getDescription().equals(re.getDescription())) {
	                continue;
	            }
	            if (column.getName().equals(LedgerEntryMeta.ALLOC_PROP)) {
	                currentRE.copyRule(re);
	            } else if (column.getName().equals(LedgerEntryMeta.ACCT_PROP)) {
	                currentRE.setToAccount(re.getToAccount());
	            } else {
	                throw new RuntimeException(Messages.getString("LedgerEntryViewer.46") + column.getName() + Messages.getString("LedgerEntryViewer.47")); //$NON-NLS-1$ //$NON-NLS-2$
	            }
	            this.bomService.store(new Entry(currentRE));
	            LOG.debug("Propagate rule from {} to {}", re, currentRE); //$NON-NLS-1$
	        }
        } finally {
            this.setBusy(false);
            bomService.setPublishEvent(true);
        }
    }
    
    /**
     * select all rows which have the same value in the column
     * as the first selected row
     * @param columnName
     * @param flag
     */
    private void	selectEntries(String columnName, boolean flag) {
        TableItem [] items = this.tableViewer.getTable().getSelection();
        if (items.length == 0) {
            return;
        }
        RunningEntry selectedRE = (RunningEntry)items[0].getData(); 
        Object [] elements = getCachedEntries();
        for (int i = 0; i < elements.length; i++) {
            RunningEntry re = (RunningEntry)elements[i];
            Object value1 = this.metaProvider.getValue(re, columnName, null);
            Object value2 = this.metaProvider.getValue(selectedRE, columnName, null);
            if (value1 != null && value2 != null && value1.equals(value2)) {
                if (flag) {
                    this.tableViewer.getTable().select(i);
                } else {
                    this.tableViewer.getTable().deselect(i);
                }
            }
        }
    }
    
    /**
     * reset the cached data
     *
     */
    public void	reset() {
        this.cachedEntries = null;
        this.tableViewer.refresh();
    }
    
    /**
     * convenience method to return selected entries for whether we are in
     * CheckBoxedTableViewer or TableViewer mode
     *
     */
    protected RunningEntry [] getSelectedEntries() {
        return Arrays.stream(((StructuredSelection)this.tableViewer.getSelection()).toArray())
            	.map(re -> (RunningEntry)re)
            	.toArray(RunningEntry[]::new);
    }
    
    /**
     * cache for performance for the ContentProvider
     *
     */
    private void	cache() {
    	if (this.cachedEntries == null) {
    		LOG.debug("start cache()=" + new Date()); //$NON-NLS-1$
    		this.cachedEntries = bomService.getLedgerEntries(this.ledgerType, account)
    				.stream()
    				.map(e -> (RunningEntry)e)
    				.collect(Collectors.toList());
    		this.cachedEntries.add(new RunningEntry(account));
    		Date today = new Date();
    		this.closingBalanceEntry = this.cachedEntries
    				.stream()
    				.filter(re -> 
    				re.getEntryId() != null && (
    						(pdButton.getSelection() && re.getPostingDate().after(today)) ||
    						(vdButton.getSelection() && re.getValueDate().after(today))
    					)		            
    				)
    				.findFirst()
    				.orElse(null);
    		this.refreshValues();
    		LOG.debug("end cache({})={}", this.cachedEntries.size(), new Date()); //$NON-NLS-1$
    	}
    }
    
    public Object []	getCachedEntries() {
        if (LedgerEntryViewer.this.cachedEntries == null) {
            LedgerEntryViewer.this.cache();
        }
        // Java 8 compatibility mode, so Entry[]::new is not available
        return cachedEntries.toArray(new Entry[cachedEntries.size()]);
    }
    
    /**
     * @return Returns the ledgerType.
     */
    public String getLedgerType() {
        return ledgerType;
    }
    
    /**
     * @param ledgerType The ledgerType to set.
     */
    public void setLedgerType(String ledgerType) {
        if (ledgerType.equals(LedgerEntryViewer.POSTING_DATE)) {
            this.ledgerType = BOMService.TD_LEDGER;
        } else if (ledgerType.equals(LedgerEntryViewer.VALUE_DATE)) {
            this.ledgerType = BOMService.VD_LEDGER;
        }
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#canGenerateHTML()
     */
	@Override
    public boolean canGenerateHTML() {
        return true;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#canGeneratePDF()
     */
	@Override
    public boolean canGeneratePDF() {
        return true;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#canGenerateXML()
     */
	@Override
    public boolean canGenerateXML() {
        return true;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Searchable#canSearch()
     */
	@Override
    public boolean canSearch() {
        return true;
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Searchable#doFind(java.lang.Object)
     */
	@Override
    public List<RunningEntry> doFind(SearchParameter param) {
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
	                Messages.getString("LedgerEntryViewer.53"), Messages.getString("LedgerEntryViewer.54"), this.metaProvider.getColumnTitles()); //$NON-NLS-1$ //$NON-NLS-2$
	        dialog.setName(this.metaProvider.getColumn(LedgerEntryMeta.DESC_PROP).getTitle());
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
        List<RunningEntry> foundList = Lists.newArrayList();
        for (int i = idx + (param.isForward() ? 1 : -1); (param.isForward() && i < this.cachedEntries.size()) ||
        	(! param.isForward() && i >= 0); i += param.isForward() ? 1 : -1) {
            RunningEntry re = (RunningEntry)this.cachedEntries.get(i);
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
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#doHTML()
     */
	@Override
    public void doHTML() {
        this._doGenerate(GenericReport.OF_HTML);
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#doPDF()
     */
	@Override
    public void doPDF() {
        this._doGenerate(GenericReport.OF_PDF);
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.Generateable#doXML()
     */
	@Override
    public void doXML() {
        this._doGenerate(GenericReport.OF_XML);
    }

    protected void	_doGenerate(String format) {
        RunningEntry [] selected = this.getSelectedEntries();
        List<Integer> idList = Lists.newArrayList();
        if (selected != null) {
	        for (int i = 0; i < selected.length; i++) {
	            if (selected[i] instanceof Entry) {
	                Integer entryId = ((Entry)selected[i]).getEntryId();
	                if (entryId != null) {
	                    idList.add(entryId);
	                }
	            }
	        }
        }
        if (idList.size() == 0) {
            GUIUtil.showError(Messages.getString("LedgerEntryViewer.55"), Messages.getString("LedgerEntryViewer.56"), null); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        AccountRunningReport report = new AccountRunningReport(this.account);
        // Java 8 compatibility mode, so Integer[]::new is not available
        report.setEntryIds(idList.toArray(new Integer[idList.size()]));
        ReportUtil.generateOnlineReport(this, this.bomService, report, format);
    }
    
	@Override
    public String toString() {
        return this.getClass().getName() + "{" + this.account + "}"; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
