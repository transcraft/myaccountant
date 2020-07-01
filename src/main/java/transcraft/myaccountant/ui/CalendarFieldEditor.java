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
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import com.google.common.collect.Lists;

import transcraft.myaccountant.utils.DateUtil;

/**
 * designed for embedding into the GenericMetaViewer or any form,
 * can be treated as an input field with a Calendar popup
 * 
 * @author david.tran@transcraft.co.uk
 */
public class CalendarFieldEditor extends Composite {
    private Text textControl;
    private Label popupButton;
    private Calendar value;
    private List<SelectionListener> listeners = Lists.newArrayList();
    
    public CalendarFieldEditor(Composite parent) {
        super(parent, SWT.SHADOW_IN);
        this.value = DateUtil.getTodayStart();
        this.createContents();
    }
    
    protected void	createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.horizontalSpacing = 5;
        ly.marginHeight = 0;
        ly.marginWidth = 0;
        ly.verticalSpacing = 10;
        this.setLayout(ly);

        this.textControl = new Text(this, SWT.BORDER);
        this.textControl.setLayoutData(new GridData(GridData.FILL_BOTH));
        this.textControl.setText(DateUtil.format(new Date()));
        this.setBackground(this.textControl.getBackground());
        this.popupButton = new Label(this, SWT.NONE);
        this.popupButton.setImage(ImageCache.get("calendar")); //$NON-NLS-1$
        this.popupButton.setLayoutData(new GridData(GridData.FILL_VERTICAL));
        this.popupButton.setToolTipText(Messages.getString("CalendarFieldEditor.1")); //$NON-NLS-1$
        this.popupButton.addListener(SWT.MouseDown, new Listener() {
        	@Override
            public void	handleEvent(Event evt) {
                popupCalendar();
            }
        });
        this.textControl.addFocusListener(new FocusListener() {
        	@Override
        	public void focusGained(FocusEvent evt) {
        		textControl.selectAll();
        	}

        	@Override
        	public void	focusLost(FocusEvent evt) {
        		String str = textControl.getText();
        		try {
        			if (str.length() > 0) {
        				Date dt = DateUtil.parse(textControl.getText());
        				if (! dt.equals(value.getTime())) {
        					value.setTime(dt);
        					notifyListeners();
        				}
        			} else {
        				value = null;
        			}
        		} catch (Exception e) {
        			GUIUtil.showError(Messages.getString("CalendarFieldEditor.2"), str + Messages.getString("CalendarFieldEditor.3"), e); //$NON-NLS-1$ //$NON-NLS-2$
        			textControl.setText(DateUtil.format(value.getTime()));
        		}
        	}
        });
    }
    protected void	popupCalendar() {
        if (! this.isEnabled()) {
            return;
        }
        CalendarCellDialog calPopup = new CalendarCellDialog(this.getShell());
        calPopup.setCellEditorWindow(this);
        calPopup.setValue(this.textControl.getText());
        calPopup.open();
        this.value = calPopup.getValue();
        try {
            String str = DateUtil.format(this.value.getTime());
            if (! str.equals(this.textControl.getText())) {
                this.textControl.setText(str);
                this.notifyListeners();
            }
        } catch (Exception e) {
            GUIUtil.showError(Messages.getString("CalendarFieldEditor.4"), Messages.getString("CalendarFieldEditor.5") + this.textControl.getText() + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    /**
     * @return Returns the value.
     */
    public Calendar getValue() {
        try {
            String str = this.textControl.getText();
            if (str.length() > 0) {
                Date dt = DateUtil.parse(this.textControl.getText());
                this.value.setTime(dt);
            } else {
                this.value = null;
            }
        } catch (Exception e) {
            textControl.setText(DateUtil.format(value.getTime()));
        }
        return this.value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(Calendar value) {
        this.value = value;
        if (value != null) {
            this.textControl.setText(DateUtil.format(value.getTime()));
        } else {
            this.textControl.setText(""); //$NON-NLS-1$
        }
    }
    /**
     * @param value The value to set.
     */
    public void setValue(Date value) {
        if (value != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(value);
            this.setValue(cal);
        } else {
            this.setValue((Calendar)null);
        }
    }
    /**
     * @return Returns the textControl.
     */
    public Text getTextControl() {
        return textControl;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.textControl.setEnabled(enabled);
    }
    protected void	notifyListeners() {
        Event e = new Event();
        e.button = 1;
        e.data = this.value;
        e.widget = this;
        e.display = Display.getCurrent();
        e.item = this.textControl;
        SelectionEvent evt = new SelectionEvent(e);
        for (SelectionListener listener : this.listeners) {
            listener.widgetSelected(evt);
        }
    }
    public void	addSelectionListener(SelectionListener listener) {
        if (! this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }
    public void	removeSelectionListener(SelectionListener listener) {
        if (this.listeners.contains(listener)) {
            this.listeners.remove(listener);
        }
    }
}
