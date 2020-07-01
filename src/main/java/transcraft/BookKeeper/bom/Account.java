/**
 * Created on 05-Jun-2005
 *
 * Copyrights (c) Transcraft Trading Limited 2003-2004. All rights reserved.
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
package transcraft.BookKeeper.bom;

import java.util.Date;

import transcraft.myaccountant.utils.DateUtil;

/**
 * @author david.tran@transcraft.co.uk
 */
public class Account implements Comparable<Account> {
    private String reference;
    private String description;
    private double openingBalance;
    private Date openingDate;
    private boolean taxed = false;
    private String defaultTaxCode;
    private Currency defaultCurrency;
    private Date ts;
    private String sortCode;
    private String accountNumber;
    private boolean external = false;
    private String category;
    
    /**
     * @param reference
     */
    public Account(String reference) {
        super();
        this.reference = reference;
        this.openingBalance = 0.0;
        this.ts = new Date();
        this.openingDate = DateUtil.datePart(this.ts);
        this.taxed = false;
   }
    /**
     * @param reference
     * @param description
     * @param openingBalance
     * @param taxed
     * @param defaultTaxCode
     */
    public Account(String reference, String description,
            double openingBalance, boolean taxed, String defaultTaxCode) {
        this(reference);
        this.description = description;
        this.openingBalance = openingBalance;
        this.taxed = taxed;
        this.defaultTaxCode = defaultTaxCode;
    }
    /**
     * @param reference
     * @param description
     * @param openingBalance
     * @param openingDate
     * @param taxed
     * @param defaultTaxCode
     * @param defaultCurrency
     */
    public Account(String reference, String description,
            double openingBalance, Date openingDate, boolean taxed,
            String defaultTaxCode, Currency defaultCurrency) {
        this(reference, description, openingBalance, taxed, defaultTaxCode);
        this.openingDate = openingDate;
        this.defaultCurrency = defaultCurrency;
    }
    /**
     * @return Returns the defaultCurrency.
     */
    public Currency getDefaultCurrency() {
        return defaultCurrency;
    }
    /**
     * @param defaultCurrency The defaultCurrency to set.
     */
    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }
    /**
     * @return Returns the defaultTaxCode.
     */
    public String getDefaultTaxCode() {
        return defaultTaxCode;
    }
    /**
     * @param defaultTaxCode The defaultTaxCode to set.
     */
    public void setDefaultTaxCode(String defaultTaxCode) {
        this.defaultTaxCode = defaultTaxCode;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return this.description != null && this.description.length() > 0 ? this.description : this.reference;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the openingBalance.
     */
    public double getOpeningBalance() {
        return openingBalance;
    }
    /**
     * @param openingBalance The openingBalance to set.
     */
    public void setOpeningBalance(double openingBalance) {
        this.openingBalance = openingBalance;
    }
    /**
     * @return Returns the openingDate.
     */
    public Date getOpeningDate() {
        return openingDate;
    }
    /**
     * @param openingDate The openingDate to set.
     */
    public void setOpeningDate(Date openingDate) {
        this.openingDate = openingDate;
    }
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        try {
            return this.reference.equalsIgnoreCase(((Account)arg0).reference);
        } catch (Exception e) {}
        return super.equals(arg0);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        try {
            return this.reference.hashCode();
        } catch (Exception e) {}
        return super.hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.reference;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Account arg0) {
        return this.reference.compareTo(((Account)arg0).reference);
    }
    
    /**
     * @return Returns the accountNumber.
     */
    public String getAccountNumber() {
        return accountNumber;
    }
    
    /**
     * @param accountNumber The accountNumber to set.
     */
    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }
    
    /**
     * @return Returns the external.
     */
    public boolean isExternal() {
        return external;
    }
    
    /**
     * @param external The external to set.
     */
    public void setExternal(boolean external) {
        this.external = external;
    }
    
    /**
     * @return Returns the sortCode.
     */
    public String getSortCode() {
        return sortCode;
    }
    
    /**
     * @param sortCode The sortCode to set.
     */
    public void setSortCode(String sortCode) {
        this.sortCode = sortCode;
    }
    
    /**
     * @return Returns the taxed.
     */
    public boolean isTaxed() {
        return taxed;
    }
    
    /**
     * @param taxed The taxed to set.
     */
    public void setTaxed(boolean taxed) {
        this.taxed = taxed;
    }
    
    /**
     * @return Returns the ts.
     */
    public Date getTs() {
        return ts;
    }
    
    /**
     * @return Returns the category.
     */
    public String getCategory() {
        return category;
    }
    
    /**
     * @param category The category to set.
     */
    public void setCategory(String category) {
        if (category != null) {
            this.category = category;
        }
    }
}
