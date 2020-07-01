/**
 * Created on 05-Jun-2005
 *
 * Copyrights (c) Transcraft Trading Limited 2003-2004. All rights reserved.
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

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.collections4.CollectionUtils;

import transcraft.myaccountant.utils.DateUtil;

/**
 * @author david.tran@transcraft.co.uk
 */
public class Entry extends AllocationRule implements Cloneable, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 6808135600139357746L;
	private Integer entryId;
    private String reference;
    private String description;
    private String fromAccount;
    private String toAccount;
    private Date ts;
    private Date postingDate;
    private Date valueDate;
    private double amount;
    private String currency;
    private String entity;
    private double quantity;
    private boolean reconciled;
    
    protected Entry() {
        super();
    }
    
    /**
     * @param entryId
     */
    public Entry(Integer entryId) {
        super(""); //$NON-NLS-1$
        this.entryId = entryId;
        this.ts = new Date();
        this.postingDate = DateUtil.datePart(this.ts);
        this.valueDate = this.postingDate;
   }
    /**
     * @param entryId
     * @param reference
     * @param description
     * @param fromAccount
     * @param amount
     */
    public Entry(Integer entryId, String reference, String description, String fromAccount,
            double amount) {
        this(entryId);
        this.reference = reference;
        this.description = description;
        this.fromAccount = fromAccount;
        this.amount = amount;
    }
    
    /**
     * @param reference
     * @param description
     * @param fromAccount
     * @param postingDate
     * @param amount
     */
    public Entry(Integer entryId, String reference, String description, String fromAccount,
            Date postingDate, double amount) {
        this(entryId, reference, description, fromAccount, amount);
        if (postingDate != null) {
            this.postingDate = postingDate;
        }
        this.valueDate = postingDate;
    }
    
    /**
     * @param reference
     * @param description
     * @param fromAccount
     * @param toAccount
     * @param postingDate
     * @param valueDate
     * @param amount
     * @param currency
     * @param entity
     * @param quantity
     */
    public Entry(Integer entryId, String reference, String description, String fromAccount,
            String toAccount, Date postingDate, Date valueDate, double amount,
            String currency, String entity, double quantity) {
        this(entryId, reference, description, fromAccount, postingDate, amount);
        this.toAccount = toAccount;
        if (valueDate != null) {
            this.valueDate = (valueDate.after(this.postingDate)) ? valueDate : this.postingDate;
        }
        this.currency = currency;
        this.entity = entity;
        this.quantity = quantity;
    }
    /**
     * Copy constructor
     * @param entry
     */
    public Entry(Entry entry) {
        super(entry);
        this.setEntry(entry);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {}
        throw new RuntimeException("I should not be here, as I am a Cloneable(" + Entry.class + ")"); //$NON-NLS-1$ //$NON-NLS-2$
    }
    /**
     * useful method row batch editing of the whole entry before committing
     * @param entry
     */
    public void	setEntry(Entry entry) {
        this.setEntryId(entry.getEntryId());
        this.setReference(entry.getReference());
        this.setDescription(entry.getDescription());
        this.setFromAccount(entry.getFromAccount());
        this.setToAccount(entry.getToAccount());
        this.setPostingDate(entry.getPostingDate());
        this.setValueDate(entry.getValueDate());
        this.setAmount(entry.getAmount());
        this.setCurrency(entry.getCurrency());
        this.setEntity(entry.getEntity());
        this.setQuantity(entry.getQuantity());
        this.setReconciled(entry.isReconciled());
        this.copyRule(entry);
    }
    
    /**
     * @return Returns the amount.
     */
    public double getAmount() {
        return amount;
    }
    /**
     * @param amount The amount to set.
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }
    /**
     * @return Returns the currency.
     */
    public String getCurrency() {
        return currency;
    }
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the entity.
     */
    public String getEntity() {
        return entity;
    }
    /**
     * @param entity The entity to set.
     */
    public void setEntity(String entity) {
        this.entity = entity;
    }
    /**
     * @return Returns the fromAccount.
     */
    public String getFromAccount() {
        return fromAccount;
    }
    /**
     * @param fromAccount The fromAccount to set.
     */
    public void setFromAccount(String fromAccount) {
        if (fromAccount != null) {
            this.fromAccount = fromAccount;
        }
    }
    /**
     * @return Returns the postingDate.
     */
    public Date getPostingDate() {
        return postingDate;
    }
    /**
     * @param postingDate The postingDate to set.
     */
    public void setPostingDate(Date postingDate) {
        if (postingDate != null) {
            this.postingDate = postingDate;
            if (this.entryId == null || this.valueDate == null || this.valueDate.before(postingDate)) {
                this.valueDate = postingDate;
            }
        }
    }
    /**
     * @return Returns the quantity.
     */
    public double getQuantity() {
        return quantity;
    }
    /**
     * @param quantity The quantity to set.
     */
    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
    /**
     * @return Returns the reference.
     */
    public String getReference() {
        return reference;
    }
    /**
     * @param reference The reference to set.
     */
    public void setReference(String reference) {
        this.reference = reference;
    }
    /**
     * @return Returns the toAccount.
     */
    public String getToAccount() {
        return toAccount;
    }
    /**
     * @param toAccount The toAccount to set.
     */
    public void setToAccount(String toAccount) {
        this.toAccount = toAccount;
    }
    /**
     * @return Returns the ts.
     */
    public Date getTs() {
        return ts;
    }
    /**
     * @return Returns the valueDate.
     */
    public Date getValueDate() {
        return valueDate;
    }
    /**
     * @param valueDate The valueDate to set.
     */
    public void setValueDate(Date valueDate) {
        this.valueDate = valueDate;
    }
    /**
     * @return Returns the reconciled.
     */
    public boolean isReconciled() {
        return reconciled;
    }
    /**
     * @param reconciled The reconciled to set.
     */
    public void setReconciled(boolean reconciled) {
        this.reconciled = reconciled;
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
     * convenience method to derive if our Entry is linked to an account. An Entry
     * is deemed linked to an account when its fromAccount, toAccount, or Allocations
     * reference the account
     * @param accountReference
     * @return
     */
    public boolean	isLinkedToAccount(String accountReference) {
        if (this.fromAccount.equals(accountReference)) {
            return true;
        }
        if (this.toAccount != null && this.toAccount.equals(accountReference)) {
            return true;
        }
        if (CollectionUtils.isNotEmpty(this.allocation)) {
            for (Allocation alloc : this.allocation){
                if (alloc.getAccount().equals(accountReference)) {
                    return true;
                }
            }
        }
        return false;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        try {
            return this.entryId.equals(((Entry)arg0).entryId);
        } catch (Exception e) {}
        return super.equals(arg0);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        try {
            return this.entryId.hashCode();
        } catch (Exception e) {}
        return super.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[id=" + this.entryId + ",ref=" + this.reference + ",desc=" + this.description + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	",from=" + this.fromAccount + ",to=" + this.toAccount + ",pd=" + this.postingDate + //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        	",vd=" + this.valueDate + ",amount=" + this.amount + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
