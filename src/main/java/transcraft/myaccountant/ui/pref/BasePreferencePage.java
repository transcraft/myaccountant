/**
 * Created on 09-Oct-2005
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
package transcraft.myaccountant.ui.pref;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The underlying FieldEditorPreferencePage does not expose the ImageDescriptor
 * for us to draw the nodes PreferenceNode with, for some reason
 * @author david.tran@transcraft.co.uk
 */
public abstract class BasePreferencePage extends FieldEditorPreferencePage {

    private ImageDescriptor imageDescriptor;
    /**
     * @param style
     */
    public BasePreferencePage(int style) {
        super(style);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param title
     * @param style
     */
    public BasePreferencePage(String title, int style) {
        super(title, style);
        // TODO Auto-generated constructor stub
    }

    /**
     * @param title
     * @param image
     * @param style
     */
    public BasePreferencePage(String title, ImageDescriptor image, int style) {
        super(title, image, style);
        this.imageDescriptor = image;
        // TODO Auto-generated constructor stub
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected abstract void createFieldEditors();

    /**
     * @return Returns the imageDescriptor.
     */
    public ImageDescriptor getImageDescriptor() {
        return imageDescriptor;
    }
}
