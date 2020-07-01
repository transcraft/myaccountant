/**
 * Created on 15-Jul-2005
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

import java.util.Date;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.CalendarCellEditor;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.action.ActionableTarget;

/**
 * Generic table based viewer driven by a MetaProvider
 * 
 * @author david.tran@transcraft.co.uk
 */
public abstract class TableMetaViewer<T,M> extends ActionableTarget<T> {
    
    protected BOMService bomService;
    protected MetaProvider<M> metaProvider;
    protected Object model;
    protected TableColumn sortByColumn;
    
    /**
     * @param parent
     * @param obj
     * @param provider
     */
    public TableMetaViewer(Composite parent, BOMService bomService, MetaProvider<M> provider, Object model) {
        super(parent, SWT.NONE);
        this.bomService = bomService;
        this.metaProvider = provider;
        this.model = model;
    }

    /**
     * set up the Table control using the MetaColumn's from the MetaProvider provided
     * 
     * @param table
     */
    protected void	setupTable(final TableViewer tableViewer) {
        setupTable(tableViewer, this.metaProvider);
        final Table table = tableViewer.getTable();
        for (int i = 0; i < table.getColumns().length; i++) {
            final TableColumn tc = table.getColumn(i);
            tc.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void widgetSelected(SelectionEvent evt) {
                    if (! metaProvider.getColumnForTitle(tc.getText()).isSortable()) {
                        return;
                    }
                    sortByColumn = tc;
                    tc.setImage(ImageCache.get("sorted")); //$NON-NLS-1$
                    for (int j = 0; j < table.getColumns().length; j++) {
                        if (table.getColumn(j) != tc) {
                            table.getColumn(j).setImage(null);
                        }
                    }
                    tableViewer.refresh();
                }
            });
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            for (int i = 0; i < this.getChildren().length; i++) {
                if (this.getChildren()[i] instanceof Table) {
                    this.getChildren()[i].setFocus();
                    break;
                }
            }
        }
    }
    
    public static void	setupTable(TableViewer tableViewer, MetaProvider<?> metaProvider) {
        final Table table = tableViewer.getTable();
        MetaColumn<?> [] metaColumns = metaProvider.getColumns();
        CellEditor [] editors = new CellEditor[metaColumns.length];
        String [] props = new String[metaColumns.length];
        for (int i = 0; i < metaColumns.length; i++) {
            TableColumn tc = new TableColumn(table, metaColumns[i].getAlignment());
            if (! metaColumns[i].isVisible()) {
                // hide invisible column, we can not remove it or not create it because
                // the TableColumns have to match with MetaColumns
                tc.setWidth(0);
                tc.setResizable(false);
            } else {
                tc.setWidth(metaColumns[i].getWidth());
                tc.setResizable(true);
            }
            tc.setText(metaColumns[i].getTitle());
            tc.setData(metaColumns[i].getName());
            CellEditor editor = null;
            if (metaColumns[i].isEnabled()) {
                if (metaColumns[i].getList() != null) {
                    // Java 8 compatibility mode, so String[]::new is not available
                    String [] items = metaColumns[i].getList().map(l -> l.toArray(new String[l.size()])).orElse(new String[0]);
                    editor = new ComboBoxCellEditor(table, items, metaColumns[i].getStyle());
                    final CCombo combo = (CCombo)editor.getControl();
                    combo.addKeyListener(new KeyAdapter() {
                    	@Override
                        public void	keyReleased(KeyEvent evt) {
                            Point pt = combo.getSelection();
                            String str = combo.getText().substring(0, pt.x).toLowerCase();
                            for (int i = 0; i < combo.getItemCount(); i++) {
                                if (combo.getItem(i).toLowerCase().startsWith(str)) {
                                    combo.select(i);
                                    //combo.setText(combo.getItem(i));
                                    combo.setSelection(pt);
                                    return;
                                }
                            }
                            combo.deselectAll();
                            combo.setSelection(pt);
                        }
                    });

                } else if (metaColumns[i].getPrototype().equals(Date.class)) {
                    editor = new CalendarCellEditor(table);
                } else {
                    editor = new TextCellEditor(table);
                }
                editor.getControl().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
            }
            editors[i] = editor;
            props[i] = metaColumns[i].getName();
        }
        tableViewer.setColumnProperties(props);
        tableViewer.setCellEditors(editors);
    }
}
