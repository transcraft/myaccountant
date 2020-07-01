/**
 * Created on 24-Oct-2005
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
package transcraft.myaccountant.ui.vc;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import transcraft.myaccountant.ui.CalendarFieldEditor;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.utils.Formatters;

/**
 * popup dialog to prompt user for parameters to clone a template invoice
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceCloneDialog extends TitleAreaDialog {

    private String title;
    private String message;
    private Text refControl;
    private String reference;
    private Date invoiceDate;
    private CalendarFieldEditor invoiceDateEditor;
    
    /**
     * @param parentShell
     */
    public InvoiceCloneDialog(Shell parentShell, String title, String message, String newRef) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.reference = newRef;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        this.setTitleImage(ImageCache.get("clone")); //$NON-NLS-1$
        this.setTitle(this.title);
        parent.getShell().setText(title);
        if (this.message != null) {
            this.setMessage(this.message);
        }
        return contents;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Label refLabel = new Label(parent, SWT.NONE);
        refLabel.setText(Messages.getString("InvoiceCloneDialog.1")); //$NON-NLS-1$
        refLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.refControl = new Text(parent, SWT.FLAT);
        this.refControl.setText(Formatters.emptyIfNull(this.reference));
        this.refControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label dateLabel = new Label(parent, SWT.NONE);
        dateLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dateLabel.setText(Messages.getString("InvoiceCloneDialog.2")); //$NON-NLS-1$
        this.invoiceDateEditor = new CalendarFieldEditor(parent);
        this.invoiceDateEditor.setValue(this.invoiceDate);
        this.invoiceDateEditor.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        return this.dialogArea;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        Calendar cal = this.invoiceDateEditor.getValue();
        if (cal != null) {
            this.invoiceDate = cal.getTime();
        }
        this.reference = this.refControl.getText();
        return super.close();
    }
    /**
     * @return Returns the invoiceDate.
     */
    public Date getInvoiceDate() {
        return invoiceDate;
    }
    /**
     * @param invoiceDate The invoiceDate to set.
     */
    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    /**
     * @param reference The reference to set.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }
}
