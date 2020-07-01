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
import java.util.Map;

import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * Invoice report for services type companies
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceServicesReport extends BaseGenericReport<InvoiceItem> {

    transient Invoice invoice;
    
    /**
     * @param name
     */
    public InvoiceServicesReport(Invoice invoice) {
        super(invoice.getReference());
        this.invoice = invoice;
        this.setReportTemplate("/templates/InvoiceServicesReport.jasper"); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        if (this.currentRow == null) {
            return null;
        }
        InvoiceItem item = (InvoiceItem)this.currentRow;
        if (field.getName().equals("itemDesc")) { //$NON-NLS-1$
            return item.getDescription();
        } else if (field.getName().equals("itemPrice")) { //$NON-NLS-1$
        	return Double.valueOf(item.getPrice());
        } else if (field.getName().equals("itemQuantity")) { //$NON-NLS-1$
        	return Double.valueOf(item.getQuantity());
        } else if (field.getName().equals("itemAmount")) { //$NON-NLS-1$
        	return Double.valueOf(item.getAmount());
        } else if (field.getName().equals("itemUnit")) { //$NON-NLS-1$
        	return item.getUnit();
        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
        	return this.invoice.getReference();
        } else if (field.getName().equals("vatTotal")) { //$NON-NLS-1$
        	return Double.valueOf(this.invoice.getTaxAmount());
        }
        return null;
    }

    public Map<String, Object>	getParameters() throws JRException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("clientAddress", this.invoice.getClientAddress()); //$NON-NLS-1$
        params.put("clientName", this.invoice.getClientName()); //$NON-NLS-1$
        params.put("companyAddress", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_ADDRESS)); //$NON-NLS-1$
        params.put("startDate", this.invoice.getStartDate()); //$NON-NLS-1$
        params.put("invoiceNo", this.invoice.getReference()); //$NON-NLS-1$
        params.put("reportTitle", "Invoice"); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("endDate", this.invoice.getEndDate()); //$NON-NLS-1$
        params.put("invoicedDate", this.invoice.getInvoicedDate()); //$NON-NLS-1$
        params.put("companyName", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNAME)); //$NON-NLS-1$
        params.put("totalLabel", Messages.getString("InvoiceServicesReport.7")); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("vatLabel", Messages.getString("InvoiceServicesReport.9")); //$NON-NLS-1$ //$NON-NLS-2$
        params.put("dueLabel", Messages.getString("InvoiceServicesReport.11")); //$NON-NLS-1$ //$NON-NLS-2$
        
        String token = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_EMAIL);
        if (token != null && token.trim().length() > 0) {
            params.put("companyEmail", Messages.getString("InvoiceServicesReport.0") + token); //$NON-NLS-1$ //$NON-NLS-2$
        }
        token = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_FAXNO);
        if (token != null && token.trim().length() > 0) {
            params.put("companyFaxNo", Messages.getString("InvoiceServicesReport.1") + token); //$NON-NLS-1$ //$NON-NLS-2$
        }
        token =PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_PHONENO);      
        if (token != null && token.trim().length() > 0) {
            params.put("companyPhoneNo", Messages.getString("InvoiceServicesReport.2") + token);   //$NON-NLS-1$ //$NON-NLS-2$
        }
        token = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNO);
        if (token != null && token.trim().length() > 0) {
            params.put("companyRegNo", Messages.getString("InvoiceServicesReport.3") + token); //$NON-NLS-1$ //$NON-NLS-2$
        }
        token = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_URL);
        if (token != null && token.trim().length() > 0) {
            params.put("companyURL", Messages.getString("InvoiceServicesReport.4") + token); //$NON-NLS-1$ //$NON-NLS-2$
        }
        token = PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_VATCODE);
        if (token != null && token.trim().length() > 0) {
            params.put("vatRegNo", Messages.getString("InvoiceServicesReport.5") + token); //$NON-NLS-1$ //$NON-NLS-2$
        }
        
        return params;
    }

    
    public void run() {
        this.reportRows.clear();
        this.reportRows.addAll(Arrays.asList(this.invoice.getItems()));
        this.rowIterator = this.reportRows.iterator();
    }
    
}
