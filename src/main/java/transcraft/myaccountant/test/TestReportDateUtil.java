/**
 * Created on 05-Sep-2005
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
package transcraft.myaccountant.test;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transcraft.myaccountant.report.ReportDateMeta;
import transcraft.myaccountant.report.ReportDateUtil;
import transcraft.myaccountant.utils.DateUtil;

/**
 * test class for ReportDateUtil
 * 
 * @author david.tran@transcraft.co.uk
 */
public class TestReportDateUtil {
    private static final Logger LOG = LoggerFactory.getLogger(TestReportDateUtil.class);
    
    /**
     * 
     */
    public TestReportDateUtil(Date dt, int quarterStart, int yearStart) {
        ReportDateMeta [] metaList = ReportDateUtil.getMeta();
        ReportDateUtil rpd = new ReportDateUtil(dt);
        rpd.setQuarterStart(quarterStart);
        rpd.setYearStart(yearStart);
        System.out.println("Today is " + DateUtil.format(dt)); //$NON-NLS-1$
        for (int i = 0; i < metaList.length; i++) {
            rpd.calculate(metaList[i]);
            System.out.println(this.dumpMeta(metaList[i]));
        }
    }

    public String	dumpMeta(ReportDateMeta meta) {
        return "[" + meta.getName() + "] => " + DateUtil.format(meta.getStartDate()) + "," + DateUtil.format(meta.getEndDate()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
    public static void main(String [] args) {
        try {
            Date dt = null;
            int yearStart = 4;
            int quarterStart = 4;
            if (args.length > 0) {
                dt = DateUtil.parse(args[0]);
                if (args.length > 1) {
                    quarterStart = Integer.parseInt(args[1]);
                }
                if (args.length > 2) {
                    yearStart = Integer.parseInt(args[2]);
                }
            }
            new TestReportDateUtil(dt, quarterStart, yearStart);
        } catch (Exception e) {
        	LOG.error("main()", e);
        }
    }
}
