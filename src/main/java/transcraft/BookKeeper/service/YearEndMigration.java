/**
 * Created on 05-Nov-2005
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
package transcraft.BookKeeper.service;

import static org.slf4j.LoggerFactory.getLogger;
import static transcraft.myaccountant.ui.GUIUtil.updateProgressMonitor;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.myaccountant.report.ReportDateUtil;
import transcraft.myaccountant.ui.CalendarFieldEditor;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.schema.Defragment;

/**
 * cut over to a new year, remove transactions in the old year
 * 
 * @author david.tran@transcraft.co.uk
 */
public class YearEndMigration {
	private static final Logger LOG = getLogger(YearEndMigration.class);

    private Date startDate;
   
    /**
     * 
     *
     */
    public YearEndMigration() {
    }
    
    /**
     * 
     */
    public YearEndMigration(Date sd) {
        this.startDate = sd;
    }


    /**
     * this migration method only works for BOMService which is not password protected. For password protected BOMService,
     * use the migrate(fromService, toPath, monitor) instead
     * @param fromPath
     * @param toPath
     * @param monitor
     */
    public void migrate(String fromPath, String toPath, IProgressMonitor monitor) {
        BOMService fromService = null;
        try {
            fromService = new BOMService(fromPath);
            this.migrate(fromService, toPath, monitor);
        } finally {
            if (fromService != null) {
                try {
                    fromService.getConn().close();
                } catch (Exception e) {}
            }
        }
    }

    /**
     * for password authenticated BOMService migration
     * @param fromService
     * @param toPath
     * @param monitor
     */
    public void migrate(BOMService fromService, String toPath, IProgressMonitor monitor) {
        File fp = new File(toPath);
        if (fp.exists()) {
            throw new RuntimeException(Messages.getString("YearEndMigration.0") + toPath + Messages.getString("YearEndMigration.19")); //$NON-NLS-1$ //$NON-NLS-2$
        }
                
        // first defragment to compact the size
        Defragment defrag = new Defragment();
       	defrag.migrate(fromService.dbFile, fromService.dbConn, toPath, monitor);
        
        if (this.startDate == null) {
            return;
        }
        
        BOMService toService = null;
        try {
            toService = new BOMService(fp);
            // auto-logon to the destination file
            fromService.propagateCredentials(toService);
            
            Map<Account, Double> closingBalances = Maps.newHashMap();
            List<RunningEntry> tobeDeleted = Lists.newArrayList();
            
            // 1. mark entries before startDate for purging, noting the ending balances at the same time
            List<Account> accounts = toService.getRefData(Account.class, false);
            for (Account account : accounts) {
                List<Entry> entriesList = toService.getLedgerEntries(BOMService.VD_LEDGER, account);
                if (entriesList.size() == 0) {
                    continue;
                }
                
                RunningEntry endingRE = (RunningEntry)entriesList.get(entriesList.size() - 1);
                // store the ending balance for now, will calculate the opening balance later
                closingBalances.put(account, Double.valueOf(endingRE.getBalance()));
                
                if (monitor != null) {
                    monitor.beginTask(Messages.getString("YearEndMigration.2") + account.getReference() + "'", entriesList.size()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                int i = 0;
                for (Entry entry : entriesList) {
                    RunningEntry re = (RunningEntry)entry;
                    if (re.getValueDate().before(this.startDate)) {
                        tobeDeleted.add(re);
                    } else {
                        break;
                    }
        			updateProgressMonitor(monitor, i++);
                }
            }

            // 2. Purge marked entries
            if (tobeDeleted.size() > 0) {
                if (monitor != null) {
                    monitor.beginTask(Messages.getString("YearEndMigration.4") +
                    		Messages.getString("YearEndMigration.5"), tobeDeleted.size()); //$NON-NLS-1$ //$NON-NLS-2$
                }
                int i = 0;
                for (RunningEntry re : tobeDeleted) {
                    toService.delete(re.getEntryId());
        			updateProgressMonitor(monitor, i++);
                }
            }
            
            // 3. recalculate balances
            if (monitor != null) {
                monitor.beginTask(Messages.getString("YearEndMigration.6"), accounts.size()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            int i = 0;
            for (Account account : accounts) {
                List<Entry> entriesList = toService.getLedgerEntries(BOMService.VD_LEDGER, account);
                if (entriesList.size() == 0) {
                    continue;
                }
                
                RunningEntry firstRE = (RunningEntry)entriesList.get(0);
                RunningEntry lastRE = (RunningEntry)entriesList.get(entriesList.size() - 1);
                double movement = lastRE.getBalance() - firstRE.getBalance();

                /*
                 * the opening balance in the account is actually the closing balance before the purge. Now
                 * we know how much the Account has moved by, work backward to derive the opening balance. The
                 * goal is to keep the ending balance the same
                 */
                double endingBalance = ((Double)closingBalances.get(account)).doubleValue();
                account.setOpeningBalance(endingBalance - movement - firstRE.getAmount());
                Calendar cal = Calendar.getInstance();
                cal.setTime(firstRE.getValueDate());
                cal.add(Calendar.DAY_OF_MONTH, -1);
                account.setOpeningDate(cal.getTime());
                toService.getConn().store(account);
    			updateProgressMonitor(monitor, i);
            }
            
            // 4. purge invoices before startDate
            List<Invoice> invoices = toService.getRefData(Invoice.class);
            invoices = invoices.stream().sorted((i1, i2) -> {
                return i1.getInvoicedDate() != null && i2.getInvoicedDate() != null &&
                	i1.getInvoicedDate().before(i2.getInvoicedDate()) ? -1 : 1;
        	}).collect(Collectors.toList());
            
            if (monitor != null) {
                monitor.beginTask(Messages.getString("YearEndMigration.8"), invoices.size()); //$NON-NLS-1$
            }
            i = 0;
            for (Invoice invoice : invoices) {
                if (invoice.getInvoicedDate().before(this.startDate)) {
                    toService.getConn().delete(invoice);
                } else {
                    break;
                }
    			updateProgressMonitor(monitor, i++);
            }
        } finally {
            if (toService != null) {
                try {
		            toService.getConn().commit();
		            toService.getConn().close();
                } catch (Exception e) {
                	LOG.error(String.format("close(%s)", toPath), e); //$NON-NLS-1$
                }
            }
            if (monitor != null) {
                monitor.done();
            }
        }
    }
    
    /**
     * @return Returns the startDate.
     */
    Date getStartDate() {
        return startDate;
    }
    /**
     * @param startDate The startDate to set.
     */
    void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * return the dialog which prompts user for parameters
     * @param parent
     * @return
     */
    public YearEndDialog	getParameterDialog(Shell parent) {
        return new YearEndDialog(parent);
    }
    
    public class YearEndDialog extends TitleAreaDialog {
        private String title = Messages.getString("YearEndMigration.9"); //$NON-NLS-1$
        private String message = Messages.getString("YearEndMigration.10"); //$NON-NLS-1$

        private CalendarFieldEditor sdEditor;
        private Button	useNewFileButton;
        private Text newFilePicker;
        
        private Date startDate;
        private boolean useNewFile = true;
        private String newFile;

        /**
         * @param parentShell
         */
        public YearEndDialog(Shell parentShell) {
            super(parentShell);
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
         */
        protected Control createContents(Composite parent) {

            Control contents = super.createContents(parent);
            this.setTitleImage(ImageCache.get("yearend")); //$NON-NLS-1$
            this.setTitle(this.title);
            parent.getShell().setText(title);
            this.setMessage(this.message);
            return contents;
        }
        /* (non-Javadoc)
         * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
         */
        protected Control createDialogArea(Composite parent) {
            Composite dialogArea = (Composite)super.createDialogArea(parent);

            Composite fileNameBar = this.getNewRow(dialogArea, 3);
            Label label = new Label(fileNameBar, SWT.NONE);
            label.setText(Messages.getString("YearEndMigration.12")); //$NON-NLS-1$
            this.newFilePicker = new Text(fileNameBar, SWT.BORDER);
            this.newFilePicker.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            this.newFilePicker.setText(this.newFile != null ? this.newFile : GUIUtil.getMemorisedPath(YearEndDialog.class));
            Button browseButton = new Button(fileNameBar, SWT.PUSH);
            browseButton.setImage(ImageCache.get("folder")); //$NON-NLS-1$
            browseButton.setToolTipText(Messages.getString("YearEndMigration.14")); //$NON-NLS-1$
            browseButton.addSelectionListener(new SelectionAdapter() {
                public void	widgetSelected(SelectionEvent evt) {
                    FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
                    fileDialog.setText(Messages.getString("YearEndMigration.15")); //$NON-NLS-1$
                    fileDialog.setFilterPath(GUIUtil.getMemorisedPath(YearEndDialog.class));
    	            fileDialog.setFilterExtensions(new String [] { "*." + BOMService.DEF_DB_SUFFIX }); //$NON-NLS-1$
    	            
    	            String path = fileDialog.open();
    	            if (path == null) {
    	                return;
    	            }
    	            
    	            GUIUtil.setMemorisedPath(YearEndDialog.class, new File(path).getAbsolutePath());
    	            YearEndDialog.this.newFilePicker.setText(path);
                }
            });

            Composite sdBar = this.getNewRow(dialogArea, 2);
            label = new Label(sdBar, SWT.FLAT);
            label.setText(Messages.getString("YearEndMigration.17")); //$NON-NLS-1$
            this.sdEditor = new CalendarFieldEditor(sdBar);
            this.sdEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            if (this.startDate == null) {
                this.startDate = ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISTAXYEAR).getStartDate();
            }
            this.sdEditor.setValue(this.startDate);

            Composite useFileBar = this.getNewRow(dialogArea, 2);
            label = new Label(useFileBar, SWT.FLAT);
            label.setText(Messages.getString("YearEndMigration.18")); //$NON-NLS-1$
            this.useNewFileButton = new Button(useFileBar, SWT.CHECK);
            this.useNewFileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            this.useNewFileButton.setSelection(this.useNewFile);
            
            return this.dialogArea;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.window.Window#close()
         */
        public boolean close() {
            this.useNewFile = this.useNewFileButton.getSelection();
            this.startDate = this.sdEditor.getValue().getTime();
            this.newFile = this.newFilePicker.getText();
            return super.close();
        }
        protected Composite	getNewRow(Composite parent, int numCols) {
            Composite bar = new Composite(parent, SWT.FLAT);
            GridLayout ly = new GridLayout();
            ly.numColumns = numCols;
            bar.setLayout(ly);
            bar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            return bar;
        }
        
        /**
         * @return Returns the newFile.
         */
        public String getNewFile() {
            return newFile;
        }
        /**
         * @return Returns the sd.
         */
        public Date getStartDate() {
            return this.startDate;
        }
        /**
         * @return Returns the useNewFile.
         */
        public boolean isUseNewFile() {
            return useNewFile;
        }
    }
}
