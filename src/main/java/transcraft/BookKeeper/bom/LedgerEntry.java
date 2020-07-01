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

import java.util.Date;

/**
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntry implements Comparable<LedgerEntry> {
    private Date postingDate;
    private double amount;
    private Integer entryId;
    private String accountReference;
        
    /**
     * @param postingDate
     * @param amount
     */
    public LedgerEntry(Integer entryId, String accountReference, Date postingDate, double amount) {
        super();
        this.entryId = entryId;
        this.accountReference = accountReference;
        this.postingDate = postingDate;
        this.amount = amount;
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
     * @return Returns the postingDate.
     */
    public Date getPostingDate() {
        return postingDate;
    }
    /**
     * @return Returns the entryId.
     */
    public Integer getEntryId() {
        return entryId;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(LedgerEntry arg0) {
        if (this.entryId == null) {
            return 1;
        }
        try {
            LedgerEntry le = (LedgerEntry)arg0;
            if (this.postingDate.equals(le.postingDate)) {
                return this.getEntryId().compareTo(le.getEntryId());
            }
            return this.postingDate.compareTo(le.postingDate);
        } catch (Exception e) {}
        return 1;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "[id=" + this.getEntryId() + ",account=" + this.getAccountReference() + //$NON-NLS-1$ //$NON-NLS-2$
        	",date=" + this.getPostingDate() + ",amount=" + this.getAmount() + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    /**
     * @return Returns the accountReference.
     */
    public String getAccountReference() {
        return accountReference;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        if (! (arg0 instanceof LedgerEntry)) {
            return false;
        }
        LedgerEntry le = (LedgerEntry)arg0;
        return this.getEntryId().equals(le.getEntryId()) && this.getAccountReference().equals(le.getAccountReference());
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return (this.getAccountReference() + this.getEntryId()).hashCode();
    }
}
