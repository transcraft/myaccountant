/**
 * Created on 07-Sep-2005
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
import java.util.Map;

import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.pref.TaxBandHelper;

/**
 * @author david.tran@transcraft.co.uk
 */
public class VATSummaryReport extends GenericReport<Map<String, Object>> {

    /**
     * 
     */
    public VATSummaryReport() {
        super(Messages.getString("VATSummaryReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/VATSummary.jasper"); //$NON-NLS-1$
        this.setDateMeta(ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISQRTR));
    }
    
    public void run() {
        double salesTotal = 0;
        double purchaseTotal = 0;
        double salesVAT = 0;
        double purchaseVAT = 0;
        List<Account> accounts = this.bomService.getRefData(Account.class);
        TaxBandHelper taxBandHelper = new TaxBandHelper();
        for (Account account : accounts) {
            if (! account.isTaxed() ||
                    (account.getCategory() != null && ! account.getCategory().equals(BOMService.ECAT_BUSINESS))) {
                continue;
            }
            List<Entry> entriesList = bomService.getLedgerEntries(BOMService.VD_LEDGER, account,
                this.getStartDate(), this.getEndDate());
            for (Entry entry : entriesList) {
	            RunningEntry re = (RunningEntry)entry;
	            double tax = ReportUtil.getVATAmount(this.bomService, re);
                if (taxBandHelper.getTaxBandForCode(re.getReference()) == null && tax == 0.0) {
                    continue;
                }
	            if (re.getAmount() == tax) {
	                continue;
	            }
	            double amount = re.getAmount() - tax;
                if (amount > 0) {
                    salesTotal += amount;
                    salesVAT += tax;
                } else {
                    purchaseTotal += amount;
                    purchaseVAT += tax;
                }
            }
        }
        
        Map<String, Object> row = Maps.newHashMap();
        row.put("SalesTotal", Double.valueOf(Math.abs(salesTotal))); //$NON-NLS-1$
        row.put("PurchaseTotal", Double.valueOf(Math.abs(purchaseTotal))); //$NON-NLS-1$
        row.put("SalesVATdue", Double.valueOf(Math.abs(salesVAT))); //$NON-NLS-1$
        row.put("PurchaseVATreclaim", Double.valueOf(Math.abs(purchaseVAT))); //$NON-NLS-1$
        row.put("NetVAT", Double.valueOf(salesVAT + purchaseVAT)); //$NON-NLS-1$
        this.reportRows.clear();
        this.reportRows.add(row);
        this.rowIterator = this.reportRows.iterator();
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        return (this.reportRows.get(0)).get(field.getName());
    }
}
