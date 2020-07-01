/**
 * Created on 10-Sep-2005
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

import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.GenericReport;


/**
 * @author david.tran@transcraft.co.uk
 */
public class BaseGenericReportMeta extends BaseMetaProvider<BaseGenericReport<?>> {

    /*
     * NOTE : have to name this attribute specifically without the word "account" in it
     * because the GenericReportViewer looks for this keyword to populate with the list
     * of Accounts
     * 
     */
    public static final String OF_PROP = "outputFormat"; //$NON-NLS-1$
    public static final String DST_PROP = "destination"; //$NON-NLS-1$
    public static final String NAME_PROP = "name"; //$NON-NLS-1$

    /**
     * 
     */
    public BaseGenericReportMeta() {
        this.columns = new MetaColumn [] {
            new MetaColumn<String>(OF_PROP, Messages.getString("BaseGenericReportMeta.3"), String.class, 120, GenericReport.getOutputFormats()), //$NON-NLS-1$
            new MetaColumn<String>(DST_PROP, Messages.getString("BaseGenericReportMeta.4"), String.class, 120, GenericReport.getDestinations()) //$NON-NLS-1$
        };
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(BaseGenericReport<?> report, String columnName, String selector) {
        if (columnName.equals(OF_PROP)) {
            return report.getOutputFormat();
        } else if (columnName.equals(DST_PROP)) {
            return report.getDestination();
        }
        return ""; //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#setValue(java.lang.Object, java.lang.String, java.lang.String, java.lang.Object)
     */
    @Override
    public void setValue(BaseGenericReport<?> report, String accountReference, String columnName, Object value) throws ParseException {
        if (columnName.equals(OF_PROP)) {
            report.setOutputFormat(value.toString());
        } else if (columnName.equals(DST_PROP)) {
            report.setDestination(value.toString());
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
