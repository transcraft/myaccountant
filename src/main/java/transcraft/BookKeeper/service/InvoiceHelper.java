/**
 * Created on 07-Nov-2005
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
package transcraft.BookKeeper.service;

import java.text.NumberFormat;
import java.util.List;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Invoice;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceHelper {

    /**
     * 
     */
    public InvoiceHelper() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * automatically derive the next invoice reference
     * @param invoice
     * @return
     */
    public String	getNextReference(BOMService bomService) {
        try {
            List<Invoice> invoices = bomService.getRefData(Invoice.class);
            if (invoices.size() == 0) {
                return ""; //$NON-NLS-1$
            }
            // obtain list of current invoice references
            List<String> currentRefs = Lists.newArrayList();
            for (Invoice inv : invoices) {
                currentRefs.add(inv.getReference());
            }
            
            // get the first invoice to derive the pattern
            Invoice invoice = (Invoice)invoices.get(0);
            String ref = invoice.getReference();
            // search for the start of the digits
            int idx = 0;
            for (; idx < ref.length(); idx++) {
                if (Character.isDigit(ref.charAt(idx))) {
                    break;
                }
            }
            // convert the digits into a number
            int nextRef = Integer.parseInt(ref.substring(idx));

            // derive the prefix
            String prefix = ref.substring(0, idx);
            
            // now advance until we hit an unused number
            int len = ref.length() - idx;
            NumberFormat nf = NumberFormat.getIntegerInstance();
            nf.setMinimumIntegerDigits(len);
            nf.setGroupingUsed(false);
            nextRef++;
            String newRef = prefix + nf.format(nextRef);
            while (currentRefs.contains(newRef)) {
                nextRef++;
                newRef = prefix + nf.format(nextRef);
            }
            
            return newRef;
        } catch (Exception e) {}
        return ""; //$NON-NLS-1$
    }
    
}
