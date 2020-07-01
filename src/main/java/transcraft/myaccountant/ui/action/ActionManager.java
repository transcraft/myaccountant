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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * @author david.tran@transcraft.co.uk
 */
public class ActionManager {

    private static List<TargetHandler> targetHandlers = Lists.newArrayList();
    private static ActionableTarget<?> currentActionableTarget;
    
    public static void	registerAction(TargetHandler handler) {
        targetHandlers.add(handler);
    }
    public static void setActionableTarget(ActionableTarget<?> target) {
        for (TargetHandler handler : targetHandlers) {
            handler.handle(target);
        }
        currentActionableTarget = target;
    }
    public static BaseTargetHandler	getTargethandler(Class<?> theClass) {
        for (TargetHandler handler : targetHandlers) {
            if (handler instanceof BaseTargetHandler) {
                return (BaseTargetHandler)handler;
            }
        }
        return new EmptyAction();
    }
    public static ActionableTarget<?>	getCurrentActionableTarget() {
        return currentActionableTarget;
    }
    /**
     * 
     */
    private ActionManager() {
        super();
        // TODO Auto-generated constructor stub
    }

}
