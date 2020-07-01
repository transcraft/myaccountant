/**
 * Created on 19-Sep-2005
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
 * VAT detailed report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class VATDetailReport extends GenericReport<Entry> {

    boolean firstTime = true;

    /**
     * @param name
     */
    public VATDetailReport() {
        super(Messages.getString("VATDetailReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/VATDetail.jasper"); //$NON-NLS-1$
        this.setDateMeta(ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISQRTR));
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
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.report.GenericReport#run()
     */
    public void run() {
        this.firstTime = true;
        this.subReports.clear();
        
        List<String> taxedAccounts = ReportUtil.getTaxedAccountRefs(this.getBomService());
        LedgerReport salesReport = new LedgerReport(taxedAccounts);
        salesReport.setEntryType(BOMService.ET_CRONLY);
        salesReport.setLedgerType(BOMService.VD_LEDGER);
        salesReport.setSplitsOnly(true);
        salesReport.setTaxRelatedOnly(true);
        this.loadSubReport("sales", Messages.getString("VATDetailReport.3"), "/templates/LedgerReport.jasper", salesReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        LedgerReport purchaseReport = new LedgerReport(taxedAccounts);
        purchaseReport.setEntryType(BOMService.ET_DRONLY);
        purchaseReport.setLedgerType(BOMService.VD_LEDGER);
        purchaseReport.setSplitsOnly(true);
        purchaseReport.setTaxRelatedOnly(true);
        this.loadSubReport("purchase", Messages.getString("VATDetailReport.6"), "/templates/LedgerReport.jasper", purchaseReport); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        this.runSubReports();
    }

}
