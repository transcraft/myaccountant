/**
 * Created on 05-Jul-2005
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
package transcraft.myaccountant.meta;

import java.text.ParseException;
import java.util.Date;

import org.eclipse.swt.SWT;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.myaccountant.utils.DateUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * descriptor for fields in the LedgerEntryViewer embedded Table object.
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryMeta extends BaseMetaProvider<Entry> {
    
    public static final String ID_PROP = "entryId"; //$NON-NLS-1$
    public static final String PD_PROP = "postingDate"; //$NON-NLS-1$
    public static final String VD_PROP = "valueDate"; //$NON-NLS-1$
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String ACCT_PROP = "toAccount"; //$NON-NLS-1$
    public static final String DR_PROP = "debit"; //$NON-NLS-1$
    public static final String CR_PROP = "credit"; //$NON-NLS-1$
    public static final String BAL_PROP = "balance"; //$NON-NLS-1$
    public static final String REC_PROP = "reconciled"; //$NON-NLS-1$
    public static final String ALLOC_PROP = "allocationRule"; //$NON-NLS-1$
    
    public LedgerEntryMeta() {
	    this.columns = new MetaColumn [] {
	      new MetaColumn<Date>(PD_PROP, Messages.getString("LedgerEntryMeta.11"), Date.class, 120), //$NON-NLS-1$
	      new MetaColumn<Date>(VD_PROP, Messages.getString("LedgerEntryMeta.12"), Date.class, 120), //$NON-NLS-1$
	      new MetaColumn<String>(REF_PROP, Messages.getString("LedgerEntryMeta.13"), String.class, 100, Lists.newArrayList()), //$NON-NLS-1$
	      new MetaColumn<String>(DESC_PROP, Messages.getString("LedgerEntryMeta.14"), String.class, 200, Lists.newArrayList()), //$NON-NLS-1$
	      new MetaColumn<String>(ACCT_PROP, Messages.getString("LedgerEntryMeta.15"), String.class, 100, Lists.newArrayList()), //$NON-NLS-1$
	      new MetaColumn<Double>(DR_PROP, Messages.getString("LedgerEntryMeta.16"), Double.class, 100, SWT.RIGHT), //$NON-NLS-1$
	      new MetaColumn<Double>(CR_PROP, Messages.getString("LedgerEntryMeta.17"), Double.class, 100, SWT.RIGHT), //$NON-NLS-1$
	      new MetaColumn<Double>(BAL_PROP, Messages.getString("LedgerEntryMeta.18"), Double.class, 100, SWT.RIGHT, false), //$NON-NLS-1$
	      //new MetaColumn(REC_PROP, "Reconciled", Boolean.class, 22, SWT.LEFT),
	      new MetaColumn<AllocationRule>(ALLOC_PROP, Messages.getString("LedgerEntryMeta.19"), AllocationRule.class, 20, SWT.LEFT), //$NON-NLS-1$
	      // this must be included so we can search by ID
	      new MetaColumn<Integer>(ID_PROP, "ID", Integer.class, false, false) //$NON-NLS-1$
	   };
	    this.getColumn(ALLOC_PROP).setImage("allocate"); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return ID_PROP;
    }
    
    @Override
    public Object getValue(Entry entry, String columnName, String accountReference) {
        if (ID_PROP.equals(columnName)) {
            return entry.getEntryId();
        } else if (PD_PROP.equals(columnName)) {
            return DateUtil.format(entry.getPostingDate());
        } else if (VD_PROP.equals(columnName)) {
            return DateUtil.format(entry.getValueDate());
        } else if (REF_PROP.equals(columnName)) {
            return entry.getReference() != null ? entry.getReference() : ""; //$NON-NLS-1$
        } else if (DESC_PROP.equals(columnName)) {
            return entry.getDescription() != null ? entry.getDescription() : ""; //$NON-NLS-1$
        } else if (ACCT_PROP.equals(columnName)) {
            return entry.getToAccount() != null ? entry.getToAccount() : ""; //$NON-NLS-1$
        } 
        if (entry instanceof RunningEntry) {
        	RunningEntry re = (RunningEntry)entry;
            if (DR_PROP.equals(columnName)) {
                return re.getDebitString();
            } else if (CR_PROP.equals(columnName)) {
                return re.getCreditString();
            } else if (BAL_PROP.equals(columnName)) {
                return re.getBalanceString();
            }
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(Entry entry, String accountReference, String columnName, Object value) throws ParseException {
        if (PD_PROP.equals(columnName)) {
            entry.setPostingDate(DateUtil.getCalendar(value).getTime());
        } else if (VD_PROP.equals(columnName)) {
            entry.setValueDate(DateUtil.getCalendar(value).getTime());
        } else if (REF_PROP.equals(columnName)) {
            entry.setReference(value.toString());
        } else if (DESC_PROP.equals(columnName)) {
            entry.setDescription(value.toString());
        } else if (ACCT_PROP.equals(columnName)) {
            entry.setToAccount(value.toString());
        } else if (DR_PROP.equals(columnName)) {
            if (value != null && value.toString().length() > 0) {
	            //value = value.toString().replaceAll(",", "");
	            double amount = Formatters.parse(value.toString()).doubleValue() * -1;
	            if (entry.getToAccount() != null && entry.getToAccount().equals(accountReference)) {
	                amount *= -1;
	            }
	            entry.setAmount(amount);
            }
        } else if (CR_PROP.equals(columnName)) {
            if (value != null && value.toString().length() > 0) {
	            //value = value.toString().replaceAll(",", "");
	            double amount = Formatters.parse(value.toString()).doubleValue();
	            if (entry.getToAccount() != null && entry.getToAccount().equals(accountReference)) {
	                amount *= -1;
	            }
	            entry.setAmount(amount);
            }
        }
    }
}
