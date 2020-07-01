/**
 * Created on 25-Aug-2005
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

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.myaccountant.comms.Quicken2000Export;
import transcraft.myaccountant.ui.GUIUtil;

/**
 * Drag source for LedgerEntryViewer
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryDragAdapter implements DragSourceListener {
    private LedgerEntryViewer viewer;
    private String data;
    /**
     * 
     */
    public LedgerEntryDragAdapter(LedgerEntryViewer viewer) {
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragStart(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragStart(DragSourceEvent evt) {
        this.viewer.getEditor().processEscape();
        Object [] selectedEntries = this.getSelectedEntries();
        evt.doit = selectedEntries.length > 0;
        evt.detail = evt.doit ? DND.DROP_COPY : DND.DROP_NONE;
        if (evt.doit) {
            StringBuffer buffer = new StringBuffer();
            Quicken2000Export qex = new Quicken2000Export(this.viewer.getBomService());
            for (int i = 0; i < selectedEntries.length; i++) {
                RunningEntry re = (RunningEntry)selectedEntries[i];
                buffer.append(qex.processRow(re, this.viewer.getAccount()));
            }
            this.data = buffer.toString();
            evt.data = this.data;
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragSetData(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragSetData(DragSourceEvent evt) {
        evt.data = this.data;
    }

    /* (non-Javadoc)
     * @see org.eclipse.swt.dnd.DragSourceListener#dragFinished(org.eclipse.swt.dnd.DragSourceEvent)
     */
    public void dragFinished(DragSourceEvent evt) {
        if (evt.detail == DND.DROP_MOVE) {
	        Object [] selectedEntries = this.getSelectedEntries();
	        if (selectedEntries.length > 0) {
	            if (! GUIUtil.confirm(Messages.getString("LedgerEntryDragAdapter.0"), Messages.getString("LedgerEntryDragAdapter.1") + //$NON-NLS-1$ //$NON-NLS-2$
	                    this.viewer.getAccount().getReference() + "' ?")) { //$NON-NLS-1$
	                GUIUtil.showError(Messages.getString("LedgerEntryDragAdapter.3"), Messages.getString("LedgerEntryDragAdapter.4"), null); //$NON-NLS-1$ //$NON-NLS-2$
	                return;
	            }
	            for (int i = 0; i < selectedEntries.length; i++) {
	                RunningEntry re = (RunningEntry)selectedEntries[i];
	                if (re.getEntryId() != null) {
	                    this.viewer.getBomService().delete(re.getEntryId());
	                }
	            }
	            this.viewer.setBusy(false);
	        }
        }
    }

    protected Object []	getSelectedEntries() {
        TableViewer tbv = viewer.getTableViewer();
        Object [] selectedEntries = new Object[0];
        if (tbv instanceof CheckboxTableViewer) {
            selectedEntries = ((CheckboxTableViewer)tbv).getCheckedElements();
        } else if (! tbv.getSelection().isEmpty()) {
            IStructuredSelection sel = (IStructuredSelection)tbv.getSelection();
            selectedEntries = sel.toArray();
        }
        return selectedEntries;
    }
    
}
