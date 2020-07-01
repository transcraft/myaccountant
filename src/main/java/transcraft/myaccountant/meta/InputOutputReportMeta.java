/**
 * Created on 30-Oct-2005
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

import transcraft.myaccountant.report.InputOutputReport;

/**
 * for InputOutputReport
 * 
 * @author david.tran@transcraft.co.uk
 */
public class InputOutputReportMeta extends GenericReportMeta {

    public static final String INCLUSION_PROP = "includeDesc"; //$NON-NLS-1$
    public static final String EXCLUSION_PROP = "excludeDesc"; //$NON-NLS-1$
    
    /**
     * 
     */
    public InputOutputReportMeta() {
        super();
        // add another 2 columns for our usage
        this.extendColumns(2);
        this.columns[this.columns.length - 2] = new MetaColumn<String>(INCLUSION_PROP, "Include Entries", String.class, 150, Lists.newArrayList(), true, SWT.MULTI); //$NON-NLS-1$
        this.columns[this.columns.length - 1] = new MetaColumn<String>(EXCLUSION_PROP, "Exclude Entries", String.class, 150, Lists.newArrayList(), true, SWT.MULTI); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    public Object getValue(InputOutputReport report, String columnName, String selector) {
        if (INCLUSION_PROP.equals(columnName)) {
            return report.getIncludedEntries();
        } else if (EXCLUSION_PROP.equals(columnName)) {
            return report.getExcludedEntries();
        }
        return super.getValue(report, columnName, selector);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    public void setValue(InputOutputReport report, String accountReference,
            String columnName, Object value) throws ParseException {
        if (INCLUSION_PROP.equals(columnName)) {
            report.setIncludedEntries((String[])value);
        } else if (EXCLUSION_PROP.equals(columnName)) {
            report.setExcludedEntries((String[])value);
        }
        super.setValue(report, accountReference, columnName, value);
    }
}
