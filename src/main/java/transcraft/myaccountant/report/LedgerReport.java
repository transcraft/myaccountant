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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.pref.TaxBandHelper;

/**
 * Ledger sub report, designed for embedding into a master report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerReport extends GenericReport<Entry> {

    transient double grossTotal = 0;
    transient double vatTotal = 0;
    transient double netTotal = 0;

    protected int	entryType = BOMService.ET_DRCR;
    protected boolean calculateVAT = true;
    /**
     * only include entries with Allocations
     */
    protected boolean splitsOnly = false;
    /**
     * external means for the caller to control if the entry will be included
     * in the report
     */
    transient protected LedgerEntryFilter filter;
    /**
     * controls whether intercategory transfers are excluded. This is useful
     * if you want to eliminate entries to do with transfers from business
     * accounts to reserve accounts and back
     */
    protected boolean excludeSameCatTxfr = false;

    /**
     * if the externalOnly flag is set, entries belonging to accounts which do
     * not have the isExternal() method returning true will be ignored
     */
    protected boolean externalOnly = false;

    /**
     * if usePostedAmount is set to true, the amount posted to the ledger is used,
     * otherwise the total original amount is used instead
     */
    protected boolean usePostedAmount = false;
    
    /**
     * if taxtRelatedOnly is set to true, only accounts with tracked taxes and entries
     * with non zero tax are included
     */
    protected boolean taxRelatedOnly = false;
    
    /**
     * this parameter supports dynamic reports being run from the viewers, where only
     * certain entry ids are selected
     */
    transient protected Integer [] entryIds;
    
    /**
     * @param name
     */
    public LedgerReport(String accountReference) {
        super(accountReference);
        this.accounts.add(accountReference);
    }

    public LedgerReport(String [] accountReferences) {
        this(Arrays.asList(accountReferences));
    }

    public LedgerReport(List<String> accounts) {
        super(accounts.toString());
        this.setAccounts(accounts);
    }
    
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        if (field.getName().equals("reportTitle")) { //$NON-NLS-1$
            return this.getDescription();
        }
        if (this.currentRow == null) {
            return null;
        }
        RunningEntry re = (RunningEntry)this.currentRow;
        if (field.getName().equals("postingDate")) { //$NON-NLS-1$
            return this.getLedgerType().equals(BOMService.VD_LEDGER) ? re.getValueDate() : re.getPostingDate();
        } else if (field.getName().equals("reference")) { //$NON-NLS-1$
        	return re.getReference();
        } else if (field.getName().equals("description")) { //$NON-NLS-1$
            return re.getDescription();
        } else if (field.getName().equals("grossAmount")) { //$NON-NLS-1$
            return Double.valueOf(this.getAmountForEntryType(this.getAmount(re)));
        } else if (field.getName().equals("vatAmount")) { //$NON-NLS-1$
            return Double.valueOf(this.getAmountForEntryType(this.getVATAmount(re)));
        } else if (field.getName().equals("netAmount")) { //$NON-NLS-1$
            return Double.valueOf(this.getAmountForEntryType(this.getAmount(re)) -
                	this.getAmountForEntryType(this.getVATAmount(re)));
        } else if (field.getName().equals("fromAccount")) { //$NON-NLS-1$
        	return re.getFromAccount();
        } else if (field.getName().equals("toAccount")) { //$NON-NLS-1$
        	return re.getToAccount();
        } else if (field.getName().equals("groupName")) { //$NON-NLS-1$
        	return re.getFromAccount();
        }
        return null;
    }

    protected double	getAmountForEntryType(double amount) {
        if (this.getEntryType() == BOMService.ET_DRCR) {
            return amount;
        }
        return Math.abs(amount);
    }
    public void run() {
        if (this.bomService == null) {
            return;
        }
        grossTotal = 0;
        netTotal = 0;
        vatTotal = 0;
        for (String accountReference : this.getAccounts()) {
            this.run(accountReference);
        }
        this.rowIterator = this.reportRows.iterator();
    }

    protected void	run(String accountReference) {
        Account account = (Account)bomService.getGeneric(Account.class, "reference", accountReference); //$NON-NLS-1$
        if (account == null) {
            account = (Account)bomService.getGeneric(LedgerAccount.class, "reference", accountReference); //$NON-NLS-1$
        }
        if (this.externalOnly && ! account.isExternal()) {
            return;
        }
        if (this.taxRelatedOnly && ! account.isTaxed()) {
            return;
        }
        List<String> sameCatAccounts = ReportUtil.getAccountRefs(this.getBomService(), account.getCategory(), account.getClass());
        TaxBandHelper taxBandHelper = new TaxBandHelper();
        if (account != null) {
	        List<Entry> entries = bomService.getLedgerEntries(this.getLedgerType(), account,
	    	                this.getStartDate(), this.getEndDate(), this.entryType);
	        List<Integer> idList = this.entryIds != null ? Arrays.asList(this.entryIds) : Lists.newArrayList();
	        entries = entries
	        	.stream()
	        	.filter(e -> {
		            RunningEntry re = (RunningEntry)e;
		            if (this.reportRows.contains(re)) {
		            	return false;
		            } else if (this.entryIds != null && ! idList.contains(re.getEntryId())) {
		                return false;
		            } else if (this.filter != null && ! this.filter.accept(re)) {
		                return false;
		            } else if (this.splitsOnly && re.getAllocations(false).length == 0) {
		                return false;
		            } else if (this.excludeSameCatTxfr && sameCatAccounts.contains(re.getToAccount())) {
		                return false;
		            }
		            double vat = this.getVATAmount(re);
		            if (this.taxRelatedOnly) {
		                if (taxBandHelper.getTaxBandForCode(re.getReference()) == null && vat == 0.0) {
		                    return false;
		                }
		                if (ReportUtil.isVATPayment(this.bomService, re)) {
		                    return false;
		                }
		            }
		            double amount = this.getAmount(re);
		            vatTotal += vat;
		            grossTotal += amount;
		            netTotal += Math.abs(amount) - Math.abs(vat);
		            return true;	        	
	        	})
	        	.collect(Collectors.toList());
	        
	        this.reportRows.addAll(entries);
        }
    }
    
    protected double getAmount(RunningEntry re) {
        double amount = this.usePostedAmount ? re.getAmount() : re.getOriginalAmount();
        return amount;
    }
    
    /**
     * @return Returns the taxRelatedOnly.
     */
    public boolean isTaxRelatedOnly() {
        return taxRelatedOnly;
    }
    /**
     * @param taxRelatedOnly The taxRelatedOnly to set.
     */
    public void setTaxRelatedOnly(boolean taxRelatedOnly) {
        this.taxRelatedOnly = taxRelatedOnly;
    }
    /**
     * @return usePostedAmount
     * this flag controls whether the original amount posted into the Ledger i.e.
     * the split amount should be used. If set to false, the gross amount before the split
     * will be used
     */
    public boolean isUsePostedAmount() {
        return usePostedAmount;
    }
    /**
     * @param usePostedAmount The usePostedAmount to set.
     * this flag controls whether the original amount posted into the Ledger i.e.
     * the split amount should be used. If set to false, the gross amount before the split
     * will be used
     */
    public void setUsePostedAmount(boolean usePostedAmount) {
        this.usePostedAmount = usePostedAmount;
    }
    /**
     * @return Returns the excludeSameCatTxfr.
     * if this flag is true, all the inter account transfers will be excluded from the
     * list of returned entries
     */
    public boolean isExcludeSameCatTxfr() {
        return excludeSameCatTxfr;
    }
    /**
     * @param excludeSameCatTxfr The excludeSameCatTxfr to set.
     * if this flag is true, all the inter account transfers will be excluded from the
     * list of returned entries
     */
    public void setExcludeSameCatTxfr(boolean excludeSameCatTxfr) {
        this.excludeSameCatTxfr = excludeSameCatTxfr;
    }
    
    /**
     * @return Returns the externalOnly.
     * If this flag is set to true, only accounts which return true for
     * Account.isExternal() will be included in the query.
     * @see Account.isExternal()
     */
    public boolean isExternalOnly() {
        return externalOnly;
    }
    /**
     * @param externalOnly The externalOnly to set.
     * If this flag is set to true, only accounts which return true for
     * Account.isExternal() will be included in the query.
     * @see Account.isExternal()
     */
    public void setExternalOnly(boolean externalOnly) {
        this.externalOnly = externalOnly;
    }
    /**
     * @return Returns the filter.
     */
    public LedgerEntryFilter getFilter() {
        return filter;
    }
    /**
     * @param filter The filter to set.
     */
    public void setFilter(LedgerEntryFilter filter) {
        this.filter = filter;
    }
    
    /**
     * this supports a selection of entries being selected from the viewer
     * 
     * @return Returns the entryIds.
     */
    public Integer[] getEntryIds() {
        return entryIds;
    }
    /**
     * this supports a selection of entries being selected from the viewer
     * @param entryIds The entryIds to set.
     */
    public void setEntryIds(Integer[] entryIds) {
        this.entryIds = entryIds;
    }
    /**
     * @return Returns the calculateVAT.
     * If this flag is true, the net amount and VAT will also be calculated
     */
    public boolean isCalculateVAT() {
        return calculateVAT;
    }
    /**
     * @param calculateVAT The calculateVAT to set.
     * If this flag is true, the net amount and VAT will also be calculated
     */
    public void setCalculateVAT(boolean calculateVAT) {
        this.calculateVAT = calculateVAT;
    }
    /**
     * @return Returns the entryType.
     * Specifies whether only credits, or only debits or both, will be included
     * in the query
     * @see BOMService.ET_* enums
     */
    public int getEntryType() {
        return entryType;
    }
    /**
     * @param entryType The entryType to set.
     * Specifies whether only credits, or only debits or both, will be included
     * in the query
     * @see BOMService.ET_* enums
     */
    public void setEntryType(int entryType) {
        this.entryType = entryType;
    }

    protected double	getVATAmount(RunningEntry re) {
        return this.isCalculateVAT() ? ReportUtil.getVATAmount(this.getBomService(), re) : 0;
    }
    
    /**
     * @return Returns the splitsOnly.
     * if this flag is true, only entries which are posted into ledgers i.e. splits
     * will be included in the query
     */
    public boolean isSplitsOnly() {
        return splitsOnly;
    }
    /**
     * @param splitsOnly The splitsOnly to set.
     * if this flag is true, only entries which are posted into ledgers i.e. splits
     * will be included in the query
     */
    public void setSplitsOnly(boolean splitsOnly) {
        this.splitsOnly = splitsOnly;
    }
}
