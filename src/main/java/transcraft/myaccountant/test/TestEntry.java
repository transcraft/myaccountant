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

import java.util.List;

import com.db4o.ObjectContainer;

import transcraft.BookKeeper.bom.Entity;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.BookKeeper.service.DAOService;

/**
 * @author david.tran@transcraft.co.uk
 */
public class TestEntry {
    public static void	main(String [] args) {
        dumpAll();
    }
    
    public static void dumpAll() {
        ObjectContainer dbServer = null;
        try {
            dbServer = DAOService.getDbServer();
	        BOMService bomService = new BOMService(dbServer);

	        List<Entry> entries = bomService.getRefData(Entry.class);
	        System.out.println("Entries"); //$NON-NLS-1$
	        for (Entry entry : entries) {
	            System.out.println(entry);
	        }
	        
	        /*
	        Ledger tdLedger = bomService.getLedger(dbServer, BOMService.TD_LEDGER, null);
	        System.out.println("TradeDate Ledger");
	        Collections.sort(tdLedger);
	        System.out.println(tdLedger.dump());
	        Ledger vdLedger = bomService.getLedger(dbServer, BOMService.VD_LEDGER, null);
	        System.out.println("ValueDate Ledger");
	        Collections.sort(vdLedger);
	        System.out.println(vdLedger.dump());
	        */
	        
	        System.out.println("Memorised Entities"); //$NON-NLS-1$
	        System.out.println(bomService.getMemorised("description", "st")); //$NON-NLS-1$ //$NON-NLS-2$
	        
	        System.out.println("Entities"); //$NON-NLS-1$
	        List<Entity> entities = bomService.getRefData(Entity.class);
	        for (Entity entity : entities) {
	            System.out.println(entity.getName());
	        }
	        System.out.println("Memorised List for description is " + bomService.getMemorisedList("description")); //$NON-NLS-1$ //$NON-NLS-2$
	        System.out.println("Memorised description for 'tr' is '" + bomService.getMemorised("description", "tr")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        } finally {
            if (dbServer != null) {
                dbServer.commit();
                dbServer.close();
            }
        }
    }
}
