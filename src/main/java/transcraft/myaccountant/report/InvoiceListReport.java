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


import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * List of Invoices report
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceListReport extends BaseGenericReport<Invoice> {

    protected transient List<Invoice> entries = Lists.newArrayList();
    
    /**
     * for subclasses only
     * @param title
     */
    protected InvoiceListReport(String title) {
        super(title);
        this.setReportTemplate("/templates/InvoiceListReport.jasper"); //$NON-NLS-1$
    }
    
    /**
     * @param name
     */
    public InvoiceListReport(String title, List<Invoice> entries) {
        this(title);
        this.entries.addAll(entries);
    }

    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        if (this.currentRow == null) {
            return null;
        }
        Invoice invoice = (Invoice)this.currentRow;
        if (field.getName().equals("reference")) { //$NON-NLS-1$
            return invoice.getReference();
        } else if (field.getName().equals("invoiceDate")) { //$NON-NLS-1$
        	return invoice.getInvoicedDate();
        } else if (field.getName().equals("clientName")) { //$NON-NLS-1$
        	return invoice.getClientName();
        } else if (field.getName().equals("netAmount")) { //$NON-NLS-1$
        	return Double.valueOf(invoice.getNetAmount());
        } else if (field.getName().equals("taxAmount")) { //$NON-NLS-1$
        	return Double.valueOf(invoice.getTaxAmount());
        } else if (field.getName().equals("grossAmount")) { //$NON-NLS-1$
        	return Double.valueOf(invoice.getGrossAmount());
        } else if (field.getName().equals("accountReference")) { //$NON-NLS-1$
        	return invoice.getAccountReference();
        } else if (field.getName().equals("receivedDate")) { //$NON-NLS-1$
        	return invoice.getReceivedDate();
        } else if (field.getName().equals("itemGroup")) { //$NON-NLS-1$
        	return "Invoices"; //$NON-NLS-1$
        }
        return null;
    }
    
    @Override
    public Map<String, Object>	getParameters() throws JRException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("companyAddress", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_ADDRESS)); //$NON-NLS-1$
        params.put("companyEmail", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_EMAIL)); //$NON-NLS-1$
        params.put("companyFaxNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_FAXNO)); //$NON-NLS-1$
        params.put("companyName", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNAME)); //$NON-NLS-1$
        params.put("companyPhoneNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_PHONENO)); //$NON-NLS-1$
        params.put("companyRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNO)); //$NON-NLS-1$
        params.put("companyURL", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_URL)); //$NON-NLS-1$
        params.put("reportTitle", this.getName()); //$NON-NLS-1$
        params.put("vatRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_VATCODE)); //$NON-NLS-1$
        return params;
    }

    
    public void run() {
        this.reportRows.clear();
        this.reportRows.addAll(this.entries);
        this.rowIterator = this.reportRows.iterator();
    }
    
}
