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

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.myaccountant.utils.DateUtil;

/**
 * Invoice Meta for first section
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceMeta1 extends BaseMetaProvider<Invoice> {
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String INVDATE_PROP = "invoicedDate"; //$NON-NLS-1$
    public static final String CLNTNAME_PROP = "clientName"; //$NON-NLS-1$
    public static final String CLNTADDR_PROP = "clientAddress"; //$NON-NLS-1$

    @SuppressWarnings("unused")
	private Invoice invoice;
    
    public InvoiceMeta1(Invoice invoice) {
        this.columns = new MetaColumn [] {
	        new MetaColumn<String>(REF_PROP, Messages.getString("InvoiceMeta1.4"), String.class, false), //$NON-NLS-1$
	        new MetaColumn<Date>(INVDATE_PROP, Messages.getString("InvoiceMeta1.5"), Date.class), //$NON-NLS-1$
	        new MetaColumn<String>(CLNTNAME_PROP, Messages.getString("InvoiceMeta1.6"), String.class, Lists.newArrayList(), true, SWT.NONE), //$NON-NLS-1$
	        new MetaColumn<String>(CLNTADDR_PROP, Messages.getString("InvoiceMeta1.7"), String.class, true, 80, SWT.MULTI|SWT.V_SCROLL|SWT.BORDER), //$NON-NLS-1$
        };
        this.invoice = invoice;
    }
    
    @Override
    public Object getValue(Invoice invoice, String columnName, String accountReference) {
        if (REF_PROP.equals(columnName)) {
            return invoice.getReference();
        } else if (INVDATE_PROP.equals(columnName)) {
            return invoice.getInvoicedDate();
        } else if (CLNTNAME_PROP.equals(columnName)) {
            return invoice.getClientName();
        } else if (CLNTADDR_PROP.equals(columnName)) {
            return invoice.getClientAddress();
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(Invoice invoice, String accountReference, String columnName, Object value) throws ParseException {
        if (INVDATE_PROP.equals(columnName)) {
            invoice.setInvoicedDate(DateUtil.getDate(value));
        } else if (CLNTNAME_PROP.equals(columnName)) {
            invoice.setClientName(value.toString());
        } else if (CLNTADDR_PROP.equals(columnName)) {
            invoice.setClientAddress(value.toString());
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
