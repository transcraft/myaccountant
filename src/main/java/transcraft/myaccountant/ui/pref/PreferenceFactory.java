/**
 * Created on 09-Oct-2005
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
package transcraft.myaccountant.ui.pref;

import static org.slf4j.LoggerFactory.getLogger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;

import com.google.common.collect.Lists;

import transcraft.myaccountant.meta.TaxBandMetaProvider;
import transcraft.myaccountant.ui.GUIUtil;

/**
 * manages preferences
 * 
 * @author david.tran@transcraft.co.uk
 */
public class PreferenceFactory {

    // preference pages
    public static final String ACCTPERIOD_PP = "AccountPeriod"; //$NON-NLS-1$
    public static final String APPGUI_PP = "AppGui"; //$NON-NLS-1$
    public static final String INVOICE_PP = "Invoice"; //$NON-NLS-1$
    public static final String INVOICE_COMPANY_PP = "Invoice.Company"; //$NON-NLS-1$
    public static final String TAXBANDS_PP = "Tax.Band"; //$NON-NLS-1$

    public static final String FINYEAR_START = "period.financialYearStart"; //$NON-NLS-1$
    public static final String VATYEAR_START = "period.vatMonthStart"; //$NON-NLS-1$
    public static final String COMPANYYEAR_START = "period.companyYearStart"; //$NON-NLS-1$
    public static final String TREE_BG_COLOR = "gui.treeBgColor"; //$NON-NLS-1$
    public static final String EXIT_APP_CONFIRMED = "gui.exitAppConfirmed"; //$NON-NLS-1$
    public static final String DEF_FONT = "gui.defaultFont"; //$NON-NLS-1$
    public static final String INVOICE_ACCTS = "invoice.accounts"; //$NON-NLS-1$
    public static final String INVOICE_OUTPATH = "invoice.outPath"; //$NON-NLS-1$
    public static final String INVOICE_ADDRESS = "invoice.companyAddress"; //$NON-NLS-1$
    public static final String INVOICE_VATCODE = "invoice.vatCode"; //$NON-NLS-1$
    public static final String INVOICE_COMPANYNO   = "invoice.companyNo"; //$NON-NLS-1$
    public static final String INVOICE_COMPANYNAME   = "invoice.companyName"; //$NON-NLS-1$
    public static final String INVOICE_PHONENO = "invoice.phoneNo"; //$NON-NLS-1$
    public static final String INVOICE_FAXNO = "invoice.faxNo"; //$NON-NLS-1$
    public static final String INVOICE_URL = "invoice.url"; //$NON-NLS-1$
    public static final String INVOICE_EMAIL = "invoice.email"; //$NON-NLS-1$
    public static final String INVOICE_AUTOLEDGER = "invoice.autoLedgerGenerate"; //$NON-NLS-1$
    public static final String TAX_BANDS = "tax.bands"; //$NON-NLS-1$
    
    /**
     * month where financial year starts, 1 is January, 2 is February etc.
     */
    private static final int	yearStart = 4;
    /**
     * month where quarter starts, 1 is January, 2 is February etc.
     * 
     */
    private static final int		quarterStart = 4;
    /**
     * month where company year starts, 1 is January, 2 is February etc.
     */
    private static final int	companyYearStart = 4;
    
    private static PreferenceStore prefStore;
    private static final Logger log = getLogger(PreferenceFactory.class);
    
    public static PreferenceStore	getPreferenceStore() {
        if (prefStore == null) {
            String userDir = System.getProperty("user.home"); //$NON-NLS-1$
            String path = userDir + File.separator + "tbook.properties"; //$NON-NLS-1$
            File fp = new File(path);
            if (! fp.exists()) {
            	if (File.separator.equals("/")) {
            		// we must be on a Unix system
            		path = userDir + File.separator + ".tbook.properties";
            		fp = new File(path);
            	}
            	if (!fp.exists()) {
	                try {
	                	log.info(fp + " does not exist, creating it");
	                    fp.createNewFile();
	                } catch (IOException e) {
	                    GUIUtil.showError(Messages.getString("PreferenceFactory.25"), "Can not create '" + path + "'", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	                    return null;
	                }
            	}
            } else {
                /*
                 * perform a stealth migration here to move tbook.properties to .tbook.properties on Unix systems
                 */
                if (File.separator.equals("/")) {
                    // we must be on a Unix system
                    String newPath = userDir + File.separator + ".tbook.properties"; //$NON-NLS-1$
                    File newFp = new File(newPath);
                    if (newFp.exists()) {
                        if (newFp.lastModified() > fp.lastModified()) {
                        	log.info(newFp + " is newer than " + fp + ", switch to " + newFp);
                            fp.delete();
                            // switch to using our Unix file instead
                            fp = newFp;
                        } else {
                            try {
                            	log.info(newFp + " is older than " + fp + ", overwrite " + newFp + " with " + fp);
                                if (fp.renameTo(newFp)) {
                                	// switch to using our Unix file instead
                                	fp = newFp;
                                } else {
                                	log.warn("Failed to rename " + fp + " to " + newFp);
                                }
                            } catch (Exception e) {
                                log.warn("Failed to move " + fp + " to " + newFp + ":" +  e); 
                            }
                        }
                    } else {
                        try {
                        	log.info(newFp + " does not exist, move " + fp + " to " + newFp);
                            if (fp.renameTo(newFp)) {
                            	// switch to using our Unix file instead
                            	fp = newFp;
                            } else {
                            	log.warn("Failed to rename " + fp + " to " + newFp);
                            }
                        } catch (Exception e) {
                            log.warn("Failed to move " + fp + " to " + newFp + ":", e);
                        }
                    }
                }
            }
            
            log.info("Preference store used is " + fp);
            prefStore = new PreferenceStore(fp.getAbsolutePath());
            prefStore.setDefault(FINYEAR_START, "" + yearStart); //$NON-NLS-1$
            prefStore.setDefault(VATYEAR_START, "" + quarterStart); //$NON-NLS-1$
            prefStore.setDefault(COMPANYYEAR_START, "" + companyYearStart); //$NON-NLS-1$
            prefStore.setDefault(EXIT_APP_CONFIRMED, true);
            prefStore.setDefault(DEF_FONT, "Verdana"); //$NON-NLS-1$
            prefStore.setDefault(INVOICE_OUTPATH, System.getProperty("user.home")); //$NON-NLS-1$
            prefStore.setDefault(INVOICE_AUTOLEDGER, true);
            prefStore.setDefault(TAX_BANDS, new TaxBandMetaProvider().getDefaultTaxBandsAsPref());
            try {
                prefStore.load();
            } catch (Exception e) {
                log.warn("getPreferenceStore(" + path + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }
        return prefStore;
    }
        
    public static void	showPreferenceDialog(Shell parent) {
        PreferenceManager mgr = new PreferenceManager();

        // Accounting period
        PreferenceNode prefNode = new PreferenceNode(ACCTPERIOD_PP, new AccountingPeriodPreferencePage());
        mgr.addToRoot(prefNode);

        // Look and Feel
        prefNode = new PreferenceNode(ACCTPERIOD_PP, new LookFeelPreferencePage());
        mgr.addToRoot(prefNode);
        
        // Invoices
        prefNode = new PreferenceNode(INVOICE_PP, new InvoicePreferencePage());
        mgr.addToRoot(prefNode);
        PreferenceNode childNode = new PreferenceNode(INVOICE_COMPANY_PP, new InvoiceCompanyPreferencePage());
        prefNode.add(childNode);
        
        // Tax bands
        prefNode = new PreferenceNode(TAXBANDS_PP, new TaxBandPreferencePage());
        mgr.addToRoot(prefNode);
        
        PreferenceDialog dlg = new PreferenceDialog(parent, mgr);
        dlg.setPreferenceStore(getPreferenceStore());
        if (dlg.open() == SWT.OK) {
	        try {
	            getPreferenceStore().save();
	        } catch (Exception e) {
	            GUIUtil.showError(Messages.getString("PreferenceFactory.35"), Messages.getString("PreferenceFactory.36"), e); //$NON-NLS-1$ //$NON-NLS-2$
	        }
        }
    }
    
    /**
     * convenience method to convert a list property to a List
     * @param prop
     * @return
     */
    public static final List<String>	getAsList(String prop) {
        String str = getPreferenceStore().getString(prop);
        List<String> list = Lists.newArrayList();
        if (str != null) {
            StringTokenizer tokenizer = new StringTokenizer(str, ListFieldEditor.DEF_SEPARATOR);
            while (tokenizer.hasMoreTokens()) {
                list.add(tokenizer.nextToken());
            }
        }
        return list;
    }
    public static final void	storeList(String prop, List<String> list) throws IOException {
        StringBuffer buffer = new StringBuffer();
        for (String value : list) {
            buffer.append(value + ListFieldEditor.DEF_SEPARATOR);
        }
        getPreferenceStore().setValue(prop, buffer.toString());
        getPreferenceStore().save();
    }
    /**
     * 
     */
    public PreferenceFactory() {
        super();
        // TODO Auto-generated constructor stub
    }

}
