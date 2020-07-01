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

import java.util.List;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;

/**
 * @author david.tran@transcraft.co.uk
 */
public class ActivityReport extends GenericReport<Entry> {

    transient boolean firstTime = true;
    
    /**
     * 
     */
    public ActivityReport() {
        super(Messages.getString("ActivityReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/ActivityReport.jasper"); //$NON-NLS-1$
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

        LedgerReport invoicedReport = new LedgerReport(ReportUtil.getLedgerAccountRefs(this.bomService, BOMService.LCAT_INCOME));
        invoicedReport.setLedgerType(BOMService.TD_LEDGER);
        invoicedReport.setCalculateVAT(false);
        //invoicedReport.setUsePostedAmount(true);
        this.loadSubReport("invoiced", Messages.getString("ActivityReport.3"), "/templates/LedgerReport.jasper", invoicedReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

        List<String> businessAccounts = ReportUtil.getEntryAccountRefs(this.bomService, BOMService.ECAT_BUSINESS);
        LedgerReport receivedReport = new LedgerReport(businessAccounts);
        receivedReport.setEntryType(BOMService.ET_CRONLY);
        receivedReport.setLedgerType(BOMService.VD_LEDGER);
        receivedReport.setExcludeSameCatTxfr(true);
        receivedReport.setExternalOnly(true);
        this.loadSubReport("received", Messages.getString("ActivityReport.6"), "/templates/LedgerReport.jasper", receivedReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        LedgerReport paidReport = new LedgerReport(businessAccounts);
        paidReport.setEntryType(BOMService.ET_DRONLY);
        paidReport.setLedgerType(BOMService.VD_LEDGER);
        paidReport.setExcludeSameCatTxfr(true);
        paidReport.setExternalOnly(true);
        this.loadSubReport("paid", Messages.getString("ActivityReport.9"), "/templates/LedgerReport.jasper", paidReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        
        List<String> expenseAccounts = ReportUtil.getLedgerAccountRefs(this.getBomService());
        expenseAccounts.removeAll(ReportUtil.getLedgerAccountRefs(this.getBomService(), BOMService.LCAT_INCOME));
        expenseAccounts.removeAll(ReportUtil.getLedgerAccountRefs(this.getBomService(), BOMService.LCAT_VAT));
        LedgerReport expensesReport = new LedgerReport(expenseAccounts);
        expensesReport.setEntryType(BOMService.ET_DRONLY);
        expensesReport.setLedgerType(BOMService.VD_LEDGER);
        this.loadSubReport("expense", Messages.getString("ActivityReport.12"), "/templates/LedgerSectionReport.jasper", expensesReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.runSubReports();
    }
}
