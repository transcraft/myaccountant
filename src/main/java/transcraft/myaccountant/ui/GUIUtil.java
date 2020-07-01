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
package transcraft.myaccountant.ui;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.text.ParsePosition;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.StatusLineManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;

import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.ui.vc.LedgerTreeViewer;

/**
 * Miscellaneous GUI Utilities
 * 
 * @author david.tran@transcraft.co.uk
 */
public class GUIUtil {
	private static final Logger log = getLogger(GUIUtil.class);
	    
    public static int	showError(String title, String msg, Throwable t) {
        MessageBox box = new MessageBox(Display.getDefault().getShells()[0]);
        box.setText(title);
        box.setMessage(msg + "\n" + (t != null ? t.getClass().getSimpleName() + ":" + t.getMessage() : "")); //$NON-NLS-1$ //$NON-NLS-2$
        log.error(msg, t);
        int rc = box.open();
        return rc;
    }
    
    /**
     * prompts for a new object with no initial value
     * @param title
     * @param msg
     * @param validator
     * @param theClass
     * @return
     */
    public static Object promptForNewObject(String title, String msg, IInputValidator validator, Class<?> theClass) {
        return promptForNewObject(title, msg, validator, theClass, null);
    }
    
    /**
     * prompts for a new object with initial value
     * @param title
     * @param msg
     * @param validator
     * @param theClass
     * @param initialValue
     * @return
     */
    public static Object promptForNewObject(String title, String msg, IInputValidator validator, Class<?> theClass, String initialValue) {
        final InputDialog dialog = new InputDialog(Display.getCurrent().getActiveShell(), title, msg, initialValue, validator);
        if (dialog.open() == Window.OK) {
            try {
                if (theClass.equals(String.class)) {
                    return dialog.getValue();
                }
                Constructor<?> constructor = theClass.getConstructor(new Class[] { String.class });
                Object obj = constructor.newInstance(new Object [] { dialog.getValue() });
                return obj;
            } catch (Exception e) {
                GUIUtil.showError("New Object", Messages.getString("GUIUtil.3"), e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return null;
    }
    
    /**
     * popup and confirm
     * 
     * @param title
     * @param msg
     * @return
     */
    public static boolean confirm(String title, String msg) {
        return confirm(title, msg, false);
    }
    
    /**
     * popup and confirm with Y/N
     * 
     * @param title
     * @param msg
     * @param yesNo
     * @return
     */
    public static boolean confirm(String title, String msg, boolean yesNo) {
        return yesNo ?
                MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), title, msg) :
                MessageDialog.openConfirm(Display.getCurrent().getActiveShell(), title, msg);
    }
    
    /**
     * confirm a file is overwritten
     * 
     * @param path
     * @return
     */
    public static boolean confirmOverwriteFile(String path) {
        File fp = new File(path);
        if (! fp.exists()) {
            return true;
        }
        return MessageDialog.openQuestion(Display.getCurrent().getActiveShell(), Messages.getString("GUIUtil.4"), //$NON-NLS-1$
                Messages.getString("GUIUtil.5") + path + Messages.getString("GUIUtil.6")); //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    /**
     * returns the character from the KeyEvent event
     * @param evt
     * @return
     */
    public static char	keyCodeToChar(KeyEvent evt) {
        char key = evt.character;
        if (key == 0) {
            key = (char)evt.keyCode;
        } else {
            if (0 <= key && key <= 0x1F) {
                if ((evt.stateMask & SWT.CTRL) != 0) {
                    key += 0x40;
                }
            } else {
                if ('a' <= key && key <= 'z') {
                    key -= 'a' - 'A';
                }
            }
        }
        return key;
    }
    
    /**
     * returns the most previously used file path
     * 
     * @param theClass
     * @return
     */
    public static String getMemorisedPath(Class<?> theClass) {
        String path = PreferenceFactory.getPreferenceStore().getString("gui.lastFileDialogPath." + theClass.getName()); //$NON-NLS-1$
        return path == null ? System.getProperty("user.home") : path; //$NON-NLS-1$
    }
    
    /**
     * stores the most previously used file path
     * @param theClass
     * @param path
     */
    public static void setMemorisedPath(Class<?> theClass, String path) {
        try {
            PreferenceFactory.getPreferenceStore().setValue("gui.lastFileDialogPath." + theClass.getName(), path); //$NON-NLS-1$
            PreferenceFactory.getPreferenceStore().save();
        } catch (Exception e) {
            log.error("GUIUtil.setMemorisedPath(" + theClass + "," + path + "):", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    /**
     * install context menu for All/None selection
     * 
     * @param list
     */
    public static void	installContextMenu(final List list) {
        Menu menu = new Menu(list);
        MenuItem mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("GUIUtil.13")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("tick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent evt) {
        		list.selectAll();
        	}
        });
        mi = new MenuItem(menu, SWT.PUSH);
        mi.setText(Messages.getString("GUIUtil.15")); //$NON-NLS-1$
        mi.setImage(ImageCache.get("untick")); //$NON-NLS-1$
        mi.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void widgetSelected(SelectionEvent evt) {
                list.deselectAll();
            }
        });
        list.setMenu(menu);
        list.addKeyListener(new KeyAdapter() {
        	@Override
	        public void	keyReleased(KeyEvent evt) {
	            if ((evt.stateMask & SWT.CONTROL) == 0) {
	                return;
	            }
	            switch(GUIUtil.keyCodeToChar(evt)) {
	            case 'A':
	              	list.selectAll();
	    	        break;
	            case 'N':
	           		list.deselectAll();
	    	        break;
	            }
	        }
        });
    }
    
    /**
     * invoke a browser and load the specified URL
     * 
     * @param url
     * @param title
     */
    public static final	void launchBrowser(final String url, String title) {
        if (OsCheck.getOperatingSystemType() == OsCheck.OSType.Linux) {
        	try {
	    		Process p = Runtime.getRuntime().exec("xdg-open " + url);
	    		if (p == null) {
	        		showError("Error", "Can not spawn " + url, null);
	    		} else {
	    			p.waitFor();
	    			log.info(p + "(" + url + ")=" + p.exitValue());
	    		}
        	} catch (Exception e) {
        		showError("Error", "Can not launch " + url, e);
        	}
        } else {
	        final Shell shell = new Shell(Display.getCurrent());
	        shell.setImage(ImageCache.get("logo")); //$NON-NLS-1$
	        shell.setText(title != null ? title : url);
	        shell.setLayout(new FillLayout());
	        GUIUtil.restoreLastLocation("gui.browserLocation", shell); //$NON-NLS-1$
	        shell.addShellListener(new ShellAdapter() {
	        	@Override
	            public void	shellClosed(ShellEvent evt) {
	                GUIUtil.storeLastLocation("gui.browserLocation", shell); //$NON-NLS-1$
	            }
	        });
        	Browser browser = new Browser(shell, SWT.BORDER);
        	browser.setUrl(url);
        	shell.open();
        }
    }
    
    /**
     * place the UI in the most recently used location upon startup
     * 
     * @param id
     * @param parent
     */
    public static final void	restoreLastLocation(String id, Composite parent) {
        try {
            String str = PreferenceFactory.getPreferenceStore().getString(id);
            if (isBlank(str)) {
                return;
            }
            Object [] values = new MessageFormat("{0,number,integer}+{1,number,integer}+{2,number,integer}x{3,number,integer}").parse(str, new ParsePosition(0)); //$NON-NLS-1$
            if (values.length == 4) {
                Rectangle rect = new Rectangle(((Long)values[0]).intValue(),((Long)values[1]).intValue(),
                        ((Long)values[2]).intValue(), ((Long)values[3]).intValue());
                parent.setBounds(rect);
            }
        } catch (Exception e) {
            log.error("GUIUtil.restoreLastLocation(" + id + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * store the location of the UI so it can be restored upon startup
     * 
     * @param id
     * @param parent
     */
    public static final void	storeLastLocation(String id, Composite parent) {
        Point loc = parent.getLocation();
        Point sz = parent.getSize();
        String str = "" + loc.x + "+" + loc.y + "+" + sz.x + "x" + sz.y; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        PreferenceFactory.getPreferenceStore().setValue(id, str);
        try {
            PreferenceFactory.getPreferenceStore().save();
        } catch (Exception e) {
            log.error("GUIUtil.storeLastLocation(" + id + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }
    
    /**
     * returns a location for temporary files 
     * @return a folder for temporary files
     */
    public static final String	getTempPath() {
        String defaultPath = System.getProperty("user.home") + File.separator + "temp" + File.separator; //$NON-NLS-1$ //$NON-NLS-2$
        new File(defaultPath).mkdirs();
        return defaultPath;
    }
    
    /**
     * returns a temporary file name with random name
     * 
     * @param suffix
     * @return file name
     */
    public static final String	getTempFile(String suffix) {
        String path = GUIUtil.getTempPath() + Math.random() + suffix;
        new File(path).deleteOnExit();
        return path;
    }
    
    /**
     * add auto-sizing capability for a table widget
     * 
     * @param table
     */
    public static final void	setupTableAutoSized(final Table table) {
        table.addControlListener(new ControlAdapter() {
            // auto resize the columns to equispaced
        	@Override
            public void	controlResized(ControlEvent evt) {
                autoSizeTable(table);
            }
        });
    }
    
    /**
     * auto-size table based on contents
     * 
     * @param table
     */
    public static final void	autoSizeTable(Table table) {
        int count = table.getColumnCount();
        for (int i = 0; i < table.getColumnCount(); i++) {
            // account for hidden columns
            if (table.getColumn(i).getWidth() == 0) {
                count--;
            }
        }
        int w = table.getSize().x - 5;
        if (count > 0 && w > 100) {
            for (int i = 0; i < count; i++) {
                if (table.getColumn(i).getWidth() != 0) {
                    table.getColumn(i).setWidth(w / count);
                }
            }
        }
    }
    
    /**
     * traverse up the widget tree to look for the parent tree viewer
     * 
     * @param me
     * @return treeViewer
     */
    public static LedgerTreeViewer findTreeViewer(Control me) {
        Composite parent = me.getParent();
        while (parent != null) {
            if (parent instanceof LedgerTreeViewer) {
                return (LedgerTreeViewer)parent;
            } else {
                for (int i = 0; i < parent.getChildren().length; i++) {
                    if (parent.getChildren()[i] instanceof LedgerTreeViewer) {
                        return (LedgerTreeViewer)parent.getChildren()[i];
                    }
                }
            }
        
            parent = parent.getParent();
        }
        return null;
    }
    
    /**
     * update a progress monitor, at the same time dispatch outstanding UI events, so
     * that the progress monitor is shown to be updated on the screen
     * 
     * @param monitor
     * @param workDone
     */
	public static void	updateProgressMonitor(IProgressMonitor monitor, int workDone) {
	    if (monitor != null) {
	        monitor.worked(workDone);
	        // need to update the screen, dispatch outstanding UI events
	        try {
		        int j = 0;
		        Display dpy = Display.getCurrent();
		        if (dpy != null) {
			        while(dpy.readAndDispatch()) {
			            j++;
			            if (j >= 50) {
			                break;
			            }
			        }
		        }
	        } catch (Exception e) {}
	    }
	}

	/**
	 * bold font used by current display
	 */
    public static final Font boldFont = new Font(Display.getCurrent(), "", 9, SWT.BOLD); //$NON-NLS-1$

	/**
	 * italic font used by current display
	 */
    public static final Font italicFont = new Font(Display.getCurrent(), "", 9, SWT.ITALIC); //$NON-NLS-1$
    
	/**
	 * title font used for the whole application
	 */
    public static final Font titleFont = new Font(Display.getCurrent(), "Arial", 10, SWT.BOLD); //$NON-NLS-1$
    
    private static Clipboard clipboard_;
    private static StatusLineManager slm_;
    
    /**
     * singleton clipboard for the session
     * 
     * @return clipboard
     */
    public static Clipboard	getClipboard() {
        if (clipboard_ == null) {
            clipboard_ = new Clipboard(Display.getCurrent());
        }
        return clipboard_;
    }
    
    /**
     * common status bar for the session
     * 
     * @return SLM
     */
    public static StatusLineManager	getStatus() {
        if (GUIUtil.slm_ == null) {
            GUIUtil.slm_ = new StatusLineManager();
        }
        return GUIUtil.slm_;
    }
    
    private GUIUtil() {}
}
