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
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.utils.DateUtil;

/**
 * List of Invoices report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class PerformanceReport extends BaseGenericReport<PerformanceReport.ReportRow> {

    protected transient List<MonthlyReportRow> monthRows = Lists.newArrayList();
    protected transient List<LedgerReportRow> ledgerRows = Lists.newArrayList();
    protected transient List<MonthlyRecRow> recRows = Lists.newArrayList();
    
    protected transient ReportDateMeta companyYear;
    
    /**
     * for subclasses only
     * @param title
     */
    protected PerformanceReport() {
        super(Messages.getString("PerformanceReport.0")); //$NON-NLS-1$
        this.setReportTemplate("/templates/PerformanceReport.jasper"); //$NON-NLS-1$
        ReportDateUtil rptUtil = new ReportDateUtil();
        this.companyYear = ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISCOMPYEAR);
        rptUtil.calculate(this.companyYear);
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        if (this.currentRow == null) {
            return null;
        }
        if (this.currentRow instanceof MonthlyReportRow) {
	        MonthlyReportRow row = (MonthlyReportRow)this.currentRow;
	        if (field.getName().equals("monthName")) { //$NON-NLS-1$
	            return row.monthName;
	        } else if (field.getName().equals("netAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.receivedAmount + row.paidAmount);
	        } else if (field.getName().equals("paidAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.paidAmount);
	        } else if (field.getName().equals("receivedAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.receivedAmount);
	        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
	        	return "monthGroup"; //$NON-NLS-1$
	        }
        } else if (this.currentRow instanceof LedgerReportRow) {
	        LedgerReportRow row = (LedgerReportRow)this.currentRow;
	        if (field.getName().equals("ledgerName")) { //$NON-NLS-1$
	            return row.ledgerName;
	        } else if (field.getName().equals("ledgerCreditAmount")) { //$NON-NLS-1$
	        	return row.ledgerAmount > 0 ? Double.valueOf(row.ledgerAmount) : null;
	        } else if (field.getName().equals("ledgerDebitAmount")) { //$NON-NLS-1$
	        	return row.ledgerAmount <= 0 ? Double.valueOf(Math.abs(row.ledgerAmount)) : null;
	        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
	        	return "ledgerGroup"; //$NON-NLS-1$
	        }
        } else if (this.currentRow instanceof MonthlyRecRow) {
            MonthlyRecRow row = (MonthlyRecRow)this.currentRow;
	        if (field.getName().equals("recMonth")) { //$NON-NLS-1$
	            return row.monthName;
	        } else if (field.getName().equals("recAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.amount);
	        } else if (field.getName().equals("recTaxAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.taxAmount);
	        } else if (field.getName().equals("recNetAmount")) { //$NON-NLS-1$
	        	return Double.valueOf(row.netAmount);
	        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
	        	return "recGroup"; //$NON-NLS-1$
	        }
        }
        return null;
    }

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
        this.reportRows.addAll(this.monthRows);
        this.reportRows.addAll(this.ledgerRows);
        this.reportRows.addAll(this.recRows);
        this.rowIterator = this.reportRows.iterator();
    }
    
    protected void	runReport() {
        // 1. do the Entry accounts
        this.monthRows = Lists.newArrayList();
        List<Account> accounts = this.bomService.getRefData(Account.class);
        List<Entry> entryList = Lists.newArrayList();
        for (Account account :  accounts) {
            if (! account.isExternal() || ! account.getCategory().equals(BOMService.ECAT_BUSINESS)) {
                continue;
            }
            List<String> sameCatAccounts = ReportUtil.getAccountRefs(this.getBomService(), account.getCategory(), account.getClass());
            List<Entry> list = this.bomService.getLedgerEntries(BOMService.VD_LEDGER, account,
                    this.companyYear.getStartDate(), this.companyYear.getEndDate());
            for (Entry entry : list) {
                RunningEntry re = (RunningEntry)entry;
                if (re.getToAccount() != null && sameCatAccounts.contains(re.getToAccount())) {
                    // ignore internal transfer entries
                    continue;
                }
                if (ReportUtil.isVATPayment(this.bomService, re)) {
                    continue;
                }
                
                // save for later recs
                entryList.add(re);
                
                Calendar vd = Calendar.getInstance();
                vd.setTime(re.getValueDate());
                int month = vd.get(Calendar.MONTH);

                MonthlyReportRow row = new MonthlyReportRow(month);
                if (! this.monthRows.contains(row)) {
                    this.monthRows.add(row);
                } else {
                    row = (MonthlyReportRow)this.monthRows.get(this.monthRows.indexOf(row));
                }
                double amount = re.getOriginalAmount() - ReportUtil.getVATAmount(this.bomService, re);
                if (amount > 0) {
                    row.receivedAmount += amount;
                } else {
                    row.paidAmount += amount;
                }
            }
        }
        this.monthRows = this.monthRows.stream().sorted().collect(Collectors.toList());
        
        // 2. do the Ledger accounts
        this.ledgerRows = Lists.newArrayList();
        this.recRows.clear();
        List<LedgerAccount> ledgerAccounts = this.bomService.getRefData(LedgerAccount.class);
        for (LedgerAccount account : ledgerAccounts) {
            if (account.getCategory() != null && account.getCategory().equals(BOMService.LCAT_VAT)) {
                continue;
            }
            List<Entry> list = this.bomService.getLedgerEntries(BOMService.VD_LEDGER, account,
            	this.companyYear.getStartDate(), this.companyYear.getEndDate());
            for (Entry entry : list) {
                RunningEntry re = (RunningEntry)entry;
                // remove this entry from the list created above. Anything left over is un-categorised
                LedgerReportRow row = new LedgerReportRow(account.getReference());
                if (! this.ledgerRows.contains(row)) {
                    this.ledgerRows.add(row);
                } else {
                    row = (LedgerReportRow)ledgerRows.get(this.ledgerRows.indexOf(row));
                }
                row.ledgerAmount += re.getAmount();
                
                // save any entry which is not part of the entry accounts for recs later
                if (! entryList.contains(re)) {
                    Calendar vd = Calendar.getInstance();
                    vd.setTime(re.getValueDate());
                    int month = vd.get(Calendar.MONTH);

	                MonthlyRecRow recRow = new MonthlyRecRow(month);
	                if (! this.recRows.contains(recRow)) {
	                    this.recRows.add(recRow);
	                } else {
	                    recRow = (MonthlyRecRow)this.recRows.get(this.recRows.indexOf(recRow));
	                }
	                recRow.amount += Math.abs(re.getOriginalAmount());
	                recRow.taxAmount += Math.abs(re.getOriginalAmount()) - Math.abs(re.getAmount());
	                recRow.netAmount += Math.abs(re.getAmount());
                }
            }
        }
        this.ledgerRows = this.ledgerRows.stream().sorted().collect(Collectors.toList());
        this.recRows = this.recRows.stream().sorted().collect(Collectors.toList());
    }
    class ReportRow {
    	
    }
    public class MonthlyReportRow extends ReportRow implements Comparable<MonthlyReportRow> {
        public int month;
        public double receivedAmount;
        public double paidAmount;
        public String monthName;
        
        public MonthlyReportRow(int month) {
            this.month = month;
            /*
             * we could use an array of month names here, but it will not work
             * in another Locale, so use the DateFormat object to get the
             * month name translated into the right language
             */
            SimpleDateFormat fmt = new SimpleDateFormat("MMMMMM"); //$NON-NLS-1$
            Calendar cal = DateUtil.getTodayStart();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, month);
            this.monthName = fmt.format(cal.getTime());
        }
        
        @Override
        public boolean equals(Object obj) {
            try {
                MonthlyReportRow row = (MonthlyReportRow)obj;
                return this.month == row.month;
            } catch (Exception e) {}
            return false;
        }
        
        @Override
        public int hashCode() {
            return super.hashCode() + this.month;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(MonthlyReportRow row) {
            try {
                return this.month - row.month;
            } catch (Exception e) {}
            return -1;
        }
    }

    public class LedgerReportRow extends ReportRow implements Comparable<LedgerReportRow> {
        public double ledgerAmount;
        public String ledgerName;
        
        public LedgerReportRow(String name) {
            this.ledgerName = name;
        }
        
        @Override
        public boolean equals(Object obj) {
            try {
                LedgerReportRow row = (LedgerReportRow)obj;
                return this.ledgerName.equals(row.ledgerName);
            } catch (Exception e) {}
            return false;
        }

        @Override
        public int hashCode() {
            return this.ledgerName.hashCode();
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(LedgerReportRow row) {
            try {
                return this.ledgerName.compareTo(row.ledgerName);
            } catch (Exception e) {}
            return -1;
        }
    }

    public class MonthlyRecRow extends ReportRow implements Comparable<MonthlyRecRow> {
        public int month;
        public double amount;
        public double netAmount;
        public double taxAmount;
        public String monthName;
        
        public MonthlyRecRow(int month) {
            this.month = month;
            /*
             * we could use an array of month names here, but it will not work
             * in another Locale, so use the DateFormat object to get the
             * month name translated into the right language
             */
            SimpleDateFormat fmt = new SimpleDateFormat("MMMMMM"); //$NON-NLS-1$
            Calendar cal = DateUtil.getTodayStart();
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.MONTH, month);
            this.monthName = fmt.format(cal.getTime());
        }
        
        @Override
        public boolean equals(Object obj) {
            try {
                MonthlyRecRow row = (MonthlyRecRow)obj;
                return this.month == row.month;
            } catch (Exception e) {}
            return false;
        }

        @Override
        public int hashCode() {
            return super.hashCode() + this.month;
        }
        
        /* (non-Javadoc)
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        @Override
        public int compareTo(MonthlyRecRow row) {
            try {
                return this.month - row.month;
            } catch (Exception e) {}
            return -1;
        }
    }

}
