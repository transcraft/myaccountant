/**
 * Created on 15-Oct-2005
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
package transcraft.myaccountant.ui.schema;

import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TreeItem;

import com.db4o.ObjectContainer;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import transcraft.BookKeeper.service.DAOService;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * @author david.tran@transcraft.co.uk
 */
public class SchemaTreeViewer extends Composite {

    private SchemaEntryViewer entryViewer;
    private ObjectContainer container;
    private Object model;
    private Map<Class<?>, List<SchemaInfo>> schemaInfoMap = Maps.newHashMap();
    private TreeViewer treeViewer;
    private SchemaTreeContentProvider contentProvider;
    /**
     * @param parent
     */
    public SchemaTreeViewer(Composite parent, ObjectContainer container, Object data) {
        super(parent, SWT.BORDER);
        this.container = container;
        this.model = data;
        this.setLayout(new FillLayout());
        this.treeViewer = new TreeViewer(this, SWT.FLAT);
        this.contentProvider = new SchemaTreeContentProvider();
        this.treeViewer.setContentProvider(this.contentProvider);
        this.treeViewer.setLabelProvider(new SchemaTreeLabelProvider(schemaInfoMap));
        this.treeViewer.setInput(this.model);
        this.setupContextMenu();
    	this.treeViewer.getTree().addMouseListener(new MouseAdapter() {
        	@Override
            public void	mouseDown(MouseEvent evt) {
                if (evt.button == 1) {
	                StructuredSelection sel = (StructuredSelection)treeViewer.getSelection();
	                if (sel.isEmpty()) {
	                    return;
	                }
	                Object obj = sel.getFirstElement();
	                selectObject(obj);
                }
            }
         });
    }

    protected void	setupContextMenu() {
        MenuManager menuMgr = new MenuManager("#PopupMenu", "contextMenu"); //$NON-NLS-1$ //$NON-NLS-2$
        menuMgr.setRemoveAllWhenShown(true);
    	menuMgr.addMenuListener(new IMenuListener() {
        	@Override
    		public void menuAboutToShow(IMenuManager manager) {
    		    manager.add(new ActionContributionItem(new RefreshAction()));
    		    TreeItem [] items = treeViewer.getTree().getSelection();
    		    if (items.length > 0) {
    		        Object data = items[0].getData();
    		        if (data instanceof SchemaInfo || data instanceof Class) {
    		            manager.add(new ActionContributionItem(new DeleteAction(data)));
    		        }
    		    }
    		}
    	});
    	Menu menu = menuMgr.createContextMenu(treeViewer.getControl());	
    	this.treeViewer.getControl().setMenu(menu);
    }

    protected void	selectObject(Object obj) {
        if (this.entryViewer == null) {
            return;
        }
        this.entryViewer.selectObject(obj);
    }
    
    /**
     * @return Returns the container.
     */
    public ObjectContainer getContainer() {
        return this.container;
    }
    /**
     * @param container The container to set.
     */
    public void setContainer(ObjectContainer container) {
        this.container = container;
        try {
            this.treeViewer.setInput(this.model);
        } catch (Exception e) {}
        this.entryViewer.selectObject(null);
    }
    /**
     * @return Returns the contentProvider.
     */
    public SchemaTreeContentProvider getContentProvider() {
        return contentProvider;
    }
    /**
     * @return Returns the entryViewer.
     */
    public SchemaEntryViewer getEntryViewer() {
        return entryViewer;
    }
    /**
     * @return Returns the map.
     */
    public Map<Class<?>, List<SchemaInfo>> getSchemaInfoMap() {
        return schemaInfoMap;
    }
    /**
     * @return Returns the model.
     */
    public Object getModel() {
        return model;
    }
    /**
     * @param model The model to set.
     */
    public void setModel(Object model) {
        this.model = model;
        this.treeViewer.setInput(model);
        this.treeViewer.refresh();
    }
    
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.widgets.Control#setEnabled(boolean)
     */
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (this.entryViewer != null) {
            this.entryViewer.setEnabled(enabled);
        }
    }

    /**
     * @param entryViewer The entryViewer to set.
     */
    public void setEntryViewer(SchemaEntryViewer entryViewer) {
        this.entryViewer = entryViewer;
    }
    

    public class RefreshAction extends Action {
        public RefreshAction() {
            super(Messages.getString("SchemaTreeViewer.2")); //$NON-NLS-1$
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            treeViewer.refresh();
        }
    }

    public class DeleteAction extends Action {
        Object selected;
        public DeleteAction(Object obj) {
            super(Messages.getString("SchemaTreeViewer.3")); //$NON-NLS-1$
            this.selected = obj;
        }
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.action.IAction#run()
         */
        public void run() {
            try {
                setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
                this.setEnabled(false);
                
	            if (selected instanceof Class) {
	                Class<?> theClass = (Class<?>)this.selected;
	                if (! GUIUtil.confirm(Messages.getString("SchemaTreeViewer.4"), Messages.getString("SchemaTreeViewer.5") + theClass)) { //$NON-NLS-1$ //$NON-NLS-2$
	                    return;
	                }
	                Object [] children = contentProvider.getChildren(theClass);
	                for (int i = 0; i < children.length; i++) {
	                    if (children[i] instanceof SchemaInfo) {
	                        SchemaInfo info = (SchemaInfo)children[i];
	                        container.delete(info.getData());
	                    }
	                }
	            } else if (selected instanceof SchemaInfo) {
	                SchemaInfo info = (SchemaInfo)this.selected;
	                if (! GUIUtil.confirm(Messages.getString("SchemaTreeViewer.6"), Messages.getString("SchemaTreeViewer.7") + info + "'(id " + info.getId() + ")")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
	                    return;
	                }
	                container.delete(info.getData());
	            }
            } finally {
                setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
                this.setEnabled(true);
                treeViewer.setInput(model);
            }
        }
    }

    
    public class SchemaTreeContentProvider implements ITreeContentProvider {
        
        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
         */
        @Override
        public Object[] getChildren(Object parentElement) {
            if (parentElement instanceof Class) {
                List<SchemaInfo> list = schemaInfoMap.get(parentElement);
                if (isNotEmpty(list)) {
                	return list
                			.stream()
                			.sorted((o1, o2) -> o1.toString().compareTo(o2.toString()))
                			.toArray(Object[]::new);
                }
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
         */
        @Override
        public Object getParent(Object element) {
            if (! (element instanceof Class)) {
                return element.getClass();
            }
            return null;
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
         */
        @Override
        public boolean hasChildren(Object element) {
            return element instanceof Class || element.equals("Root"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
         */
        @Override
        public Object[] getElements(Object inputElement) {
            if (inputElement.equals("Root")) { //$NON-NLS-1$
	            Set<Class<?>> keys = schemaInfoMap.keySet();
	            List<Class<?>> list = new ArrayList<Class<?>>(keys);
	            list = list.stream().sorted((o1, o2) ->
	                    ((Class<?>)o1).getName().compareTo(((Class<?>)o2).getName()))
	            		.collect(Collectors.toList());
                // Java 8 compatibility mode, so Object[]::new is not available
	            return list.toArray(new Object[list.size()]);
            } else if (inputElement instanceof Class) {
                return this.getChildren(inputElement);
            }
            return null;
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
            if (newInput != null) {
                try {
                    setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
                    setEnabled(false);
                    
                    /*
                     *  this map is referenced by the label provider, so clear and re-populate instead of
                     *  re-assign
                     */                    
                    schemaInfoMap.clear();
                    schemaInfoMap.putAll(DAOService.getObjectTree(container, null));
                } finally {
                    setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
                    setEnabled(true);
                }
            }
        }
    }
    
    public class SchemaTreeLabelProvider implements ILabelProvider {
        List<ILabelProviderListener> listeners = Lists.newArrayList();
        Map<Class<?>, List<SchemaInfo>> map;

        public SchemaTreeLabelProvider(Map<Class<?>, List<SchemaInfo>> map) {
			this.map = map;
		}

		/* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
         */
        @Override
        public Image getImage(Object element) {
            String imgName = "node"; //$NON-NLS-1$
            if (element instanceof Class) {
                imgName = "eclipse"; //$NON-NLS-1$
            } else if (element instanceof SchemaInfo) {
                imgName = "java_app"; //$NON-NLS-1$
            }
            return ImageCache.get(imgName);
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
         */
        @Override
        public String getText(Object element) {
        	StringBuilder builder = new StringBuilder();
        	if (element instanceof Class) {
        		Class<?> clz = (Class<?>)element;
                builder.append(clz.getName());
        		if (map.containsKey(clz)) {
        			builder.append(String.format(" (%d entries)", map.get(clz).size()));
        		}
        	} else {
        		builder.append(element.toString());
        	}
            return builder.toString();
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
