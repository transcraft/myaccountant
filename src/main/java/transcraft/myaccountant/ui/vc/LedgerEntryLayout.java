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
package transcraft.myaccountant.ui.vc;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Layout for LedgerEntryViewer (refactored to make it more CVS friendly)
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryLayout extends Layout {
	private static final Logger LOG = LoggerFactory.getLogger(LedgerEntryLayout.class);
	
    private static final int MARGIN_WIDTH = 5;
    private static final int MARGIN_HEIGHT = 2;

    /**
     * @param viewer
     */
    LedgerEntryLayout(LedgerEntryViewer viewer) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#computeSize(org.eclipse.swt.widgets.Composite, int, int, boolean)
     */
    @Override
    protected Point computeSize(Composite composite, int wHint, int hHint,
            boolean flushCache) {
        Point pt = this.computeLayoutSize(composite, wHint, hHint, flushCache, true);
        return pt;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Layout#layout(org.eclipse.swt.widgets.Composite, boolean)
     */
    @Override
    protected void layout(Composite composite, boolean flushCache) {
    }
    
    private Point computeLayoutSize(Composite composite, int wHint, int yHint, boolean flushCache, boolean doLayout) {
        Control [] children = composite.getChildren();
        int w = composite.getParent().getClientArea().width - (MARGIN_WIDTH * 2);
        int h = 0;
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof LedgerEntryEditor) {
                // this is the floating overlay entry editor, so do not position it explicitly
                continue;
            }
            
            Point pt = children[i].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
            if (children[i] instanceof Table) {
                pt.y = composite.getParent().getClientArea().height - (h + MARGIN_HEIGHT);
                w = pt.x;
            }
            if (doLayout) {
                children[i].setBounds(MARGIN_WIDTH, h + MARGIN_HEIGHT, w, pt.y);
            }
            h += pt.y + (MARGIN_HEIGHT * 2);
        }
        LOG.debug("Layout w={},h={}", w, h);
        return new Point(w, h);
    }
}