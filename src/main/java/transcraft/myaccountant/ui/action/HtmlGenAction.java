/**
 * Created on 06-Oct-2005
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

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Prints PDF of the selected entity
 * 
 * @author david.tran@transcraft.co.uk
 */
public class HtmlGenAction extends BaseTargetHandler {

    public HtmlGenAction() {
        super(Messages.getString("HtmlGenAction.0"), ImageDescriptor.createFromFile(HtmlGenAction.class, "/images/ie.gif")); //$NON-NLS-1$ //$NON-NLS-2$
        setToolTipText(Messages.getString("HtmlGenAction.2")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.action.IAction#run()
     */
    @Override
    public void run() {
        ActionableTarget<?> target = ActionManager.getCurrentActionableTarget();
        if (target != null) {
            target.doHTML();
        }
    }
    
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.TargetHandler#handle(transcraft.BookKeeper.gui.action.ActionableTarget)
     */
    @Override
    public void handle(ActionableTarget<?> target) {
        this.setEnabled(target != null && target.canGenerateHTML());
    }
}
