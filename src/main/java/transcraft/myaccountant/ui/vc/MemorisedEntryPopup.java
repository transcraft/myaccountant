/**
 * Created on 08-Jul-2005
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

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transcraft.myaccountant.ui.TableRowCellEditor;

/**
 * @author david.tran@transcraft.co.uk
 */
public class MemorisedEntryPopup implements KeyListener, TraverseListener {
	private static final Logger LOG = LoggerFactory.getLogger(MemorisedEntryPopup.class);
	
    private TableRowCellEditor<?> cellEditorWindow;
    private Object value;
    private java.util.List<String> list;
    private Shell popup;
    private List listControl;
    
    /**
     * @param parent
     */
    public MemorisedEntryPopup(TableRowCellEditor<?> parent) {
        this.cellEditorWindow = parent;
        this.createContents();
    }
    
    protected void createContents() {
        if (this.popup != null && ! this.popup.isDisposed()) {
            return;
        }
	    Shell shell = this.cellEditorWindow.getShell();
	    this.popup = new Shell(shell, SWT.MODELESS|SWT.NO_TRIM|SWT.ON_TOP);
	    this.popup.setLayout(new FillLayout());
        this.listControl = new List(this.popup, SWT.V_SCROLL);
        this.listControl.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (cellEditorWindow != null && popup.isVisible()) {
                	if (listControl.getSelectionCount() > 0) {
                		String selected = listControl.getSelection()[0];
                		if (selected.contains(cellEditorWindow.getText(true))) {
                			cellEditorWindow.updateValueFromPopup(selected, 0);
                		}
                		//cellEditorWindow.getTextControl().forceFocus();
                	}
                }
            }
        });
        if (this.list != null) {
            this.setList(this.list);
        }
        this.listControl.addKeyListener(this);
        this.listControl.addTraverseListener(this);
    }
    
    public String advance(int advanceCount) {
    	int idx = this.listControl.getSelectionIndex();
    	if (idx >= 0) {
        	idx += advanceCount;
        	idx = Math.max(idx, 0);
        	idx %= this.listControl.getItemCount();
        	this.listControl.select(idx);
        	this.listControl.showSelection();
        	return this.listControl.getItem(idx);
    	}
    	return null;
    }
    
    public String	match(String str, int advanceCount) {
        if (isNotEmpty(this.list)) {
        	String token = str.substring(0, str.length() + advanceCount);
        	try {
	        	LOG.debug("match('{}',{})", token, advanceCount);
		        for (int i = 0; i < this.list.size(); i++) {
		            String item = this.list.get(i);
		            if (item.toLowerCase().startsWith(token.toLowerCase())) {
	                    this.listControl.select(i);
	                    this.listControl.showSelection();
	                    return item;
		            }
		        }
        	} catch (Exception e) {
	        	LOG.error(String.format("match('%s',%d)", str, advanceCount), e);
        	}
        }
        LOG.debug("Nothing matched '{}' in list, deselectAll", str);
        this.listControl.deselectAll();
        return str;
    }
    
    public void	setList(java.util.List<String> list) {
        this.list = list;
        this.listControl.removeAll();
        int i = 0;
        boolean found = false;
        for (String item : this.list) {
            this.listControl.add(item);
            if (value != null && item.equals(this.value)) {
                listControl.select(i);
                listControl.showSelection();
                found = true;
            }
        }
        if (!found) {
            listControl.deselectAll();
        }
    }
    public void	setValue(Object value){
        this.createContents();
        this.value = value;
        if (this.list != null) {
	        int idx = this.list.indexOf(value);
	        if (idx >= 0) {
	            this.listControl.select(idx);
	            this.listControl.showSelection();
	        } else {
	        	this.listControl.deselectAll();
            	LOG.debug("setValue({}) not found", value);
	        }
        }
    }
    public Object	getValue() {
        return this.value;
    }
    /**
     * @return Returns the popup.
     */
    public Shell getPopup() {
        return popup;
    }

    /**
     * @return Returns the listControl.
     */
    public List getListControl() {
        return listControl;
    }
	public void open() {
	    this.createContents();
        Rectangle cellRect = this.cellEditorWindow.getBounds();
        cellRect.x = 0; cellRect.y = 0;
        Rectangle rect = Display.getCurrent().map(this.cellEditorWindow, null, cellRect);
        this.popup.setLocation(rect.x, rect.y + rect.height);
        this.popup.setSize(rect.width, 90);
        this.popup.open();
    }
	
	public void close() {
	    if (this.popup != null && ! this.popup.isDisposed()) {
	        this.popup.setVisible(false);
	    }
	}
	public boolean isVisible() {
	    return this.popup != null && ! this.popup.isDisposed() && this.popup.isVisible();
	}
    public void	keyPressed(KeyEvent evt) {
    }
    public void	keyReleased(KeyEvent evt) {
        switch(evt.keyCode) {
        case SWT.TAB:
        	break;
        case SWT.ARROW_LEFT:
        case SWT.ARROW_RIGHT:
        case SWT.BS:
        	{
        		String str = cellEditorWindow.getText(false);
        		cellEditorWindow.setText(str.substring(0,str.length()));
        	}
        	break;
        case SWT.ESC:
        	cellEditorWindow.processEscape();
        	break;
        default:
        	{
        		if (!Character.isISOControl(evt.character)) {
                    cellEditorWindow.insert("" + evt.character);
        		}
        	}
        }
        if (evt.keyCode != SWT.TAB) {
        	cellEditorWindow.keyReleased(evt);        	
        }
        evt.doit = false;
    }

	@Override
	public void keyTraversed(TraverseEvent evt) {
        switch(evt.keyCode) {
        case SWT.TAB:
        	cellEditorWindow.keyTraversed(evt);
        	break;
        }
        evt.doit = false;
	}

	@Override
	public String toString() {
		return this.cellEditorWindow.toString() + "." + this.getClass().getSimpleName();
	}
    
}
