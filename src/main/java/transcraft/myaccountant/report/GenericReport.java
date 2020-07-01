/**
 * Created on 11-Sep-2005
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
package transcraft.myaccountant.report;

import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.utils.DateUtil;

/**
 * 
 * Base class for a generic report
 * @author david.tran@transcraft.co.uk
 */
public abstract class GenericReport<T> extends BaseGenericReport<T> {
	private static final Logger LOG = LoggerFactory.getLogger(GenericReport.class);
	
    private Date startDate = DateUtil.getTodayStart().getTime();
    private Date endDate = DateUtil.getTodayStart().getTime();
    private ReportDateMeta dateMeta;
    private String accountType = ReportUtil.ACTYPE_ENTRY;
    
    protected List<String> accounts = Lists.newArrayList();
    protected Map<String, GenericReport<T>.SubReportData>	subReports = Maps.newHashMap();
    protected String ledgerType = BOMService.VD_LEDGER;
    
    /**
     * this flag controls the ability to select the Accounts
     * from the dropdown list on the screen
     */
    protected boolean autoAccountSelect = true;
    
    public GenericReport(String name) {
        super(name);
        this.setDateMeta(ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISMONTH));
    }

    /**
     * @return Returns the accountType.
     * this is an enumeration into the types of accounts to be evaluated
     * in the query, such as Entry accounts, Ledger accounts, or Tax tracking accounts
     * @see ReportUtil.ACTYPE_* enums
     */
    public String getAccountType() {
        return accountType;
    }
    
    /**
     * @param accountType The accountType to set.
     * this is an enumeration into the types of accounts to be evaluated
     * in the query, such as Entry accounts, Ledger accounts, or Tax tracking accounts
     * @see ReportUtil.ACTYPE_* enums
     */
    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }
    
    /**
     * @return Returns the accounts.
     */
    public List<String> getAccounts() {
        List<String> list = Lists.newArrayList();
        list.addAll(this.accounts);
        return list;
    }
    
    /**
     * @param accounts The accounts to set.
     */
    public void setAccounts(List<String> accounts) {
        this.accounts.clear();
        this.accounts.addAll(accounts);
    }
    
    /**
     * @return Returns the endDate.
     */
    public Date getEndDate() {
        return endDate;
    }
    
    /**
     * @param endDate The endDate to set.
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
        //this.dateMeta = ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_CUSTOM);
    }
    
    /**
     * @return Returns the startDate.
     */
    public Date getStartDate() {
        return startDate;
    }
    
    /**
     * @param startDate The startDate to set.
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
        //this.dateMeta = ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_CUSTOM);
    }
    
    /**
     * @return Returns the autoAccountSelect.
     */
    public boolean isAutoAccountSelect() {
        return autoAccountSelect;
    }
    
    /**
     * @param autoAccountSelect The autoAccountSelect to set.
     */
    public void setAutoAccountSelect(boolean autoAccountSelect) {
        this.autoAccountSelect = autoAccountSelect;
    }
    
    /**
     * @return Returns the ledgerType.
     * specifies whether Trade date or value date ledger is used
     * for query
     */
    public String getLedgerType() {
        return ledgerType;
    }
    
    /**
     * @param ledgerType The ledgerType to set.
     * specifies whether Trade date or value date ledger is used
     * for query
     */
    public void setLedgerType(String ledgerType) {
        this.ledgerType = ledgerType;
    }
    
    /**
     * @return Returns the dateMeta.
     */
    public ReportDateMeta getDateMeta() {
        if (this.dateMeta == null) {
            this.setDateMeta(ReportDateUtil.getMetaForType(ReportDateUtil.RPTD_THISMONTH));
        }
        return this.dateMeta;
    }
    
    /**
     * @param meta The dateMeta to set.
     */
    public void setDateMeta(ReportDateMeta meta) {
        this.dateMeta = meta;
        ReportDateUtil rpdu = new ReportDateUtil();
        rpdu.calculate(meta);
        this.startDate = meta.getStartDate();
        this.endDate = meta.getEndDate();
    }
    
    /*
     * load a sub report, should be invoked from the master report
     */
    protected void	loadSubReport(String id, String name, String templateName, GenericReport<T> subReport) {
        try {
        	LOG.info("loadSubReport({},{},{})", id, name, templateName);
            SubReportData data = new SubReportData(id, name);
            subReport.setDescription(name);
            InputStream is = ActivityReport.class.getResourceAsStream(templateName);
            data.template = (JasperReport)JRLoader.loadObject(is);
            data.dataSource = subReport;
            this.subReports.put(id, data);
        } catch (Exception e) {
        	LOG.error(String.format("loadSubReport(%s,%s,%s)", id, name, templateName), e);
            GUIUtil.showError(Messages.getString("GenericReport.0"), Messages.getString("GenericReport.1") + templateName + "'", e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }
    
    public Map<String, Object>	getParameters() throws JRException {
        Map<String, Object> params = Maps.newHashMap();
        params.put("reportTitle", this.getDescription()); //$NON-NLS-1$
        params.put("startDate", this.getStartDate()); //$NON-NLS-1$
        params.put("endDate", this.getEndDate()); //$NON-NLS-1$
        params.put("bomService", this.bomService); //$NON-NLS-1$
        params.put("companyAddress", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_ADDRESS)); //$NON-NLS-1$
        params.put("companyEmail", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_EMAIL)); //$NON-NLS-1$
        params.put("companyFaxNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_FAXNO)); //$NON-NLS-1$
        params.put("companyName", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNAME)); //$NON-NLS-1$
        params.put("companyPhoneNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_PHONENO)); //$NON-NLS-1$
        params.put("companyRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_COMPANYNO)); //$NON-NLS-1$
        params.put("companyURL", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_URL)); //$NON-NLS-1$
        params.put("reportTitle", this.getName()); //$NON-NLS-1$
        params.put("vatRegNo", PreferenceFactory.getPreferenceStore().getString(PreferenceFactory.INVOICE_VATCODE)); //$NON-NLS-1$
        for (Map.Entry<String, GenericReport<T>.SubReportData> entry : this.subReports.entrySet()) {
            String subReportID = entry.getKey().toString();
            SubReportData data = (SubReportData)entry.getValue();
            params.put(subReportID + "Template", data.template); //$NON-NLS-1$
            params.put(subReportID + "DataSource", data.dataSource); //$NON-NLS-1$
        }
        return params;
    }

    /**
     * @return Returns the bomService.
     */
    public BOMService getBomService() {
        return bomService;
    }
    
    protected  void	runSubReports() {
        for (Map.Entry<String, GenericReport<T>.SubReportData> entry :  this.subReports.entrySet()) {
            SubReportData data = entry.getValue();
            if (data.dataSource instanceof GenericReport) {
                GenericReport<?> subReport = (GenericReport<?>)data.dataSource;
                subReport.startDate = this.startDate;
                subReport.endDate = this.endDate;
                subReport.dateMeta = this.dateMeta;
                subReport.outputFormat = this.outputFormat;
                subReport.setBomService(this.bomService);
                LOG.info("run({})", subReport.dump());
                subReport.run();
            }
        }
    }
    
    private class SubReportData {
        public JasperReport template;
        public JRDataSource dataSource;
        @SuppressWarnings("unused")
		public String name;
        @SuppressWarnings("unused")
		public String id;
        
        public SubReportData(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
