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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.report.ReportUtil;


public class LedgerTreeContentProvider implements ITreeContentProvider {
    public static final String ACCOUNTS = Messages.getString("LedgerTreeContentProvider.0"); //$NON-NLS-1$
    public static final String LEDGERACCOUNTS = Messages.getString("LedgerTreeContentProvider.1"); //$NON-NLS-1$
    public static final String SCHEDULEDENTRIES = Messages.getString("LedgerTreeContentProvider.2"); //$NON-NLS-1$
    public static final String REPORTS = Messages.getString("LedgerTreeContentProvider.3"); //$NON-NLS-1$
    public static final String ALLOCRULES = Messages.getString("LedgerTreeContentProvider.4"); //$NON-NLS-1$
    public static final String INVOICES = Messages.getString("LedgerTreeContentProvider.5"); //$NON-NLS-1$
    
    private BOMService bomService;
    
    public LedgerTreeContentProvider(BOMService bomService) {
        this.bomService = bomService;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
    	if (parentElement.equals(ACCOUNTS)) {
    		return bomService.getRefData(Account.class)
    				.stream()
    				.filter(account -> account.toString().trim().length() > 0)
    				.sorted()
    				.toArray(Account[]::new);
    	} else if (parentElement.equals(LEDGERACCOUNTS)) {
    		return bomService.getRefData(LedgerAccount.class)
    				.stream()
    				.sorted()
    				.toArray(LedgerAccount[]::new);
    	} else if (parentElement.equals(SCHEDULEDENTRIES)) {
    		return bomService.getRefData(ScheduledEntry.class)
    				.stream()
    				.sorted((se1, se2) -> se1.getDescription().compareTo(se2.getDescription()))
    				.toArray(ScheduledEntry[]::new);
    	} else if (parentElement.equals(REPORTS)) {
    		return ReportUtil.getAvailableReports(bomService);
    	} else if (parentElement.equals(ALLOCRULES)) {
    		return bomService.getRefData(AllocationRule.class)
    				.stream()
    				.toArray(AllocationRule[]::new);
    	} else if (parentElement.equals(INVOICES)) {
    		return bomService.getRefData(Invoice.class)
    				.stream()
    				.sorted((i1, i2) -> i1.getReference().compareTo(i2.getReference())
    						)
    				.toArray(Invoice[]::new);
    	}

    	return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        if (element.equals(ACCOUNTS)) {
            return bomService.getRefDataCount(Account.class) > 0;
        } else if (element.equals(LEDGERACCOUNTS)) {
            return bomService.getRefDataCount(LedgerAccount.class) > 0;
        } else if (element.equals(SCHEDULEDENTRIES)) {
            return bomService.getRefDataCount(ScheduledEntry.class) > 0;
        } else if (element.equals(REPORTS)) {
            return ReportUtil.getAvailableReports().length > 0;
        } else if (element.equals(ALLOCRULES)) {
                return bomService.getRefDataCount(AllocationRule.class) > 0;
        } else if (element.equals(INVOICES)) {
            return bomService.getRefDataCount(Invoice.class) > 0;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
     */
    @Override
    public Object[] getElements(Object inputElement) {
        return new String [] { ACCOUNTS, INVOICES, REPORTS, LEDGERACCOUNTS, SCHEDULEDENTRIES, ALLOCRULES };
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