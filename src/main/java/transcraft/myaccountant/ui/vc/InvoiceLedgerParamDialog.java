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
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.CalendarFieldEditor;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.utils.DateUtil;


public class InvoiceLedgerParamDialog extends TitleAreaDialog {
    private org.eclipse.swt.widgets.List accountsList;
    private CalendarFieldEditor receivedDateEditor;
    private String accountReference;
    private Date receivedDate;
    private BOMService bomService;
    private boolean withReceivedDate = true;
    
    
    public InvoiceLedgerParamDialog(Shell parentShell, BOMService bomService) {
        super(parentShell);
        this.bomService = bomService;
    }
    
    public InvoiceLedgerParamDialog(Shell parentShell, BOMService bomService, boolean withReceivedDate) {
        this(parentShell, bomService);
        this.withReceivedDate = withReceivedDate;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        this.setTitleImage(ImageCache.get("account")); //$NON-NLS-1$
        String title = Messages.getString("InvoiceLedgerParamDialog.1"); //$NON-NLS-1$
        this.setTitle(title);
        this.setMessage(Messages.getString("InvoiceLedgerParamDialog.2")); //$NON-NLS-1$
        return contents;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite)super.createDialogArea(parent);
        dialogArea.setLayout(new GridLayout());
        this.accountsList = new org.eclipse.swt.widgets.List(dialogArea, SWT.FLAT|SWT.V_SCROLL);
        this.accountsList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        List<String> accounts = bomService.getMemorisedList(Messages.getString("InvoiceLedgerParamDialog.0")); //$NON-NLS-1$
        // Java 8 compatibility mode, so String[]::new is not available
        accountsList.setItems(accounts.toArray(new String[accounts.size()]));
        if (this.accountReference != null) {
            int idx = accounts.indexOf(this.accountReference);
            if (idx >= 0) {
                this.accountsList.select(idx);
            }
        }
        if (this.withReceivedDate) {
            this.receivedDateEditor = new CalendarFieldEditor(dialogArea);
            this.receivedDateEditor.setValue(this.receivedDate != null ? this.receivedDate : DateUtil.getTodayStart().getTime());
        }
        return dialogArea;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        int idx = this.accountsList.getSelectionIndex();
        if (idx >= 0) {
            this.accountReference = this.accountsList.getItem(idx);
        }
        if (this.receivedDateEditor != null) {
	        Calendar cal = this.receivedDateEditor.getValue();
	        if (cal != null) {
	            this.receivedDate = cal.getTime();
	        }
        }
        return super.close();
    }
    
    /**
     * @return Returns the accountReference.
     */
    public String getAccountReference() {
        return accountReference;
    }
    /**
     * @param accountReference The accountReference to set.
     */
    public void setAccountReference(String accountReference) {
        this.accountReference = accountReference;
    }
    /**
     * @return Returns the receivedDate.
     */
    public Date getReceivedDate() {
        return receivedDate;
    }
    /**
     * @param receivedDate The receivedDate to set.
     */
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }
}