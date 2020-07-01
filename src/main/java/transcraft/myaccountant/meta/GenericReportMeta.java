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
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.SWT;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.ReportDateUtil;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.utils.DateUtil;


/**
 * @author david.tran@transcraft.co.uk
 */
public class GenericReportMeta extends BaseMetaProvider<GenericReport<?>> {

    /*
     * NOTE : have to name this attribute specifically without the word "account" in it
     * because the GenericReportViewer looks for this keyword to populate with the list
     * of Accounts
     * 
     */
    public static final String ACTYPE_PROP = "acType"; //$NON-NLS-1$
    
    public static final String ACCTS_PROP = "accounts"; //$NON-NLS-1$
    public static final String DATERANGE_PROP = "dateRange"; //$NON-NLS-1$
    public static final String SD_PROP = "startDate"; //$NON-NLS-1$
    public static final String ED_PROP = "endDate"; //$NON-NLS-1$
    public static final String LEDGERTYE_PROP = "ledgerType"; //$NON-NLS-1$
    public static final String OF_PROP = "outputFormat"; //$NON-NLS-1$
    public static final String DST_PROP = "destination"; //$NON-NLS-1$
    public static final String NAME_PROP = "name"; //$NON-NLS-1$

    /**
     * 
     */
    public GenericReportMeta() {
        this.columns = new MetaColumn [] {
            new MetaColumn<String>(ACTYPE_PROP, Messages.getString("GenericReportMeta.9"), String.class, 120, Arrays.asList(ReportUtil.getAccountTypes()), false, SWT.RADIO), //$NON-NLS-1$
            new MetaColumn<String>(ACCTS_PROP, Messages.getString("GenericReportMeta.10"), String.class, 120, Lists.newArrayList(), false, SWT.MULTI), //$NON-NLS-1$
            new MetaColumn<String>(DATERANGE_PROP, Messages.getString("GenericReportMeta.11"), String.class, 120, ReportDateUtil.getMetaNames()), //$NON-NLS-1$
            new MetaColumn<Date>(SD_PROP, Messages.getString("GenericReportMeta.12"), Date.class, 120), //$NON-NLS-1$
            new MetaColumn<Date>(ED_PROP, Messages.getString("GenericReportMeta.13"), Date.class, 120), //$NON-NLS-1$
            new MetaColumn<String>(LEDGERTYE_PROP, Messages.getString("GenericReportMeta.14"), String.class, 120, BOMService.ledgerTypes, false), //$NON-NLS-1$
            new MetaColumn<String>(OF_PROP, Messages.getString("GenericReportMeta.15"), String.class, 120, GenericReport.getOutputFormats()), //$NON-NLS-1$
            new MetaColumn<String>(DST_PROP, Messages.getString("GenericReportMeta.16"), String.class, 120, GenericReport.getDestinations()) //$NON-NLS-1$
        };
    }

    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getValue(java.lang.Object, java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(GenericReport<?> report, String columnName, String selector) {
        if (columnName.equals(ACTYPE_PROP)) {
            return report.getAccountType();
        } else if (columnName.equals(ACCTS_PROP)) {
            List<String> accounts = report.getAccounts();
            // Java 8 compatibility mode, so String[]::new is not available
            return accounts.toArray(new String[accounts.size()]);
        } else if (columnName.equals(DATERANGE_PROP)) {
            return report.getDateMeta().toString();
        } else if (columnName.equals(SD_PROP)) {
            return report.getStartDate();
        } else if (columnName.equals(ED_PROP)) {
            return report.getEndDate();
        } else if (columnName.equals(LEDGERTYE_PROP)) {
            if (report.getLedgerType().equals(BOMService.VD_LEDGER)) {
                return BOMService.LT_VD;
            } else {
                return BOMService.LT_TD;
            }
        } else if (columnName.equals(OF_PROP)) {
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
    public void setValue(GenericReport<?> report, String accountReference, String columnName, Object value) throws ParseException {
        if (columnName.equals(ACTYPE_PROP)) {
            report.setAccountType(value.toString());
        } else if (columnName.equals(ACCTS_PROP)) {
            String [] values = (String[])value;
            List<String> accounts = Arrays.asList(values);
            report.setAccounts(accounts);
        } else if (columnName.equals(DATERANGE_PROP)) {
            report.setDateMeta(ReportDateUtil.getMetaForName(value.toString()));
        } else if (columnName.equals(SD_PROP)) {
            report.setStartDate(value == null ? null : DateUtil.getCalendar(value).getTime());
        } else if (columnName.equals(ED_PROP)) {
            report.setEndDate(value == null ? null : (DateUtil.getCalendar(value)).getTime());
        } else if (columnName.equals(LEDGERTYE_PROP)) {
            report.setLedgerType(value.equals(BOMService.LT_VD) ? BOMService.VD_LEDGER : BOMService.TD_LEDGER);
        } else if (columnName.equals(OF_PROP)) {
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
