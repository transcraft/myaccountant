/**
 * Created on 23-Oct-2005
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
package transcraft.myaccountant.ui.action;


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import transcraft.myaccountant.ui.NameAndValueDialog;

/**
 * @author david.tran@transcraft.co.uk
 */
public class SearchParametersDialog extends NameAndValueDialog {

    private Button forwardButton;
    private Button reverseButton;
    private boolean forwardSearch = true;
    private boolean matchAllSearch = false;
    private Button matchAllButton;

    /**
     * @param parentShell
     * @param title
     * @param message
     * @param names
     */
    public SearchParametersDialog(Shell parentShell, String title,
            String message, String[] names) {
        super(parentShell, title, message, names);
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    protected Control createDialogArea(Composite parent) {
        Composite dialogArea = (Composite)super.createDialogArea(parent);

        Group group = new Group(dialogArea, SWT.SHADOW_ETCHED_IN);
        group.setLayout(new GridLayout());
        this.forwardButton = new Button(group, SWT.RADIO);
        this.forwardButton.setText(Messages.getString("SearchParametersDialog.0")); //$NON-NLS-1$
        this.forwardButton.setToolTipText(Messages.getString("SearchParametersDialog.1")); //$NON-NLS-1$
        this.forwardButton.setSelection(true);
        this.forwardButton.setSelection(this.forwardSearch);

        this.reverseButton = new Button(group, SWT.RADIO);
        this.reverseButton.setText(Messages.getString("SearchParametersDialog.2")); //$NON-NLS-1$
        this.reverseButton.setToolTipText(Messages.getString("SearchParametersDialog.3")); //$NON-NLS-1$
        this.reverseButton.setSelection(! this.forwardSearch);
        
        this.matchAllButton = new Button(dialogArea, SWT.CHECK);
        this.matchAllButton.setText(Messages.getString("SearchParametersDialog.4")); //$NON-NLS-1$
        this.matchAllButton.setToolTipText(Messages.getString("SearchParametersDialog.5")); //$NON-NLS-1$
        this.matchAllButton.setSelection(this.matchAllSearch);

        return dialogArea;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#close()
     */
    public boolean close() {
        this.forwardSearch = this.forwardButton.getSelection();
        this.matchAllSearch = this.matchAllButton.getSelection();
        return super.close();
    }
    /**
     * @return Returns the forwardSearch flag.
     */
    public boolean isForwardSearch() {
        return this.forwardSearch;
    }
    /**
     * @param forward The forward to set.
     */
    public void setForwardSearch(boolean forward) {
        this.forwardSearch = forward;
    }
    /**
     * @return Returns the matchAllSearch.
     */
    public boolean isMatchAllSearch() {
        return matchAllSearch;
    }
    /**
     * @param matchAllSearch The matchAllSearch to set.
     */
    public void setMatchAllSearch(boolean matchAllSearch) {
        this.matchAllSearch = matchAllSearch;
    }
}
