/**
 * Created on 04-Oct-2005
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

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRField;

/**
 * batch of reports
 * 
 * @author david.tran@transcraft.co.uk
 */
public class BatchReport extends BaseGenericReport<Object> {

    private List<BaseGenericReport<?>> reports = Lists.newArrayList();
    /**
     * 
     */
    public BatchReport(String name) {
        super(name);
    }

    public void	addReport(GenericReport<?> report) {
        int idx = this.reports.indexOf(report);
        if (idx < 0) {
            this.reports.add(report.makeCopy(report.getDescription()));
        } else {
            this.reports.set(idx, report.makeCopy(report.getDescription()));
        }
    }
    
    public void	removeReport(GenericReport<?> report) {
        this.reports.remove(report);
    }
    public GenericReport<?> []	getReports() {
        // Java 8 compatibility mode, so GenericReport<?>[]::new is not available
        return this.reports.toArray(new GenericReport<?>[reports.size()]);
    }
    
    /* (non-Javadoc)
     * @see transcraft.myaccountant.report.BaseGenericReport#getParameters()
     */
    public Map<String, Object> getParameters() throws JRException {
        return Maps.newHashMap();
    }
    /* (non-Javadoc)
     * @see net.sf.jasperreports.engine.JRDataSource#getFieldValue(net.sf.jasperreports.engine.JRField)
     */
    public Object getFieldValue(JRField field) throws JRException {
        return null;
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.report.GenericReport#run()
     */
    public void run() {
    }
}
