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

import static org.slf4j.LoggerFactory.getLogger;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.service.DAOService;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * DB4O Schema object viewer
 * @author david.tran@transcraft.co.uk
 */
public class SchemaEntryViewer extends Composite {

    private SchemaTreeViewer treeViewer;
    private static final Logger log = getLogger(SchemaEntryViewer.class);
    
    private static final java.util.List<String> METHOD_NAMES = Lists.newArrayList(
    		"toString"
    		);
    
    /**
     * @param parent
     * @param style
     */
    public SchemaEntryViewer(Composite parent, SchemaTreeViewer treeViewer) {
        super(parent, SWT.FLAT);
        this.treeViewer = treeViewer;
        this.treeViewer.setEntryViewer(this);
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.verticalSpacing = 5;
        this.setLayout(ly);
    }
    
    public void	selectObject(Object obj) {
        // clear out the old resources
        Control [] children = this.getChildren();
        for (int i = 0; i < children.length; i++) {
            children[i].dispose();
        }
        
        if (obj == null) {
            return;
        }

        Label title = new Label(this, SWT.SHADOW_ETCHED_OUT);
        title.setText(obj.toString());
        title.setFont(GUIUtil.titleFont);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER);
        gd.horizontalSpan = 2;
        title.setLayoutData(gd);

        Label label = new Label(this, SWT.NONE);
        label.setText(Messages.getString("SchemaEntryViewer.0")); //$NON-NLS-1$
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));

        Class<?> [] hierachy = DAOService.getHierachy(this.treeViewer.getContainer(), obj);
        List hierachyList = new List(this, SWT.BORDER);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.heightHint = 100;
        hierachyList.setLayoutData(gd);
        for (int i = 0; i < hierachy.length; i++) {
            hierachyList.add(hierachy[i].getName());
        }

        if (obj instanceof SchemaInfo) {
            SchemaInfo info = (SchemaInfo)obj;
            label = new Label(this, SWT.SHADOW_ETCHED_OUT);
            label.setText(Messages.getString("SchemaEntryViewer.1")); //$NON-NLS-1$
            label.setFont(GUIUtil.titleFont);
            gd = new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER);
            gd.horizontalSpan = 2;
            label.setLayoutData(gd);
            
            for (int i = 0; i < hierachy.length; i++) {
                populateForClass(info, hierachy[i]);
            }
        }
        
        this.layout(true);
        this.redraw();
        if (this.getParent() instanceof ScrolledComposite) {
            ScrolledComposite scroll = (ScrolledComposite)this.getParent();
            scroll.setContent(this);
            scroll.setMinSize(this.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        }
    }

    protected void	populateForClass(SchemaInfo info, Class<?> theClass) {
        java.util.List<Method> methods = Arrays.asList(theClass.getDeclaredMethods());
        methods = methods.stream().sorted((o1, o2) ->
                ((Method)o1).getName().compareTo(((Method)o2).getName())
        ).collect(Collectors.toList());
        
        Label title = new Label(this, SWT.SHADOW_ETCHED_OUT);
        title.setFont(GUIUtil.boldFont);
        title.setText("{" + theClass.getName() + "}"); //$NON-NLS-1$ //$NON-NLS-2$
        GridData gd = new GridData(GridData.FILL_HORIZONTAL|GridData.HORIZONTAL_ALIGN_CENTER);
        gd.horizontalSpan = 2;
        title.setLayoutData(gd);

        for (int i = 0; i < methods.size(); i++) {
            Method method = (Method)methods.get(i);
            if ((method.getModifiers() & Modifier.PUBLIC) == 0 ||
                    (method.getModifiers() & Modifier.STATIC) != 0 ||
                    (method.getModifiers() & Modifier.ABSTRACT) != 0) {
                continue;
            }
            Label label = new Label(this, SWT.NONE);
            label.setFont(GUIUtil.italicFont);
            label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
            label.setText(method.getName());

            label = new Label(this, SWT.FLAT);
            gd = new GridData(GridData.FILL_HORIZONTAL);
            label.setLayoutData(gd);
            if (METHOD_NAMES.contains(method.getName()) ||
            		(method.getName().startsWith("get") && method.getParameterTypes().length == 0)) { //$NON-NLS-1$
                try {
                    Object value = method.invoke(info.getData(), (Object[])null);
                    if (value != null) {
                        label.setText(Formatters.format(value) + " (" + value.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                } catch (Exception e) {
                    log.error(method.getName() + ":", e); //$NON-NLS-1$
                }
            }
        }
    }
}
