/**
 * Created on 16-Jul-2005
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

import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.myaccountant.ui.pref.TaxBandHelper;
import transcraft.myaccountant.utils.Formatters;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceItemMeta extends BaseMetaProvider<InvoiceItem> {

    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String PRICE_PROP = "price"; //$NON-NLS-1$
    public static final String QUANTITY_PROP = "quantity"; //$NON-NLS-1$
    public static final String AMOUNT_PROP = "amount"; //$NON-NLS-1$
    public static final String UNIT_PROP = "unit"; //$NON-NLS-1$
    public static final String TAXBAND_PROP = "taxBand"; //$NON-NLS-1$
    
    public InvoiceItemMeta(Invoice invoice) {
        this.columns = new MetaColumn [] {
            new MetaColumn<String>(DESC_PROP, Messages.getString("InvoiceItemMeta.6"), String.class), //$NON-NLS-1$
            new MetaColumn<Double>(PRICE_PROP, Messages.getString("InvoiceItemMeta.7"), Double.class), //$NON-NLS-1$
            new MetaColumn<Double>(QUANTITY_PROP, Messages.getString("InvoiceItemMeta.8"), Double.class), //$NON-NLS-1$
            new MetaColumn<String>(UNIT_PROP, Messages.getString("InvoiceItemMeta.9"), String.class), //$NON-NLS-1$
            new MetaColumn<String>(TAXBAND_PROP, Messages.getString("InvoiceItemMeta.10"), String.class, Arrays.asList(new TaxBandHelper().getTaxCodes())), //$NON-NLS-1$
            new MetaColumn<Double>(AMOUNT_PROP, Messages.getString("InvoiceItemMeta.11"), Double.class), //$NON-NLS-1$
        };
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(InvoiceItem item, String columnName, String selector) {
        if (DESC_PROP.equals(columnName)) {
            return Formatters.emptyIfNull(item.getDescription());
        } else if (PRICE_PROP.equals(columnName)) {
            double price = item.getPrice();
            return price != 0 ? Formatters.format(price) : ""; //$NON-NLS-1$
        } else if (QUANTITY_PROP.equals(columnName)) {
            double quantity = item.getQuantity();
            return quantity != 0 ? Formatters.format(quantity) : ""; //$NON-NLS-1$
        } else if (UNIT_PROP.equals(columnName)) {
            return Formatters.emptyIfNull(item.getUnit());
        } else if (TAXBAND_PROP.equals(columnName)) {
            return item.getTaxBand();
        } else if (AMOUNT_PROP.equals(columnName)) {
            double amount = item.getAmount();
            return amount != 0 ? Formatters.format(amount) : ""; //$NON-NLS-1$
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValue(InvoiceItem item, String selector, String columnName,
            Object value) throws ParseException {
        if (DESC_PROP.equals(columnName)) {
            item.setDescription(value.toString());
        } else if (PRICE_PROP.equals(columnName)) {
            item.setPrice(Formatters.parse(value.toString()).doubleValue());
        } else if (QUANTITY_PROP.equals(columnName)) {
            item.setQuantity(Formatters.parse(value.toString()).intValue());
        } else if (UNIT_PROP.equals(columnName)) {
            item.setUnit(value.toString());
        } else if (TAXBAND_PROP.equals(columnName)) {
            item.setTaxBand(value.toString());
        } else if (AMOUNT_PROP.equals(columnName)) {
            item.setAmount(Formatters.parse(value.toString()).doubleValue());
        }
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return DESC_PROP;
    }
}
