/**
 * Created on 12-Jun-2005
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

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.CoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.slf4j.Logger;

import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.BookKeeper.service.YearEndMigration;
import transcraft.myaccountant.ui.action.ClipCopyAction;
import transcraft.myaccountant.ui.action.ClipCutAction;
import transcraft.myaccountant.ui.action.ClipPasteAction;
import transcraft.myaccountant.ui.action.HelpAction;
import transcraft.myaccountant.ui.action.HtmlGenAction;
import transcraft.myaccountant.ui.action.PdfGenAction;
import transcraft.myaccountant.ui.action.SearchAction;
import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.ui.schema.SchemaPopup;
import transcraft.myaccountant.ui.vc.BookExplorer;
import transcraft.myaccountant.utils.FileUtil;

/**
 * @author david.tran@transcraft.co.uk
 */
public class MainApp extends ApplicationWindow implements IPropertyChangeListener, BOMListener {
	
	// LOG4J file name timestamp
	static{
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HHmmss");
        System.setProperty("log4j.timestamp", dateFormat.format(new Date()));
    }
	
    private static final Logger LOG = getLogger(MainApp.class);

    public static final String APP_TITLE = "Transcraft My Accountant"; //$NON-NLS-1$
    public static final String PROP_APP_LOCATION = "gui.mainApp.location"; //$NON-NLS-1$
    public static final String PROP_MEMORISED_DB = "gui.memorisedDBs"; //$NON-NLS-1$
    public static final int MAX_MEMORISED_DB = 3;
    
    private CTabFolder rootTab;
    private MenuManager fileMgr;
    
    private NewDBAction newAction = new NewDBAction();
    private OpenDBAction openAction = new OpenDBAction();
    private ExitAppAction exitAction = new ExitAppAction();
    private PrefAction prefAction = new PrefAction();
    private InitAction initAction = new InitAction();
    private SwitchUserAction adminAction = new SwitchUserAction();
    private DisplayHelpAction helpAction = new DisplayHelpAction();
    private YearEndMigrateAction yearEndAction = new YearEndMigrateAction();
    private SchemaBrowserAction schemaBrowserAction = new SchemaBrowserAction();
    
    private Font	defaultFont;
    private String fileName;
    
    public MainApp(String fn) {
        super(null);
        this.fileName = fn;
        this.addMenuBar();
        this.addCoolBar(SWT.NONE);
        this.addStatusLine();
    }
    public void run() {
        this.setBlockOnOpen(true);
        try {
            this.open();
        } catch (Exception e) {
            LOG.error("run():", e); //$NON-NLS-1$
        }
        Display.getCurrent().dispose();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        if (PreferenceFactory.getPreferenceStore().getBoolean(PreferenceFactory.EXIT_APP_CONFIRMED) &&
                ! GUIUtil.confirm(Messages.getString("MainApp.4"), Messages.getString("MainApp.5"))) { //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        this.done();
        try {
            return super.close();
        } catch (Exception e) {
            System.exit(1);
        }
        return false;
    }
    public static void main(String [] args) {
    	LOG.info("Application starting ...");
    	String fn = null;
    	if (args.length >= 1) {
    		fn = args[0];
    	}
        new MainApp(fn).run();
    }
    protected void done() {
        if (this.rootTab != null) {
            CTabItem [] tabs = this.rootTab.getItems();
            for (int i = 0; i < tabs.length; i++) {
                try {
                    BookExplorer bv = (BookExplorer)tabs[i].getControl();
                    bv.done();       
                    
                } catch (Exception e) {
                    LOG.error(String.format("done(%d)", i), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        }
        GUIUtil.storeLastLocation(PROP_APP_LOCATION, this.getShell());
    	LOG.info("Application ended.");
    }
    protected Control	createContents(Composite parent) {
        ImageCache.init(parent.getDisplay());
        parent.getShell().setImage(ImageCache.get("logo")); //$NON-NLS-1$
        parent.getShell().setText(APP_TITLE);
        // make the icon the default for all popups
        Window.setDefaultImage(ImageCache.get("logo")); //$NON-NLS-1$
        
        // initialise preferences
        this.propertyChange(null);
        PreferenceFactory.getPreferenceStore().addPropertyChangeListener(this);
        
        this.rootTab = new CTabFolder(parent.getShell(), SWT.BOTTOM);
        
        this.rootTab.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                /*
                 * perform context sensitive enabling/disabling of the menu buttons
                 * depending on the selected tab
                 */
                CTabItem item = (CTabItem)evt.item;
                BookExplorer bex = (BookExplorer)item.getControl();
                try {
                    if (bex != null) {
	                    MainApp.this.getShell().setText(APP_TITLE + " - " + bex.getBookName()); //$NON-NLS-1$
	                    MainApp.this.yearEndAction.setEnabled(bex.isAdminMode());
	                    MainApp.this.initAction.setEnabled(bex.isAdminMode());
                    }
                } catch (Exception e) {
                    LOG.error("widgetSelected():", e); //$NON-NLS-1$
                }
            }
        });
        
        GUIUtil.restoreLastLocation(PROP_APP_LOCATION, parent.getShell());
        if (this.fileName != null) {
            openBook(this.fileName);
            this.fileName = null;
        }
        return this.rootTab;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt == null || (evt.getProperty() != null && evt.getProperty().equals(PreferenceFactory.DEF_FONT))) {
	        String fontName = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.DEF_FONT);
	        if (fontName != null && fontName.length() > 0) {
	        	this.defaultFont = new Font(Display.getCurrent(),
	                    PreferenceConverter.getFontData(PreferenceFactory.getPreferenceStore(), fontName));
	            if (this.defaultFont != null) {
	                this.defaultFont = Display.getCurrent().getSystemFont();
	            }
                this.getShell().redraw();
                this.getShell().update();
	        }
        }
        if (evt == null || (evt.getProperty() != null && evt.getProperty().equals(PROP_MEMORISED_DB))) {
            this.rebuildFileMenuManager();
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent evt) {
        if (evt.op == BOMEvent.OP_USER) {
            this.refreshForSelectedBook();
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ApplicationWindow#getFont()
     */
    protected Font getFont() {
        return this.defaultFont != null ? this.defaultFont : super.getFont();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ApplicationWindow#createCoolBarManager(int)
     */
    protected CoolBarManager createCoolBarManager(int style) {
        CoolBarManager cbm = new CoolBarManager(style);
        ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
        tbm.add(this.newAction);
        tbm.add(this.openAction);
        tbm.add(this.exitAction);
        cbm.add(tbm);
        tbm = new ToolBarManager(SWT.FLAT);
        tbm.add(new ClipCopyAction());
        tbm.add(new ClipCutAction());
        tbm.add(new ClipPasteAction());
        tbm.add(new SearchAction());
        tbm.add(new HelpAction());
        cbm.add(tbm);
        tbm = new ToolBarManager(SWT.FLAT);
        tbm.add(new HtmlGenAction());
        tbm.add(new PdfGenAction());
        cbm.add(tbm);
        tbm = new ToolBarManager(SWT.FLAT);
        tbm.add(this.prefAction);
        tbm.add(this.yearEndAction);
        tbm.add(this.schemaBrowserAction);
        tbm.add(this.initAction);
        tbm.add(this.adminAction);
        tbm.add(this.helpAction);
        cbm.add(tbm);
        return cbm;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ApplicationWindow#createMenuManager()
     */
    protected MenuManager createMenuManager() {
        MenuManager mgr = new MenuManager();
        this.fileMgr = new MenuManager(Messages.getString("MainApp.12")); //$NON-NLS-1$
        this.rebuildFileMenuManager();
        mgr.add(fileMgr);
        MenuManager editMgr = new MenuManager(Messages.getString("MainApp.13")); //$NON-NLS-1$
        editMgr.add(new ClipCopyAction());
        editMgr.add(new ClipCutAction());
        editMgr.add(new ClipPasteAction());
        editMgr.add(new SearchAction());
        mgr.add(editMgr);
        MenuManager genMgr = new MenuManager(Messages.getString("MainApp.14")); //$NON-NLS-1$
        genMgr.add(new HtmlGenAction());
        genMgr.add(new PdfGenAction());
        mgr.add(genMgr);
        MenuManager toolsMgr = new MenuManager(Messages.getString("MainApp.15")); //$NON-NLS-1$
        toolsMgr.add(this.prefAction);
        toolsMgr.add(this.yearEndAction);
        toolsMgr.add(this.schemaBrowserAction);
        toolsMgr.add(this.initAction);
        toolsMgr.add(this.adminAction);
        mgr.add(toolsMgr);
        MenuManager helpMgr = new MenuManager(Messages.getString("MainApp.16")); //$NON-NLS-1$
        helpMgr.add(this.helpAction);
        helpMgr.add(new HelpAction());
        mgr.add(helpMgr);
        return mgr;
    }
    
    /**
     * separate out this menu so it can be rebuilt each time DB is open or closed
     *
     */
    private void	rebuildFileMenuManager() {
        this.fileMgr.removeAll();;
        this.fileMgr.add(this.newAction);
        this.fileMgr.add(this.openAction);
        List<String> memorisedDBs = PreferenceFactory.getAsList(PROP_MEMORISED_DB);
        int i = 0;
        for (String path : memorisedDBs) {
            this.fileMgr.add(new MemorisedOpenDBAction(path));
            i++;
            if (i >= MAX_MEMORISED_DB) {
            	break;
            }
        }
        this.fileMgr.add(this.exitAction);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.ApplicationWindow#createStatusLineManager()
     */
    protected StatusLineManager createStatusLineManager() {
        return GUIUtil.getStatus();
    }
    
    public class NewDBAction extends Action {
        public NewDBAction() {
            super(Messages.getString("MainApp.17"), ImageDescriptor.createFromFile(NewDBAction.class, "/images/newdb.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.19")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            FileDialog fileDialog = new FileDialog(getShell(), SWT.SAVE);
            fileDialog.setText(Messages.getString("MainApp.20")); //$NON-NLS-1$
            fileDialog.setFilterPath(System.getProperty("user.home")); //$NON-NLS-1$
            String [] fileExt = new String[] { "*." + BOMService.DEF_DB_SUFFIX }; //$NON-NLS-1$
            fileDialog.setFilterExtensions(fileExt);
            String selected = fileDialog.open();
            if (selected != null) {
                if (! selected.endsWith("." + BOMService.DEF_DB_SUFFIX)) { //$NON-NLS-1$
                    selected += "." + BOMService.DEF_DB_SUFFIX; //$NON-NLS-1$
                }
                File fp = new File(selected);
                if (fp.exists()) {
                    GUIUtil.showError(Messages.getString("MainApp.25"), Messages.getString("MainApp.26") + fp.getName() + Messages.getString("MainApp.27"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                openBook(selected);
            }
        }
    }

    public class OpenDBAction extends Action {
        public OpenDBAction() {
            super(Messages.getString("MainApp.28"), ImageDescriptor.createFromFile(OpenDBAction.class, "/images/opendb.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.30")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
            fileDialog.setText(Messages.getString("MainApp.31")); //$NON-NLS-1$
            fileDialog.setFilterPath(GUIUtil.getMemorisedPath(OpenDBAction.class));
            String [] fileExt = new String[] { "*." + BOMService.DEF_DB_SUFFIX }; //$NON-NLS-1$
            fileDialog.setFilterExtensions(fileExt);
            String selected = fileDialog.open();
            if (selected != null) {
                File fp = new File(selected);
                if (! fp.exists()) {
                    GUIUtil.showError(Messages.getString("MainApp.33"), Messages.getString("MainApp.34") + fp.getName() + Messages.getString("MainApp.35"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                openBook(selected);
	            GUIUtil.setMemorisedPath(OpenDBAction.class, new File(selected).getParent());
            }
        }
    }

    public class MemorisedOpenDBAction extends Action {
        String path;
        public MemorisedOpenDBAction(String path) {
            super(new File(path).getName());
            this.setToolTipText(Messages.getString("MainApp.36") + path); //$NON-NLS-1$
            this.path = path;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            openBook(this.path);
        }
    }
    
    public class ExitAppAction extends Action {
        public ExitAppAction() {
            super(Messages.getString("MainApp.37"), ImageDescriptor.createFromFile(ExitAppAction.class, "/images/exitapp.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.39")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            close();
        }
    }

    public class SwitchUserAction extends Action {
        public SwitchUserAction() {
            super(Messages.getString("MainApp.40"), ImageDescriptor.createFromFile(PrefAction.class, "/images/keys.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            this.setToolTipText(Messages.getString("MainApp.42")); //$NON-NLS-1$
            this.setEnabled(false);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            MainApp.this.switchUser();
        }
    }
    
    public class DisplayHelpAction extends Action {
        public DisplayHelpAction() {
            super(Messages.getString("MainApp.81"), ImageDescriptor.createFromFile(PrefAction.class, "/images/help.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            this.setToolTipText(Messages.getString("MainApp.82")); //$NON-NLS-1$
            this.setEnabled(true);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            MainApp.this.displayHelp();
        }
    }

    public class PrefAction extends Action {
        public PrefAction() {
            super(Messages.getString("MainApp.43"), ImageDescriptor.createFromFile(PrefAction.class, "/images/customise.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.45")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            PreferenceFactory.showPreferenceDialog(getShell());
        }
    }

    public class InitAction extends Action {
        public InitAction() {
            super(Messages.getString("MainApp.46"), ImageDescriptor.createFromFile(PrefAction.class, "/images/init.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.48")); //$NON-NLS-1$
            this.setEnabled(false);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            initBook();
        }
    }

    public class SchemaBrowserAction extends Action {
        public SchemaBrowserAction() {
            super(Messages.getString("MainApp.49"), ImageDescriptor.createFromFile(PrefAction.class, "/images/schema.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.51")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            popupSchemaBrowser();
        }
    }
    
    public class YearEndMigrateAction extends Action {
        public YearEndMigrateAction() {
            super(Messages.getString("MainApp.52"), ImageDescriptor.createFromFile(PrefAction.class, "/images/yearend.gif")); //$NON-NLS-1$ //$NON-NLS-2$
            setToolTipText(Messages.getString("MainApp.54")); //$NON-NLS-1$
            this.setEnabled(false);
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            CTabItem item = MainApp.this.rootTab.getSelection();
            BookExplorer bex = (BookExplorer)item.getControl();
            if (bex == null || bex.isDisposed()) {
                GUIUtil.showError(Messages.getString("MainApp.55"), Messages.getString("MainApp.56"), null); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
            
            //String sourcePath = bex.getBookAbsolutePath();
            FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
            fileDialog.setText(Messages.getString("MainApp.57") + bex.getBookName() + " ..."); //$NON-NLS-1$ //$NON-NLS-2$
            YearEndMigration yeMigrator = new YearEndMigration();
            YearEndMigration.YearEndDialog dialog = yeMigrator.getParameterDialog(MainApp.this.getShell());
            if (dialog.open() != Dialog.OK) {
                return;
            }
            
            String newYearFile = dialog.getNewFile();
            if (newYearFile != null) {
                if (! newYearFile.endsWith(BOMService.DEF_DB_SUFFIX)) {
                    newYearFile += (newYearFile.endsWith(".") ? "" : ".") + BOMService.DEF_DB_SUFFIX; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                }
                File fp = new File(newYearFile);
                if (fp.exists()) {
                    GUIUtil.showError(Messages.getString("MainApp.62"), Messages.getString("MainApp.63") + fp.getName() + Messages.getString("MainApp.64"), null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                    return;
                }
                boolean ok = MainApp.this.doYearEndMigration(bex.getBomService(), newYearFile, dialog.getStartDate());
	            GUIUtil.setMemorisedPath(YearEndMigrateAction.class, fp.getParent());
	            if (ok && dialog.isUseNewFile()) {
	                // first close the existing book
	                item.dispose();
	                // open the new book
	                MainApp.this.openBook(newYearFile);
	            }
            }
        }
    }

    protected boolean	doYearEndMigration(final BOMService fromService, final String toFile, final Date startDate) {
        try {
            MainApp.this.getShell().setEnabled(false);
            new ProgressMonitorDialog(this.getShell()).run(false, false, new IRunnableWithProgress() {
                public void	run(IProgressMonitor monitor) {
                    new YearEndMigration(startDate).migrate(fromService, toFile, monitor);
                }
            });
            return true;
        } catch (Exception e) {
            LOG.error(String.format("doYearEndMigration(%s)", toFile), e); //$NON-NLS-1$
            GUIUtil.showError(Messages.getString("MainApp.65"), Messages.getString("MainApp.66") + toFile + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            File fp = new File(toFile);
            if (fp.exists()) {
                fp.delete();
            }
        } finally {
            MainApp.this.getShell().setEnabled(true);
        }
        return false;
    }
    
    protected final void	switchUser() {
        CTabItem item = this.rootTab.getSelection();
        if (item == null) {
            return;
        }
        BookExplorer bex = (BookExplorer)item.getControl();
        try {
            bex.authenticate();
        } catch (Exception e) {
            GUIUtil.showError(Messages.getString("MainApp.68"), Messages.getString("MainApp.69"), e); //$NON-NLS-1$ //$NON-NLS-2$
            item.dispose();
        }
    }

    protected final void    displayHelp() {
        try {
        	File docsIndexFile = FileUtil.getAppLocation("docs/index.html");
        	LOG.debug("path={}", docsIndexFile);
            GUIUtil.launchBrowser(docsIndexFile.toString(), Messages.getString("MainApp.83")); //$NON-NLS-1$
        } catch (Exception e) {
        	LOG.error("displayHelp();", e); //$NON-NLS-1$
            GUIUtil.showError(Messages.getString("MainApp.68"), Messages.getString("MainApp.71"), e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    protected void	openBook(String fileName) {
        if (StringUtils.isBlank(fileName)) {
            return;
        }
        CTabItem [] tabs = this.rootTab.getItems();
        File fp = new File(fileName);
        CTabItem tabItem = null;
        for (int i = 0; i < tabs.length; i++) {
            BookExplorer bv = (BookExplorer)tabs[i].getControl();
            if (bv.getBookName().equals(fp.getName())) {
                tabItem = tabs[i];
                break;
            }
        }
        if (tabItem == null) {
            // create a new Tab and BookExplorer
            CTabItem tab = new CTabItem(this.rootTab, SWT.CLOSE);
            tab.setText(fp.getName());
            tab.setToolTipText(fp.getAbsolutePath());
            try {
	            final BookExplorer bv = new BookExplorer(this.rootTab, fp);
	            if (bv.isValid()) {
	                tab.setControl(bv);
	                tab.addDisposeListener(new DisposeListener() {
	                	@Override
	                	public void	widgetDisposed(DisposeEvent evt) {
	                		bv.done();
	                		MainApp.this.refreshForSelectedBook();
	                	}
	                });
	                bv.getBomService().addListener(MainApp.this);
	            } else {
	                bv.dispose();
	                tab.dispose();
	            }
	            tabItem = tab;
	            bv.setVisible(true);
            } catch (Exception e) {
                GUIUtil.showError(Messages.getString("MainApp.70"), Messages.getString("MainApp.71") + fp.getName() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                LOG.error(String.format("openBook(%s)", fileName), e); //$NON-NLS-1$
                tab.dispose();
            }
        }
     
        if (tabItem != null) {
            this.rootTab.setSelection(tabItem);
            this.rootTab.showItem(tabItem);
        }
        this.refreshForSelectedBook();
    }
    
    /**
     * update functions and titles for the selected book
     *
     */
    protected void	refreshForSelectedBook() {
        this.yearEndAction.setEnabled(this.rootTab.getItemCount() != 0);
        this.schemaBrowserAction.setEnabled(this.rootTab.getItemCount() == 0);
        this.initAction.setEnabled(this.rootTab.getItemCount() != 0);
        this.adminAction.setEnabled(this.rootTab.getItemCount() != 0);

        try {
	        CTabItem item = this.rootTab.getSelection();
	        if (item != null) {
		        BookExplorer bex = (BookExplorer)item.getControl();
		        this.getShell().setText(APP_TITLE + " - " + bex.getBookName() + //$NON-NLS-1$
		                (bex.getCurrentUserName() != null ? " [" + bex.getCurrentUserName() + "]" : "")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	            this.yearEndAction.setEnabled(bex.isAdminMode());
	        } else {
		        this.getShell().setText(APP_TITLE);
	            this.yearEndAction.setEnabled(false);
	        }
        } catch (Exception e) {
            LOG.error("refreshForSelectedBook()", e); //$NON-NLS-1$
        }
    }
    
    private SchemaPopup popup;
    protected void	popupSchemaBrowser() {
        if (this.popup == null) {
            this.popup = new SchemaPopup(this.getShell(), null, Messages.getString("MainApp.77")); //$NON-NLS-1$
        }
        this.popup.open(null);
    }
    protected void initBook() {
        CTabItem item = this.rootTab.getSelection();
        if (item != null) {
            BookExplorer bookViewer = (BookExplorer)item.getControl();
            if (! GUIUtil.confirm(Messages.getString("MainApp.78"), Messages.getString("MainApp.79") //$NON-NLS-1$ //$NON-NLS-2$
                    + bookViewer.getBookName() + Messages.getString("MainApp.80"))) { //$NON-NLS-1$
                return;
            }
            bookViewer.initDB();
        }
    }
    
}
