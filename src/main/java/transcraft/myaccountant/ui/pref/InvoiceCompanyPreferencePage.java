/**
 * Created on 09-Oct-2005
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
package transcraft.myaccountant.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 * manages preferences for invoices
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceCompanyPreferencePage extends BasePreferencePage {

    /**
     *
     */
    public InvoiceCompanyPreferencePage() {
        super(Messages.getString("InvoiceCompanyPreferencePage.0"), ImageDescriptor.createFromFile(InvoiceCompanyPreferencePage.class, //$NON-NLS-1$
        "/images/invoice.gif"), FieldEditorPreferencePage.FLAT); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        StringFieldEditor companyNameEditor = new StringFieldEditor(PreferenceFactory.INVOICE_COMPANYNAME,
                Messages.getString("InvoiceCompanyPreferencePage.2"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(companyNameEditor);
        StringFieldEditor addressEditor = new StringFieldEditor(PreferenceFactory.INVOICE_ADDRESS,
                Messages.getString("InvoiceCompanyPreferencePage.3"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(addressEditor);
        StringFieldEditor companyCodeEditor = new StringFieldEditor(PreferenceFactory.INVOICE_COMPANYNO,
                Messages.getString("InvoiceCompanyPreferencePage.4"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(companyCodeEditor);
        StringFieldEditor vatCodeEditor = new StringFieldEditor(PreferenceFactory.INVOICE_VATCODE,
                Messages.getString("InvoiceCompanyPreferencePage.5"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(vatCodeEditor);
        StringFieldEditor phoneEditor = new StringFieldEditor(PreferenceFactory.INVOICE_PHONENO,
                Messages.getString("InvoiceCompanyPreferencePage.6"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(phoneEditor);
        StringFieldEditor faxEditor = new StringFieldEditor(PreferenceFactory.INVOICE_FAXNO,
                Messages.getString("InvoiceCompanyPreferencePage.7"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(faxEditor);
        StringFieldEditor urlEditor = new StringFieldEditor(PreferenceFactory.INVOICE_URL,
                Messages.getString("InvoiceCompanyPreferencePage.8"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(urlEditor);
        StringFieldEditor emailEditor = new StringFieldEditor(PreferenceFactory.INVOICE_EMAIL,
                Messages.getString("InvoiceCompanyPreferencePage.9"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(emailEditor);
    }
}
