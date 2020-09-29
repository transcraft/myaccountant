/**
 * Created on 02-Jul-2005
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

import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.ui.TableRowCellEditor;
import transcraft.myaccountant.ui.TableRowEditor;

/**
 * floating editor for a single ledger entry
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryEditor extends TableRowEditor<Entry> {
	static final Logger LOG = getLogger(LedgerEntryEditor.class);
	
    private final Account account;
    
    private Date currentPostingDate;
    private Date currentValueDate;
    
    /**
     * @param parentShell
     */
    public LedgerEntryEditor(LedgerEntryViewer parent, Table table, BOMService bomService, Account account) {
        super(parent, table, bomService, new LedgerEntryMeta());
        this.account = account;
    }    
    
    
    @Override
	protected String getMetaSelector() {
    	return account.getReference();
	}

	@Override
    protected void	loadModelForTableItem(int x, int y) {
    	super.loadModelForTableItem(x, y);
    	
        if (getModel().getEntryId() == null) {
        	if (currentPostingDate != null) {
        		getModel().setPostingDate(currentPostingDate);
        	}
        	if (currentValueDate != null) {
        		getModel().setValueDate(currentValueDate);
        	}
        }
    }

    @Override
    public void	refreshEditorValues() {
        if (getModel() == null) {
            return;
        }
        
        MetaColumn <?>[] metaColumns = metaProvider.getColumns();
        for (int i = 0; i < metaColumns.length; i++) {
            TableRowCellEditor<?> editor = getCellEditor(metaColumns[i]);
            if (editor != null) {
                Object value = metaProvider.getValue(getModel(), metaColumns[i].getName(), account.getReference());
                editor.setText(value != null ? value.toString() : ""); //$NON-NLS-1$
                editor.refreshInternalValues();
            }
        }
    }
    
    @Override
    protected void	saveRowData() {
        if (getModel() == null) {
            return;
        }
        try {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
	        int currentIdx = this.table.getSelectionIndex();
	        Entry entryForUpdate =  getModel();
	        LOG.debug("saveRowData({})=>{}", getModel(), entryForUpdate);
	        if (entryForUpdate.getEntryId() != null) {
	            entryForUpdate = this.bomService.getEntry(getModel().getEntryId());
	            if (entryForUpdate != null) {
	                entryForUpdate.setEntry(getModel());
	                this.bomService.store(new Entry(entryForUpdate));
	            } else {
	                this.bomService.store(new Entry(getModel()));
	            }
	        } else {
	            this.bomService.store(new Entry(entryForUpdate));
	        }
	        currentPostingDate = getModel().getPostingDate();
	        currentValueDate = getModel().getValueDate();
	        this.setModified(false);
	        if (currentIdx < (this.table.getItemCount() - 1)) {
	            currentIdx++;
	        }
	        this.table.setSelection(currentIdx);
	        this.loadModelForTableItem(this.table.getItem(currentIdx));
        } finally {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
        }
    }

	@Override
	protected LedgerEntryCellEditor makeCellEditor(MetaColumn<?> metaColumn) {
		return new LedgerEntryCellEditor(this, bomService, metaColumn);
	}
    
}
