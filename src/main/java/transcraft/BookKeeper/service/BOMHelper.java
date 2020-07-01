/**
 * Created on 18-Oct-2005
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.Calendar;
import java.util.List;

import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * Helper class for BOM operations
 * 
 * @author david.tran@transcraft.co.uk
 */
public class BOMHelper {

    private BOMService bomService;
    /**
     * 
     */
    public BOMHelper(BOMService bomService) {
        this.bomService = bomService;
    }

    public void	generateEntries(Invoice [] invoices, String accountReference, boolean markAsPaid) {
        List<String> invoiceAccounts = PreferenceFactory.getAsList(PreferenceFactory.INVOICE_ACCTS);
        if (invoiceAccounts.size() == 0) {
            throw new RuntimeException(Messages.getString("BOMHelper.0")); //$NON-NLS-1$
        }
        for (int i = 0; i < invoices.length; i++) {
            Invoice invoice = invoices[i];
            Entry entry = invoice.getEntryId() != null ? this.bomService.getEntry(invoice.getEntryId()) :
                (Entry)this.bomService.getGeneric(Entry.class, "reference", invoice.getReference()); //$NON-NLS-1$
            if (entry == null) {
                entry = new Entry(null, invoice.getReference(), invoice.getClientName(), accountReference, invoice.getGrossAmount());
            } else {
                entry.setDescription(invoice.getClientName());
                entry.setFromAccount(accountReference);
                entry.setAmount(invoice.getGrossAmount());
                entry.reset();
            }
            entry.setPostingDate(invoice.getInvoicedDate());
            entry.setValueDate(invoice.getReceivedDate() != null ? invoice.getReceivedDate() : invoice.getInvoicedDate());
            for (int j = 0; j < invoice.getItems().length; j++) {
                InvoiceItem item = invoice.getItems()[j];
                entry.addAllocation((String)invoiceAccounts.get(0), item.getAmount() / invoice.getGrossAmount(), item.getDescription());
                double taxAmount = invoice.getTaxAmount();
                if (taxAmount > 0) {
                    entry.addAllocation(BOMService.LCAT_VAT, item.getTax() / invoice.getGrossAmount(), item.getDescription());
                }
            }
            this.bomService.store(entry);
            invoice.setAccountReference(accountReference);
            if (markAsPaid) {
                invoice.setEntryId(entry.getEntryId());
            }
            this.bomService.storeGeneric(invoice, "reference", invoice.getReference()); //$NON-NLS-1$
        }
    }
    
    /**
     * generate invoices from running entries
     * @param entries
     */
    public void	generateInvoices(RunningEntry [] entries) {
        List<String> invoiceAccounts = PreferenceFactory.getAsList(PreferenceFactory.INVOICE_ACCTS);
        if (invoiceAccounts.size() == 0) {
            throw new RuntimeException(Messages.getString("BOMHelper.3")); //$NON-NLS-1$
        }
        for (int i = 0; i < entries.length; i++) {
            RunningEntry re = entries[i];
            if (isBlank(re.getReference())) {
                continue;
            }
            Invoice invoice = (Invoice)this.bomService.getGeneric(Invoice.class, "reference", re.getReference()); //$NON-NLS-1$
            if (invoice == null) {
                invoice = new Invoice(re.getReference());
            } else {
                // remove the existing items
                invoice.reset();
            }
            invoice.setAccountReference(re.getFromAccount());
            invoice.setInvoicedDate(re.getPostingDate());
            invoice.setReceivedDate(re.getValueDate());
            invoice.setClientName(re.getDescription());
            invoice.setEntryId(re.getEntryId());
            invoice.setClientAddress(this.getClientAddress(invoice.getClientName()));
            Allocation [] allocs = re.getAllocations(false);
            for (int j = 0; j < allocs.length; j++) {
                if (invoiceAccounts.contains(allocs[j].getAccount())) {
                    InvoiceItem item = new InvoiceItem();
                    item.setAmount(re.getOriginalAmount() * allocs[j].getPercentage());
                    item.setDescription(allocs[j].getDescription() != null ? allocs[j].getDescription() : "Services"); //$NON-NLS-1$
                    invoice.addItem(item);
                }
            }
            this.bomService.storeGeneric(invoice, "reference", invoice.getReference()); //$NON-NLS-1$
        }
    }

    /**
     * performs a certain amount of loose token matching
     * to try to match the client address
     * @param clientName
     * @return
     */
    private static final int CLIENT_NAME_THRESHOLD = 3;
    
    public String	getClientAddress(String clientName) {
        if (clientName == null || clientName.length() <= CLIENT_NAME_THRESHOLD) {
            return null;
        }
        List<Invoice> invoices = this.bomService.getRefData(Invoice.class);
        String [] tokens = clientName.split("\\W"); //$NON-NLS-1$
        for (Invoice invoice : invoices) {
            if (isBlank(invoice.getClientAddress())) {
                continue;
            }
            String name = invoice.getClientName();
            for (int i = 0; i < tokens.length - 1; i++) {
                String token = (String)tokens[i];
                if (token.length() <= CLIENT_NAME_THRESHOLD) {
                    continue;
                }
                if (name.indexOf(token) >= 0) {
                    return invoice.getClientAddress();
                }
            }
        }
        return null;
    }

    /**
     * make a scheduled payment
     * @param se
     */
    public void	makeScheduledPayment(ScheduledEntry se){
        Entry entry = new Entry(se);
        // indicate a new entry
        entry.setEntryId(null);
        if (se.getName() != null) {
            AllocationRule rule = (AllocationRule)this.bomService.getGeneric(AllocationRule.class, "name", se.getName()); //$NON-NLS-1$
            if (rule != null) {
                entry.copyRule(rule);
            }
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(entry.getPostingDate());
        cal.add(Calendar.DAY_OF_MONTH, se.getScheduleVDIncr());
        entry.setValueDate(cal.getTime());
        this.bomService.store(entry);
        if (se.getScheduleCount() > 0) {
            se.setScheduleCount(se.getScheduleCount() - 1);
        }
        cal.setTime(entry.getPostingDate());
        cal.add(se.getScheduleType(), se.getScheduleFrequency());
        se.setPostingDate(cal.getTime());
        if (se.getEntryId() == null) {
            se.setEntryId(this.bomService.getNextId(ScheduledEntry.class));
        }
        this.bomService.storeGeneric(se, "entryId", se.getEntryId()); //$NON-NLS-1$
    }

}
