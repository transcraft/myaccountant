/**
 * Created on 24-Sep-2005
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

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Account Categories, facilitates reporting
 * 
 * @author david.tran@transcraft.co.uk
 */
public class Categories {

    protected List<String> categories = Lists.newArrayList();
    protected String forClass;
    
    /**
     * 
     */
    public Categories(String forClass) {
        this.forClass = forClass;
    }

    public void	addCategory(String category) {
        if (! this.categories.contains(category)) {
            this.categories.add(category);
        }
    }
    
    public String [] getList() {
        // Java 8 compatibility mode, so String[]::new is not available
        return categories.toArray(new String[categories.size()]);
    }
    
    /**
     * @return Returns the forClass.
     */
    public String getForClass() {
        return forClass;
    }
    /**
     * @param forClass The forClass to set.
     */
    public void setForClass(String forClass) {
        this.forClass = forClass;
    }
}
