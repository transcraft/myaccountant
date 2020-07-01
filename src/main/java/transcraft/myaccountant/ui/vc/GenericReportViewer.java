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
package transcraft.myaccountant.ui.vc;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.slf4j.Logger;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.service.BOMEvent;
import transcraft.BookKeeper.service.BOMListener;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.GenericReportMeta;
import transcraft.myaccountant.meta.MetaColumn;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.report.ReportDateUtil;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.GUIUtil;
import transcraft.myaccountant.ui.ImageCache;

/**
 * @author david.tran@transcraft.co.uk
 */
public class GenericReportViewer extends GenericMetaViewer<BaseGenericReport<?>> implements BOMListener {
	private static final Logger LOG = getLogger(GenericReportViewer.class);
    
    /**
     * @param parent
     * @param style
     */
    public GenericReportViewer(Composite parent, BOMService bomService, MetaProvider<BaseGenericReport<?>> provider, 
    		BaseGenericReport<?> report) {
        super(parent, bomService, provider, report);
    }
    
    public void	createContents() {
        List<Account> accounts = this.bomService.getRefData(Account.class);
        MetaColumn<?> column = this.metaProvider.getColumn(GenericReportMeta.ACCTS_PROP);
        if (column != null) {
            for (Account account : accounts) {
                if (account.isTaxed()) {
                    column.getList().ifPresent(l -> l.add(account.getReference()));
                }
            }
        }

        if (model instanceof GenericReport) {
            GenericReport<?> genericReport = (GenericReport<?>)this.model;
	        this.metaProvider.getColumn(GenericReportMeta.ACTYPE_PROP).setEnabled(! genericReport.isAutoAccountSelect());
	        this.metaProvider.getColumn(GenericReportMeta.ACCTS_PROP).setEnabled(! genericReport.isAutoAccountSelect());
	        this.metaProvider.getColumn(GenericReportMeta.LEDGERTYE_PROP).setEnabled(! genericReport.isAutoAccountSelect());
        }
        
        // now create the Controls
        super.createContents();
        
        if (! ReportUtil.isBuiltInReport(model)) {
	        this.saveButton.setImage(ImageCache.get("save")); //$NON-NLS-1$
	        this.saveButton.setToolTipText(Messages.getString("GenericReportViewer.1")); //$NON-NLS-1$
	        this.saveButton.addSelectionListener(new SelectionAdapter() {
	        	@Override
	            public void	widgetSelected(SelectionEvent evt) {
	                saveReport(false);
	            }
	        });
        } else {
            this.saveButton.setVisible(false);
        }
        
        // save as report button
        final Button saveAsButton = new Button(this.actionBar, SWT.SHADOW_ETCHED_OUT);
        saveAsButton.setImage(ImageCache.get("saveas")); //$NON-NLS-1$
        saveAsButton.setToolTipText(Messages.getString("GenericReportViewer.3")); //$NON-NLS-1$
        saveAsButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                saveReport(true);
            }
        });
        
        // Generate report button
        final Button genButton = new Button(this.actionBar, SWT.SHADOW_ETCHED_OUT);
        genButton.setImage(ImageCache.get("report")); //$NON-NLS-1$
        genButton.setToolTipText(Messages.getString("GenericReportViewer.5")); //$NON-NLS-1$
        genButton.addSelectionListener(new SelectionAdapter() {
        	@Override
            public void	widgetSelected(SelectionEvent evt) {
                generateReport();
            }
        });
        
        this.actionBar.pack();
        
        Control dateRangeControl = (Control)this.controls.get(this.metaProvider.getColumn(GenericReportMeta.DATERANGE_PROP));
        if (dateRangeControl != null && dateRangeControl instanceof Combo) {
            final Combo combo = (Combo)dateRangeControl;
            combo.addSelectionListener(new SelectionAdapter() {
            	@Override
                public void	widgetSelected(SelectionEvent evt) {
                    if (model instanceof GenericReport) {
	                    String sel = combo.getText();
	                    ((GenericReport<?>)model).setDateMeta(ReportDateUtil.getMetaForName(sel));
	                    refreshValues();
                    }
                }
            });
        }
        Control acTypeControl = (Control)this.controls.get(this.metaProvider.getColumn(GenericReportMeta.ACTYPE_PROP));
        if (acTypeControl != null && acTypeControl instanceof Group) {
            Group group = (Group)acTypeControl;
            for (int i = 0; i < group.getChildren().length; i++) {
                Button acButton = (Button)group.getChildren()[i];
                acButton.addSelectionListener(new SelectionAdapter() {
                	@Override
                   public void	widgetSelected(SelectionEvent evt) {
                       selectAccountType(((Button)evt.widget).getText());
                   }
                });
            }
        }
        
        
        this.bomService.addListener(this);
        this.dataChanged(null);
    }

	@Override    
    public void dataChanged(BOMEvent evt) {
        if (evt == null || evt.entity instanceof Account) {
            if (model instanceof GenericReport) {
	            GenericReport<?> genericReport = (GenericReport<?>)model;
	            MetaColumn<?> [] columns = this.metaProvider.getColumns();
	            String [] descs = null;	// for description dropdown
	            for (int i = 0; i < columns.length; i++) {
	                String [] items = null;
	                if (this.metaProvider.getColumns()[i].getName().toLowerCase().indexOf("account") >= 0) { //$NON-NLS-1$
	                    items = ReportUtil.getAccountsForType(this.bomService, genericReport.getAccountType());
	                } else if (this.metaProvider.getColumns()[i].getName().toLowerCase().indexOf(Messages.getString("GenericReportViewer.7")) >= 0) { //$NON-NLS-1$
	                    if (descs == null) {
	                        List<String> list = this.getBomService()
	                        		.getMemorisedList("description");
	                                // Java 8 compatibility mode, so String[]::new is not available
	                        descs = list.toArray(new String[list.size()]);
	                    }
	                    items = descs;
	                }
	                if (items != null) {
	                    Control control = (Control)this.controls.get(columns[i]);
	                    if (control instanceof Combo) {
		                    Combo combo = (Combo)control;
		                    combo.setItems(items);
	                    } else if (control instanceof Group) {
	                        Group group = (Group)control;
	                        Control [] children = group.getChildren();
	                        for (int j= 0; j < children.length; j++) {
	                            children[j].dispose();
	                        }
	                        for (int j = 0; j < items.length; j++) {
	                            Button button = new Button(group, SWT.RADIO);
	                            button.setText(items[j]);
	                        }
	                    } else if (control instanceof org.eclipse.swt.widgets.List) {
	                        org.eclipse.swt.widgets.List listControl = (org.eclipse.swt.widgets.List)control;
	                        listControl.setItems(items);
	                    }
	                }
	            }
            }
        }
        this.refreshValues();
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.vc.GenericMetaViewer#tidyUp()
     */
    protected void tidyUp() {
        // do nothing, don't care if we are modified
    }
    protected void	selectAccountType(String acType) {
        if (model instanceof GenericReport) {
	        ((GenericReport<?>)model).setAccountType(acType);
	        this.dataChanged(null);
        }
    }

    public void	saveReport(boolean asNew) {
        try {
            this.updateValues();
        } catch (Exception e) {
            LOG.error("saveReport(" + asNew + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        if (asNew) {
            Object reportName = GUIUtil.promptForNewObject(Messages.getString("GenericReportViewer.11"), Messages.getString("GenericReportViewer.12"), null, String.class); //$NON-NLS-1$ //$NON-NLS-2$
	        if (reportName != null) {
	            if (ReportUtil.isBuiltInReport(reportName.toString())) {
	                GUIUtil.showError(Messages.getString("GenericReportViewer.13"), reportName + Messages.getString("GenericReportViewer.14"), null); //$NON-NLS-1$ //$NON-NLS-2$
	                return;
	            }
	            if (ReportUtil.isReportExist(this.bomService, reportName.toString())) {
	                if (! GUIUtil.confirm(Messages.getString("GenericReportViewer.15"), Messages.getString("GenericReportViewer.16") + reportName + Messages.getString("GenericReportViewer.17"))) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                    return;
	                }
	            }
		        BaseGenericReport<?> newReport = model.makeCopy(reportName.toString());
	            bomService.storeGeneric(newReport, "name", model.getName()); //$NON-NLS-1$
	        }
        } else {
            bomService.storeGeneric(model, "name", model.getName()); //$NON-NLS-1$
        }
    }
    
    public void	generateReport() {
        InputStream template = null;
        String tempPath = null;
        try {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_WAIT));
            this.setEnabled(false);
            
	        this.updateValues();
	        
	        if (GenericReportMeta.class.isAssignableFrom(this.metaProvider.getClass()) &&
	                this.metaProvider.getColumn(GenericReportMeta.ACCTS_PROP).isEnabled()) {
	            if (((GenericReport<?>)model).getAccounts().size() == 0) {
	                throw new RuntimeException(Messages.getString("GenericReportViewer.20")); //$NON-NLS-1$
	            }
	        }
	        
	        template = GenericReport.class.getResourceAsStream(model.getReportTemplate());
	        if (template == null) {
	            throw new RuntimeException(Messages.getString("GenericReportViewer.21") + 
	            		model.getReportTemplate() + "'" ); //$NON-NLS-1$ //$NON-NLS-2$
	        }
	        String outputFile = null;
            String suffix = "." + model.getOutputFormat(); //$NON-NLS-1$
            String [] fileExt = new String[] { "*" + suffix }; //$NON-NLS-1$
	        if (model.getDestination().equals(GenericReport.DST_FILE)) {
	            FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN);
	            fileDialog.setText(Messages.getString("GenericReportViewer.25")); //$NON-NLS-1$
	            fileDialog.setFilterPath(GUIUtil.getMemorisedPath(GenericReportViewer.class));
	            
	            fileDialog.setFilterExtensions(fileExt);
	            outputFile = fileDialog.open();
	            if (outputFile == null) {
	                return;
	            }
	            
	            if (! outputFile.endsWith(suffix)) {
	                outputFile += suffix;
	            }
	            
	            if (! GUIUtil.confirmOverwriteFile(outputFile)) {
	                return;
	            }
	            
	            GUIUtil.setMemorisedPath(GenericReportViewer.class, new File(outputFile).getParent());
	        } else {
	            tempPath = GUIUtil.getTempFile(suffix);
	            outputFile = tempPath;
	        }
	        model.run(this.bomService);
            JasperPrint jp = JasperFillManager.fillReport(template, model.getParameters(), model);

	        if (model.getOutputFormat().equals(GenericReport.OF_PDF)) {
		        FileOutputStream fos = new FileOutputStream(outputFile);
	            JasperExportManager.exportReportToPdfStream(jp, fos);
		        fos.close();
	        } else if (model.getOutputFormat().equals(GenericReport.OF_HTML)) {
	        	ReportUtil.exportToHtml(jp, outputFile);
	        } else if (model.getOutputFormat().equals(GenericReport.OF_XML)) {
	            JasperExportManager.exportReportToXmlFile(jp, outputFile, true);
	        }
	        
	        if (new File(outputFile).exists()) {
	            GUIUtil.launchBrowser(new File(outputFile).toURI().toURL().toString(), model.toString()); //$NON-NLS-1$
	        } else {
	            throw new RuntimeException(Messages.getString("GenericReportViewer.27") + outputFile + Messages.getString("GenericReportViewer.28")); //$NON-NLS-1$ //$NON-NLS-2$
	        }
        } catch (Exception e) {
            LOG.error(String.format("generateReport(%s)", model.getName()), e);
            GUIUtil.showError(model.toString(), Messages.getString("GenericReportViewer.29"), e); //$NON-NLS-1$
        } finally {
            this.setCursor(Display.getCurrent().getSystemCursor(SWT.CURSOR_ARROW));
            this.setEnabled(true);
            try {
                template.close();
            } catch (Exception e) {
            	LOG.error(String.format("Failed to close template %s", model.getReportTemplate()), e);
            }
        }
    }
}
