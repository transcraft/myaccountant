/**
 * Created on 22-Jun-2020
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

import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * a composite control with a text and auto complete suggestions
 * 
 * @author david.tran@transcraft.co.uk
 *
 */
public class AutoCompletableText extends Composite implements TraverseListener {

	private final Text textControl;
	private final AutoCompleteField acf;

	public AutoCompletableText(Composite parent, int style, String ...proposals) {
		super(parent, style);

		GridLayout ly = new GridLayout();
		ly.numColumns = 1;
		ly.horizontalSpacing = 0;
		ly.marginHeight = 0;
		ly.marginWidth = 0;
		ly.verticalSpacing = 0;

		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		this.setLayout(ly);

		textControl = new Text(this, SWT.BORDER_DASH);
		this.textControl.setLayoutData(gridData);
		this.textControl.addTraverseListener(this);

		acf = new AutoCompleteField(textControl, new TextContentAdapter(), proposals);        
	}

	public void addModifyListener(ModifyListener l) {
		textControl.addModifyListener(l);
	}

	public void removeModifyListener(ModifyListener l) {
		textControl.removeModifyListener(l);
	}

	public void setText(String str) {
		/*
		 * only set the value if if has changed, or the auto-complete
		 * popup goes crazy and gets activated!
		 */
		if (str != null) {
			if (!str.equals(this.textControl.getText())) {
				this.textControl.setText(str);
			} else {
				// ignore to prevent spurious Auto-suggest popup
			}
		} else if (this.textControl.getText().length() > 0) {
			this.textControl.setText("");
		}
	}

	public String getText() {
		return textControl.getText();
	}

	@Override
	public void keyTraversed(TraverseEvent evt) {
		switch(evt.keyCode) {
		case SWT.TAB:
		case SWT.TRAVERSE_TAB_NEXT:
		case SWT.TRAVERSE_TAB_PREVIOUS:
			evt.doit = true;
			break;
		}
	}

	public void updateProposals(String [] values) {
		this.acf.setProposals(values);
	}
}
