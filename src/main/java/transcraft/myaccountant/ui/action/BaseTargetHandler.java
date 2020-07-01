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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * base class for all TargetHandlers, will automatically register
 * with ActionManager
 * 
 * @author david.tran@transcraft.co.uk
 */
public abstract class BaseTargetHandler extends Action implements TargetHandler {

    /**
     * @param name
     * @param imgDesc
     */
    public BaseTargetHandler(String name, ImageDescriptor imgDesc) {
        super(name, imgDesc);
        this.init();
    }
    public BaseTargetHandler(String name) {
        super(name);
        this.init();
    }

    /**
     * 
     */
    public BaseTargetHandler() {
        super();
        this.init();
    }
    /**
     * @param text
     * @param style
     */
    public BaseTargetHandler(String text, int style) {
        super(text, style);
        this.init();
    }
    private void	init() {
        this.setEnabled(false);
        ActionManager.registerAction(this);
    }
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.gui.action.TargetHandler#handle(transcraft.BookKeeper.gui.action.ActionableTarget)
     */
    public abstract void handle(ActionableTarget<?> target);

}
