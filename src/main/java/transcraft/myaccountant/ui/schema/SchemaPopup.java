/**
 * Created on 15-Oct-2005
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
package transcraft.myaccountant.ui.schema;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.db4o.ObjectContainer;

import transcraft.myaccountant.ui.ImageCache;

/**
 * @author david.tran@transcraft.co.uk
 */
public class SchemaPopup extends Composite {

    Shell popup;
    ObjectContainer container;
    Object model;
    @SuppressWarnings("unused")
	private SchemaViewer viewer;
    
    /**
     * 
     * @param parent
     * @param container
     * @param data
     */
    public SchemaPopup(Shell parent, ObjectContainer container, Object data) {
        super(parent, SWT.FLAT);
        this.container = container;
        this.model = data;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Shell#open()
     */
    public void open() {
        this.popup = new Shell(Display.getCurrent(), SWT.SHELL_TRIM);
        this.popup.setImage(ImageCache.get("logo")); //$NON-NLS-1$
        this.popup.setText(Messages.getString("SchemaPopup.1")); //$NON-NLS-1$
        this.popup.setLayout(new FillLayout());
        viewer = new SchemaViewer(this.popup, this.container, this.model);
        this.popup.setSize(1024, 800);
        this.popup.open();
    }
    public void open(ObjectContainer container) {
        this.container = container;
        this.open();
    }
}
