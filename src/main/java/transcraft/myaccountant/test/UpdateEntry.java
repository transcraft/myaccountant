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
package transcraft.myaccountant.test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.db4o.ObjectContainer;

import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.BookKeeper.service.DAOService;

/**
 * @author david.tran@transcraft.co.uk
 */
public class UpdateEntry extends TestEntry {
    static DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$

    public static final String PD_PROP = "postingDate"; //$NON-NLS-1$
    public static final String VD_PROP = "valueDate"; //$NON-NLS-1$
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String FRACCT_PROP = "fromAccount"; //$NON-NLS-1$
    public static final String TOACCT_PROP = "toAccount"; //$NON-NLS-1$
    public static final String AMOUNT_PROP = "amount"; //$NON-NLS-1$
    
    public static void	main(String [] args) {
        if (args.length < 3) {
            System.err.println("Usage: UpdateEntry id field value"); //$NON-NLS-1$
            System.exit(1);
        }
        ObjectContainer dbServer = null;
        try {
            Integer entryId = Integer.valueOf(args[0]);
            dbServer = DAOService.getDbServer();
	        BOMService bomService = new BOMService(dbServer);
            Entry entry = bomService.getEntry(entryId);
            if (PD_PROP.equals(args[1])) {
                Date postingDate = dtf.parse(args[2]);
                entry.setPostingDate(postingDate);
            } else if (VD_PROP.equals(args[1])) {
                Date valueDate = dtf.parse(args[2]);
                entry.setPostingDate(valueDate);
            } else if (REF_PROP.equals(args[1])) {
                entry.setReference(args[2]);
            } else if (DESC_PROP.equals(args[1])) {
                entry.setDescription(args[2]);
            } else if (FRACCT_PROP.equals(args[1])) {
                entry.setFromAccount(args[2]);
            } else if (TOACCT_PROP.equals(args[1])) {
                entry.setToAccount(args[2]);
            } else if (AMOUNT_PROP.equals(args[1])) {
                entry.setAmount(Double.parseDouble(args[2]));
            }
	        bomService.store(entry);
        } catch (Exception e) {
            System.err.println(e);
        } finally {
            if (dbServer != null) {
                dbServer.close();
            }
        }
        dumpAll();
    }
}
