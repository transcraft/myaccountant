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
import java.util.Date;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.myaccountant.utils.DateUtil;

/**
 * Invoice Meta provider for second section
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceMeta2 extends BaseMetaProvider<Invoice> {
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String STARTDATE_PROP = "startDate"; //$NON-NLS-1$
    public static final String ENDDATE_PROP = "endDate"; //$NON-NLS-1$
    public static final String NETAMT_PROP = "netAmount"; //$NON-NLS-1$
    public static final String TAXAMT_PROP = "taxAmount"; //$NON-NLS-1$
    public static final String GRSAMT_PROP = "grossAmount"; //$NON-NLS-1$
    public static final String ACCTREF_PROP = "accountReference"; //$NON-NLS-1$
    public static final String RECDATE_PROP = "receivedDate"; //$NON-NLS-1$

    @SuppressWarnings("unused")
	private Invoice invoice;
    
    public InvoiceMeta2(Invoice invoice) {
        this.columns = new MetaColumn [] {
	        new MetaColumn<Date>(STARTDATE_PROP, Messages.getString("InvoiceMeta2.8"), Date.class), //$NON-NLS-1$
	        new MetaColumn<Date>(ENDDATE_PROP, Messages.getString("InvoiceMeta2.9"), Date.class), //$NON-NLS-1$
	        new MetaColumn<Double>(NETAMT_PROP, Messages.getString("InvoiceMeta2.10"), Double.class, false), //$NON-NLS-1$
	        new MetaColumn<Double>(TAXAMT_PROP, Messages.getString("InvoiceMeta2.11"), Double.class, false), //$NON-NLS-1$
	        new MetaColumn<Double>(GRSAMT_PROP, Messages.getString("InvoiceMeta2.12"), Double.class, false), //$NON-NLS-1$
	        new MetaColumn<Date>(RECDATE_PROP, Messages.getString("InvoiceMeta2.13"), Date.class, false), //$NON-NLS-1$
	        new MetaColumn<String>(ACCTREF_PROP, Messages.getString("InvoiceMeta2.14"), String.class, false), //$NON-NLS-1$
        };
        this.invoice = invoice;
    }
    
    @Override
    public Object getValue(Invoice invoice, String columnName, String accountReference) {
        if (STARTDATE_PROP.equals(columnName)) {
            return invoice.getStartDate();
        } else if (ENDDATE_PROP.equals(columnName)) {
            return invoice.getEndDate();
        } else if (ACCTREF_PROP.equals(columnName)) {
            return invoice.getAccountReference();
        } else if (RECDATE_PROP.equals(columnName)) {
            return invoice.getReceivedDate();
        } else if (NETAMT_PROP.equals(columnName)) {
            return Double.valueOf(invoice.getNetAmount());
        } else if (TAXAMT_PROP.equals(columnName)) {
            return Double.valueOf(invoice.getTaxAmount());
        } else if (GRSAMT_PROP.equals(columnName)) {
            return Double.valueOf(invoice.getGrossAmount());
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(Invoice invoice, String accountReference, String columnName, Object value) throws ParseException {
        if (STARTDATE_PROP.equals(columnName)) {
            invoice.setStartDate(DateUtil.getDate(value));
        } else if (ENDDATE_PROP.equals(columnName)) {
            invoice.setEndDate(DateUtil.getDate(value));
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
