/**
 * Created on 23-Jun-2020
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
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.slf4j.Logger;

import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.LedgerEntryMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.ui.TableRowCellEditor;

public class LedgerEntryCellEditor extends  TableRowCellEditor<Entry> implements BOMListener {
	static private final Logger LOG = getLogger(LedgerEntryCellEditor.class);
    
	private final BOMService bomService;
	
    /**
     * popup editor for a particular table cell
     * 
     * @param parent
     * @param rowEditor
     * @param style
     */
    public LedgerEntryCellEditor(LedgerEntryEditor rowEditor, BOMService bomService, MetaColumn<?> metaColumn) {
        super(rowEditor, metaColumn);
        this.bomService = bomService;
        
        // initialise the drop down list
        this.dataChanged(null);
        bomService.addListener(this);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.service.BOMListener#dataChanged(transcraft.BookKeeper.service.BOMEvent)
     */
    @Override
    public void dataChanged(BOMEvent evt) {
        if (evt != null && (evt.entity instanceof Entry)) {
            return;
        }
        if (this.metaColumn.getList().isPresent() &&
                (evt == null || this.metaColumn.getPrototype().equals(evt.entity.getClass()))) {
            List<String> list = this.bomService.getMemorisedList(this.metaColumn.getName());
            if (this.metaColumn.getName().equals(LedgerEntryMeta.REF_PROP)) {
            	list = list
            			.stream()
                        // these must be unique references, so they are unlikely to
                        // be re-used
            			.filter(ref -> ref.length() > 0 && !Character.isDigit(ref.charAt(ref.length() - 1)))
            			.collect(Collectors.toList());
            }
            this.metaColumn.setList(list);
            this.refreshPopupContents(list);
        }
    }

    @Override
    protected boolean ignoreKeyEvent(KeyEvent evt) {
        if (this.metaColumn.getName().equals(LedgerEntryMeta.PD_PROP)) {
            // make sure we can not traverse back from the first column
            if ((evt.stateMask & SWT.SHIFT) != 0) {
                return true;
            }
        } else if (this.metaColumn.getName().equals(LedgerEntryMeta.CR_PROP)) {
            // make sure we can not traverse forward from the last column
            if ((evt.stateMask & SWT.SHIFT) == 0) {
                return true;
            }
        }
        return false;
    }
    
    @Override
	protected void	goToNextField(int mask) {
        if (this.metaColumn.getName().equals(LedgerEntryMeta.DESC_PROP) && 
        		this.rowEditor.getModel().getAmount() == 0) {
        	RunningEntry entry = (RunningEntry)this.rowEditor.getModel();
            /*
             * auto fill of entry
             */
        	String value = this.getText(false);
            Entry memorisedEntry = this.bomService.getMemorised(LedgerEntryMeta.DESC_PROP,
            		value, null);
            if (memorisedEntry != null) {
                entry.setAmount(memorisedEntry.getAmount());
                entry.copyRule(memorisedEntry);
                if (memorisedEntry.getToAccount() != null) {
                    if (memorisedEntry.getToAccount().equals(entry.getFromAccount())) {
                        entry.setAmount(memorisedEntry.getAmount() * -1);
                    } else {
                        entry.setToAccount(memorisedEntry.getToAccount());
                        entry.setAmount(memorisedEntry.getAmount());
                    }
                }
                this.rowEditor.refreshEditorValues();
            } else {
            	LOG.debug("Nothing memorised for '{}'", value);
            }
        } else {
        	LOG.debug("No need to auto-fill {}", this.rowEditor.getModel().getDescription());
        }
        
        /*
         * safeguard against falling off the edge at either end of the row
         */
        Control [] items = this.getParent().getTabList();
        for (int i = 0; i < items.length; i++) {
            if (items[i] == this) {
                if ((mask & SWT.SHIFT) != 0) {
                    // traverse previous
                    if (i > 0) {
                        i--;
                    } else {
                        break;
                    }
                } else {
                    // traverse next
                    if (i < items.length - 1) {
                        i++;
                    } else {
                        break;
                    }
                }
                TableRowCellEditor<?> nextEditor = (TableRowCellEditor<?>)items[i];
                nextEditor.setFocus();
                break;
            }
        }
    	
    }
}