/**
 * Created on 25-Oct-2005
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
package transcraft.myaccountant.report;

import java.util.stream.Collectors;

import transcraft.BookKeeper.bom.Invoice;

/**
 * report for unpaid invoices
 * @author david.tran@transcraft.co.uk
 */
public class UnpaidInvoiceReport extends InvoiceListReport {

    /**
     * @param title
     * @param entries
     */
    public UnpaidInvoiceReport() {
        super(Messages.getString("UnpaidInvoiceReport.0")); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.report.BaseGenericReport#run()
     */
    public void run() {
        this.entries.clear();
        if (this.bomService != null) {
            this.entries.addAll(this.bomService.getRefData(Invoice.class));
            this.entries = this.entries.stream().sorted((o1, o2) -> {
                   try {
                       return ((Invoice)o1).getReference().compareTo(((Invoice)o2).getReference());
                   } catch (Exception e) {}
                   return -1;
            })
            .filter(i -> i.getEntryId() == null)
    	    .collect(Collectors.toList());
        }
        super.run();
    }
}
