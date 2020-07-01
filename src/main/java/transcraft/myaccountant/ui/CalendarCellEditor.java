/**
 * Created on 25-Jun-2005
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
package transcraft.myaccountant.ui;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import transcraft.myaccountant.utils.DateUtil;

/**
 * Calendar popup editor, for embedding as a TableCellEditor. To
 * embed as a generic inout field, use CalendarFieldEditor instead
 * 
 * @author david.tran@transcraft.co.uk
 */
public class CalendarCellEditor extends DialogCellEditor {
	
    public CalendarCellEditor(Composite parent) {
        super(parent, SWT.NONE);
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DialogCellEditor#openDialogBox(org.eclipse.swt.widgets.Control)
     */
    protected Object openDialogBox(Control cellEditorWindow) {
    	CalendarCellDialog dialog = new CalendarCellDialog(cellEditorWindow.getShell());
    	Object value = getValue();
    	if (value != null) {
            try {
                Date dt = DateUtil.parse(value);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dt);
        		dialog.setValue(cal);
            } catch (Exception e) {}
    	}
    	dialog.setCellEditorWindow(cellEditorWindow);
    	value = dialog.open();
    	return value;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DialogCellEditor#createButton(org.eclipse.swt.widgets.Composite)
     */
    protected Button createButton(Composite parent) {
        Button button = new Button(parent, SWT.FLAT);
        button.setImage(ImageCache.get("calendar")); //$NON-NLS-1$
        return button;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.DialogCellEditor#updateContents(java.lang.Object)
     */
    protected void updateContents(Object value) {
        if (value != null && (value instanceof Calendar)) {
            String str = DateUtil.format(((Calendar)value).getTime());
            super.updateContents(str);
        } else {
            super.updateContents(value);
        }
    }
}

