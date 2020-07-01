/**
 * Created on 11-Oct-2005
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
package transcraft.BookKeeper.bom;

import transcraft.myaccountant.ui.pref.TaxBandHelper;

/**
 * for invoices with multiple items
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceItem {

    private String description = ""; //$NON-NLS-1$
    private double price;
    private double quantity = 1;
    private double amount;
    private String unit = ""; //$NON-NLS-1$
    private String taxBand;
    
    /**
     * 
     */
    public InvoiceItem() {
    }
   
    /**
     * copy constructor
     * @param item
     */
    public InvoiceItem(InvoiceItem item) {
        this.description = item.description;
        this.price = item.price;
        this.quantity = item.quantity;
        this.amount = item.amount;
        this.unit = item.unit;
        this.taxBand = item.taxBand;
    }
    /**
     * @return Returns the amount.
     */
    public double getAmount() {
        return amount;
    }
    /**
     * returns the calculated tax amount
     * @return
     */
    public double	getTax() {
        double tax = new TaxBandHelper().calculateTax(this.amount, new String[] { this.taxBand });
        return tax;
    }
    /**
     * returns the calculated tax amount
     * @return
     */
    public double getGrossAmount() {
        return this.getAmount() + this.getTax();
    }
    /**
     * @param amount The amount to set.
     */
    public void setAmount(double amount) {
        this.amount = amount;
        this.price = 0;
        this.quantity = 0;
    }
    /**
     * @return Returns the price.
     */
    public double getPrice() {
        return price;
    }
    /**
     * @param price The price to set.
     */
    public void setPrice(double price) {
        this.price = price;
        this.amount = this.price * this.quantity;
    }
    /**
     * @return Returns the quantity.
     */
    public double getQuantity() {
        return quantity;
    }
    /**
     * @param quantity The quantity to set.
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        this.amount = this.price * this.quantity;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @return Returns the taxBand.
     */
    public String getTaxBand() {
        return taxBand;
    }
    /**
     * @param taxBand The taxBand to set.
     */
    public void setTaxBand(String taxBand) {
        this.taxBand = taxBand;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * @return Returns the unit.
     */
    public String getUnit() {
        return unit;
    }
    /**
     * @param unit The unit to set.
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        if (arg0 instanceof InvoiceItem) {
           return this.hashCode() == ((InvoiceItem)arg0).hashCode();
        }
        return false;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.description.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.description;
    }
}
