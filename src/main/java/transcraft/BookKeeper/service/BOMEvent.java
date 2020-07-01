/**
 * Created on 01-Jul-2005
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
package transcraft.BookKeeper.service;

/**
 * a BOM Event
 * @author david.tran@transcraft.co.uk
 */
public class BOMEvent {
    public static final int	OP_ADD = 0;
    public static final int	OP_DEL = 1;
    public static final int	OP_UPD = 2;
    public static final int OP_BROADCAST = 3;
    public static final int	OP_USER = 4;
    
    public Object entity;
    public Object oldEntity;
    public Object source;
    public int	op;
    
    /**
     * convenience method to pass in all values
     * @param source
     * @param entity
     * @param oldEntity
     * @param op
     */
    public BOMEvent(Object source, Object entity, Object oldEntity, int op) {
        this.source = source;
        this.entity = entity;
        this.oldEntity = oldEntity;
        this.op = op;
    }
}
