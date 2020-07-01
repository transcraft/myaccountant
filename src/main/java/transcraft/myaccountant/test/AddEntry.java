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
public class AddEntry extends TestEntry {
    static DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
    
    public static void	main(String [] args) {
        if (args.length < 4) {
            System.err.println("Usage: AddEntry ref desc account amount [tradeDate] [valueDate] [toAccount]"); //$NON-NLS-1$
            System.exit(1);
        }
        String ref = args[0];
        String desc = args[1];
        String account = args[2];
        double amount = Double.parseDouble(args[3]);
        Date postingDate = null;
        Date valueDate = null;
        String toAccount = null;
        ObjectContainer dbServer = null;
        try {
            if (args.length >= 5 && ! args[4].equalsIgnoreCase("null")) { //$NON-NLS-1$
                postingDate = dtf.parse(args[4]);
            }
            if (args.length >= 6 && ! args[5].equalsIgnoreCase("null")) { //$NON-NLS-1$
                valueDate = dtf.parse(args[5]);
            }
            if (args.length >= 7 && ! args[6].equalsIgnoreCase("null")) { //$NON-NLS-1$
                toAccount = args[6];
            }
            dbServer = DAOService.getDbServer();
            Entry entry = new Entry(null, ref, desc, account, toAccount, postingDate, valueDate, amount, null, null, 0);
	        BOMService bomService = new BOMService(dbServer);
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
