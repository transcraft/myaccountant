/**
 * Created on 17-Oct-2005
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

import java.util.Date;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * label provider for LedgerEntryViewer (refactored to make it more CVS friendly)
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryLabelProvider implements ITableLabelProvider, IColorProvider, IFontProvider {
	private static final Logger log = getLogger(LedgerEntryLabelProvider.class);
	
    private LedgerEntryViewer viewer;


    /**
     * @param viewer
     */
    LedgerEntryLabelProvider(LedgerEntryViewer viewer) {
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
        if (! (element instanceof RunningEntry)) {
            return null;
        }
        if (this.viewer.account instanceof LedgerAccount) {
            return null;
        }
        RunningEntry re = (RunningEntry)element;
        MetaColumn<?> column = this.viewer.metaProvider.getColumns()[columnIndex];
        if (column.getName().equals(LedgerEntryMeta.PD_PROP)) {
            return re.isReconciled() ? ImageCache.get("links") : null; //$NON-NLS-1$
        } else if (column.getName().equals(LedgerEntryMeta.ALLOC_PROP)) {
        	return re.getTotalAllocation() > 0 ? ImageCache.get("split") : null; //$NON-NLS-1$
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
        if (! (element instanceof RunningEntry)) {
            return null;
        }
        try {
            return this.viewer.metaProvider.getValue((RunningEntry)element, this.viewer.metaProvider.getColumns()[columnIndex].getName(), this.viewer.account.getReference()).toString();
        } catch (Exception e) {
            log.error(this.getClass() + ".getColumnText(" + columnIndex + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
     */
    public void dispose() {
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
     */
    public boolean isLabelProperty(Object element, String property) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
    }
    
        /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        if (element instanceof RunningEntry) {
            RunningEntry re = (RunningEntry)element;
            if (this.viewer.closingBalanceEntry != null && re.getEntryId() != null &&
                    re.getEntryId().equals(this.viewer.closingBalanceEntry.getEntryId())) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
            }
        }
        //return Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        if (element instanceof RunningEntry) {
            RunningEntry entry = (RunningEntry)element;
            Date today = new Date();
            if ((this.viewer.pdButton.getSelection() && entry.getPostingDate().after(today)) ||
                    (this.viewer.vdButton.getSelection() && entry.getValueDate().after(today))) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND);
            } else if (this.viewer.closingBalanceEntry != null && entry.getEntryId() != null &&
                    entry.getEntryId().equals(this.viewer.closingBalanceEntry.getEntryId())) {
                return Display.getCurrent().getSystemColor(entry.getBalance() < 0 ? SWT.COLOR_RED : SWT.COLOR_BLUE);
            }
        }
        return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    }

    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        if (element instanceof RunningEntry) {
            RunningEntry entry = (RunningEntry)element;
            if (this.viewer.closingBalanceEntry != null && entry.getEntryId() != null &&
                    entry.getEntryId().equals(this.viewer.closingBalanceEntry.getEntryId())) {
                return GUIUtil.boldFont;
            }
        }
        return null;
    }
}