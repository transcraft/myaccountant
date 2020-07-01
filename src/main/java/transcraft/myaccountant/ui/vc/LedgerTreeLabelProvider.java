/**
 * Created on 24-Oct-2005
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

import java.util.List;

import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.BatchReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.pref.PreferenceFactory;


public class LedgerTreeLabelProvider implements ILabelProvider, IColorProvider, IFontProvider {
    List<ILabelProviderListener> listeners = Lists.newArrayList();
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
     */
    public Image getImage(Object element) {
        if (element.getClass().equals(Account.class)) {
            Account acct = (Account)element;
            return(Image)ImageCache.get(acct.isTaxed() ? "account_taxed" : "account"); //$NON-NLS-1$ //$NON-NLS-2$
        } else if (element.getClass().equals(LedgerAccount.class)) {
            Account acct = (Account)element;
            if (PreferenceFactory.getAsList(PreferenceFactory.INVOICE_ACCTS).contains(acct.getReference())) {
                return ImageCache.get("invoice"); //$NON-NLS-1$
            }
            return(Image)ImageCache.get("ledger"); //$NON-NLS-1$
        } else if (element.getClass().equals(ScheduledEntry.class)) {
            return(Image)ImageCache.get("clock"); //$NON-NLS-1$
        } else if (element.getClass().equals(BatchReport.class)) {
            return(Image)ImageCache.get("batchrpt"); //$NON-NLS-1$
        } else if (element instanceof BaseGenericReport) {
            return(Image)ImageCache.get("report"); //$NON-NLS-1$
        } else if (element.getClass().equals(AllocationRule.class)) {
                return(Image)ImageCache.get("split"); //$NON-NLS-1$
        } else if (element.getClass().equals(Invoice.class)) {
            return(Image)ImageCache.get("invoice"); //$NON-NLS-1$
        }
        return (Image)ImageCache.get("node"); //$NON-NLS-1$
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        return element.toString();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void addListener(ILabelProviderListener listener) {
        this.listeners.add(listener);
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
        return false;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
     */
    public Color getBackground(Object element) {
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
     */
    public Color getForeground(Object element) {
        if (element instanceof Invoice) {
            Invoice invoice = (Invoice)element;
            if (invoice.getEntryId() == null) {
                return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
            }
        }
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
     */
    public Font getFont(Object element) {
        if ((element instanceof BaseGenericReport)) {
            if (ReportUtil.isBuiltInReport((BaseGenericReport<?>)element)) {
                return GUIUtil.italicFont;
            }
        } else if (element instanceof LedgerAccount) {
            return GUIUtil.italicFont;
        }
        return null;
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
     */
    public void removeListener(ILabelProviderListener listener) {
        this.listeners.remove(listener);
    }
}