/**
 * Created on 18-Aug-2005
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

import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;

import com.google.common.collect.Lists;


/**
 * @author david.tran@transcraft.co.uk
 */
public abstract class ActionableTarget<T> extends Composite 
	implements Clipboardable, Generateable, Searchable<T>, FocusListener {

    /**
     * @param parent
     * @param style
     */
    public ActionableTarget(Composite parent, int style) {
        super(parent, style);
        this.addFocusListener(this);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGenerateHTML()
     */
    @Override
    public boolean canGenerateHTML() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGeneratePDF()
     */
    @Override
    public boolean canGeneratePDF() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGenerateXML()
     */
    @Override
    public boolean canGenerateXML() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doHTML()
     */
    @Override
    public void doHTML() {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doPDF()
     */
    @Override
    public void doPDF() {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doXML()
     */
    @Override
    public void doXML() {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#doCopy()
     */
    @Override
    public void doCopy() {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#doCut()
     */
    @Override
    public void doCut() {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#doPaste()
     */
    @Override
    public void doPaste(Object data) {
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#canCopy()
     */
    @Override
    public boolean canCopy() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#canCut()
     */
    @Override
    public boolean canCut() {
        return false;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.Clipboardable#canPaste()
     */
    @Override
    public boolean canPaste() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#canSearch()
     */
    @Override
    public boolean canSearch() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#doFind(java.lang.Object)
     */
    @Override
    public List<T> doFind(SearchParameter param) {
        return Lists.newArrayList();
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#canHelp()
     */
    @Override
    public boolean canHelp() {
        return false;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Searchable#doHelp()
     */
    @Override
    public void doHelp() {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent e) {
        ActionManager.setActionableTarget(this);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    @Override
    public void setVisible(boolean visible) {
        if (visible) {
            ActionManager.setActionableTarget(this);
        } else {
            ActionableTarget<?> target = ActionManager.getCurrentActionableTarget();
            if (target != null && target == this) {
                ActionManager.setActionableTarget(null);
            }
        }
        super.setVisible(visible);
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.events.FocusListener#focusLost(org.eclipse.swt.events.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent e) {
        ActionManager.setActionableTarget(null);
    }    
}
