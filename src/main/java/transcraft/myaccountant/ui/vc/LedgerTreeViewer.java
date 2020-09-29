/**
 * Created on 14-Jun-2005
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.resource.StringConverter;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ext.DatabaseFileLockedException;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.BookKeeper.service.InvoiceHelper;
import transcraft.myaccountant.comms.Quicken2000Export;
import transcraft.myaccountant.comms.Quicken2000Import;
import transcraft.myaccountant.meta.AccountMeta;
import transcraft.myaccountant.meta.AllocationRuleMeta;
import transcraft.myaccountant.meta.BaseGenericReportMeta;
import transcraft.myaccountant.meta.BatchReportMeta;
import transcraft.myaccountant.meta.GenericReportMeta;
import transcraft.myaccountant.meta.InvoiceListMeta;
import transcraft.myaccountant.meta.InvoiceMeta1;
import transcraft.myaccountant.meta.LedgerAccountMeta;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaHelper;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.meta.ScheduledEntryMeta;
import transcraft.myaccountant.meta.ScheduledListMeta;
import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.BatchReport;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.action.ActionManager;
import transcraft.myaccountant.ui.action.ActionableTarget;
import transcraft.myaccountant.ui.action.ClipCutAction;
import transcraft.myaccountant.ui.action.SearchParameter;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * hierachical view of Ledger contents
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerTreeViewer extends ActionableTarget<RunningEntry> 
	implements BOMListener, IPropertyChangeListener {
	private static final Logger LOG = LoggerFactory.getLogger(LedgerTreeViewer.class);
    
    private BOMService bomService;
    private File dbFile;
    private TreeViewer treeViewer;
    private boolean valid = false;
    private CTabFolder tabFolder;
    private RGB bgColor;

    public LedgerTreeViewer(Composite parent, BOMService bomService, File dbFile) {
        super(parent, SWT.BORDER);
        this.bomService = bomService;
        this.dbFile = dbFile;
        this.setLayout(new FillLayout());
        this.createContents();
    }
    
	protected void	createContents() {
        this.valid = false;
        final ViewForm vf = new ViewForm(this, SWT.BORDER);
        this.treeViewer = new TreeViewer(vf, SWT.FLAT);
        this.treeViewer.setContentProvider(new LedgerTreeContentProvider(this.bomService));
        this.treeViewer.setLabelProvider(new LedgerTreeLabelProvider());
        try {
            this.treeViewer.setInput(dbFile.getName());
        } catch (DatabaseFileLockedException e) {
            GUIUtil.showError(Messages.getString("LedgerTreeViewer.0"), Messages.getString("LedgerTreeViewer.1") + dbFile.getName() + Messages.getString("LedgerTreeViewer.2"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            return;
        }

        vf.setContent(this.treeViewer.getControl());
        this.treeViewer.setSelection(new StructuredSelection(LedgerTreeContentProvider.ACCOUNTS));
        
        this.treeViewer.getTree().addPaintListener(new PaintListener() {
        	@Override
            public void	paintControl(PaintEvent evt) {
                if (bgColor != null) {
	                Rectangle rect = getBounds();
	                Color grey = new Color(getDisplay(), 240, 240, 240);
	                evt.gc.setForeground(grey);
	                evt.gc.setBackground(new Color(getDisplay(), bgColor));
	                evt.gc.setXORMode(true);
	                evt.gc.fillGradientRectangle(rect.x, rect.y, rect.width, rect.height, true);
	                grey.dispose();
                }
            }
        });
        
        this.treeViewer.getTree().addMouseListener(new MouseAdapter() {
        	@Override
        	public void	mouseDown(MouseEvent evt) {
        		if (tabFolder != null && evt.button == 1) {
        			StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
        			if (sel.isEmpty()) {
        				return;
        			}
        			Object obj = sel.getFirstElement();
        			selectTab(obj);
        		}
        	}
        });

    	this.treeViewer.setExpandedElements(new Object [] {
                LedgerTreeContentProvider.ACCOUNTS,
                LedgerTreeContentProvider.REPORTS,
        });

    	this.bomService.addListener(this);
    	this.setupContextMenu();
        this.treeViewer.getControl().setFocus();
    	this.valid = true;

    	/*
    	 * keyboard driven activities
    	 */
        this.treeViewer.getTree().addKeyListener(new KeyAdapter() {
        	@Override
            public void	keyPressed(KeyEvent evt) {
                if ((evt.stateMask & SWT.CTRL) == 0) {
                    return;
                }
	            String str = Action.findKeyString(evt.keyCode).toLowerCase();
                if (str.equals("c")) { //$NON-NLS-1$
                    doCopy();
                    evt.doit = false;
                } else if (str.equals("x")) { //$NON-NLS-1$
                    ActionManager.getTargethandler(ClipCutAction.class).run();
                    evt.doit = false;
                } else if (str.equals("v")) { //$NON-NLS-1$
                    doPaste();
                    evt.doit = false;
                }
            }
         });

        this.treeViewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE | DND.DROP_DEFAULT,
                new Transfer [] { TextTransfer.getInstance() }, new LedgerTreeDropAdapter(this));

        // initialise preferences
        this.propertyChange(null);
        PreferenceFactory.getPreferenceStore().addPropertyChangeListener(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || (evt.getProperty() != null && evt.getProperty().equals(PreferenceFactory.TREE_BG_COLOR))) {
	        String treeBgColor = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.TREE_BG_COLOR);
	        if (treeBgColor != null && treeBgColor.length() > 0) {
		        this.bgColor = StringConverter.asRGB(treeBgColor);
	        } else {
	            this.bgColor = null;
	        }
	        this.treeViewer.getTree().redraw();
        }
    }
    
    public Control	selectTab(Object obj) {
        Control control = null;
        if (obj instanceof Account) {
            control = selectTab(obj, LedgerEntryViewer.class, new LedgerEntryMeta());
        } else if (obj.equals(LedgerTreeContentProvider.INVOICES)) {
            // this must come befor the InvoiceViewer
            control = selectTab(obj, InvoiceListViewer.class, new InvoiceListMeta());
        } else if (obj instanceof Invoice) {
            control = selectTab(obj, InvoiceViewer.class, new InvoiceMeta1((Invoice)obj));
        } else if (obj.equals(LedgerTreeContentProvider.SCHEDULEDENTRIES)) {
            control = selectTab(obj, ScheduledListViewer.class, new ScheduledListMeta());
        } else if (obj instanceof ScheduledEntry) {
            control = selectTab(obj, ScheduledEntryViewer.class, new ScheduledEntryMeta());
        } else if (obj instanceof BatchReport) {
            // this selector must come before BaseGenericReport because of the inheritance
            control = selectTab(obj, BatchReportViewer.class, new BatchReportMeta());
        } else if (obj instanceof GenericReport) {
            // this selector must come before BaseGenericReport because of the inheritance
            MetaProvider<?> metaProvider = null;
            try {
                Class<?> metaProviderClass = MetaHelper.getMetaProviderPrototype(obj);
                metaProvider = (MetaProvider<?>)metaProviderClass.getConstructor().newInstance();
            } catch (Exception e) {
                metaProvider = new GenericReportMeta();
            }
            control = selectTab(obj, GenericReportViewer.class, metaProvider);
        } else if (obj instanceof BaseGenericReport) {
            control = selectTab(obj, GenericReportViewer.class, new BaseGenericReportMeta());
        } else if (obj instanceof AllocationRule) {
            control = selectTab(obj, AllocationRuleViewer.class, new AllocationRuleMeta((AllocationRule)obj));
        }
        return control;
    }
    
    protected Control	selectTab(Object model, Class<?> viewerClass, MetaProvider<?> provider) {
    	LOG.debug("selectTab({}({}),{},{})", 
    			model.getClass().getName(), 
    			model, 
    			viewerClass.getName(), 
    			provider.getClass().getName()
    	);
        CTabItem [] items = this.tabFolder.getItems();
        for (int i = 0; i < items.length; i++) {
            CTabItem item = items[i];
            if (item.getText().equals(model.toString()) && item.getControl().getClass().equals(viewerClass)) {
                this.tabFolder.showItem(item);
                this.tabFolder.setSelection(i);
                Control control = item.getControl();
                if (control instanceof ActionableTarget) {
                    ActionManager.setActionableTarget((ActionableTarget<?>)control);
                } else {
                    ActionManager.setActionableTarget(this);
                }
                return item.getControl();
            }
        }
        
        // create a new Tab
        CTabItem tabItem = new CTabItem(tabFolder, SWT.CLOSE);
        tabItem.setText(model.toString());
        try {
            Constructor<?> [] constructors = viewerClass.getConstructors();
            Control viewer = null;
            for (int i = 0; i < constructors.length; i++) {
                try {
	                viewer = (Control)constructors[i].newInstance(new Object [] {
		                tabFolder, this.bomService, provider, model
	                });
	                break;
                } catch (InvocationTargetException e) {
                    LOG.error("selectTab(" + viewerClass + ")", e);
                } catch (IllegalArgumentException e) {
                    LOG.error("selectTab(" + viewerClass + ")", e);
                }
                
            }
            if (viewer == null) {
                throw new RuntimeException(Messages.getString("LedgerTreeViewer.6") + model.getClass()); //$NON-NLS-1$
            }
	        tabItem.setControl(viewer);
            tabItem.setData(model);
            if (viewer instanceof ActionableTarget) {
                ActionManager.setActionableTarget((ActionableTarget<?>)viewer);
            } else {
                ActionManager.setActionableTarget(this);
            }
        } catch (Exception e) {
            LOG.error(String.format("selectTab(%s)", model), e); //$NON-NLS-1$ //$NON-NLS-2$
            GUIUtil.showError(Messages.getString("LedgerTreeViewer.9"), Messages.getString("LedgerTreeViewer.10") + model + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            Label label = new Label(tabFolder, SWT.BORDER);
            label.setText(Messages.getString("LedgerTreeViewer.12") + model); //$NON-NLS-1$
            tabItem.setControl(label);
        }
        this.tabFolder.setSelection(tabItem);
        tabItem.addDisposeListener(new DisposeListener() {
        	@Override
            public void	widgetDisposed(DisposeEvent evt) {
                treeViewer.setSelection(null, false);
            }
        });
        this.tabFolder.showItem(tabItem);
        tabFolder.requestLayout();        
        tabFolder.setFocus();
        return tabItem.getControl();
    }
    
    protected void	setupContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
    	menuMgr.addMenuListener(new IMenuListener() {
        	@Override
    		public void menuAboutToShow(IMenuManager manager) {
    		    manager.add(new ActionContributionItem(new RefreshAction()));
    		    TreeItem [] items = treeViewer.getTree().getSelection();
    		    if (items.length > 0) {
    		        Object data = items[0].getData();
    		        if (data.equals(LedgerTreeContentProvider.ACCOUNTS)) {
    	    		    manager.add(new ActionContributionItem(new AccountInsertAction()));
    		        } else if (data.equals(LedgerTreeContentProvider.SCHEDULEDENTRIES)) {
        	    		manager.add(new ActionContributionItem(new ScheduleEntryInsertAction()));
    		        } else if (data.equals(LedgerTreeContentProvider.ALLOCRULES)) {
    	    		    manager.add(new ActionContributionItem(new AllocationRuleInsertAction()));
    		        } else if (data.equals(LedgerTreeContentProvider.INVOICES)) {
    	    		    manager.add(new ActionContributionItem(new InvoiceInsertAction()));
    		        } else if (data instanceof Account) {
    		            Account selectedAcct = (Account)data;
    		            manager.add(new ActionContributionItem(new AccountEditAction(selectedAcct)));
    		            if (bomService.isEmptyAccount(selectedAcct)) {
    		                manager.add(new ActionContributionItem(new AccountDeleteAction(selectedAcct)));
    		            }
    		            if (! (data instanceof LedgerAccount)) {
    		                manager.add(new ActionContributionItem(new CopyAction()));
    		                manager.add(new ActionContributionItem(new PasteAction()));
    		            }
    		        } else if (data instanceof ScheduledEntry) {
    		            ScheduledEntry selectedSE = (ScheduledEntry)data;
    		            manager.add(new ActionContributionItem(new ScheduledEntryEditAction(selectedSE)));
   		                manager.add(new ActionContributionItem(new ScheduledEntryDeleteAction(selectedSE)));
    		        } else if (data.equals(LedgerTreeContentProvider.REPORTS)) {
    	    		    manager.add(new ActionContributionItem(new BatchReportInsertAction()));
    		        } else if (data instanceof BaseGenericReport) {
    		            BaseGenericReport<?> report = (BaseGenericReport<?>)data;
    		            if (!ReportUtil.isBuiltInReport(report)) {
    		                manager.add(new ActionContributionItem(new GenericReportDeleteAction(report)));
    		            }
    		        } else if (data instanceof Invoice) {
    		            Invoice invoice = (Invoice)data;
   		                manager.add(new ActionContributionItem(new InvoiceEditAction(invoice)));
   		                manager.add(new ActionContributionItem(new InvoiceDeleteAction(invoice)));
    		        } else if (data instanceof AllocationRule) {
    		            AllocationRule rule = (AllocationRule)data;
    		            manager.add(new ActionContributionItem(new AllocationRuleDeleteAction(rule)));
    		        }
    		    }
    		}
    	});
    	Menu menu = menuMgr.createContextMenu(treeViewer.getControl());	
    	this.treeViewer.getControl().setMenu(menu);
    }

    public void doCopy() {
        StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
        if (sel.isEmpty()) {
            return;
        }
        final Object obj = sel.getFirstElement();
        if (obj.getClass().equals(Account.class)) {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
            this.setEnabled(false);
            try {
                new ProgressMonitorDialog(this.getShell()).run(false, false, new IRunnableWithProgress() {
                    public void	run(IProgressMonitor monitor) {
			   	        String data = new Quicken2000Export(bomService).run((Account)obj, null);
			   	        GUIUtil.getClipboard().setContents(new Object [] { data }, new Transfer [] { TextTransfer.getInstance() });
                    }
                });
		    } catch (Exception e) {
	        } finally {
	            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
	            this.setEnabled(true);
	        }
        }
    }

    public void doPaste() {
        Object data = GUIUtil.getClipboard().getContents(TextTransfer.getInstance());
        StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
        if (sel.isEmpty()) {
            return;
        }
        final Object obj = sel.getFirstElement();
        if (obj.getClass().equals(Account.class)) {
            this.doImport((Account)obj, data);
        }
    }
    
    public void	doImport(final Account target, final Object data) {
        this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
        this.setEnabled(false);
        try {
            new Quicken2000Import(bomService).run(data, (Account)target, null);
        } catch (Exception e) {
            LOG.error("doImport:", e); //$NON-NLS-1$
        } finally {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
            this.setEnabled(true);
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#canSearch()
     */
    public boolean canSearch() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#doFind(java.lang.Object, boolean)
     */
    public List<RunningEntry> doFind(SearchParameter param) {
        if (param.getPrototype() != null && param.getPrototype().equals(LedgerEntryViewer.class)) {
            Account account = (Account)this.bomService.getGeneric(Account.class, "reference", param.getScope()); //$NON-NLS-1$
            if (account != null) {
                LedgerEntryViewer viewer = (LedgerEntryViewer)this.selectTab(account);
                if (viewer != null) {
                    viewer.doFind(param);
                }
            }
        }
        return super.doFind(param);
    }
    
    
    public class RefreshAction extends Action {
        public RefreshAction() {
            super(Messages.getString("LedgerTreeViewer.85")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            treeViewer.refresh();
        }
    }

    class AccountInsertAction extends Action {
        public AccountInsertAction() {
            super(Messages.getString("LedgerTreeViewer.21")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            Account newAcct = (Account)GUIUtil.promptForNewObject(Messages.getString("LedgerTreeViewer.22"), Messages.getString("LedgerTreeViewer.23"), null, Account.class); //$NON-NLS-1$ //$NON-NLS-2$
            if (newAcct != null) {
                Account acct = (Account)bomService.getGeneric(Account.class, Messages.getString("LedgerTreeViewer.24"), newAcct.getReference()); //$NON-NLS-1$
                if (acct != null) {
                    GUIUtil.showError(Messages.getString("LedgerTreeViewer.25"), Messages.getString("LedgerTreeViewer.26") + newAcct.getReference() + Messages.getString("LedgerTreeViewer.27"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                bomService.storeGeneric(newAcct, "reference", newAcct.getReference()); //$NON-NLS-1$
	            selectTab(newAcct, AccountViewer.class, new AccountMeta());
	            treeViewer.refresh();
	            treeViewer.setSelection(new StructuredSelection(newAcct));
            }
        }
    }

    public class AccountEditAction extends Action {
        Account selectedAcct;
        public AccountEditAction(Account account) {
            super(Messages.getString("LedgerTreeViewer.29")); //$NON-NLS-1$
            this.selectedAcct = account;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            selectTab(selectedAcct, AccountViewer.class,
                    this.selectedAcct instanceof LedgerAccount ? new LedgerAccountMeta() : new AccountMeta());
            treeViewer.refresh();
        }
    }

    public class AccountDeleteAction extends Action {
        Account selectedAcct;
        public AccountDeleteAction(Account account) {
            super(Messages.getString("LedgerTreeViewer.30")); //$NON-NLS-1$
            this.selectedAcct = account;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeViewer.31"), //$NON-NLS-1$
                    Messages.getString("LedgerTreeViewer.32") + this.selectedAcct.getReference() + "' ?", true)) { //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            bomService.deleteAccount(selectedAcct);
            treeViewer.refresh();
        }
    }

    public class InvoiceInsertAction extends Action {
        public InvoiceInsertAction() {
            super(Messages.getString("LedgerTreeViewer.34")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            Invoice newInvoice = (Invoice)GUIUtil.promptForNewObject(Messages.getString("LedgerTreeViewer.35"), //$NON-NLS-1$
                    Messages.getString("LedgerTreeViewer.36"), null, Invoice.class, //$NON-NLS-1$
                    new InvoiceHelper().getNextReference(LedgerTreeViewer.this.bomService));
            if (newInvoice != null) {
                Invoice invoice = (Invoice)bomService.getGeneric(Invoice.class, Messages.getString("LedgerTreeViewer.37"), newInvoice.getReference()); //$NON-NLS-1$
                if (invoice != null) {
                    GUIUtil.showError(Messages.getString("LedgerTreeViewer.38"), Messages.getString("LedgerTreeViewer.39") + newInvoice.getReference() + Messages.getString("LedgerTreeViewer.40"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                bomService.storeGeneric(newInvoice, "reference", newInvoice.getReference()); //$NON-NLS-1$
	            selectTab(newInvoice, InvoiceViewer.class, new InvoiceMeta1(newInvoice));
	            treeViewer.refresh();
	            treeViewer.setSelection(new StructuredSelection(newInvoice));
            }
        }
    }

    public class InvoiceEditAction extends Action {
        Invoice selectedInvoice;
        public InvoiceEditAction(Invoice invoice) {
            super(Messages.getString("LedgerTreeViewer.42")); //$NON-NLS-1$
            this.selectedInvoice = invoice;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            selectTab(selectedInvoice);
            treeViewer.refresh();
        }
    }

    public class InvoiceDeleteAction extends Action {
        Invoice selectedInvoice;
        public InvoiceDeleteAction(Invoice invoice) {
            super(Messages.getString("LedgerTreeViewer.43")); //$NON-NLS-1$
            this.selectedInvoice = invoice;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeViewer.44"), //$NON-NLS-1$
                    Messages.getString("LedgerTreeViewer.45") + this.selectedInvoice.getReference() + "' ?", true)) { //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            bomService.deleteGeneric(selectedInvoice, "reference", selectedInvoice.getReference()); //$NON-NLS-1$
            treeViewer.refresh();
        }
    }

    public class AllocationRuleInsertAction extends Action {
        public AllocationRuleInsertAction() {
            super(Messages.getString("LedgerTreeViewer.48")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            AllocationRule newRule = (AllocationRule)GUIUtil.promptForNewObject(Messages.getString("LedgerTreeViewer.49"), Messages.getString("LedgerTreeViewer.50"), null, AllocationRule.class); //$NON-NLS-1$ //$NON-NLS-2$
            if (newRule != null) {
                AllocationRule rule = (AllocationRule)bomService.getGeneric(AllocationRule.class, "name", newRule.getName()); //$NON-NLS-1$
                if (rule != null) {
                    GUIUtil.showError(Messages.getString("LedgerTreeViewer.52"), Messages.getString("LedgerTreeViewer.53") + newRule.getName() + Messages.getString("LedgerTreeViewer.54"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                bomService.storeGeneric(newRule, Messages.getString("LedgerTreeViewer.55"), newRule.getName()); //$NON-NLS-1$
	            selectTab(newRule, AllocationRuleViewer.class, new AllocationRuleMeta(newRule));
	            treeViewer.refresh();
	            treeViewer.setSelection(new StructuredSelection(newRule));
            }
        }
    }

    public class AllocationRuleDeleteAction extends Action {
        private AllocationRule selectedRule;
        public AllocationRuleDeleteAction(AllocationRule rule) {
            super(Messages.getString("LedgerTreeViewer.56")); //$NON-NLS-1$
            this.selectedRule = rule;
        }
            /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeViewer.57"), //$NON-NLS-1$
                    Messages.getString("LedgerTreeViewer.58") + this.selectedRule.getName() + "' ?", true)) { //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            bomService.deleteGeneric(selectedRule, "name", this.selectedRule.getName()); //$NON-NLS-1$
            treeViewer.refresh();
        }
}

    public class ScheduleEntryInsertAction extends Action {
        public ScheduleEntryInsertAction() {
            super(Messages.getString("LedgerTreeViewer.61")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            Integer entryId = bomService.getNextId(ScheduledEntry.class);
            ScheduledEntry newSE = new ScheduledEntry(entryId);
            bomService.storeGeneric(newSE, "entryId", newSE.getEntryId()); //$NON-NLS-1$
            selectTab(newSE, ScheduledEntryViewer.class, new ScheduledEntryMeta());
            treeViewer.refresh();
            treeViewer.setSelection(new StructuredSelection(newSE));
        }
    }
    
    public class ScheduledEntryEditAction extends Action {
        ScheduledEntry selectedSE;
        public ScheduledEntryEditAction(ScheduledEntry se) {
            super(Messages.getString("LedgerTreeViewer.63")); //$NON-NLS-1$
            this.selectedSE = se;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            selectTab(selectedSE, ScheduledEntryViewer.class, new ScheduledEntryMeta());
            treeViewer.refresh();
        }
    }

    public class ScheduledEntryDeleteAction extends Action {
        ScheduledEntry selectedSE;
        public ScheduledEntryDeleteAction(ScheduledEntry se) {
            super(Messages.getString("LedgerTreeViewer.64")); //$NON-NLS-1$
            this.selectedSE = se;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeViewer.65"), Messages.getString("LedgerTreeViewer.66"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            bomService.deleteGeneric(selectedSE, "entryId", selectedSE.getEntryId()); //$NON-NLS-1$
            treeViewer.refresh();
        }
    }

    public class GenericReportDeleteAction extends Action {
        BaseGenericReport<?> selectedReport;
        public GenericReportDeleteAction(BaseGenericReport<?> report) {
            super(Messages.getString("LedgerTreeViewer.68")); //$NON-NLS-1$
            this.selectedReport = report;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeViewer.69"), Messages.getString("LedgerTreeViewer.70") + selectedReport.getName() + " ?", true)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                return;
            }
            bomService.deleteGeneric(selectedReport, "name", selectedReport.getName()); //$NON-NLS-1$
            treeViewer.refresh();
        }
    }

    public class BatchReportInsertAction extends Action {
        public BatchReportInsertAction() {
            super(Messages.getString("LedgerTreeViewer.73")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            BatchReport newReport = (BatchReport)GUIUtil.promptForNewObject(Messages.getString("LedgerTreeViewer.74"), //$NON-NLS-1$
                    Messages.getString("LedgerTreeViewer.75"), null, BatchReport.class); //$NON-NLS-1$
            if (newReport != null) {
                BatchReport report = (BatchReport)bomService.getGeneric(BatchReport.class, "name", newReport.getName()); //$NON-NLS-1$
                if (report != null) {
                    GUIUtil.showError(Messages.getString("LedgerTreeViewer.77"), Messages.getString("LedgerTreeViewer.78") + newReport.getName() + Messages.getString("LedgerTreeViewer.79"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                bomService.storeGeneric(newReport, "name", newReport.getName()); //$NON-NLS-1$
	            selectTab(newReport, BatchReportViewer.class, new BatchReportMeta());
	            treeViewer.refresh();
	            treeViewer.setSelection(new StructuredSelection(newReport));
            }
        }
    }
    
    public class CopyAction extends Action {
        public CopyAction() {
            super(Messages.getString("LedgerTreeViewer.81")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            doCopy();
        }
    }

    public class PasteAction extends Action {
        public PasteAction() {
            super(Messages.getString("LedgerTreeViewer.82")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            doPaste();
        }
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent evt) {
        if (evt.op == BOMEvent.OP_BROADCAST) {
            this.treeViewer.refresh();
            return;
        }
        CTabItem [] items = tabFolder.getItems();
        for (int i = 0; i < items.length; i++) {
            CTabItem item = items[i];
            item.setText(item.getData().toString());
            if (evt.op == BOMEvent.OP_DEL && evt.entity.equals(item.getData())) {
                item.dispose();
                break;
            }
        }
        this.treeViewer.refresh();
    }

    public boolean isValid() {
        return this.valid;
    }
    public TreeViewer	getTreeViewer() {
        return this.treeViewer;
    }
    /**
     * @return Returns the tabFolder.
     */
    public CTabFolder getTabFolder() {
        return tabFolder;
    }
    /**
     * @param tabFolder The tabFolder to set.
     */
    public void setTabFolder(CTabFolder tabFolder) {
        this.tabFolder = tabFolder;
    }
    
}
