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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.AllocationRuleMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AllocationRuleViewer extends TableMetaViewer<AllocationRule, Allocation> 
	implements BOMListener, ICellEditorValidator {
	private static final Logger LOG = LoggerFactory.getLogger(AllocationRuleViewer.class);
    
    private CheckboxTableViewer tableViewer;
    private AllocationRule allocationRule;
    private MenuItem miDel;
    private Label title;
    
    // for stand alone mode. In embedded mode, this value should be set to false
    private boolean autoSave = true;
    
    /**
     * @param parent
     * @param obj
     * @param provider
     */
    public AllocationRuleViewer(Composite parent, BOMService bomService, MetaProvider<Allocation> provider,
    		AllocationRule rule) {
        super(parent, bomService, provider, rule);
        this.createContents();
    }
    
	protected void	createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 1;
        ly.verticalSpacing = 10;
        this.setLayout(ly);

        this.title = new Label(this, SWT.SHADOW_IN);
        title.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));

        this.tableViewer = CheckboxTableViewer.newCheckList(this, SWT.BORDER|SWT.FULL_SELECTION);
        this.tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final Table table = this.tableViewer.getTable();
        table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // set up the Table control using the MetaProvider
        this.setupTable(this.tableViewer);

        // set up validate for allocation percentage
        for (int i = 0; i < this.metaProvider.getColumns().length; i++) {
            if (this.tableViewer.getCellEditors()[i] != null) {
                this.tableViewer.getCellEditors()[i].setValidator(this);
            }
        }
        
        this.installContextMenu(table);
        
        // load the list for the Account dropdown
        this.dataChanged(null);
        this.bomService.addListener(this);
        GUIUtil.setupTableAutoSized(table);

        this.tableViewer.setContentProvider(new AllocationRuleContentProvider());
        this.tableViewer.setLabelProvider(new AllocationRuleLabelProvider());
        this.tableViewer.setCellModifier(new AllocationRuleCellModifier());
        this.setRule((AllocationRule)this.model);
        
        this.tableViewer.refresh();
    }
	
    /**
     * @return Returns the tableViewer.
     */
    public TableViewer getTableViewer() {
        return tableViewer;
    }
    
    /**
     * implements the ICellEditorValidator
     * 
     * @param obj
     */
    public String isValid(Object obj) {
        return null;
        //return (this.allocationRule != null && this.allocationRule.getTotalAllocation() <= 1) ? null : "No more allocation possible";
    }
    
    /**
     * @return Returns the allocationRule.
     */
    AllocationRule getAllocationRule() {
        return allocationRule;
    }
    
    /**
     * @return Returns the autoSave.
     */
    protected boolean isAutoSave() {
        return autoSave;
    }
    
    /**
     * @param autoSave The autoSave to set.
     */
    public void setAutoSave(boolean autoSave) {
        this.autoSave = autoSave;
    }
    
    /**
     * @return Returns the rule.
     */
    public AllocationRule getRule() {
        return this.allocationRule;
    }
    
    /**
     * @param rule The rule to set.
     */
    public void setRule(AllocationRule rule) {
        if (rule != null) {
            if (this.allocationRule == null) {
                this.allocationRule = new AllocationRule(rule);
            } else {
                this.allocationRule.copyRule(rule);
            }
            this.title.setText(this.allocationRule instanceof Entry ? Messages.getString("AllocationRuleViewer.0") + ((Entry)this.allocationRule).getAmount() : //$NON-NLS-1$
                Messages.getString("AllocationRuleViewer.1") + this.allocationRule.getName()); //$NON-NLS-1$
        }
        this.tableViewer.setInput(this.allocationRule.getAllocations(true));
        this.tableViewer.getTable().setFocus();
    }
    
    public void	reset() {
        this.tableViewer.getTable().clearAll();
        this.allocationRule.reset();
        if (isAutoSave()) {
            // auto save this value to the DB. In embedded mode leave it to the parent to save
            this.bomService.storeGeneric(this.allocationRule, AllocationRuleMeta.NAME_PROP, allocationRule.getName());
        }
        this.tableViewer.setInput(this.allocationRule);
    }
    
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("AllocationRuleViewer.2")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
           public void widgetSelected(SelectionEvent evt) {
               tableViewer.setAllChecked(true);
           }
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("AllocationRuleViewer.4")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.setAllChecked(false);
            }
         });
        miDel = new MenuItem(menu, SWT.PUSH);
        miDel.setText(Messages.getString("AllocationRuleViewer.6")); //$NON-NLS-1$
        miDel.setImage(ImageCache.get("trash")); //$NON-NLS-1$
        miDel.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                Object [] selectedAllocs = tableViewer.getCheckedElements();
                for (int i = 0; i < selectedAllocs.length; i++) {
                    Allocation alloc = (Allocation)selectedAllocs[i];
                    if (alloc.getAccount().trim().length() != 0) {
                        allocationRule.removeAllocation(alloc.getAccount(), alloc.getPercentage());
                    }
                }
                if (isAutoSave()) {
	                if (allocationRule instanceof Entry) {
	                    bomService.store(new Entry((Entry)allocationRule));
	                } else {
	                    bomService.storeGeneric(allocationRule, AllocationRuleMeta.NAME_PROP, allocationRule.getName());
	                }
                }
                tableViewer.setInput(allocationRule);
            }
        });
        table.setMenu(menu);
        table.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseDown(MouseEvent evt) {
                switch (evt.button) {
                case 3:
                    miDel.setEnabled(tableViewer.getCheckedElements().length > 0);
                    break;
                }
            }
        });
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    @Override
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.entity instanceof Account) {
            CellEditor [] editors = this.tableViewer.getCellEditors();
            List<String> list = this.bomService.getMemorisedList(Messages.getString("AllocationRuleViewer.8")); //$NON-NLS-1$
            List<LedgerAccount> ledgerAccounts = this.bomService.getRefData(LedgerAccount.class);
            for (LedgerAccount la : ledgerAccounts) {
                if (! list.contains(la.getReference())) {
                    list.add(la.getReference());
                }
            }
            Collections.sort(list);
            for (int i = 0; i < editors.length; i++) {
                if ((editors[i] instanceof ComboBoxCellEditor) &&
                    this.metaProvider.getColumns()[i].getName().indexOf(Messages.getString("AllocationRuleViewer.9")) >= 0) { //$NON-NLS-1$
                    ComboBoxCellEditor editor = (ComboBoxCellEditor)editors[0];
                    this.metaProvider.getColumns()[i].setList(list);
                    // Java 8 compatibility mode, so String[]::new is not available
                    editor.setItems(list.toArray(new String[list.size()]));
                }
            }
        }
    }
    
    public class AllocationRuleContentProvider implements IStructuredContentProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
        	if (inputElement instanceof Allocation[]) {
                Object [] allocs = (Allocation[])inputElement;
                return allocs;
        	}
        	return new Allocation[0];
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
    
    public class AllocationRuleLabelProvider implements ITableLabelProvider, IColorProvider {
        private ArrayList<ILabelProviderListener> listeners = Lists.newArrayList();

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
            return null;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
         */
        @Override
        public Image getColumnImage(final Object element, int columnIndex) {
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
         */
        @Override
        public String getColumnText(final Object element, int columnIndex) {
        	Allocation alloc = null;
            if (element instanceof TableItem) {
                alloc = (Allocation)(((TableItem)element).getData());
            } else {
            	alloc = (Allocation)element;
            }
            Object value = metaProvider.getValue(alloc, metaProvider.getColumns()[columnIndex].getName(), null);
            return value != null ? value.toString() : ""; //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void addListener(ILabelProviderListener listener) {
            this.listeners.add(listener);
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
            MetaColumn<?> [] columns = metaProvider.getColumns();
            for (int i = 0; i < columns.length; i++) {
                if (columns[i].getName().equals(property)) {
                    return ! columns[i].isEnabled();
                }
            }
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
         */
        @Override
        public void removeListener(ILabelProviderListener listener) {
            this.listeners.remove(listener);
        }
    }

    public class AllocationRuleCellModifier implements ICellModifier {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object, java.lang.String)
         */
        @Override
        public boolean canModify(Object element, String property) {
            return true;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object, java.lang.String)
         */
        @Override
        public Object getValue(final Object element, String property) {
        	Allocation alloc = null;
            if (element instanceof TableItem) {
                alloc = (Allocation)((TableItem)element).getData();
            } else {
            	alloc = (Allocation)element;
            }
            Object value = metaProvider.getValue(alloc, property, null);
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList().isPresent()) {
                return Integer.valueOf(column.getListSelectionIndex(value));
            }
            return value;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object, java.lang.String, java.lang.Object)
         */
        @Override
        public void modify(final Object element, String property, final Object value) {
        	LOG.debug("modify({},{},{})", element, property, value); //$NON-NLS-1$
            if (value == null) {
                // failed validation
                return;
            }
            
            Allocation alloc = null;
            if (element instanceof TableItem) {
                alloc = (Allocation)((TableItem)element).getData();
            } else {
            	alloc = (Allocation)element;
            }
            if (element == null) {
            	LOG.debug("Can not find allocation for {}=>{}", property, value); //$NON-NLS-1$
            	return;
            }
            if (! Arrays.asList(allocationRule.getAllocations(false)).contains(alloc)) {
                if (alloc.getAccount() != null && alloc.getAccount().length() > 0) {
                    // allow the blank entry through, everything else must have been 
                    // because the Viewer is no longer in synch with the Model
                	LOG.debug("%s does not contain {}", allocationRule, element); //$NON-NLS-1$
                    return;
                }
            }
            
            Object newValue = value;
            MetaColumn<?> column = metaProvider.getColumn(property);
            if (column != null && column.getList().isPresent()) {
                if (value.toString().equals("-1")) { //$NON-NLS-1$
                    /*
                     * this looks like a bug in ComboBoxCellEditor, which seems to not
                     * cater for the scenario where the underlying CCombo is editable at all i.e.
                     * the value typed in by the user does not exist in the dropdown, so
                     * a -1 is always returned for a pretty valid value !
                     * We are doing a work around here and go directly to the CCombo to grab the
                     * value ourselves
                     */
                    Object [] props = tableViewer.getColumnProperties();
                    for (int i = 0; i < props.length; i++) {
                        if (props[i].equals(property)) {
                            CellEditor editor = tableViewer.getCellEditors()[i];
                            if (editor.getControl() instanceof CCombo) {
                                newValue = ((CCombo)editor.getControl()).getText();
                            }
                            break;
                        }
                    }
                } else {
                    Object str = column.getList()
                    		.map(l -> l.get(Integer.parseInt(value.toString())))
                    		.orElse(null);
                    LOG.debug("{}:{}=>{}",  property, value, str); //$NON-NLS-1$
                    newValue = str;
                }
            }
            
            try {
                metaProvider.setValue(alloc, null, property, newValue);
                LOG.debug("Adding {}", alloc); //$NON-NLS-1$
                allocationRule.addAllocation(alloc);
                if (isAutoSave()) {
                    // auto save this value to the DB. In embedded mode leave it to the parent to save
                    bomService.storeGeneric(allocationRule, AllocationRuleMeta.NAME_PROP, allocationRule.getName());
                }
                
                /*
                 * call setInput(), passing the allocations with a dummy row (if one does not exist)
                 */
                Allocation[] allocs = allocationRule.getAllocations(true);
                LOG.debug("modify({},{})=>{}", property, newValue, Arrays.asList(allocs)); //$NON-NLS-1$
                AllocationRuleViewer.this.tableViewer.setInput(allocs);
                AllocationRuleViewer.this.tableViewer.getTable().setFocus();
            } catch (Exception e) {
            	LOG.error(String.format("modify(%s,%s,%s)", element, property, value), e); //$NON-NLS-1$
                GUIUtil.showError(Messages.getString("AllocationRuleViewer.12"), 
                		Messages.getString("AllocationRuleViewer.13") + property + Messages.getString("AllocationRuleViewer.14") + value, e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }
        }
    }
}
