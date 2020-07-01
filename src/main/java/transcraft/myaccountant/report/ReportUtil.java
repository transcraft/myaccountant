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
package transcraft.myaccountant.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.engine.export.HtmlResourceHandler;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.RunningEntry;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.GUIUtil;

/**
 * utilities for reporting
 * @author david.tran@transcraft.co.uk
 */
public class ReportUtil {
	private static final Logger LOG = LoggerFactory.getLogger(ReportUtil.class);
	
    /**
     * utilities for reporting
     */
    public ReportUtil() {
    }

    private static final BaseGenericReport<?> [] availableReports = new BaseGenericReport<?>[] {
            new ActivityReport(),
            new VATSummaryReport(),
            new VATDetailReport(),
            new InputOutputReport(),
            new UnpaidInvoiceReport(),
            new UnreconciledReport(),
            new MonthlyCashflowReport(),
            new PerformanceReport(),
    };
    
    public static final BaseGenericReport<?>	[] getAvailableReports() {
        return availableReports;
    }

    public static final BaseGenericReport<?> [] getAvailableReports(BOMService bomService) {
        return ReportUtil.getAvailableReports(bomService, true);
    }
    
    public static final BaseGenericReport<?> [] getAvailableReports(BOMService bomService, boolean includeBatch) {
        List<BaseGenericReport<?>> list = Lists.newArrayList();
        list.addAll(Arrays.asList(getAvailableReports()));
        if (bomService != null) {
	        List<BaseGenericReport<?>> memorisedReports = bomService.getRefData(BaseGenericReport.class, true)
	        		.stream()
	        		.map(r -> (BaseGenericReport<?>)r)
	        		.collect(Collectors.toList());
	        for (BaseGenericReport<?> report : memorisedReports) {
	            if (! list.contains(report)) {
	                list.add(report);
	            }
	        }
	        if (! includeBatch) {
		        List<BatchReport> batchReports = bomService.getRefData(BatchReport.class, false);
		        list.removeAll(batchReports);
	        }
        }
        // Java 8 compatibility mode, so BaseGenericReport<?>[]::new is not available
        return list.toArray(new BaseGenericReport<?>[list.size()]);
    }
    
    public static final boolean	isBuiltInReport(BaseGenericReport<?> report) {
        return isBuiltInReport(report.getName());
    }
    
    public static final boolean	isBuiltInReport(String name) {
        for (int i = 0; i < availableReports.length; i++) {
            if (availableReports[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public static final	boolean	isReportExist(BOMService bomService, String name) {
        BaseGenericReport<?> [] reports = getAvailableReports(bomService);
        for (int i = 0; i < reports.length; i++) {
            if (reports[i].getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
    
    public static final String ACTYPE_ENTRY = "Entry"; //$NON-NLS-1$
    public static final String ACTYPE_LEDGER = "Ledger"; //$NON-NLS-1$
    public static final String ACTYPE_VAT = "VAT"; //$NON-NLS-1$
    
    public static final String [] getAccountTypes() {
        return new String [] {
            ACTYPE_ENTRY, ACTYPE_LEDGER, ACTYPE_VAT
        };
    }
    
    public static List<String>	getTaxedAccountRefs(BOMService bomService) {
        List<String> list = Lists.newArrayList();
        List<Account> accounts = bomService.getRefData(Account.class);
        for (Account account : accounts) {
            if (account.isTaxed()) {
                list.add(account.getReference());
            }
        }
        return list;
    }
    
    public static final String [] getAccountsForType(BOMService bomService, String acType) {
        List<String> list = Lists.newArrayList();
        if (acType.equals(ACTYPE_ENTRY)) {
            list.addAll(ReportUtil.getEntryAccountRefs(bomService));
        } else if (acType.equals(ACTYPE_LEDGER)) {
            list.addAll(ReportUtil.getLedgerAccountRefs(bomService));
        } else if (acType.equals(ACTYPE_VAT)) {
            list.addAll(ReportUtil.getLedgerAccountRefs(bomService, BOMService.LCAT_VAT));
        }
        // Java 8 compatibility mode, so String[]::new is not available
        return list.toArray(new String[list.size()]);
    }
    
    public static List<String>	getEntryAccountRefs(BOMService bomService) {
        return ReportUtil.getEntryAccountRefs(bomService, BOMService.ALL_CATS);
    }
    
    public static List<String>	getEntryAccountRefs(BOMService bomService, String category) {
        return ReportUtil.getAccountRefs(bomService, category, Account.class);
    }
    
    public static List<String>	getLedgerAccountRefs(BOMService bomService) {
        return ReportUtil.getLedgerAccountRefs(bomService, BOMService.ALL_CATS);
    }
    
    public static List<String>	getLedgerAccountRefs(BOMService bomService, String category) {
        return ReportUtil.getAccountRefs(bomService, category, LedgerAccount.class);
    }
    
    public static <T> List<String>	getAccountRefs(BOMService bomService, String category, Class<T> theClass) {
        List<String> list = Lists.newArrayList();
        List<T> accounts = bomService.getRefData(theClass);
        for (T a : accounts) {
            if (category == null) {
                continue;
            }
            Account account = (Account)a;
            if (category.equals(BOMService.ALL_CATS) ||
                    (account.getCategory() != null && account.getCategory().equals(category))) {
                list.add(account.getReference());
            }
        }
        return list;
    }
    
    public static List<String>	getAllAccountRefs(BOMService bomService) {
        List<String> accounts = ReportUtil.getEntryAccountRefs(bomService);
        accounts.addAll(ReportUtil.getLedgerAccountRefs(bomService));
        Collections.sort(accounts);
        return accounts;        
    }
    
    /**
     * entry point to report generation
     * 
     * @param parent
     * @param bomService
     * @param report
     * @param format
     */
    public static void	generateOnlineReport(Composite parent, BOMService bomService, BaseGenericReport<?> report, String format) {
        InputStream template = null;
        String tempPath = null;
        try {
        	LOG.info("generateOnlineReport({})", report.dump());
            parent.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
            parent.setEnabled(false);
            
            report.setOutputFormat(format);
            report.setDestination(GenericReport.DST_SCREEN);
            template = GenericReport.class.getResourceAsStream(report.getReportTemplate());
	        if (template == null) {
	            throw new RuntimeException(Messages.getString("ReportUtil.3") + report.getReportTemplate() + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	        String outputFile = null;
            String suffix = "." + report.getOutputFormat(); //$NON-NLS-1$
            tempPath = GUIUtil.getTempPath() + report.getName() + suffix;
            outputFile = tempPath;
	        report.run(bomService);
	        LOG.info("fillReport({},{},{})", report.getReportTemplate(),
	        		report.getParameters(), report.getName());
            JasperPrint jp = JasperFillManager.fillReport(template, report.getParameters(), report);
            
        	if (GUIUtil.confirmOverwriteFile(outputFile)) {
		        if (report.getOutputFormat().equals(GenericReport.OF_PDF)) {
	        		FileOutputStream fos = new FileOutputStream(outputFile);
	        		JasperExportManager.exportReportToPdfStream(jp, fos);
	        		fos.close();
		        } else if (report.getOutputFormat().equals(GenericReport.OF_HTML)) {
		        	/*
		    		JRHtmlExporter exporter = new JRHtmlExporter();
		    		
		    		exporter.setParameter(JRExporterParameter.JASPER_PRINT, jp);
		    		exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, outputFile);
		    		exporter.setParameter(JRHtmlExporterParameter.SIZE_UNIT, JRHtmlExporterParameter.SIZE_UNIT_POINT);
		    		exporter.exportReport();
					*/
		   
		        	exportToHtml(jp, outputFile);
		        } else if (report.getOutputFormat().equals(GenericReport.OF_XML)) {
		            JasperExportManager.exportReportToXmlFile(jp, outputFile, true);
		        }
        	}	        
	        if (new File(outputFile).exists()) {
	            GUIUtil.launchBrowser(new File(outputFile).toURI().toURL().toString(), report.toString()); //$NON-NLS-1$
	        } else {
	            throw new RuntimeException(Messages.getString("ReportUtil.7") + outputFile + Messages.getString("ReportUtil.8")); //$NON-NLS-1$ //$NON-NLS-2$
	        }
        } catch (Exception e) {
            LOG.error(String.format("generateOnlineReport(%s)", report.getName()), e);
            GUIUtil.showError(Messages.getString("ReportUtil.9"), Messages.getString("ReportUtil.10"), e); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            parent.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
            parent.setEnabled(true);
            try {
                template.close();
            } catch (Exception e) {}
        }
    }
    
    private static Map<String, String> HTML_IMAGE_CACHE = Maps.newHashMap();
    
    public static void exportToHtml(JasperPrint jp, String outputFile) throws JRException {
		HtmlExporter exporter = new HtmlExporter();
		exporter.setExporterInput(new SimpleExporterInput(jp));
		SimpleHtmlExporterOutput output = new SimpleHtmlExporterOutput(outputFile);
		output.setImageHandler(new HtmlResourceHandler() {
			
			@Override
			public void handleResource(String id, byte[] data) {
                HTML_IMAGE_CACHE.put(id, "data:image/jpg;base64," + Base64.getEncoder().encodeToString(data));				
			}
			
			@Override
			public String getResourcePath(String id) {
				return HTML_IMAGE_CACHE.get(id);
			}
		});
		exporter.setExporterOutput(output);
		exporter.exportReport();    	
    }

    public static double	getVATAmount(BOMService bomService, RunningEntry re) {
        Allocation [] allocs = re.getAllocations(false);
        double vat = 0;
        List<String> vatAccounts = getLedgerAccountRefs(bomService, BOMService.LCAT_VAT);
        for (int i = 0; i < allocs.length; i++) {
            if (vatAccounts.contains(allocs[i].getAccount())) {
                vat += allocs[i].getPercentage() * re.getOriginalAmount();
            }
        }
        return Math.abs(vat) * (re.getAmount() < 0 ? -1 : 1);
    }

    /**
     * check to see if this entry is a VAT payment. VAT payments are <b>not</b> 
     * included in any performance or cashflow reports
     * @param bomService
     * @param re
     * @return
     */
    public static boolean isVATPayment(BOMService bomService, RunningEntry re) {
        return Math.abs(getVATAmount(bomService, re)) == Math.abs(re.getOriginalAmount());
    }
}
