/**
 * Created on 16-Jul-2005
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
package transcraft.myaccountant.meta;

import java.text.ParseException;

import org.eclipse.swt.SWT;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * @author david.tran@transcraft.co.uk
 */
public class AllocationRuleMeta extends BaseMetaProvider<Allocation> {

    public static final String ACCT_PROP = "account"; //$NON-NLS-1$
    public static final String PCT_PROP = "percentage"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String AMT_PROP = "amount"; //$NON-NLS-1$
    public static final String NAME_PROP = "name"; //$NON-NLS-1$
    
    private AllocationRule rule;
    
    public AllocationRuleMeta(AllocationRule rule) {
        this.rule = rule;
        this.columns = new MetaColumn [] {
            new MetaColumn<String>(ACCT_PROP, Messages.getString("AllocationRuleMeta.5"), String.class, 100, Lists.newArrayList(), true, SWT.NONE), //$NON-NLS-1$
            new MetaColumn<String>(DESC_PROP, Messages.getString("AllocationRuleMeta.6"), String.class, 200), //$NON-NLS-1$
            new MetaColumn<Double>(PCT_PROP, Messages.getString("AllocationRuleMeta.7"), Double.class, 200), //$NON-NLS-1$
            new MetaColumn<Double>(AMT_PROP, Messages.getString("AllocationRuleMeta.8"), Double.class, 200, false, false), //$NON-NLS-1$
        };
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(Allocation alloc, String columnName, String selector) {
        if (ACCT_PROP.equals(columnName)) {
            return alloc.getAccount();
        } else if (DESC_PROP.equals(columnName)) {
            return Formatters.emptyIfNull(alloc.getDescription());
        } else if (PCT_PROP.equals(columnName)) {
            return alloc.getPercentage() == 0 ? "" : //$NON-NLS-1$
                (this.getColumn(AMT_PROP).isVisible() ?
                        Formatters.format(alloc.getPercentage()) : Formatters.format(alloc.getPercentage(), 6));
        } else if (AMT_PROP.equals(columnName)) {
            if (this.rule != null && this.rule instanceof Entry) {
                Entry entry = (Entry)this.rule;
                double val = Math.abs(entry.getAmount()) * alloc.getPercentage();
                return val == 0.0 ? "" : Formatters.format(val); //$NON-NLS-1$
            }
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValue(Allocation alloc, String selector, String columnName,
            Object value) throws ParseException {
        if (ACCT_PROP.equals(columnName)) {
            alloc.setAccount(value.toString());
        } else if (DESC_PROP.equals(columnName)) {
            alloc.setDescription(value.toString());
        } else if (PCT_PROP.equals(columnName)) {
            String str = value.toString();
            if (str.length() > 0) {
                double v = Formatters.parse(str).doubleValue();
                if (Math.abs(v) > 1.0) {
                    v /= 100;
                }
                if (Math.abs(v) > 1.0) {
                    GUIUtil.showError(Messages.getString("AllocationRuleMeta.12"), Messages.getString("AllocationRuleMeta.13"), null); //$NON-NLS-1$ //$NON-NLS-2$
                } else {
                    alloc.setPercentage(v);
                }
            }
        } else if (AMT_PROP.equals(columnName)) {
            if (this.rule instanceof Entry) {
                Entry entry = (Entry)this.rule;
	            String str = value.toString();
	            if (str.length() > 0) {
	                double v = Formatters.parse(str).doubleValue();
                    alloc.setPercentage(v / entry.getAmount());
	            }
            }
        }
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return NAME_PROP;
    }
}
