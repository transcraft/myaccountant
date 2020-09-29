/**
 * Created on 23-Jun-2020
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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;

/**
 * floating editor for a single ledger entry
 * 
 * @author david.tran@transcraft.co.uk
 */
public abstract class TableRowEditor<T extends AllocationRule> extends Composite {
	static private final Logger LOG = getLogger(TableRowEditor.class);
	
    private final Map<MetaColumn<?>, TableRowCellEditor<T>> editors = Maps.newHashMap();

    protected final Table table;
    protected final BOMService bomService;
    protected final MetaProvider<T> metaProvider;

    private TableItem item;
    private T model;
    private boolean modified = false;
    
    /**
     * @param parentShell
     */
    public TableRowEditor(Composite parent, Table table, BOMService bomService, MetaProvider<T> metaProvider) {
        super(parent, SWT.NONE);
        this.bomService = bomService;
        this.metaProvider = metaProvider;
        this.table = table;
        this.moveAbove(this.table);
	    this.setVisible(false);
	    this.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	    
	    // disable ourselves if our parent changes shape
	    parent.addControlListener(new ControlAdapter() {
        	@Override
	        public void controlMoved(ControlEvent e) {
	            setVisible(false);
	        }
	    });
	    
	    /*
	     * hook in event to bring up this editor whenever the table is clicked on
	     */
        table.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                if (TableRowEditor.this.isVisible()) {
                    loadModelForTableItem((TableItem)evt.item);
                }
            }

        	@Override
        	public void	widgetDefaultSelected(SelectionEvent evt) {
                loadModelForTableItem((TableItem)evt.item);
            }
        });
        
        /* remove ourselves whenever user scrolls */
        ScrollBar sb = table.getVerticalBar();
        sb.addListener(SWT.Selection, new Listener() {
        	@Override
        	public void	handleEvent(Event evt) {
        		if (isVisible()) {
        			processEscape();
        		}
        	}
        });
    }
    
    protected TableItem getTableItem() {
    	return this.item;
    }
    
    public T getModel() {
    	return this.model;
    }
    
    @Override
    public void setVisible(boolean flag) {
        if (! flag) {
            super.setVisible(flag);
            return;
        }
        if (super.isVisible()) {
            return;
        }
        this.doInternalLayout();
	    super.setVisible(flag);
    }
    
    /**
     * lay out the editors for the row to exactly overlay the row
     *
     */
    protected void	doInternalLayout() {
        if (this.item == null) {
            return;
        }
        
        int xoffset = this.item.getBounds(0).x;
        MetaColumn<?> [] metaColumns = metaProvider.getColumns();
        for (int i = 0; i < metaColumns.length; i++) {
	        TableRowCellEditor<?> editor = (TableRowCellEditor<?>)this.editors.get(metaColumns[i]);
	        if (editor == null) {
	            continue;
	        }
	        Rectangle rect = this.item.getBounds(i);
	        rect.y = 1;
	        rect.x -= xoffset;
	        editor.setBounds(rect);
	    }
        // overlay the row with this control
        Rectangle firstRect = this.item.getBounds(0);
        Rectangle lastRect = this.item.getBounds(table.getColumnCount() - 1);
        Rectangle tableRect = table.getBounds();
        firstRect.x += tableRect.x;
        firstRect.y += tableRect.y;
        if (System.getProperty("os.name").toLowerCase().indexOf("win") < 0) {
        	// TODO temporary hack for Linux, where the y value is always one row out
        	firstRect.y += firstRect.height + 2;
        }
        this.setBounds(firstRect.x + 2, firstRect.y + 2, 6 + lastRect.x + lastRect.width - firstRect.x, this.table.getItemHeight() + 1);
        
        Control [] items = this.getTabList();
    	if (items[0] instanceof TableRowCellEditor) {
    		TableRowCellEditor<?> ce = (TableRowCellEditor<?>)items[0];
    		ce.selectAll();
    		ce.setFocus();
    	}
    }
    
    public void	loadModelForTableItem(TableItem item) {
        if (item == null) {
            this.setVisible(false);
            return;
        }
        
        Rectangle rect = item.getBounds(0);
        LOG.debug("loadTableItem(x={},y={},w={},h={})", rect.x, rect.y, rect.width, rect.height);
        this.loadModelForTableItem(rect.x, rect.y + (rect.height / 2));
    }
    
    @SuppressWarnings("unchecked")
	protected void	loadModelForTableItem(int x, int y) {
        Control [] items = this.getTabList();
        for (int i = 0; i < items.length; i++) {
        	if (items[i] instanceof TableRowCellEditor) {
        		TableRowCellEditor<?> ce = (TableRowCellEditor<?>)items[i];
                ce.deactivate();
                ce.modifyCell();
                ce.dismissPopup();
        	}
        }
    	
        if (this.table == null) {
            this.setVisible(false);
            return;
        }
        
        this.item = table.getItem(new Point(x, y));
        if (this.item == null) {
            this.setVisible(false);
            return;
        }

        this.model = (T)this.item.getData();
        
        TableRowCellEditor<T> selectedEditor = null;
        MetaColumn<?> [] metaColumns = metaProvider.getColumns();
        for (int i = 0; i < metaColumns.length; i++) {
            TableRowCellEditor<T> editor = this.editors.get(metaColumns[i]);
            if (editor == null) {
            	try {
            		editor = makeCellEditor(metaColumns[i]);
            		this.editors.put(metaColumns[i], editor);
            	} catch (Exception e) {
            		LOG.error(metaColumns[i].getName(), e);
            	}
            }
            editor.deactivate();
            editor.setEnabled(metaColumns[i].isEnabled());
            if (editor.isEnabled()) {
                editor.activate();
                Rectangle rect = item.getBounds(i);
	            if (rect.contains(x, y)) {
	                selectedEditor = editor;
	            }
            }
        }
        
        this.refreshEditorValues();
        this.setModified(false);
        this.setVisible(true);
        this.doInternalLayout();
        if (selectedEditor != null) {
            selectedEditor.showMemorisedPopup();
        }
    }
    
    /**
     * invoked when user moves the row editor to a new row i.e. when the underlying
     * row data is changed
     */
    public abstract void	refreshEditorValues();
    
    protected abstract void	saveRowData();
    
    protected abstract TableRowCellEditor<T> makeCellEditor(MetaColumn<?> metaColumn);
    
    /**
     * 
     * @return a string used to apply as the selector field for the {@link MetaProvider#getValue(Object, String, String)}
     */
    protected abstract String getMetaSelector();
    
    protected TableRowCellEditor<?> getCellEditor(MetaColumn<?> column) {
    	return editors.get(column);
    }
    
    public void	processEscape() {
        ((TableRowCellEditor<?>)this.editors.values().iterator().next()).processEscape();
    }
    
    /**
     * @return Returns the modified.
     */
    public boolean isModified() {
        return this.modified;
    }
    
    /**
     * @param modified The modified to set.
     */
    protected void setModified(boolean modified) {
        this.modified = modified;
    }
}
