/**
 * Created on 02-Aug-2005
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


/**
 * @author david.tran@transcraft.co.uk
 */
public class LedgerAccount extends Account {

    /**
     * @param reference
     */
    public LedgerAccount(String reference) {
        super(reference);
        this.setExternal(false);
    }
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Account#setExternal(boolean)
     */
    public void setExternal(boolean external) {
        super.setExternal(false);
    }
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Account#setAccountNumber(java.lang.String)
     */
    public void setAccountNumber(String accountNumber) {
    }
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Account#setSortCode(java.lang.String)
     */
    public void setSortCode(String sortCode) {
    }
    
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Account#setDefaultTaxCode(java.lang.String)
     */
    public void setDefaultTaxCode(String defaultTaxCode) {
    }
    /* (non-Javadoc)
     * @see transcraft.BookKeeper.bom.Account#setTaxed(boolean)
     */
    public void setTaxed(boolean taxed) {
    }
}
