/**
 * Created on 17-Oct-2005
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

import java.util.Arrays;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import transcraft.myaccountant.utils.Formatters;

/**
 * returns both name and value
 * @author david.tran@transcraft.co.uk
 */
public class NameAndValueDialog extends TitleAreaDialog {

    private String title;
    private String message;
    private String [] names;
    private List nameList;
    private Text valueControl;
    private String name;
    private String value;
    private boolean caseSensitive = false;
    private Button caseSensitiveButton;
    
    /**
     * @param parentShell
     */
    public NameAndValueDialog(Shell parentShell, String title, String message, String [] names) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.names = names;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#createContents(org.eclipse.swt.widgets.Composite)
     */
    protected Control createContents(Composite parent) {

        Control contents = super.createContents(parent);
        this.setTitleImage(ImageCache.get(Messages.getString("NameAndValueDialog.0"))); //$NON-NLS-1$
        if (this.title != null) {
            this.setTitle(this.title);
            parent.getShell().setText(title);
        }
        if (this.message != null) {
            this.setMessage(this.message);
        }
        return contents;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite)super.createDialogArea(parent);
        dialogArea.setLayout(new GridLayout());

        this.nameList = new List(dialogArea, SWT.FLAT|SWT.V_SCROLL);
        this.nameList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        if (this.names != null) {
            this.nameList.setItems(this.names);
            if (this.name != null) {
                this.nameList.select(Arrays.asList(this.names).indexOf(this.name));
            }
        }
        this.nameList.setSize(this.nameList.computeSize(SWT.DEFAULT, 200));
        this.nameList.setToolTipText(Messages.getString("NameAndValueDialog.1")); //$NON-NLS-1$

        this.valueControl = new Text(dialogArea, SWT.FLAT);
        this.valueControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        this.valueControl.setText(Formatters.emptyIfNull(this.value));
        this.valueControl.setToolTipText(Messages.getString("NameAndValueDialog.2")); //$NON-NLS-1$

        this.caseSensitiveButton = new Button(dialogArea, SWT.CHECK);
        this.caseSensitiveButton.setText(Messages.getString("NameAndValueDialog.3")); //$NON-NLS-1$
        this.caseSensitiveButton.setToolTipText(Messages.getString("NameAndValueDialog.4")); //$NON-NLS-1$
        this.caseSensitiveButton.setSelection(this.caseSensitive);

        this.valueControl.setFocus();
        this.valueControl.selectAll();
        
        return dialogArea;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        String [] sel = this.nameList.getSelection();
        this.name = sel.length > 0 ? sel[0] : null;
        this.value = this.valueControl.getText();
        this.caseSensitive = this.caseSensitiveButton.getSelection();
        return super.close();
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return this.name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return this.value;
    }
    /**
     * @return Returns the caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    /**
     * @param caseSensitive The caseSensitive to set.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
}
