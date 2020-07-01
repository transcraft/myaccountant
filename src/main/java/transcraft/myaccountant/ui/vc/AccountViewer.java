/**
 * Created on 15-Jul-2005
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

import java.util.Arrays;

import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Categories;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.AccountMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AccountViewer extends GenericMetaViewer<Account> implements BOMListener {

    /**
     * @param parent
     * @param account
     * @param provider
     */
    public AccountViewer(Composite parent, BOMService bomService, MetaProvider<Account> provider, Account account) {
        super(parent, bomService, provider, account);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.GenericMetaViewer#createContents()
     */
    protected void createContents() {
        // initialise the lists in the MetaColumns first before creating the Controls
        this.dataChanged(null);
        super.createContents();
        Button button = (Button)this.controls.get(this.metaProvider.getColumn(AccountMeta.EXT_PROP));
        if (button != null) {
        	button.addSelectionListener(new SelectionAdapter() {
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			toggleAccess();
        		}
        	});
        }
        button = (Button)this.controls.get(this.metaProvider.getColumn(AccountMeta.TAX_PROP));
        if (button != null) {
        	button.addSelectionListener(new SelectionAdapter() {
        		@Override
        		public void widgetSelected(SelectionEvent e) {
        			toggleAccess();
        		}
        	});
        }
        this.toggleAccess();
        this.bomService.addListener(this);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.entity instanceof Categories) {
            MetaColumn<?> column = this.metaProvider.getColumn(AccountMeta.CAT_PROP);
            if (column == null) {
                return;
            }
            String [] cats = this.bomService.getCategories(this.model).getList();
            column.setList(Arrays.asList(cats));
        }
    }
    public void	toggleAccess() {
        boolean enabled = false;
        Button button = (Button)this.controls.get(this.metaProvider.getColumn(AccountMeta.EXT_PROP));
        if (button != null) {
            enabled = button.getSelection();
        }
        Control sortCodeControl = this.controls.get(this.metaProvider.getColumn(AccountMeta.SCODE_PROP));
        if (sortCodeControl != null) {
            sortCodeControl.setEnabled(enabled);
        }
        Control acNoControl = this.controls.get(this.metaProvider.getColumn(AccountMeta.ACNO_PROP)); 
        if (acNoControl != null) {
            acNoControl.setEnabled(enabled);
        }
        enabled = false;
        button = (Button)this.controls.get(this.metaProvider.getColumn(AccountMeta.TAX_PROP));
        if (button != null) {
            enabled = button.getSelection();
        }
        Control defTaxControl = (Control)this.controls.get(this.metaProvider.getColumn(AccountMeta.DEFTAX_PROP));
        if (defTaxControl != null) {
            defTaxControl.setEnabled(enabled);
        }
    }
}
