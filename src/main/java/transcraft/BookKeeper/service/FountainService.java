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
package transcraft.BookKeeper.service;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;

import transcraft.BookKeeper.bom.NumberFountain;

/**
 * @author david.tran@transcraft.co.uk
 */
public class FountainService {
    public Integer	getNextId(ObjectContainer dbServer, String fountain) {
        boolean myServer = false;
        Integer nextId = Integer.valueOf(1);
        try {
            if (dbServer == null) {
                dbServer = DAOService.getDbServer();
                myServer = true;
            }
            NumberFountain nf = null;
            ObjectSet<NumberFountain> rs = dbServer.query(NumberFountain.class);
            if (rs.hasNext()) {
                nf = (NumberFountain)rs.next();
            } else {
                nf = new NumberFountain();
            }
            nextId = nf.getNextId(fountain);
            dbServer.store(nf);
        } finally {
            if (myServer && dbServer != null) {
                dbServer.commit();
                dbServer.close();
            }
        }
        return nextId;
    }
}
