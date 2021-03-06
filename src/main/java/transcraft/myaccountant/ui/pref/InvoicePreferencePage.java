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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;


/**
 * manages preferences for invoices
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InvoicePreferencePage extends BasePreferencePage {

    /**
     *
     */
    public InvoicePreferencePage() {
        super(Messages.getString("InvoicePreferencePage.0"), ImageDescriptor.createFromFile(InvoicePreferencePage.class, //$NON-NLS-1$
        "/images/invoice.gif"), FieldEditorPreferencePage.GRID); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        ListFieldEditor listEditor = new ListFieldEditor(PreferenceFactory.INVOICE_ACCTS,
                Messages.getString("InvoicePreferencePage.2"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(listEditor);
        DirectoryFieldEditor outPathEditor = new DirectoryFieldEditor(PreferenceFactory.INVOICE_OUTPATH,
                Messages.getString("InvoicePreferencePage.3"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(outPathEditor);
        BooleanFieldEditor autoLedgerEditor = new BooleanFieldEditor(PreferenceFactory.INVOICE_AUTOLEDGER,
                Messages.getString("InvoicePreferencePage.4"), this.getFieldEditorParent()); //$NON-NLS-1$
        this.addField(autoLedgerEditor);
    }
}
