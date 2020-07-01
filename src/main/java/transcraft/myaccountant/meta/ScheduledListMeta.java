/**
 * Created on 05-Jul-2005
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
package transcraft.myaccountant.meta;

import java.text.ParseException;

import transcraft.BookKeeper.bom.ScheduledEntry;

/**
 * descriptor for fields in the ScheduledListViewer object.
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ScheduledListMeta extends ScheduledEntryMeta {
    
    public ScheduledListMeta() {
        super();
        this.getColumn(ALLOC_PROP).setVisible(false);
        this.getColumn(SCH_FREQ_PROP).setVisible(false);
        this.getColumn(SCH_VDINCR_PROP).setVisible(false);
        this.getColumn(PD_PROP).setSortable(true);
        this.getColumn(DESC_PROP).setSortable(true);
        this.getColumn(FR_ACCT_PROP).setSortable(true);
        this.getColumn(AMT_PROP).setSortable(true);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    public void setValue(ScheduledEntry entry, String accountReference,
            String columnName, Object value) throws ParseException {
        // do nothing
    }
}
