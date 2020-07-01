/**
 * Created on 04-Aug-2005
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMHelper;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.meta.ScheduledEntryMeta;
import transcraft.myaccountant.ui.AutoCompletableText;
import transcraft.myaccountant.ui.ImageCache;

/**
 * Viewer screen to view and maintain the ScheduledEntry object
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ScheduledEntryViewer extends GenericMetaViewer<ScheduledEntry> implements BOMListener {
    
    /**
     * @param parent
     * @param bomService
     * @param provider
     * @param obj
     */
    public ScheduledEntryViewer(Composite parent, BOMService bomService,
            MetaProvider<ScheduledEntry> provider, ScheduledEntry scheduledEntry) {
        super(parent, bomService, provider, scheduledEntry);
    }
    
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.GenericMetaViewer#createContents()
     */
    protected void createContents() {
        super.createContents();
        final Button button = new Button(this.actionBar, SWT.SHADOW_ETCHED_OUT);
        button.setImage(ImageCache.get("pay")); //$NON-NLS-1$
        button.setToolTipText(Messages.getString("ScheduledEntryViewer.1")); //$NON-NLS-1$
        button.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                new BOMHelper(bomService).makeScheduledPayment(model);
            }
        });
        this.dataChanged(null);
        this.bomService.addListener(this);
        final AutoCompletableText descControl = (AutoCompletableText)this.getControlForColumn(ScheduledEntryMeta.DESC_PROP);
        descControl.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
                Entry entry = bomService.getMemorised("description", descControl.getText()); //$NON-NLS-1$
                if (entry != null) {
                    updateFromMemorised(entry);
                }
			}
		});
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
	@Override
    public void dataChanged(BOMEvent event) {
        if (event == null || event.entity.getClass().equals(Account.class)) {
            List<String> list = this.bomService.getMemorisedList(Messages.getString("ScheduledEntryViewer.3")) //$NON-NLS-1$
            		.stream()
                    // these must be unique references, so they are unlikely to
                    // be re-used
            		.filter(ref -> ref.length() > 0 && !Character.isDigit(ref.charAt(ref.length() - 1)))
            		.collect(Collectors.toList());
            this.updateAutoCompleteProposals(ScheduledEntryMeta.REF_PROP, list);
            list = this.bomService.getMemorisedList("account"); //$NON-NLS-1$
            this.updateAutoCompleteProposals(ScheduledEntryMeta.FR_ACCT_PROP, list);
            this.updateAutoCompleteProposals(ScheduledEntryMeta.TO_ACCT_PROP, list);
        }
        if (event == null || event.entity.getClass().equals(AllocationRule.class)) {
            List<String> list = this.bomService.getMemorisedList("allocationRule"); //$NON-NLS-1$
            this.updateAutoCompleteProposals(ScheduledEntryMeta.ALLOC_PROP, list);
        }
        if (event == null || event.entity.getClass().equals(Entry.class)) {
            List<String> list = this.bomService.getMemorisedList("description"); //$NON-NLS-1$
            this.updateAutoCompleteProposals(ScheduledEntryMeta.DESC_PROP, list);
        }
        if (event == null || event.entity instanceof Entry) {
            this.refreshValues();
        }
    }
    
    protected void	updateFromMemorised(Entry entry) {
        if (entry.getFromAccount() != null) {
            AutoCompletableText act = (AutoCompletableText)this.getControlForColumn(ScheduledEntryMeta.FR_ACCT_PROP);
            act.setText(entry.getFromAccount());
            if (entry.getToAccount() != null) {
                act = (AutoCompletableText)this.getControlForColumn(ScheduledEntryMeta.TO_ACCT_PROP);
                act.setText(entry.getToAccount());
            }
            Text amountInput = (Text)this.getControlForColumn(ScheduledEntryMeta.AMT_PROP);
            amountInput.setText("" + entry.getAmount()); //$NON-NLS-1$
            if (entry.getName() != null) {
                act = (AutoCompletableText)this.getControlForColumn(ScheduledEntryMeta.ALLOC_PROP);
                act.setText(entry.getName());
            }
        }
    }
    
    private void	updateAutoCompleteProposals(String columnName, List<String> list) {
        Control control = this.getControlForColumn(columnName);
        if (control != null && control instanceof AutoCompletableText) {
            AutoCompletableText input = (AutoCompletableText)control;
            // Java 8 compatibility mode, so String[]::new is not available
            input.updateProposals(list.toArray(new String[list.size()]));
        }
        this.setListForColumnName(columnName, list);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.GenericMetaViewer#validate()
     */
    @Override
    protected void validate() {
        String frAccount = model.getFromAccount();
        if (isBlank(frAccount)) {
            throw new RuntimeException(Messages.getString("ScheduledEntryViewer.8")); //$NON-NLS-1$
        }
        Object toAccount = model.getToAccount();
        if (toAccount != null && frAccount.equals(toAccount)) {
            throw new RuntimeException(Messages.getString("ScheduledEntryViewer.9")); //$NON-NLS-1$
        }
        if (model.getAmount() == 0) {
            throw new RuntimeException(Messages.getString("ScheduledEntryViewer.10")); //$NON-NLS-1$
        }
    }
}
