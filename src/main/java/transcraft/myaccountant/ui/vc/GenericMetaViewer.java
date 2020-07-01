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

import static org.slf4j.LoggerFactory.getLogger;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import com.google.common.collect.Maps;

import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.AutoCompletableText;
import transcraft.myaccountant.ui.CalendarFieldEditor;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.utils.Formatters;

/**
 * Generic field based Viewer driven by a MetaProvider
 * 
 * @author david.tran@transcraft.co.uk
 */
public class GenericMetaViewer<T> extends ScrolledComposite implements SelectionListener, FocusListener {
	private static final Logger LOG = getLogger(GenericMetaViewer.class);
	
    protected T model;
    protected MetaProvider<T> metaProvider;
    protected BOMService bomService;
    protected HashMap<MetaColumn<?>, Control> controls = Maps.newHashMap();
    protected ValueChangedListener valueChangedListener;
    protected boolean modified;
    
    protected Composite actionBar;
    protected Button saveButton;

    /**
     * @param parent
     * @param style
     */
    public GenericMetaViewer(Composite parent, BOMService bomService, MetaProvider<T> provider, T obj) {
        super(parent, SWT.BORDER|SWT.V_SCROLL|SWT.H_SCROLL);
        this.model = obj;
        this.metaProvider = provider;
        this.bomService = bomService;
        this.valueChangedListener = new ValueChangedListener();
        this.createContents();
    }
    
    protected void	createContents() {
        //this.setExpandHorizontal(true);
        //this.setExpandVertical(true);
        this.setAlwaysShowScrollBars(false);


        Composite contents = new Composite(this, SWT.NONE);
        this.setContent(contents);
        
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.horizontalSpacing = 5;
        ly.marginWidth = 50;
        ly.verticalSpacing = 5;
        //ly.makeColumnsEqualWidth = true;
        contents.setLayout(ly);
        
        MetaColumn<?> [] columns = this.metaProvider.getColumns();
        for (int i = 0; i < columns.length; i++) {
            Label title = new Label(contents, SWT.SHADOW_IN);
            title.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW));
            title.setFont(GUIUtil.titleFont);
            title.setText(columns[i].isVisible() ? columns[i].getTitle() : ""); //$NON-NLS-1$
            Control input = null;
            if (columns[i].getPrototype().equals(Boolean.class)) {
                Button button = new Button(contents, SWT.CHECK);
                input = button;
                button.addSelectionListener(this.valueChangedListener);
            } else if (columns[i].getPrototype().equals(Date.class)) {
                CalendarFieldEditor calEditor = new CalendarFieldEditor(contents);
                calEditor.addSelectionListener(this.valueChangedListener);
	            input = calEditor;
            } else if (columns[i].getList().isPresent()) {
                // Java 8 compatibility mode, so String[]::new is not available
                String [] items = columns[i].getList().map(l -> l.toArray(new String[l.size()])).orElse(new String[0]); 
                if (columns[i].isMultiSelect()) {
                    List list = new List(contents, SWT.MULTI|SWT.V_SCROLL|SWT.BORDER);
                    list.setItems(items);
                    list.addSelectionListener(this.valueChangedListener);
                    GUIUtil.installContextMenu(list);
                    input = list;
                } else if (columns[i].isRadioGroup()) {
                    Group group = new Group(contents, SWT.NONE);
                    GridLayout gly = new GridLayout();
                    gly.numColumns = 2;
                    group.setLayout(gly);
                    for (int j = 0; j < items.length; j++) {
                        Button button = new Button(group, SWT.RADIO);
                        button.setText(items[j]);
                        button.addSelectionListener(this.valueChangedListener);
                    }
                    input = group;
                } else {
                	AutoCompletableText text = new AutoCompletableText(contents, SWT.BORDER);
	                input = text;
                }
            } else if (columns[i].getArray().isPresent()) {
            	Combo combo = new Combo(contents, SWT.BORDER);
            	combo.setItems(columns[i].getArray().get());
            	input = combo;
            } else {
                int style = columns[i].getStyle() == SWT.NONE ?
                        (columns[i].isEnabled() ? SWT.BORDER : SWT.NONE) : columns[i].getStyle();
                Text text = new Text(contents, style);
                text.addFocusListener(this);
                text.addSelectionListener(this.valueChangedListener);
                input = text;
            }
            if (input != null) {
                if ((columns[i].getStyle() & SWT.MULTI) != 0) {
                    GridData gd = new GridData(columns[i].getList() == null ?
                            // multiline Text
	                        GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL :
                            GridData.HORIZONTAL_ALIGN_FILL
	                );
                    gd.heightHint = Math.min(100, columns[i].getWidth());
                    input.setLayoutData(gd);
                } else {
                  	input.setSize(columns[i].getWidth(), Math.max(input.getSize().y, 30));
                    if (! columns[i].getPrototype().equals(Date.class)) {
                       	input.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
                    }
                }
            	input.setEnabled(columns[i].isEnabled());
            	input.setVisible(columns[i].isVisible());
            	this.controls.put(columns[i], input);
            }
        }

        this.refreshValues();
        
        this.actionBar = this.createActionBar(contents);
        
        Point sz = contents.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        this.setMinSize(sz);
        contents.setSize(sz);
        
        this.addDisposeListener(new DisposeListener() {
        	@Override
            public void widgetDisposed(DisposeEvent evt) {
                tidyUp();
            }
        });
    }

    protected void	tidyUp() {
        if (isModified()) {
            if (GUIUtil.confirm(Messages.getString("GenericMetaViewer.1"), 
            		Messages.getString("GenericMetaViewer.2") + model + "' ?", true)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                try {
                    store(model);
                } catch (Exception e) {}
            }
        }
    }
    
    /**
     * @return Returns the modified.
     */
    protected boolean isModified() {
        return modified;
    }
    
    /**
     * @param modified The modified to set.
     */
    protected void setModified(boolean modified) {
        this.modified = modified;
    }
    
    protected Composite	createActionBar(Composite parent) {
        Composite bar = new Composite(parent, SWT.SHADOW_OUT);
        bar.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        GridLayout ly1 = new GridLayout();
        ly1.numColumns = 10;
        bar.setLayout(ly1);
        this.saveButton = new Button(bar, SWT.SHADOW_ETCHED_OUT);
        this.saveButton.setImage(ImageCache.get("save")); //$NON-NLS-1$
        this.saveButton.setToolTipText(Messages.getString("GenericMetaViewer.5")); //$NON-NLS-1$
        this.saveButton.addSelectionListener(this);
        return bar;
    }
    
    public Control	getControlForColumn(String columnName) {
        return this.getControlForColumn(this.metaProvider.getColumn(columnName));
    }
    
    public Control	getControlForColumn(MetaColumn<?> column) {
        try {
            return (Control)this.controls.get(column);
        } catch (Exception e) {
            LOG.error("getControlForColumn():", e); //$NON-NLS-1$
        }
        return null;
    }
    
    public void	refreshValues() {
        MetaColumn<?> [] columns = this.metaProvider.getColumns();
        for (int i = 0; i < columns.length; i++) {
            Object value = this.metaProvider.getValue(this.model, columns[i].getName(), null);
            Object control = this.controls.get(columns[i]);
            if (columns[i].getPrototype().equals(Boolean.class)) {
                Button button = (Button)control;
                button.setSelection(((Boolean)value).booleanValue());
            } else if (columns[i].getPrototype().equals(Date.class)) {
                CalendarFieldEditor input = (CalendarFieldEditor)control;
                input.setValue((Date)value);
            } else if (columns[i].getList().isPresent()) {
                if (columns[i].isMultiSelect()) {
                    List list = (List)control;
                    list.deselectAll();
                    if (value != null && (value instanceof String[])) {
	                    String [] items = list.getItems();
	                    String [] sels = (String[])value;
	                    for (int j = 0; j < sels.length; j++) {
	                        for (int k = 0; k < items.length; k++) {
	                            if (items[k].equals(sels[j])) {
	                                list.select(k);
	                                //break;
	                            }
	                        }
	                    }
                    }
                } else if (columns[i].isRadioGroup()) {
                    Group group = (Group)control;
                    for (int j = 0; j < group.getChildren().length; j++) {
                        Button radio = (Button)group.getChildren()[j];
                        if (radio.getText().equals(value.toString())) {
                            radio.setSelection(true);
                            break;
                        }
                    }
                } else if (control instanceof AutoCompletableText) {
                	AutoCompletableText act = (AutoCompletableText)control;
                    // Java 8 compatibility mode, so String[]::new is not available
                	act.updateProposals(columns[i].getList().map(l -> l.toArray(new String[l.size()])).orElse(new String[0]));
                	act.setText(value != null ? value.toString() : null);
                }
            } else if (columns[i].getArray().isPresent()) {
            	Combo combo = (Combo)control;
            	combo.setText(value != null ? value.toString() : "");
            } else {
                Text input = (Text)control;
                input.setText(Formatters.format(value));
            }
        }

    }
    
    protected void	setListForColumnName(String columnName, java.util.List<String> list) {
        MetaColumn<?> column = this.metaProvider.getColumn(columnName);
        column.setList(list);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent evt) {
        this.store();
    }

    protected void	updateValues() throws ParseException {
        MetaColumn<?> [] columns = this.metaProvider.getColumns();
	    for (int i = 0; i < columns.length; i++) {
	        Control control = (Control)this.controls.get(columns[i]);
	        if (columns[i].getPrototype().equals(Boolean.class)) {
	            Button button = (Button)control;
	            this.metaProvider.setValue(this.model, null, columns[i].getName(), Boolean.valueOf(button.getSelection()));
	        } else if (columns[i].getPrototype().equals(Date.class)) {
	            CalendarFieldEditor input = (CalendarFieldEditor)control;
	            this.metaProvider.setValue(this.model, null, columns[i].getName(), input.getValue());
            } else if (columns[i].getList().isPresent()) {
                if (columns[i].isMultiSelect()) {
                    List list = (List)control;
                    String [] sels = list.getSelection();
		            this.metaProvider.setValue(this.model, null, columns[i].getName(), sels);
                } else if (columns[i].isRadioGroup()) {
                    Group group = (Group)control;
                    for (int j = 0; j < group.getChildren().length; j++) {
                        Button button = (Button)group.getChildren()[j];
                        if (button.getSelection()) {
                            this.metaProvider.setValue(this.model, null, columns[i].getName(), button.getText());
                            break;
                        }
                    }
                } else {
		            AutoCompletableText act = (AutoCompletableText)control;
		            this.metaProvider.setValue(this.model, null, columns[i].getName(), act.getText());
                }
            } else if (columns[i].getArray().isPresent()) {
            	Combo combo = (Combo)control;
	            this.metaProvider.setValue(this.model, null, columns[i].getName(), combo.getText());
	        } else {
	            Text input = (Text)control;
	            this.metaProvider.setValue(this.model, null, columns[i].getName(), input.getText());
	        }
	    }
    }
    
    protected void	validate() {}
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusGained(java.awt.event.FocusEvent)
     */
    @Override
    public void focusGained(FocusEvent evt) {
        Object obj = evt.getSource();
        if (obj instanceof Text) {
            ((Text)obj).selectAll();
        }
    }
    
    /* (non-Javadoc)
     * @see java.awt.event.FocusListener#focusLost(java.awt.event.FocusEvent)
     */
    @Override
    public void focusLost(FocusEvent arg0) {
    }
    
    /**
     * @return Returns the bomService.
     */
    protected BOMService getBomService() {
        return bomService;
    }
    
    /**
     * @return Returns the controls.
     */
    protected HashMap<MetaColumn<?>, Control> getControls() {
        return controls;
    }
    
    protected void	store() {
        try {
            this.updateValues();
            this.validate();
		    this.store(this.model);
        } catch (Exception e) {
            GUIUtil.showError(Messages.getString("GenericMetaViewer.8"), Messages.getString("GenericMetaViewer.9"), e); //$NON-NLS-1$ //$NON-NLS-2$
            LOG.error("store()", e);
        }
    }
    
    protected void	store(T obj) {
        String primaryKey = this.metaProvider.getPrimaryKey();
        Object primaryValue = this.metaProvider.getValue(obj, primaryKey, null);
        if (primaryValue == null) {
            primaryValue = this.bomService.getNextId(obj.getClass());
        }
        this.getBomService().storeGeneric(obj, primaryKey, primaryValue);
        this.setModified(false);
        saveButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        this.refreshValues();
    }
    
    public class ValueChangedListener implements SelectionListener {
        
        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
         */
    	@Override
        public void widgetDefaultSelected(SelectionEvent evt) {
            this._doUpdate(evt);
        }
    	
        /* (non-Javadoc)
         * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
         */
        @Override
    	public void widgetSelected(SelectionEvent evt) {
            this._doUpdate(evt);
        }
        
        private void	_doUpdate(SelectionEvent evt) {
            try {
                updateValues();
            } catch (ParseException e) {
                LOG.error("ValueChangeListener:", e); //$NON-NLS-1$
            }
            setModified(true);
            saveButton.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED));
        }
    }
}
