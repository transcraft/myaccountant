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


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.report.MonthlyCashflowReport.ReportRow;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * List of Invoices report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class MonthlyCashflowReport extends BaseGenericReport<ReportRow> {

    protected transient List<ReportRow> entries = Lists.newArrayList();
    protected transient ReportDateMeta taxYear;
    
    /**
     * for subclasses only
     * @param title
     */
    protected MonthlyCashflowReport() {
        super(Messages.getString("MonthlyCashflowReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/MonthlyCashflowReport.jasper"); //$NON-NLS-1$
        ReportDateUtil rptUtil = new ReportDateUtil();
        this.taxYear = ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISTAXYEAR);
        rptUtil.calculate(this.taxYear);
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        if (this.currentRow == null) {
            return null;
        }
        ReportRow row = (ReportRow)this.currentRow;
        if (field.getName().equals("monthName")) { //$NON-NLS-1$
            return row.monthName;
        } else if (field.getName().equals("netAmount")) { //$NON-NLS-1$
        	return Double.valueOf(row.grossAmount - row.taxAmount);
        } else if (field.getName().equals("taxAmount")) { //$NON-NLS-1$
        	return Double.valueOf(row.taxAmount);
        } else if (field.getName().equals("grossAmount")) { //$NON-NLS-1$
        	return Double.valueOf(row.grossAmount);
        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
        	return row.group;
        }
        return null;
    }

    @Override
    public Map<String, Object>	getParameters() throws JRException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyAddress", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_ADDRESS)); //$NON-NLS-1$
        params.put("companyEmail", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_EMAIL)); //$NON-NLS-1$
        params.put("companyFaxNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_FAXNO)); //$NON-NLS-1$
        params.put("companyName", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNAME)); //$NON-NLS-1$
        params.put("companyPhoneNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_PHONENO)); //$NON-NLS-1$
        params.put("companyRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNO)); //$NON-NLS-1$
        params.put("companyURL", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_URL)); //$NON-NLS-1$
        params.put("reportTitle", this.getName()); //$NON-NLS-1$
        params.put("vatRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_VATCODE)); //$NON-NLS-1$
        return params;
    }

    
    public void run() {
        this.reportRows.clear();
        this.runReport();
        this.reportRows.addAll(this.entries);
        this.rowIterator = this.reportRows.iterator();
    }
    
    protected void	runReport() {
        this.entries = Lists.newArrayList();
        List<Account> accounts = this.bomService.getRefData(Account.class);
        for (Account account : accounts) {
            if (! account.isExternal() || ! account.isTaxed()) {
                continue;
            }
            List<String> sameCatAccounts = ReportUtil.getAccountRefs(this.getBomService(), account.getCategory(), account.getClass());
            List<Entry> list = this.bomService.getLedgerEntries(BOMService.VD_LEDGER, account);
            for (Entry entry : list) {
                RunningEntry re = (RunningEntry)entry;
                if (re.getValueDate().before(taxYear.getStartDate()) || re.getValueDate().after(taxYear.getEndDate())) {
                    // ignore entries which do not fall into our tax year
                    continue;
                }
                if (re.getToAccount() != null && sameCatAccounts.contains(re.getToAccount())) {
                    // ignore internal transfer entries
                    continue;
                }
                Calendar vd = Calendar.getInstance();
                vd.setTime(re.getValueDate());
                int month = vd.get(Calendar.MONTH);
                String group = (re.getAmount() > 0) ? "input" : "output"; //$NON-NLS-1$ //$NON-NLS-2$
                
                ReportRow row = new ReportRow(group, month);
                if (! this.entries.contains(row)) {
                    this.entries.add(row);
                } else {
                    row = (ReportRow)this.entries.get(this.entries.indexOf(row));
                }
                row.grossAmount += re.getOriginalAmount();
                row.taxAmount += ReportUtil.getVATAmount(this.bomService, re);
            }
        }
        this.entries = this.entries.stream().sorted().collect(Collectors.toList());
    }
    public class ReportRow implements Comparable<ReportRow> {
        public String group;
        public int month;
        public double taxAmount;
        public double grossAmount;
        public String monthName;
        
        public ReportRow(String group, int month) {
            this.group = group;
            this.month = month;
            SimpleDateFormat fmt = new SimpleDateFormat("MMMMMM"); //$NON-NLS-1$
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.MONTH, month);
            this.monthName = fmt.format(cal.getTime());
        }
        
        @Override
        public boolean equals(Object obj) {
            try {
                ReportRow row = (ReportRow)obj;
                return this.group.equals(row.group) && this.month == row.month;
            } catch (Exception e) {}
            return false;
        }
        
        @Override
        public int hashCode() {
            return (this.group + this.month).hashCode();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(ReportRow row) {
            try {
                int diff = this.group.compareTo(row.group);
                if (diff == 0) {
                    diff = this.month - row.month;
                    return diff;
                } else {
                    return diff;
                }
            } catch (Exception e) {}
            return -1;
        }
    }
}
