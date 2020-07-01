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

import org.eclipse.swt.SWT;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.myaccountant.utils.Formatters;

/**
 * Invoice Meta for first section
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceListMeta extends BaseMetaProvider<Invoice> {
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String INVDATE_PROP = "invoicedDate"; //$NON-NLS-1$
    public static final String CLNTNAME_PROP = "clientName"; //$NON-NLS-1$
    public static final String NETAMT_PROP = "netAmount"; //$NON-NLS-1$
    public static final String TAXAMT_PROP = "taxAmount"; //$NON-NLS-1$
    public static final String GRSAMT_PROP = "grossAmount"; //$NON-NLS-1$
    public static final String ACCTREF_PROP = "accountReference"; //$NON-NLS-1$
    public static final String RECDATE_PROP = "receivedDate"; //$NON-NLS-1$
    public static final String STARTDATE_PROP = "startDate"; //$NON-NLS-1$
    public static final String ENDDATE_PROP = "endDate"; //$NON-NLS-1$

    public InvoiceListMeta() {
        this.columns = new MetaColumn [] {
	        new MetaColumn<String>(REF_PROP, Messages.getString("InvoiceListMeta.10"), String.class, 100, SWT.LEFT, false), //$NON-NLS-1$
	        new MetaColumn<Date>(INVDATE_PROP, Messages.getString("InvoiceListMeta.11"), Date.class, 100), //$NON-NLS-1$
	        new MetaColumn<String>(CLNTNAME_PROP, Messages.getString("InvoiceListMeta.12"), String.class, 350), //$NON-NLS-1$
	        new MetaColumn<String>(ACCTREF_PROP, Messages.getString("InvoiceListMeta.13"), String.class, 120, false, false), //$NON-NLS-1$
	        new MetaColumn<Date>(RECDATE_PROP, Messages.getString("InvoiceListMeta.14"), Date.class, 100), //$NON-NLS-1$
	        new MetaColumn<Double>(NETAMT_PROP, Messages.getString("InvoiceListMeta.15"), Double.class, 100), //$NON-NLS-1$
	        new MetaColumn<Double>(TAXAMT_PROP, Messages.getString("InvoiceListMeta.16"), Double.class, 100), //$NON-NLS-1$
	        new MetaColumn<Double>(GRSAMT_PROP, Messages.getString("InvoiceListMeta.17"), Double.class, 100), //$NON-NLS-1$
	        new MetaColumn<Date>(STARTDATE_PROP, Messages.getString("InvoiceListMeta.18"), Date.class, 100), //$NON-NLS-1$
	        new MetaColumn<Date>(ENDDATE_PROP, Messages.getString("InvoiceListMeta.19"), Date.class, 100), //$NON-NLS-1$
        };
        this.getColumn(REF_PROP).setSortable(true);
        this.getColumn(INVDATE_PROP).setSortable(true);
        this.getColumn(CLNTNAME_PROP).setSortable(true);
        this.getColumn(RECDATE_PROP).setSortable(true);
        this.getColumn(ACCTREF_PROP).setSortable(true);
    }
    
    @Override
    public Object getValue(Invoice invoice, String columnName, String accountReference) {
        if (REF_PROP.equals(columnName)) {
            return invoice.getReference();
        } else if (INVDATE_PROP.equals(columnName)) {
            return invoice.getInvoicedDate();
        } else if (CLNTNAME_PROP.equals(columnName)) {
            return invoice.getClientName();
        } else if (ACCTREF_PROP.equals(columnName)) {
            return invoice.getAccountReference();
        } else if (RECDATE_PROP.equals(columnName)) {
            return invoice.getReceivedDate();
        } else if (NETAMT_PROP.equals(columnName)) {
            return Formatters.format(invoice.getNetAmount());
        } else if (TAXAMT_PROP.equals(columnName)) {
            return Formatters.format(invoice.getTaxAmount());
        } else if (GRSAMT_PROP.equals(columnName)) {
            return Formatters.format(invoice.getGrossAmount());
        } else if (STARTDATE_PROP.equals(columnName)) {
            return invoice.getStartDate();
        } else if (ENDDATE_PROP.equals(columnName)) {
            return invoice.getEndDate();
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(Invoice invoice, String accountReference, String columnName, Object value) throws ParseException {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountantmeta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return REF_PROP;
    }
}
