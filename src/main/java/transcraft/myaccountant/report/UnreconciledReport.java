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
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;

/**
 * reconciliation report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class UnreconciledReport extends GenericReport<Entry> {

    transient boolean firstTime = true;
    
    /**
     * 
     */
    public UnreconciledReport() {
        super(Messages.getString("UnreconciledReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/InputOutputReport.jasper"); //$NON-NLS-1$
        this.setAutoAccountSelect(false);
        this.setDateMeta(ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_ALL));
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
        LedgerEntryFilter filter = new LedgerEntryFilter() {
            public boolean accept(Entry entry) {
                return ! entry.isReconciled();
            }
        };
        
        LedgerReport receivedReport = new LedgerReport(this.getAccounts());
        receivedReport.setEntryType(BOMService.ET_CRONLY);
        receivedReport.setLedgerType(BOMService.VD_LEDGER);
        receivedReport.setFilter(filter);
        this.loadSubReport("received", Messages.getString("UnreconciledReport.3"), "/templates/LedgerReport.jasper", receivedReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        LedgerReport paidReport = new LedgerReport(this.getAccounts());
        paidReport.setEntryType(BOMService.ET_DRONLY);
        paidReport.setLedgerType(BOMService.VD_LEDGER);
        paidReport.setFilter(filter);
        this.loadSubReport("paid", Messages.getString("UnreconciledReport.6"), "/templates/LedgerReport.jasper", paidReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.runSubReports();
    }
}
