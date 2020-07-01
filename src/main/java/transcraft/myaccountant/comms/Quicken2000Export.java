/**
 * Created on 19-Aug-2005
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
package transcraft.myaccountant.comms;

import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.utils.Formatters;

/**
 * exort of Quicken format data
 * 
 * @author david.tran@transcraft.co.uk
 */
public class Quicken2000Export {

    private BOMService bomService;
    private static final Logger log = getLogger(Quicken2000Export.class);
    
    public Quicken2000Export(BOMService bomService) {
        this.bomService = bomService;
    }

    public String	run(Account account) {
        return this.run(account, null);
    }
    
    public String	run(Account account, IProgressMonitor monitor) {
        StringBuffer buffer = new StringBuffer();
        try {
            this.bomService.setPublishEvent(false);
            List<Entry> entryList = this.bomService.getLedgerEntries(BOMService.TD_LEDGER, account);
            if (monitor != null) {
                monitor.beginTask("Exporting", entryList.size()); //$NON-NLS-1$
            }
            int i = 0;
            for (Entry entry : entryList) {
                RunningEntry re = (RunningEntry)entry;
                buffer.append(this.processRow(re, account));
                if (monitor != null) {
                    monitor.worked(i);
                    if (monitor.isCanceled()) {
                        return buffer.toString();
                    }
                }
            }
            if (monitor != null) {
                monitor.done();
            }

        } catch (Exception e) {
            log.error(this.getClass() + ".run():" + e); //$NON-NLS-1$
        } finally {
            this.bomService.setPublishEvent(true);
        }
        return buffer.toString();
    }
    
    protected String	getAccountName(String name) {
        if (this.bomService.getGeneric(Account.class, "reference", name) != null || //$NON-NLS-1$
                this.bomService.getGeneric(LedgerAccount.class, "reference", name) != null){ //$NON-NLS-1$
            name = "[" + name + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
        return name;        
    }
    
    public String	processRow(RunningEntry re, Account account)
    {
        String toAccount = Formatters.emptyIfNull(re.getToAccount());
        StringBuffer splitLines = new StringBuffer();
        double amount = re.getOriginalAmount();
        if (re.getToAccount() != null && re.getToAccount().equals(account.getReference())) {
            toAccount = re.getFromAccount();
            amount *= -1;
        }
        toAccount = this.getAccountName(toAccount);
        if (! toAccount.startsWith("[")) { //$NON-NLS-1$
            Allocation [] allocs = re.getAllocations(false);
            for (int i = 0; i < allocs.length; i++) {
                if (i == 0) {
                    toAccount = allocs[i].getAccount();
                    amount *= allocs[i].getPercentage();
                } else {
                    splitLines.append("\t\t" + this.getAccountName(allocs[i].getAccount()) + "\t\t" + //$NON-NLS-1$ //$NON-NLS-2$
                            (re.getOriginalAmount() * allocs[i].getPercentage()) + "\n"); //$NON-NLS-1$
                }
            }
        }
        return DateFormat.getDateInstance(DateFormat.SHORT).format(re.getPostingDate()) + "\t" + //$NON-NLS-1$
        	Formatters.emptyIfNull(re.getReference()) + "\t" + //$NON-NLS-1$
        	Formatters.emptyIfNull(re.getDescription()) + "\t\t" + //$NON-NLS-1$
        	toAccount + "\t" + //$NON-NLS-1$
        	"\t" + //$NON-NLS-1$
        	amount +
        	"\n" + splitLines; //$NON-NLS-1$
    }
}
