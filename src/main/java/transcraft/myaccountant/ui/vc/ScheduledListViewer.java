/**
 * Created on 08-Aug-2005
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

import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
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

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMHelper;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.meta.ScheduledListMeta;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.utils.DateUtil;

/**
 * table viewer of Scheduled Entries
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ScheduledListViewer extends TableMetaViewer<ScheduledEntry, ScheduledEntry>
	implements BOMListener, KeyListener {

    private TableViewer tableViewer;
    private MenuItem miPay;

    /**
     * @param parent
     * @param bomService
     * @param provider
     * @param data
     */
    public ScheduledListViewer(Composite parent, BOMService bomService,
            MetaProvider<ScheduledEntry> provider, String model) {
        super(parent, bomService, provider, model);
        this.createContents();
    }
    
	protected void	createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 1;
        ly.verticalSpacing = 10;
        this.setLayout(ly);

        Label title = new Label(this, SWT.SHADOW_IN);
        title.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
        title.setFont(GUIUtil.titleFont);
        title.setText(Messages.getString("ScheduledListViewer.0")); //$NON-NLS-1$

        this.tableViewer = new TableViewer(this, SWT.BORDER|SWT.FULL_SELECTION|SWT.MULTI);
        this.tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        
        final Table table = this.tableViewer.getTable();
        table.setBackground(new Color(this.getDisplay(), 236, 233, 216));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // set up the Table control using the MetaProvider
        this.setupTable(this.tableViewer);

        this.tableViewer.setContentProvider(new ScheduledListContentProvider());
        this.tableViewer.setLabelProvider(new ScheduledListLabelProvider());
        this.installContextMenu(table);
        
        this.tableViewer.setInput(this.model);

        // load the list for the Account dropdown
        this.dataChanged(null);
        this.bomService.addListener(this);
        
        this.tableViewer.getTable().addKeyListener(this);
        
        this.tableViewer.getTable().addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void	widgetDefaultSelected(SelectionEvent evt) {
        		gotoEntry();
        	}
        });
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setVisible(boolean)
     */
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            this.tableViewer.getTable().setFocus();
        }
    }
    
    protected void	gotoEntry() {
        StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
        if (! sel.isEmpty()) {
            LedgerTreeViewer treeViewer = GUIUtil.findTreeViewer(this);
            if (treeViewer != null) {
                ScheduledEntry se = (ScheduledEntry)sel.getFirstElement();
                treeViewer.selectTab(se);
            }
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent event) {
        if (event == null || event.op == BOMEvent.OP_BROADCAST || event.entity instanceof ScheduledEntry) {
            this.tableViewer.refresh();
        }
    }
    protected void	installContextMenu(final Table table) {
        Menu menu = new Menu(table);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        mi.setText(Messages.getString("ScheduledListViewer.2")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		IStructuredContentProvider contentProvider  = (IStructuredContentProvider)tableViewer.getContentProvider();
        		tableViewer.setSelection(new StructuredSelection(contentProvider.getElements(model)));
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        mi.setText(Messages.getString("ScheduledListViewer.4")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                tableViewer.setSelection(null);
            }
         });
        final MenuItem miDel = new MenuItem(menu, SWT.PUSH);
        miDel.setImage(ImageCache.get("trash")); //$NON-NLS-1$
        miDel.setText(Messages.getString("ScheduledListViewer.6")); //$NON-NLS-1$
        miDel.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                deleteSelected();
            }
        });
        miPay = new MenuItem(menu, SWT.PUSH);
        miPay.setImage(ImageCache.get("account")); //$NON-NLS-1$
        miPay.setText(Messages.getString("ScheduledListViewer.8")); //$NON-NLS-1$
        miPay.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("rawtypes")
        	@Override
			public void	widgetSelected(SelectionEvent evt) {
                IStructuredSelection entries = (IStructuredSelection)tableViewer.getSelection();
                for (Iterator iter = entries.iterator(); iter.hasNext(); ) {
                    ScheduledEntry se = (ScheduledEntry)iter.next();
                    new BOMHelper(bomService).makeScheduledPayment(se);
                }
                tableViewer.refresh();
            }
        });
        table.setMenu(menu);
        table.addMouseListener(new MouseAdapter() {
        	@Override
            public void mouseDown(MouseEvent evt) {
                switch (evt.button) {
                case 3:
                    miPay.setEnabled(!tableViewer.getSelection().isEmpty());
                    miDel.setEnabled(!tableViewer.getSelection().isEmpty());
                    break;
                }
            }
        });
    }
    
    protected void	deleteSelected() {
        if (! GUIUtil.confirm(Messages.getString("ScheduledListViewer.9"), Messages.getString("ScheduledListViewer.10"), true)) { //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }
        try {
            this.bomService.setPublishEvent(false);
            StructuredSelection sel = (StructuredSelection)this.tableViewer.getSelection();
            for (@SuppressWarnings("rawtypes")
			Iterator iter = sel.iterator(); iter.hasNext(); ) {
                ScheduledEntry se = (ScheduledEntry)iter.next();
                if (se.getEntryId() != null) {
                    bomService.deleteGeneric(se, this.metaProvider.getPrimaryKey(), se.getEntryId());
                }
            }
        } finally {
            bomService.setPublishEvent(true);
        }        
    }

	@Override    
    public void	keyPressed(KeyEvent evt) { /* not used */ }
    
	@Override	
    public void	keyReleased(KeyEvent evt) {
        if ((evt.stateMask & SWT.CONTROL) == 0) {
            return;
        }
        switch(GUIUtil.keyCodeToChar(evt)) {
        case 'A':
           	this.tableViewer.getTable().selectAll();
	        break;
        case 'N':
       		this.tableViewer.getTable().deselectAll();
	        break;
        }
    }    
    
    public class ScheduledListContentProvider implements IStructuredContentProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override    	
        public Object[] getElements(Object inputElement) {
        	return bomService.getRefData(ScheduledEntry.class)
        			.stream()
        			.sorted((se1, se2) -> {
        				if (sortByColumn != null) {
        					if (sortByColumn.getData().equals(ScheduledListMeta.DESC_PROP)) {
        						return se1.getDescription().compareTo(se2.getDescription());
        					} else if (sortByColumn.getData().equals(ScheduledListMeta.FR_ACCT_PROP)) {
        						return se1.getFromAccount().compareTo(se2.getFromAccount());
        					} else if (sortByColumn.getData().equals(ScheduledListMeta.AMT_PROP)) {
        						return (int)(se1.getAmount() - se2.getAmount());
        					}
        				}
        				return se1.getPostingDate() != null && se2.getPostingDate() != null &&
        						se1.getPostingDate().before(se2.getPostingDate()) ? -1 : 1;
        			})
        			.toArray(ScheduledEntry[]::new);
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

    public class ScheduledListLabelProvider implements ITableLabelProvider, IColorProvider {
        private List<ILabelProviderListener> listeners = Lists.newArrayList();

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
            Calendar today = DateUtil.getTodayStart();
            today.add(Calendar.DAY_OF_MONTH, 1);
            ScheduledEntry se = (ScheduledEntry)element;
            return se.getPostingDate().after(today.getTime()) ? Display.getCurrent().getSystemColor(SWT.COLOR_RED) : null;
        }
        
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
        @Override
        public String getColumnText(final Object element, int columnIndex) {
        	ScheduledEntry entry = null;
            if (element instanceof TableItem) {
                entry = (ScheduledEntry)((TableItem)element).getData();
            } else {
            	entry = (ScheduledEntry)element;
            }
            Object value = metaProvider.getValue(entry, metaProvider.getColumns()[columnIndex].getName(), null);
            if (value != null) {
                String str = ""; //$NON-NLS-1$
                if (metaProvider.getColumns()[columnIndex].getPrototype().equals(Date.class)) {
                    str = DateUtil.format((Date)value);
                } else {
                    str = value.toString();
                }
                return str;
            }
            return ""; //$NON-NLS-1$
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
}
