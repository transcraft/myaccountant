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

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ColorFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * manages preferences for accounting period
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LookFeelPreferencePage extends BasePreferencePage {

    /**
     *
     */
    public LookFeelPreferencePage() {
        super(Messages.getString("LookFeelPreferencePage.0"), ImageDescriptor.createFromFile(LookFeelPreferencePage.class, //$NON-NLS-1$
        "/images/gui.gif"), FieldEditorPreferencePage.GRID); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
     */
    protected void createFieldEditors() {
        ColorFieldEditor treeBgEditor = new ColorFieldEditor(PreferenceFactory.TREE_BG_COLOR, Messages.getString("LookFeelPreferencePage.2"), //$NON-NLS-1$
                this.getFieldEditorParent());
        this.addField(treeBgEditor);
        BooleanFieldEditor exitAppEditor = new BooleanFieldEditor(PreferenceFactory.EXIT_APP_CONFIRMED, Messages.getString("LookFeelPreferencePage.3"), //$NON-NLS-1$
                this.getFieldEditorParent());
        this.addField(exitAppEditor);
        // still can not get the font set here to propagate to the whole app !!
        //FontFieldEditor defFontEditor = new FontFieldEditor(PreferenceFactory.DEF_FONT, "Default application font", this.getFieldEditorParent());
        //this.addField(defFontEditor);
    }
}
