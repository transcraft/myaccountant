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

import java.util.HashMap;

/**
 * @author david.tran@transcraft.co.uk
 */
public class NumberFountain extends HashMap<String, Integer> {
    /**
	 * 
	 */
	private static final long serialVersionUID = -746498652399679460L;

	public synchronized Integer	getNextId(String fountain) {
        Integer id = null;
        try {
            id = (Integer)super.get(fountain);
            if (id == null) {
                id = Integer.valueOf(0);
            }
        } catch (Exception e) {
            id = Integer.valueOf(0);
        }
        id = Integer.valueOf(id.intValue() + 1);
        super.put(fountain, id);
        return id;
    }
}
