/**
 * Created on 08-Jul-2005
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


import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.InvoiceItem;
import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.meta.InvoiceItemMeta;
import transcraft.myaccountant.meta.InvoiceMeta1;
import transcraft.myaccountant.meta.InvoiceMeta2;
import transcraft.myaccountant.meta.MetaProvider;
import transcraft.myaccountant.report.BaseGenericReport;
import transcraft.myaccountant.report.InvoiceServicesReport;
import transcraft.myaccountant.report.ReportUtil;
import transcraft.myaccountant.ui.action.ActionableTarget;

/**
 * @author david.tran@transcraft.co.uk
 */
public class InvoiceViewer extends ActionableTarget<Invoice> {
    private InvoiceMeta1Viewer invoiceViewer;
    private InvoiceMeta2Viewer invoice2Viewer;
    private BOMService bomService;
    private MetaProvider<Invoice> provider;
    private MetaProvider<Invoice> invoice2Provider;
    private MetaProvider<InvoiceItem> itemProvider;
    private Invoice invoice;
    private InvoiceItemViewer itemViewer;

    /**
     * @param parent
     */
    public InvoiceViewer(Composite parent, BOMService bomService, InvoiceMeta1 provider, Invoice invoice) {
        super(parent, SWT.FLAT);
        this.bomService = bomService;
        this.provider = provider;
        this.invoice = invoice;
        this.invoice2Provider = new InvoiceMeta2(invoice);
        this.itemProvider = new InvoiceItemMeta(invoice);
        this.createContents();
    }    
    
	protected void createContents() {
        GridLayout ly = new GridLayout();
        ly.numColumns = 2;
        ly.marginWidth = 10;
        this.setLayout(ly);
        this.invoiceViewer = new InvoiceMeta1Viewer(this, this.bomService, this.provider, this.invoice);
        this.invoiceViewer.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL|GridData.FILL_HORIZONTAL));
        this.invoice2Viewer = new InvoiceMeta2Viewer(this, this.bomService, this.invoice2Provider, this.invoice);
        this.invoice2Viewer.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL|GridData.FILL_HORIZONTAL));

        this.itemViewer = new InvoiceItemViewer(this, this.bomService, this.itemProvider, this.invoice);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        this.itemViewer.setLayoutData(gd);
        this.invoiceViewer.setItemViewer(this.itemViewer);
        this.invoiceViewer.setInvoice2Viewer(this.invoice2Viewer);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGenerateHTML()
     */
    @Override
    public boolean canGenerateHTML() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#canGeneratePDF()
     */
    @Override
    public boolean canGeneratePDF() {
        return true;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doHTML()
     */
    @Override
    public void doHTML() {
        this._doGenerate(BaseGenericReport.OF_HTML);
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.ui.action.Generateable#doPDF()
     */
    @Override
    public void doPDF() {
        this._doGenerate(BaseGenericReport.OF_PDF);
    }
    
    protected void	_doGenerate(String format) {
        InvoiceServicesReport report = new InvoiceServicesReport(this.invoice);
        ReportUtil.generateOnlineReport(this, this.bomService, report, format);
    }
}
