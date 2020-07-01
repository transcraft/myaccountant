/**
 * Created on 05-Nov-2005
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
package transcraft.myaccountant.ui;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author david.tran@transcraft.co.uk
 */
public class PasswordDialog extends TitleAreaDialog {

    private String userName;
    private String password;
    private String oldPassword;
    
    private Text userNameField;
    private Text passwordField;
    private Text oldPasswordField;
    
    /**
     * @param parentShell
     */
    public PasswordDialog(Shell parentShell) {
        super(parentShell);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {
        Composite contents = (Composite)super.createContents(parent);
        this.setTitle(Messages.getString("PasswordDialog.0")); //$NON-NLS-1$
        this.setMessage(Messages.getString("PasswordDialog.1")); //$NON-NLS-1$
        this.setTitleImage(ImageCache.get("keys")); //$NON-NLS-1$
        return contents;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite)super.createDialogArea(parent);
        GridLayout ly = new GridLayout();
        dialogArea.setLayout(ly);
        
        Label label = new Label(dialogArea, SWT.FLAT);
        label.setText(Messages.getString("PasswordDialog.3")); //$NON-NLS-1$
        
        this.userNameField = new Text(dialogArea, SWT.FLAT);
        userNameField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        label = new Label(dialogArea, SWT.FLAT);
        label.setText(Messages.getString("PasswordDialog.4")); //$NON-NLS-1$

        this.passwordField = new Text(dialogArea, SWT.FLAT);
        passwordField.setEchoChar('*');
        passwordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        label = new Label(dialogArea, SWT.FLAT);
        label.setText(Messages.getString("PasswordDialog.5")); //$NON-NLS-1$

        this.oldPasswordField = new Text(dialogArea, SWT.FLAT);
        oldPasswordField.setEchoChar('*');
        oldPasswordField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        return dialogArea;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        this.userName = this.userNameField.getText();
        this.password = this.passwordField.getText();
        this.oldPassword = this.oldPasswordField.getText();
        return super.close();
    }
    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return this.password;
    }
    /**
     * @return Returns the userName.
     */
    public String getUserName() {
        return this.userName;
    }
    /**
     * @return Returns the oldPassword.
     */
    public String getOldPassword() {
        return oldPassword;
    }
}
