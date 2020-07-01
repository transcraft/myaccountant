/**
 * Created on 10-Oct-2005
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
package transcraft.BookKeeper.bom;

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.myaccountant.ui.pref.TaxBandHelper;
import transcraft.myaccountant.utils.DateUtil;

/**
 * invoice data
 * 
 * @author david.tran@transcraft.co.uk
 */
public class Invoice {
    private static final Logger LOG = getLogger(Invoice.class);

    private String reference;
    private Date invoicedDate;
    private Date startDate;
    private Date endDate;
    private String	clientName;
    private String	clientAddress;
    private String accountReference;
    private transient ArrayList<BOMListener> listeners = Lists.newArrayList();
    
    /**
     *  this value is only set once the invoice has been paid
     * 
     */
    private Integer entryId;
    /**
     * this value is suppressed until the entryId is set
     */
    private Date receivedDate;
    
    private List<InvoiceItem> items = Lists.newArrayList();
    
    /**
     * 
     */
    public Invoice(String reference) {
        this.reference = reference;
        this.invoicedDate = DateUtil.getTodayStart().getTime();
    }
    
    public void	addItem(InvoiceItem item) {
    	InvoiceItem oi = null;
    	int i = this.items.indexOf(item);
        if (i >= 0) {
        	oi = this.items.get(i);
        	this.items.set(i, item);
        } else {
            this.items.add(item);
        }
        // pseudo event to update totals
        this.notifyListener(new BOMEvent(this, oi, item, BOMEvent.OP_UPD));
    }
    
    public void	removeItem(InvoiceItem item) {
        if (this.items.contains(item)) {
            this.items.remove(item);
            // pseudo event to update totals
            this.notifyListener(new BOMEvent(this, item, null, BOMEvent.OP_UPD));
        }
    }
    
    public void	copy(Invoice invoice) {
        this.receivedDate = invoice.receivedDate;
        this.reference = invoice.reference;
        this.invoicedDate = invoice.invoicedDate;
        this.clientAddress = invoice.clientAddress;
        this.clientName = invoice.clientName;
        this.startDate = invoice.startDate;
        this.endDate = invoice.endDate;
        this.accountReference = invoice.accountReference;
        this.entryId = invoice.entryId;
        // must do a deep copy here, or will get problem in the cascade delete in DB4O
        this.items = Lists.newArrayList();
        for (InvoiceItem item : invoice.items) {
            this.addItem(new InvoiceItem(item));
        }
    }
    
    public void	reset() {
        this.items.clear();
    }
    
    public InvoiceItem []	getItems() {
    	return items
    		.stream()
    		// filter out spurious blank lines
    		.filter(item -> StringUtils.isNotBlank(item.getDescription()))
    		.toArray(InvoiceItem[]::new);
    }
    
    /**
     * @return Returns the amount.
     */
    public double getNetAmount() {
        double amount = 0.0;
        for (InvoiceItem item : this.items) {
            try {
                amount += item.getAmount();
            } catch (Exception e) {
                LOG.error(this.getClass() + ".getNetAmount():", e); //$NON-NLS-1$
            }
        }
        return amount;
    }
    
    /**
     * @return Returns the tax amount.
     */
    public double getTaxAmount() {
        double tax = 0.0;
        TaxBandHelper helper = new TaxBandHelper();
        for (InvoiceItem item : this.items) {
            tax += helper.calculateTax(item.getAmount(), new String[] { item.getTaxBand() });
        }
        return tax;
    }
    
    /**
     * @return Returns the gross amount.
     */
    public double getGrossAmount() {
        return this.getNetAmount() + this.getTaxAmount();
    }
    
    /**
     * @return Returns the clientAddress.
     */
    public String getClientAddress() {
        return clientAddress;
    }
    
    /**
     * @param clientAddress The clientAddress to set.
     */
    public void setClientAddress(String clientAddress) {
        this.clientAddress = clientAddress;
    }
    
    /**
     * @return Returns the accountReference.
     */
    public String getAccountReference() {
        return accountReference;
    }
    
    /**
     * @param accountReference The accountReference to set.
     */
    public void setAccountReference(String accountReference) {
        this.accountReference = accountReference;
    }
    
    /**
     * @return Returns the clientName.
     */
    public String getClientName() {
        return clientName;
    }
    
    /**
     * @param clientName The clientName to set.
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    /**
     * @return Returns the invoicedDate.
     */
    public Date getInvoicedDate() {
        return this.invoicedDate;
    }
    
    /**
     * @param invoicedDate The invoicedDate to set.
     */
    public void setInvoicedDate(Date invoicedDate) {
        this.invoicedDate = invoicedDate;
    }
    
    /**
     * @return Returns the receivedDate.
     */
    public Date getReceivedDate() {
        return this.receivedDate;
    }
    
    /**
     * @param receivedDate The receivedDate to set.
     */
    public void setReceivedDate(Date receivedDate) {
        this.receivedDate = receivedDate;
    }
    
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    
    /**
     * @return Returns the endDate.
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * @param endDate The endDate to set.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    /**
     * @return Returns the startDate.
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * @param startDate The startDate to set.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }
    
    /**
     * @return Returns the entryId.
     */
    public Integer getEntryId() {
        return entryId;
    }
    
    /**
     * @param entryId The entryId to set.
     */
    public void setEntryId(Integer entryId) {
        this.entryId = entryId;
    }
    
    /**
     * set tax band for all items
     * @param taxBand
     */
    public void	setTaxBand(String taxBand) {
        for (InvoiceItem item : this.items) {
            item.setTaxBand(taxBand);
        }
    }
    
    /**
     * for private use in cloning, can not be used outside this class
     *
     */
    private Invoice() {
        
    }
    
    /**
     * covenience method to create a new invoice using us as a template.
     * The date ranges will be calculated automatically, with the contents
     * duplicated. This is useful for services companies
     * @param newRef
     * @param newDate
     * @return
     */
    public Invoice	templateClone(String newRef, Date newDate) {
        Invoice invoice = new Invoice();
        invoice.copy(this);
        // very important, must override the reference to the one passed in !
        invoice.reference = newRef;
        // very important, mark this invoice as unpaid !!
        invoice.entryId = null;
        invoice.setInvoicedDate(newDate);
        if (this.getReceivedDate() != null) {
            long diff = this.getReceivedDate().getTime() - this.getInvoicedDate().getTime();
            long newDt = newDate.getTime() + diff;
            invoice.setReceivedDate(new Date(newDt));
        }
        if (this.getStartDate() != null) {
            long diff = this.getStartDate().getTime() - this.getInvoicedDate().getTime();
            long newDt = newDate.getTime() + diff;
            invoice.setStartDate(new Date(newDt));
        }
        if (this.getEndDate() != null) {
            long diff = this.getEndDate().getTime() - this.getInvoicedDate().getTime();
            long newDt = newDate.getTime() + diff;
            invoice.setEndDate(new Date(newDt));
        }
        return invoice;
    }
    
    public void	addListener(BOMListener listener) {
    	if (listeners == null) {
    		listeners = Lists.newArrayList();
    	}
        if (! this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }
    
    public void	removeListener(BOMListener listener) {
        this.listeners.remove(listener);
    }
    
    protected void	notifyListener(BOMEvent event) {
        for (BOMListener listener : this.listeners) {
            try {
                listener.dataChanged(event);
            } catch (Exception e) {
                LOG.error("notifyListerner("+event+")",e);
            }
        }
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        if (arg0 instanceof Invoice) {
            return this.reference.equals(((Invoice)arg0).reference);
        }
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.reference.hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.reference;
    }
}
