/**
 * Created on 19-Oct-2005
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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.vc.TableMetaViewer;

/**
 * Table field editor
 * @author david.tran@transcraft.co.uk
 */
public class TableFieldEditor<T> extends FieldEditor {

    private MetaProvider<T> metaProvider;
    private Class<T> prototype;
    private PrefTableViewer viewer;
    T [] prefData;
    private static final Logger log = getLogger(TableFieldEditor.class);
    
    /**
     * @param name
     * @param labelText
     * @param parent
     */
    public TableFieldEditor(String name, String labelText,
            MetaProvider<T> provider, Class<T> prototype, Composite parent) {
        this.init(name, labelText);
        this.metaProvider = provider;
        this.prototype = prototype;
        this.createControl(parent);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
     */
    @Override
    protected void adjustForNumColumns(int numColumns) {
		((GridData)this.viewer.getTable().getLayoutData()).horizontalSpan = 1;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite, int)
     */
    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        if (this.metaProvider == null) {
            return;
        }
		GridData gd = new GridData(GridData.FILL_BOTH);
		this.getTable(parent).setLayoutData(gd);
    }
    
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem selectAllItem = new MenuItem(menu, SWT.PUSH);
        selectAllItem.setText(Messages.getString("TableFieldEditor.0")); //$NON-NLS-1$
        selectAllItem.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        selectAllItem.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                table.selectAll();
            }
        });
        MenuItem selectNoneItem = new MenuItem(menu, SWT.PUSH);
        selectNoneItem.setText(Messages.getString("TableFieldEditor.2")); //$NON-NLS-1$
        selectNoneItem.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        selectNoneItem.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                table.deselectAll();
            }
        });
        MenuItem deleteSelectedItem = new MenuItem(menu, SWT.PUSH);
        deleteSelectedItem.setText(Messages.getString("TableFieldEditor.4")); //$NON-NLS-1$
        deleteSelectedItem.setImage(ImageCache.get("trash")); //$NON-NLS-1$
        deleteSelectedItem.addSelectionListener(new SelectionAdapter() {
        	@SuppressWarnings("unchecked")
			@Override
            public void widgetSelected(SelectionEvent evt) {
                List<Object> list = Lists.newArrayList();
                TableItem [] sel = table.getSelection();
                for (int i = 0; i < sel.length; i++) {
                    list.add(sel[i].getData());
                }
                List<T> items = Lists.newArrayList(Arrays.asList(prefData));
                items.removeAll(list);
                prefData = items.toArray((T[])new Object[list.size()]);
                viewer.refresh();
            }
        });
        MenuItem addItem = new MenuItem(menu, SWT.PUSH);
        addItem.setText(Messages.getString("TableFieldEditor.6")); //$NON-NLS-1$
        addItem.setImage(ImageCache.get("plus")); //$NON-NLS-1$
        addItem.addSelectionListener(new SelectionAdapter() {
        	@SuppressWarnings("unchecked")
			@Override
            public void widgetSelected(SelectionEvent evt) {
                try {
                    List<T> list = Lists.newArrayList(Arrays.asList(prefData));
                    T obj = prototype.getConstructor().newInstance();
                    list.add(obj);
                    prefData = list.toArray((T[])new Object[list.size()]);
                    viewer.refresh();
                } catch (Exception e) {
                    log.error(String.format("getElements(%s):", prototype), e); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
        });
        table.setMenu(menu);
    }
    
    protected Table	getTable(Composite parent) {
        if (this.viewer == null) {
            this.viewer = new PrefTableViewer(parent);
            this.installContextMenu(this.viewer.getTable());
        }
        return this.viewer.getTable();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doLoad()
     */
    @Override
    protected void doLoad() {
        String value = this.getPreferenceStore().getString(this.getPreferenceName());
        this.prefData = this.metaProvider.loadFromPreference(this.prototype, value);
        this.viewer.setInput(this.prefData);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
     */
    @Override
    protected void doLoadDefault() {
        String value = this.getPreferenceStore().getDefaultString(this.getPreferenceName());
        this.prefData = this.metaProvider.loadFromPreference(this.prototype, value);
        this.viewer.setInput(""); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#doStore()
     */
    @Override
    protected void doStore() {
        this.getPreferenceStore().setValue(this.getPreferenceName(), this.metaProvider.storeToPreference(this.prefData));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
     */
    @Override
    public int getNumberOfControls() {
        return 1;
    }

    public class PrefTableViewer extends TableViewer {
        /**
         * @param parent
         */
        public PrefTableViewer(Composite parent) {
            super(parent, SWT.BORDER|SWT.MULTI|SWT.FULL_SELECTION);
            TableMetaViewer.setupTable(this, metaProvider);
            this.setContentProvider(new PrefTableContentProvider());
            this.setLabelProvider(new PrefTableLabelProvider());
            this.setCellModifier(new PrefTableCellModifier());
            Table tbl = this.getTable();
            tbl.setBackground(new Color(Display.getCurrent(), 236, 233, 216));
            tbl.setHeaderVisible(true);
            tbl.setLinesVisible(true);
            GUIUtil.setupTableAutoSized(tbl);
        }
    }
    
    public class PrefTableContentProvider implements IStructuredContentProvider {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
            return prefData;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */
        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
    
    public class PrefTableLabelProvider implements ITableLabelProvider, IColorProvider {
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(Object element, int columnIndex) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @SuppressWarnings("unchecked")
		@Override
        public String getColumnText(final Object element, int columnIndex) {
        	T model = null;
            if (element instanceof TableItem) {
                model = (T)((TableItem)element).getData();
            } else {
            	model = (T)element;
            }
            Object value = metaProvider.getValue(model, metaProvider.getColumns()[columnIndex].getName(), null);
            return value != null ? value.toString() : ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void addListener(ILabelProviderListener listener) {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
         */
        @Override
        public void dispose() {
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean isLabelProperty(Object element, String property) {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void removeListener(ILabelProviderListener listener) {
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
         */
        @Override
        public Color getBackground(Object element) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
         */
        @Override
        public Color getForeground(Object element) {
            return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
        }
    }

    public class PrefTableCellModifier implements ICellModifier {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean canModify(Object element, String property) {
            return metaProvider.getColumn(property).isEnabled();
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
         */
        @SuppressWarnings("unchecked")
		@Override
        public Object getValue(final Object element, String property) {
        	T model = null;
            if (element instanceof TableItem) {
                model = (T)((TableItem)element).getData();
            } else {
            	model = (T)element;
            }
            Object value = metaProvider.getValue(model, property, null);
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList() != null) {
                return Integer.valueOf(column.getListSelectionIndex(value));
            }
            return value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
         */
        @SuppressWarnings("unchecked")
		@Override
        public void modify(final Object element, String property, final Object value) {
            if (value == null) {
                // failed validation
                return;
            }
            T model = null;
            if (element instanceof TableItem) {
                model = (T)((TableItem)element).getData();
            } else {
            	model = (T)element;
            }
            
            Object newValue = value;
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList() != null) {
                newValue = column.getList().map(l -> l.get(Integer.parseInt(value.toString()))).orElse(null);
            }
            try {
                metaProvider.setValue(model, null, property, newValue);
            } catch (Exception e) {
                GUIUtil.showError(Messages.getString("TableFieldEditor.12"), Messages.getString("TableFieldEditor.13") + property + Messages.getString("TableFieldEditor.14") + value, e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
            viewer.refresh();
        }
    }
}
