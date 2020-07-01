/**
 * Created on 26-Jun-2005
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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transcraft.myaccountant.utils.DateUtil;

/**
 * @author david.tran@transcraft.co.uk
 */
public class CalendarCellDialog extends Dialog {
	private static final Logger LOG = LoggerFactory.getLogger(CalendarCellDialog.class);
	
    private Calendar value;
    private Control cellEditorWindow;
    private boolean datePicked = false;
    private Shell shell;
    private DateTime calendar;
    
    /**
     * @param parent
     */
    public CalendarCellDialog(Shell parent) {
        this(parent, SWT.PRIMARY_MODAL);
        createContents();
    }

    private void createContents() {
	    shell = new Shell(getParent(), SWT.APPLICATION_MODAL | SWT.DIALOG_TRIM);
	    shell.setLayout(new FillLayout());
	    calendar = new DateTime(shell, SWT.CALENDAR);
    }
    
    /**
     * @param parent
     * @param style
     */
    public CalendarCellDialog(Shell parent, int style) {
        super(parent, style);
        this.value = Calendar.getInstance();
    }
    
    public void	setValue(Calendar value) {
        this.value = value;
    }
    
    public void	setValue(Date value) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(value);
        this.value = cal;
    }
    
    public void	setValue(int year, int month, int day) {
    	LOG.debug("setValue(y={},m={},d={}", year, month, day);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        this.value = cal;
    }
    
    public void	setValue(String value){
        try {
            Date dt = DateUtil.parse(value);
            this.setValue(dt);
        } catch (Exception e) {}
    }
    
    public Calendar	getValue() {
        return this.value;
    }
    
    public void setCellEditorWindow(Control cellEditorWindow) {
        this.cellEditorWindow = cellEditorWindow;
    }
	
    public boolean isOpened() {
    	return shell != null && !shell.isDisposed() && shell.getVisible();
    }
    
    public void close() {
    	if (shell != null && !shell.isDisposed() && shell.isVisible()) {
    		shell.close();
    	}
    }
    
    public Object open() {
	    this.datePicked = false;
	    if (shell != null && shell.isDisposed()) {
	    	createContents();
	    }
	    
	    calendar.setDate(value.get(Calendar.YEAR), value.get(Calendar.MONTH), value.get(Calendar.DAY_OF_MONTH));
	    calendar.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
	            setValue(calendar.getYear(), calendar.getMonth(), calendar.getDay());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
	            setValue(calendar.getYear(), calendar.getMonth(), calendar.getDay());
	            datePicked = true;
			}
		});
	    
	    shell.pack();
	    shell.open();
	    if (this.cellEditorWindow != null) {
	        Rectangle cellRect = this.cellEditorWindow.getBounds();
	        cellRect.x = 0; cellRect.y = 0;
	        Rectangle rect = Display.getCurrent().map(this.cellEditorWindow, null, cellRect);
	        shell.setLocation(rect.x, rect.y + rect.height);
	    }

	    calendar.setFocus();
	    Display display = getParent().getDisplay();
	    while (! shell.isDisposed()) {
	        if (!display.readAndDispatch()) {
	            display.sleep();
	        }
	        if (this.datePicked) {
	        	shell.close();
	        }
	    }
	    return this.value;
	}
}
