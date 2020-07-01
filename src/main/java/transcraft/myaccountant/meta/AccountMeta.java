/**
 * Created on 14-Jul-2005
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
import java.util.Arrays;
import java.util.Date;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Currency;
import transcraft.myaccountant.ui.pref.TaxBandHelper;
import transcraft.myaccountant.utils.DateUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AccountMeta extends BaseMetaProvider<Account> {
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String OB_PROP = "openingBalance"; //$NON-NLS-1$
    public static final String OD_PROP = "openingDate"; //$NON-NLS-1$
    public static final String TAX_PROP = "taxed"; //$NON-NLS-1$
    public static final String DEFTAX_PROP = "defaultTaxCode"; //$NON-NLS-1$
    public static final String CUR_PROP = "defaultCurrency"; //$NON-NLS-1$
    public static final String CAT_PROP = "category"; //$NON-NLS-1$
    public static final String ACNO_PROP = "accountNumber"; //$NON-NLS-1$
    public static final String SCODE_PROP = "sortCode"; //$NON-NLS-1$
    public static final String EXT_PROP = "external"; //$NON-NLS-1$

    public AccountMeta() {
        this.columns = new MetaColumn [] {
	        new MetaColumn<String>(REF_PROP, Messages.getString("AccountMeta.11"), String.class, false), //$NON-NLS-1$
	        new MetaColumn<String>(DESC_PROP, Messages.getString("AccountMeta.12"), String.class), //$NON-NLS-1$
	        new MetaColumn<Double>(OB_PROP, Messages.getString("AccountMeta.13"), Double.class, 200), //$NON-NLS-1$
	        new MetaColumn<Date>(OD_PROP, Messages.getString("AccountMeta.14"), Date.class), //$NON-NLS-1$
	        new MetaColumn<Boolean>(TAX_PROP, Messages.getString("AccountMeta.15"), Boolean.class), //$NON-NLS-1$
	        new MetaColumn<String>(DEFTAX_PROP, Messages.getString("AccountMeta.16"), String.class, Arrays.asList(new TaxBandHelper().getTaxCodes())), //$NON-NLS-1$
	        new MetaColumn<Boolean>(EXT_PROP, Messages.getString("AccountMeta.17"), Boolean.class), //$NON-NLS-1$
	        new MetaColumn<String>(SCODE_PROP, Messages.getString("AccountMeta.18"), String.class), //$NON-NLS-1$
	        new MetaColumn<String>(ACNO_PROP, Messages.getString("AccountMeta.19"), String.class), //$NON-NLS-1$
	        new MetaColumn<Currency>(CUR_PROP, Messages.getString("AccountMeta.20"), Currency.class, false), //$NON-NLS-1$
	        new MetaColumn<String>(CAT_PROP, Messages.getString("AccountMeta.21"), String.class, Lists.newArrayList()), //$NON-NLS-1$
	   };
    }
    
    @Override    
    public Object getValue(Account account, String columnName, String accountReference) {
        if (REF_PROP.equals(columnName)) {
            return account.getReference();
        } else if (DESC_PROP.equals(columnName)) {
            return account.getDescription();
        } else if (OB_PROP.equals(columnName)) {
            return Double.valueOf(account.getOpeningBalance());
        } else if (OD_PROP.equals(columnName)) {
            return account.getOpeningDate();
        } else if (TAX_PROP.equals(columnName)) {
            return Boolean.valueOf(account.isTaxed());
        } else if (DEFTAX_PROP.equals(columnName)) {
            return account.getDefaultTaxCode();
        } else if (EXT_PROP.equals(columnName)) {
            return Boolean.valueOf(account.isExternal());
        } else if (SCODE_PROP.equals(columnName)) {
            return account.getSortCode();
        } else if (ACNO_PROP.equals(columnName)) {
            return account.getAccountNumber();
        } else if (CUR_PROP.equals(columnName)) {
            return account.getDefaultCurrency();
        } else if (CAT_PROP.equals(columnName)) {
            return account.getCategory();
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(Account account, String accountReference, String columnName, Object value) throws ParseException {
        if (DESC_PROP.equals(columnName)) {
            account.setDescription(value.toString());
        } else if (OB_PROP.equals(columnName)) {
            account.setOpeningBalance(Formatters.parse(value.toString()).doubleValue());
        } else if (OD_PROP.equals(columnName)) {
            account.setOpeningDate(DateUtil.parse(value));
        } else if (TAX_PROP.equals(columnName)) {
            account.setTaxed(Boolean.valueOf(value.toString()).booleanValue());
        } else if (DEFTAX_PROP.equals(columnName)) {
            account.setDefaultTaxCode(value.toString());
        } else if (EXT_PROP.equals(columnName)) {
            account.setExternal(Boolean.valueOf(value.toString()).booleanValue());
        } else if (SCODE_PROP.equals(columnName)) {
            account.setSortCode(value.toString());
        } else if (ACNO_PROP.equals(columnName)) {
            account.setAccountNumber(value.toString());
        } else if (CAT_PROP.equals(columnName)) {
            account.setCategory(value.toString());
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return REF_PROP;
    }
}
