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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMHelper;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.InvoiceMeta1;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.ui.AutoCompletableText;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;
import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.utils.Formatters;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceMeta1Viewer extends GenericMetaViewer<Invoice>
	implements BOMListener, IPropertyChangeListener {
	private static final Logger log = getLogger(InvoiceMeta1Viewer.class);
	
    private InvoiceMeta2Viewer invoice2Viewer;
    private InvoiceItemViewer itemViewer;
    private Button generateEntryButton;
    
    /**
     * @param parent
     * @param bomService
     * @param provider
     * @param Invoice
     */
    public InvoiceMeta1Viewer(Composite parent, BOMService bomService,
            MetaProvider<Invoice> provider, Invoice Invoice) {
        super(parent, bomService, provider, Invoice);
    }
    
    public void	createContents() {
        super.createContents();
        
        final AutoCompletableText clientNameControl = (AutoCompletableText)this
        		.getControlForColumn(InvoiceMeta1.CLNTNAME_PROP);
        clientNameControl.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
                   String clientAddr = new BOMHelper(bomService).getClientAddress(clientNameControl.getText());
                   Text clientAddrControl = (Text)getControlForColumn(InvoiceMeta1.CLNTADDR_PROP);
                   clientAddrControl.setText(Formatters.emptyIfNull(clientAddr));
			}
		});
        
        this.generateEntryButton = new Button(this.actionBar, SWT.PUSH);
        this.generateEntryButton.setImage(ImageCache.get("invoice")); //$NON-NLS-1$
        this.generateEntryButton.setToolTipText(Messages.getString("InvoiceMeta1Viewer.1")); //$NON-NLS-1$
        this.generateEntryButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                generateAccountEntry();
            }
        });
        this.generateEntryButton.setVisible(! PreferenceFactory.getPreferenceStore().getBoolean(PreferenceFactory.INVOICE_AUTOLEDGER));
        
        // initialise the dropdown list
        this.bomService.addListener(this);
        this.dataChanged(null);
        PreferenceFactory.getPreferenceStore().addPropertyChangeListener(this);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    @Override
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.op == BOMEvent.OP_BROADCAST || evt.entity instanceof Invoice) {
            MetaColumn<?> column = this.metaProvider.getColumn(InvoiceMeta1.CLNTNAME_PROP);
            List<String> clientNames = this.bomService.getMemorisedList("clientName"); //$NON-NLS-1$
            column.setList(clientNames);
            if (evt != null && model.equals(evt.entity)) {
                model = (Invoice)evt.entity;
            }
            this.refreshValues();
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getProperty().equals(PreferenceFactory.INVOICE_AUTOLEDGER)) {
            this.generateEntryButton.setVisible(! PreferenceFactory
            		.getPreferenceStore()
            		.getBoolean(PreferenceFactory.INVOICE_AUTOLEDGER)
            );
        }
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.GenericMetaViewer#store(java.lang.Object)
     */
    @Override
    protected void store(Invoice invoice) {
        try {
            this.updateValues();
        } catch (Exception e) {
            log.error(this.getClass() + ".store():", e); //$NON-NLS-1$
        }
        try {
            this.invoice2Viewer.updateValues();
        } catch (Exception e) {
            log.error(this.getClass() + ".store():", e); //$NON-NLS-1$
        }
        if (this.itemViewer != null) {
            this.itemViewer.store();
        }
        if (PreferenceFactory.getPreferenceStore().getBoolean(PreferenceFactory.INVOICE_AUTOLEDGER)) {
            if (! generateAccountEntry()) {
                if (GUIUtil.confirm(Messages.getString("InvoiceMeta1Viewer.5"), //$NON-NLS-1$
                        Messages.getString("InvoiceMeta1Viewer.6"), true)) { //$NON-NLS-1$
                    super.store(invoice);
                }
            } else {
                // the account entry generation would have stored the invoice for us, so no need to do it here
            }
        } else {
            super.store(invoice);
        }
    }
    
    protected boolean	generateAccountEntry() {
        InvoiceLedgerParamDialog dialog = new InvoiceLedgerParamDialog(this.getShell(), this.bomService);
        dialog.setAccountReference(model.getAccountReference());
        dialog.setReceivedDate(model.getReceivedDate());
        if (dialog.open() != Dialog.OK) {
            return false;
        }
        if (dialog.getAccountReference() == null) {
            GUIUtil.showError(Messages.getString("InvoiceMeta1Viewer.7"), Messages.getString("InvoiceMeta1Viewer.8"), null); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        model.setReceivedDate(dialog.getReceivedDate());
        try {
            this.updateValues();
        } catch (Exception e) {}
        new BOMHelper(this.bomService).generateEntries(new Invoice[] { model }, dialog.getAccountReference(), false);
        return true;
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.GenericMetaViewer#isModified()
     */
    @Override
    protected boolean isModified() {
        return super.isModified() || (this.invoice2Viewer != null && this.invoice2Viewer.isModified()) ||
        	(this.itemViewer != null && this.itemViewer.isModified());
    }
    
    /**
     * @return Returns the invoice2Viewer.
     */
    public InvoiceMeta2Viewer getInvoice2Viewer() {
        return invoice2Viewer;
    }
    
    /**
     * @param invoice2Viewer The invoice2Viewer to set.
     */
    public void setInvoice2Viewer(InvoiceMeta2Viewer invoice2Viewer) {
        this.invoice2Viewer = invoice2Viewer;
    }
    
    /**
     * @return Returns the itemViewer.
     */
    public InvoiceItemViewer getItemViewer() {
        return itemViewer;
    }
    
    /**
     * @param itemViewer The itemViewer to set.
     */
    public void setItemViewer(InvoiceItemViewer itemViewer) {
        this.itemViewer = itemViewer;
        // make sure the Viewer does not modify our rules in the DB, as we will be making a copy of the final allocation for our entry
        this.itemViewer.setAutoSave(false);
    }
}
