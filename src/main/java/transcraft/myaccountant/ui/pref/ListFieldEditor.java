/**
 * Created on 10-Oct-2005
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

import java.util.StringTokenizer;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Widget;

import com.google.common.collect.Lists;

/**
 * borrowed from package com.bull.eclipse.jonas.editors;
 * 
 * @author ???
 */
public class ListFieldEditor extends FieldEditor {

    public static final String DEF_SEPARATOR = "|"; //$NON-NLS-1$
	/**
	 * The list widget; <code>null</code> if none
	 * (before creation or after disposal).
	 */
	protected List list;

	/**
	 * The button box containing the Add, Remove, Up, and Down buttons;
	 * <code>null</code> if none (before creation or after disposal).
	 */
	protected Composite buttonBox;

	protected Button addButton;
	protected Button removeButton;
	protected Button updateButton;
	protected Button upButton;
	protected Button downButton;

	/**
	 * The selection listener.
	 */
	protected SelectionListener selectionListener;
	/**
	 * Creates a new list field editor 
	 */
	public ListFieldEditor() {
	}
	/**
	 * Creates a list field editor.
	 * 
	 * @param name the name of the preference this field editor works on
	 * @param labelText the label text of the field editor
	 * @param parent the parent of the field editor's control
	 */
	public ListFieldEditor(String name, String labelText, Composite parent) {
		init(name, labelText);
		createControl(parent);
	}
	
	
	/**
	 * Notifies that the Add button has been pressed.
	 */
	protected void addPressed() {
		setPresentsDefaultValue(false);
		String input = getNewInputObject();

		if (input != null) {
			int index = list.getSelectionIndex();
			if (index >= 0)
				list.add(input, index + 1);
			else
				list.add(input, 0);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the Add button has been pressed.
	 */
	protected void updatePressed() {
		String input = getNewInputObject();

		if (input != null) {
			int index = list.getSelectionIndex();
			if (index >= 0)
				list.setItem(index, input);
			selectionChanged();
		}
	}
	
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) list.getLayoutData()).horizontalSpan = numColumns - 1;
	}
	/**
	 * Creates the Add, Remove, Up, and Down button in the given button box.
	 *
	 * @param buttonBox the box for the buttons
	 */
	protected void createButtons(Composite buttonBox) {
		addButton = createPushButton(buttonBox, "Add"); //$NON-NLS-1$
		removeButton = createPushButton(buttonBox, "Remove"); //$NON-NLS-1$
		updateButton = createPushButton(buttonBox, "Update"); //$NON-NLS-1$
		upButton = createPushButton(buttonBox, "Page up"); //$NON-NLS-1$
		downButton = createPushButton(buttonBox, "Page down"); //$NON-NLS-1$
	}

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent the parent control
	 * @param key the resource name used to supply the button's label text
	 */
	protected Button createPushButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(label);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		//data.heightHint = convertVerticalDLUsToPixels(button, IDialogConstants.BUTTON_HEIGHT);
		int widthHint = convertHorizontalDLUsToPixels(button, IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT, SWT.DEFAULT, true).x);
		//		data.horizontalSpan = hs;  use to have multiple buttons on the same row
		button.setLayoutData(data);
		button.addSelectionListener(getSelectionListener());
		return button;
	}
	/**
	 * Creates a selection listener.
	 */
	public void createSelectionListener() {
		selectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				Widget widget = event.widget;
				if (widget == addButton) {
					addPressed();
				} else if (widget == removeButton) {
					removePressed();
				} else if (widget == updateButton) {
					updatePressed();
				} else if (widget == upButton) {
					upPressed();
				} else if (widget == downButton) {
					downPressed();
				} else if (widget == list) {
					selectionChanged();
				}
			}
		};
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doFillIntoGrid(Composite parent, int numColumns) {

		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		list = getListControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		list.setLayoutData(gd);

		buttonBox = getButtonBoxControl(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		buttonBox.setLayoutData(gd);
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doLoad() {
		if (list != null) {
			String s = getPreferenceStore().getString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doLoadDefault() {
		if (list != null) {
			list.removeAll();
			String s = getPreferenceStore().getDefaultString(getPreferenceName());
			String[] array = parseString(s);
			for (int i = 0; i < array.length; i++) {
				list.add(array[i]);
			}
		}
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	protected void doStore() {
		String s = createList(list.getItems());
		if (s != null)
			getPreferenceStore().setValue(getPreferenceName(), s);
	}
	/**
	 * Notifies that the Down button has been pressed.
	 */
	protected void downPressed() {
		swap(false);
	}
	/**
	 * Returns this field editor's button box containing the Add, Remove,
	 * Up, and Down button.
	 *
	 * @param parent the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxControl(Composite parent) {
		if (buttonBox == null) {
			buttonBox = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.numColumns = 1; // change value to have multiple buttons on the same row
			layout.marginWidth = 0;
			layout.marginHeight = 0;
			layout.verticalSpacing = 0;
			buttonBox.setLayout(layout);
			createButtons(buttonBox);
			buttonBox.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					addButton = null;
					removeButton = null;
					upButton = null;
					downButton = null;
					buttonBox = null;
				}
			});

		} else {
			checkParent(buttonBox, parent);
		}

		selectionChanged();
		return buttonBox;
	}
	/**
	 * Returns this field editor's list control.
	 *
	 * @param parent the parent control
	 * @return the list control
	 */
	public List getListControl(Composite parent) {
		if (list == null) {
			list = new List(parent, SWT.BORDER | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);
			list.addSelectionListener(getSelectionListener());
			list.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent event) {
					list = null;
				}
			});
		} else {
			checkParent(list, parent);
		}
		return list;
	}

	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	public int getNumberOfControls() {
		return 2;
	}
	/**
	 * Returns this field editor's selection listener.
	 * The listener is created if nessessary.
	 *
	 * @return the selection listener
	 */
	protected SelectionListener getSelectionListener() {
		if (selectionListener == null)
			createSelectionListener();
		return selectionListener;
	}
	/**
	 * Returns this field editor's shell.
	 * <p>
	 * This method is internal to the framework; subclassers should not call
	 * this method.
	 * </p>
	 *
	 * @return the shell
	 */
	protected Shell getShell() {
		if (addButton == null)
			return null;
		return addButton.getShell();
	}

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	protected void removePressed() {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		if (index >= 0) {
			list.remove(index);
			selectionChanged();
		}
	}
	/**
	 * Notifies that the list selection has changed.
	 */
	protected void selectionChanged() {

		int index = list.getSelectionIndex();
		int size = list.getItemCount();

		removeButton.setEnabled(index >= 0);
		upButton.setEnabled(size > 1 && index > 0);
		downButton.setEnabled(size > 1 && index >= 0 && index < size - 1);
	}
	/* (non-Javadoc)
	 * Method declared on FieldEditor.
	 */
	public void setFocus() {
		if (list != null) {
			list.setFocus();
		}
	}
	/**
	 * Moves the currently selected item up or down.
	 *
	 * @param up <code>true</code> if the item should move up,
	 *  and <code>false</code> if it should move down
	 */
	protected void swap(boolean up) {
		setPresentsDefaultValue(false);
		int index = list.getSelectionIndex();
		int target = up ? index - 1 : index + 1;

		if (index >= 0) {
			String[] selection = list.getSelection();
			list.remove(index);
			list.add(selection[0], target);
			list.setSelection(target);
		}
		selectionChanged();
	}
	/**
	 * Notifies that the Up button has been pressed.
	 */
	protected void upPressed() {
		swap(true);
	}

	/**
	 * Combines the given list of items into a single string.
	 * This method is the converse of <code>parseString</code>. 
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param items the list of items
	 * @return the combined string
	 * @see #parseString
	 */

	protected String createList(String[] items) {
		StringBuffer path = new StringBuffer(""); //$NON-NLS-1$

		for (int i = 0; i < items.length; i++) {
			path.append(items[i]);
			path.append(DEF_SEPARATOR);
		}
		return path.toString();
	}

	/**
	 * Creates and returns a new item for the list.
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @return a new item
	 */
	protected String getNewInputObject() {

		String defaultValue = ""; //$NON-NLS-1$
		if (list.getSelection().length != 0) {
			defaultValue = list.getSelection()[0];
		}

		InputDialog dialog = new InputDialog(getShell(), Messages.getString("ListFieldEditor.3"), Messages.getString("ListFieldEditor.4"), defaultValue, null); //$NON-NLS-1$ //$NON-NLS-2$
		String param = null;
		int dialogCode = dialog.open();
		if (dialogCode == InputDialog.OK) {
			param = dialog.getValue();
			if (param != null) {
				param = param.trim();
				if (param.length() == 0)
					return null;
			}
		}
		return param;
	}

	/**
	 * Splits the given string into a list of strings.
	 * This method is the converse of <code>createList</code>. 
	 * <p>
	 * Subclasses must implement this method.
	 * </p>
	 *
	 * @param stringList the string
	 * @return an array of <code>String</code>
	 * @see #createList
	 */
	protected String[] parseString(String stringList) {
		StringTokenizer st = new StringTokenizer(stringList, DEF_SEPARATOR); //$NON-NLS-1$
		java.util.List<String> v = Lists.newArrayList();
		while (st.hasMoreElements()) {
			v.add(st.nextElement().toString());
		}
        // Java 8 compatibility mode, so String[]::new is not available
		return v.toArray(new String[v.size()]);
	}
}
