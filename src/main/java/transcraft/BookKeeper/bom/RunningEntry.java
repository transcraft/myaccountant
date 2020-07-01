/**
 * Created on 16-Jun-2005
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

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transcraft.myaccountant.utils.Formatters;

/**
 * @author david.tran@transcraft.co.uk
 */
public class RunningEntry extends Entry {
	private static final Logger LOG = LoggerFactory.getLogger(RunningEntry.class);
	
    /**
	 * 
	 */
	private static final long serialVersionUID = -3888937014338917836L;
	private double balance = 0;
    private Account forAccount;
    /**
     * this is the original amount before the posting. This is used
     * when the forAccount above is a LedgerAccount
     */
    private double originalAmount;
    
    public RunningEntry(Account forAccount) {
        this(null, "", "", forAccount.getReference(), "", new Date(), new Date(), 0, null, null, 0, forAccount); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    
    public RunningEntry(Entry entry, Account forAccount) {
        super(entry);
        this.forAccount = forAccount;
        this.setOriginalAmount(super.getAmount());
        /*
         * make sure when we come out of this code, the fromAccount is set to the forAccount and
         * the amount reflects the correct amount
         */
        if (! super.getFromAccount().equals(forAccount.getReference())) {
            if (super.getToAccount() != null && super.getToAccount().equals(forAccount.getReference())) {
                // swap the accounts around
                super.setToAccount(super.getFromAccount());
                super.setAmount(super.getAmount() * -1);
                this.setOriginalAmount(this.getOriginalAmount() * -1);
            } else {
                // must be one of the allocations
                double amount = 0.0;
                for (Allocation alloc : super.allocation) {
                    if (alloc.getAccount().equals(forAccount.getReference())) {
                        amount += alloc.getPercentage() * Math.abs(super.getAmount());
                    }
                }
                super.setAmount(amount);
            }
            super.setFromAccount(forAccount.getReference());
        }
    }
    /**
     * @param entryId
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
    public RunningEntry(Integer entryId, String reference, String description,
            String fromAccount, String toAccount, Date postingDate,
            Date valueDate, double amount, String currency, String entity,
            double quantity, Account forAccount) {
        super(entryId, reference, description, fromAccount, toAccount,
                postingDate, valueDate, amount, currency, entity, quantity);
        this.forAccount = forAccount;
    }
    
    /**
     * @return Returns the originalAmount.
     */
    public double getOriginalAmount() {
        return originalAmount;
    }
    /**
     * @param originalAmount The originalAmount to set.
     */
    public void setOriginalAmount(double originalAmount) {
        this.originalAmount = originalAmount;
    }
    /**
     * @return the balance.
     */
    public double getBalance() {
        return balance;
    }
    /**
     * @param balance The balance to set.
     */
    public void setBalance(double balance) {
        this.balance = balance;
    }
    
    public String	getBalanceString() {
        return this.getEntryId() == null ? "" : Formatters.format(this.balance); //$NON-NLS-1$
    }
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Entry#getFromAccount()
     */
    @Override
    public String getFromAccount() {
        return this.forAccount.getReference();
    }
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Entry#getToAccount()
     */
    @Override
    public String getToAccount() {
        try {
	        if (super.getFromAccount().equals(this.forAccount.getReference())) {
	            return super.getToAccount();
	        }
        } catch (Exception e) {
            LOG.error("getToAccount()", e);
        }
        return super.getFromAccount();
    }
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Entry#setFromAccount(java.lang.String)
     */
    @Override
    public void setFromAccount(String fromAccount) {
        String account = super.getFromAccount();
        if (account == null || account.equals(this.forAccount.getReference())) {
            super.setFromAccount(fromAccount);
        } else {
            super.setToAccount(fromAccount);
        }
    }

    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Entry#setToAccount(java.lang.String)
     */
    @Override
    public void setToAccount(String toAccount) {
        String account = super.getFromAccount();
        if (account == null || forAccount == null || account.equals(this.forAccount.getReference())) {
            super.setToAccount(toAccount);
        } else {
            super.setFromAccount(toAccount);
        }
    }

    public double	getDebit() {
        double amount = this.getAmount();
        /*
        if (super.getToAccount() != null && super.getToAccount().equals(this.forAccount.getReference())) {
            amount *= -1;
        }
        */
        return amount < 0 ? (amount * -1) : 0;
    }
    
    public String	getDebitString() {
        double amount = this.getDebit();
        return amount != 0 ? Formatters.format(amount) : ""; //$NON-NLS-1$
    }
    
    public double	getCredit() {
        double amount = this.getAmount();
        /*
        if (super.getToAccount() != null && super.getToAccount().equals(this.forAccount.getReference())) {
            amount *= -1;
        }
        */
        return amount > 0 ? amount : 0;
    }
    
    public String	getCreditString() {
        double amount = this.getCredit();
        return amount != 0 ? Formatters.format(amount) : ""; //$NON-NLS-1$
    }
}
