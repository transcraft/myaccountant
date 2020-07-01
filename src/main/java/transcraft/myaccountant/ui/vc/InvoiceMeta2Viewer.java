/**
 * Created on 13-Oct-2005
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
package transcraft.myaccountant.ui.vc;

import org.eclipse.swt.widgets.Composite;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaProvider;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceMeta2Viewer extends GenericMetaViewer<Invoice> implements BOMListener {
    
    /**
     * @param parent
     * @param bomService
     * @param provider
     * @param obj
     */
    public InvoiceMeta2Viewer(Composite parent, BOMService bomService,
            MetaProvider<Invoice> provider, Invoice obj) {
        super(parent, bomService, provider, obj);
        model.addListener(this);
    }
    
    public void	createContents() {
        super.createContents();
        
        this.actionBar.setVisible(false);
        
        // initialise the dropdown list
        this.bomService.addListener(this);
        this.dataChanged(null);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.entity instanceof Invoice || evt.entity instanceof InvoiceItem) {
            this.refreshValues();
        }
    }
}
