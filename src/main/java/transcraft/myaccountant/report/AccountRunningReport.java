/**
 * Created on 15-Sep-2005
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
package transcraft.myaccountant.report;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.service.BOMService;

/**
 * used for providing adhoc reports from LedgerEntryViewer
 * @author david.tran@transcraft.co.uk
 */
public class AccountRunningReport extends GenericReport<Entry> {

    transient boolean firstTime = true;
    protected Account account;
    protected Integer [] entryIds;
    
    /**
     * 
     */
    public AccountRunningReport(Account account) {
        super(account.getReference());
        this.setReportTemplate("/templates/AccountRunningReport.jasper"); //$NON-NLS-1$
        this.account = account;
        this.setStartDate(null);
        this.setEndDate(null);
        this.setDescription(account.getDescription());
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#next()
     */
    public boolean next() throws JRException {
        if (this.firstTime) {
            this.firstTime = false;
            return true;
        }
        return false;
    }
    /**
     * this supports a selection of entries being selected from the viewer
     * @param entryIds The entryIds to set.
     */
    public void setEntryIds(Integer[] entryIds) {
        this.entryIds = entryIds;
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        return null;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.report.GenericReport#run()
     */
    public void run() {
        this.firstTime = true;
        this.subReports.clear();

        LedgerReport accountReport = new LedgerReport(this.account.getReference());
        accountReport.setDescription(""); //$NON-NLS-1$
        accountReport.setLedgerType(BOMService.VD_LEDGER);
        accountReport.setEntryIds(this.entryIds);
        if ((this.account instanceof LedgerAccount) || ! this.account.isTaxed()) {
            accountReport.setCalculateVAT(false);
        }
        if (this.account instanceof LedgerAccount) {
            accountReport.setUsePostedAmount(true);
        }
        this.loadSubReport("account", this.account.getDescription(), "/templates/LedgerReport.jasper", accountReport); //$NON-NLS-1$ //$NON-NLS-2$
        this.runSubReports();
    }
}
