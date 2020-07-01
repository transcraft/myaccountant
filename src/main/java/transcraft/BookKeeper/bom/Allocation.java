/**
 * Created on 18-Jul-2005
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

import java.io.Serializable;

public class Allocation implements Comparable<Allocation>, Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 8501138873448726324L;
	String account;
    double percentage;
    String description;
    
    public Allocation(String account, double percentage) {
        this.account = account;
        this.percentage = percentage;
    }
    
    public Allocation(Allocation alloc) {
        this.account = alloc.account;
        this.percentage = alloc.percentage;
        this.description = alloc.description;
    }

    public String getAccount() {
        return account;
    }

    public double getPercentage() {
        return percentage;
    }
    
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        try {
            Allocation alloc = (Allocation)arg0;
            if (! this.account.equals(alloc.account)) {
                return false;
            }
            if (this.description == null || alloc.description == null) {
                return true;
            }
            if (this.description != null && alloc.description != null &&
                    this.description.equals(alloc.description)) {
                return true;
            }
        } catch (Exception e) {}
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.account.hashCode() + (this.description != null ? this.description.hashCode() : 0);
    }
    
    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(Allocation arg0) {
        if (! (arg0 instanceof Allocation)) {
            return -1;
        }
        return this.account.compareTo(((Allocation)arg0).account);
    }
    
    public void setAccount(String account) {
        this.account = account;
    }
    
    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format("(%s[%s]=%f", this.description, this.account, this.percentage); //$NON-NLS-1$
    }
}