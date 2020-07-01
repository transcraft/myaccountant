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

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.slf4j.LoggerFactory.getLogger;

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;

/**
 * import of Quicken format data
 * 
 * @author david.tran@transcraft.co.uk
 */
public class Quicken2000Import {
	private static final Logger LOG = getLogger(Quicken2000Import.class);
	
    private BOMService bomService;
    
    public Quicken2000Import(BOMService bomService) {
        this.bomService = bomService;
    }

    public void	run(Object data, Account account) {
        this.run(data, account, null);
    }
    
    public void	run(Object data, Account account, IProgressMonitor monitor) {
        try {
            this.bomService.setPublishEvent(false);
            String [] rows = data.toString().split("\n"); //$NON-NLS-1$
            if (monitor != null) {
                monitor.beginTask("Importing", rows.length); //$NON-NLS-1$
            }
            for (int i = 0; i < rows.length; i++) {
                LOG.debug("{}={}", i, rows[i]); //$NON-NLS-1$
                Entry entry = this.processRow(rows[i], account);
                if (entry == null) {
                    continue;
                }
                if (i < rows.length - 1) {
                    // look ahead to check for splits
                    while (i < (rows.length - 1)) {
                        Entry nextEntry = this.processRow(rows[i + 1], account);
                        if (nextEntry == null) {
                            this.bomService.store(entry);
                            break;
                        } else {
    	                    if (nextEntry.getEntity() != null && nextEntry.getEntity().equalsIgnoreCase("split")) { //$NON-NLS-1$
    	                        // merge the splits
    	                        double amount1 = entry.getAmount();
    	                        double amount2 = nextEntry.getAmount();
    	                        double total = amount1 + amount2;
    	                        entry.setAmount(total);
    	                        if (total != 0) {
                                    if (entry.getEntity() != null) {
                                        entry.addAllocation(entry.getEntity(), Math.abs(amount1) / total);
                                    }
    	                            entry.addAllocation(nextEntry.getToAccount(), Math.abs(amount2) / total);
    	                            entry.setToAccount(null);
    	                            entry.setEntity(null);
    	                        }
    	                        i++;
    	                    } else {
    	                        break;
                            }
                        }
                    }
                }
                this.bomService.store(entry);

                if (monitor != null) {
                    monitor.worked(i);
                    if (monitor.isCanceled()) {
                        return;
                    }
                }
            }
            if (monitor != null) {
                monitor.done();
            }
        } catch (Exception e) {
            LOG.error(this.getClass() + ".run():", e); //$NON-NLS-1$
        } finally {
            this.bomService.setPublishEvent(true);
        }
    }
    
    protected Entry	processRow(String row, Account account)
    {
        Entry entry = null;
        try {
            row = row.trim();
	        if (isBlank(row)) {
	            return entry;
	        }
	        String [] fields = row.split("\t"); //$NON-NLS-1$
	        if (fields.length == 3) {
	            String toAccount = fields[0].substring(1, fields[0].length() - 1);
		        double amount = Double.parseDouble(fields[2]);
		        entry = new Entry(null, null, null, account.getReference(), toAccount, null, null, amount, null, "split", 0); //$NON-NLS-1$
	        } else if (fields.length >= 7) {
	            DateFormat dtf = DateFormat.getDateInstance(DateFormat.SHORT);
	            String desc = fields[2].trim();
	            // this is not a split row
	            Date ts = dtf.parse(fields[0]);
	            String ref = fields[1].trim();
		        if (ref.endsWith(" S")) { //$NON-NLS-1$
		            int idx = ref.indexOf(" "); //$NON-NLS-1$
		            if (idx > 0) {
		                ref = ref.substring(0, idx);
		            }
		        }
	            String category = fields[4].trim();
	            String toAccount = category.startsWith("[") ? category.substring(1, category.length() - 1) : null; //$NON-NLS-1$
		        double amount = Double.parseDouble(fields[6]);
		        entry = new Entry(null, ref, desc, account.getReference(), toAccount, ts, ts, amount, null, category.length() > 0 ? category : null, 0);
		        /*
		         * if this is a tax tracked account and the entry is not a transfer between our known
		         * entry accounts, then we have to automatically convert this entry into a split allocation
		         * 
		         */
		        if (category != null && toAccount == null && account.isTaxed()) {
		            entry.addAllocation(category, amount < 0 ? -1.0 : 1.0);
		        }
	        }
	        
       } catch (Exception e) {
            LOG.error(row + ":", e); //$NON-NLS-1$
        }
        return entry;
    }
}
