/**
 * Created on 25-Aug-2005
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


import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

import transcraft.BookKeeper.bom.Account;
import transcraft.myaccountant.ui.GUIUtil;

/**
 * Drop target for LedgerTreeViewer
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerTreeDropAdapter extends ViewerDropAdapter {
    private LedgerTreeViewer viewer;
    
    /**
     * @param viewer
     */
    public LedgerTreeDropAdapter(LedgerTreeViewer viewer) {
        super(viewer.getTreeViewer());
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
        Object obj = this.getCurrentTarget();
        if (data != null && data instanceof String && (obj.getClass().equals(Account.class))) {
            Account account = (Account)obj;
            if (! GUIUtil.confirm(Messages.getString("LedgerTreeDropAdapter.0"), Messages.getString("LedgerTreeDropAdapter.1") + //$NON-NLS-1$ //$NON-NLS-2$
                    account.getReference() + Messages.getString("LedgerTreeDropAdapter.2"))) { //$NON-NLS-1$
                return false;
            }
            this.viewer.doImport((Account)this.getCurrentTarget(), data);
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        boolean ok = target != null && target.getClass().equals(Account.class);
        return ok;
    }
}
