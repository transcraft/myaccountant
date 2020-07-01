/**
 * Created on 04-Sep-2005
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

import java.util.Calendar;
import java.util.Date;

import transcraft.myaccountant.ui.pref.PreferenceFactory;
import transcraft.myaccountant.utils.DateUtil;

/**
 * support helper class for date range calculations for reporting
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ReportDateUtil {

    public static final int RPTD_THISMONTH = 0;
    public static final int RPTD_LASTMONTH = 1;
    public static final int RPTD_THISTAXYEAR = 2;
    public static final int RPTD_LASTTAXYEAR = 3;
    public static final int RPTD_THISQRTR = 4;
    public static final int RPTD_LASTQRTR = 5;
    public static final int RPTD_THISWEEK = 6;
    public static final int RPTD_LASTWEEK = 7;
    public static final int RPTD_CUSTOM = 8;
    public static final int RPTD_ALL = 9;
    public static final int RPTD_THISCOMPYEAR = 10;
    public static final int RPTD_LASTCOMPYEAR = 11;
    
    public static final ReportDateMeta [] getMeta() {
        return new ReportDateMeta [] {
            new ReportDateMeta(RPTD_THISMONTH, Messages.getString("ReportDateUtil.0")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_LASTMONTH, Messages.getString("ReportDateUtil.1")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_THISTAXYEAR, Messages.getString("ReportDateUtil.2")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_LASTTAXYEAR, Messages.getString("ReportDateUtil.3")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_THISCOMPYEAR, Messages.getString("ReportDateUtil.4")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_LASTCOMPYEAR, Messages.getString("ReportDateUtil.5")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_THISQRTR, Messages.getString("ReportDateUtil.6")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_LASTQRTR, Messages.getString("ReportDateUtil.7")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_THISWEEK, Messages.getString("ReportDateUtil.8")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_LASTWEEK, Messages.getString("ReportDateUtil.9")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_ALL, Messages.getString("ReportDateUtil.10")), //$NON-NLS-1$
            new ReportDateMeta(RPTD_CUSTOM, Messages.getString("ReportDateUtil.11")), //$NON-NLS-1$
        };
    }

    public static final String [] getMetaNames() {
        ReportDateMeta [] metas = getMeta();
        String [] names = new String[metas.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = metas[i].getName();
        }
        return names;
    }
    
    public static final ReportDateMeta	getMetaForType(int type) {
        ReportDateMeta [] metas = ReportDateUtil.getMeta();
        ReportDateMeta meta = null;
        for (int i = 0; i < metas.length; i++) {
            if (metas[i].getType() == type) {
                meta = metas[i];
                break;
            }
        }
        if (meta == null) {
            meta = new ReportDateMeta(metas[metas.length - 1]);
        }
        new ReportDateUtil().calculate(meta);
        return meta;
    }

    public static final ReportDateMeta	getMetaForName(String name) {
        ReportDateMeta [] metas = ReportDateUtil.getMeta();
        ReportDateMeta meta = null;
        for (int i = 0; i < metas.length; i++) {
            if (metas[i].getName().equals(name)) {
                meta = metas[i];
                break;
            }
        }
        if (meta == null) {
            meta = new ReportDateMeta(metas[metas.length - 1]);
        }
        new ReportDateUtil().calculate(meta);
        return meta;
    }
    
    private Calendar currentBusinessDay = DateUtil.getTodayStart();
    private int yearStart;
    private int quarterStart;
    private int companyYearStart;
    
    public ReportDateUtil() {
        this.yearStart = PreferenceFactory.getPreferenceStore().getInt(PreferenceFactory.FINYEAR_START);
        this.quarterStart = PreferenceFactory.getPreferenceStore().getInt(PreferenceFactory.VATYEAR_START);
        this.companyYearStart = PreferenceFactory.getPreferenceStore().getInt(PreferenceFactory.COMPANYYEAR_START);
    }
    
    public ReportDateUtil(Calendar cal) {
        if (cal != null) {
            this.currentBusinessDay = cal;
        }
    }
    
    public ReportDateUtil(Date dt) {
        if (dt != null) {
            this.currentBusinessDay.setTime(dt);
        }
    }

    public void	calculate(ReportDateMeta meta) {
        switch (meta.getType()) {
	        case RPTD_THISMONTH:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 1);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_LASTMONTH:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	            cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.add(Calendar.MONTH, -1);
	            meta.setStartDate(cal.getTime());
	        }
	        break;
	        case RPTD_THISQRTR:
	        {
	            int quarter = this.getCurrentQuarter();
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
                if (cal.get((Calendar.MONTH) + 1) < quarter) {
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                }
	            cal.set(Calendar.MONTH, quarter - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 3);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_LASTQRTR:
	        {
	            int quarter = this.getLastQuarter();
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
                if ((cal.get(Calendar.MONTH) + 1) < quarter) {
                    cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) - 1);
                }
	            cal.set(Calendar.MONTH, quarter - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 3);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_THISTAXYEAR:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.MONTH, this.yearStart - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 12);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_LASTTAXYEAR:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.add(Calendar.YEAR, -1);
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.MONTH, this.yearStart - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 12);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_THISCOMPYEAR:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.MONTH, this.companyYearStart - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 12);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_LASTCOMPYEAR:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.add(Calendar.YEAR, -1);
	            cal.set(Calendar.DAY_OF_MONTH, 1);
	            cal.set(Calendar.MONTH, this.companyYearStart - 1);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.MONTH, 12);
	            cal.add(Calendar.DAY_OF_MONTH, -1);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_THISWEEK:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_WEEK, 1);
	            cal.add(Calendar.DAY_OF_WEEK, -7);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.DAY_OF_WEEK, 6);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_LASTWEEK:
	        {
	            Calendar cal = this.getCurrentBusinessDay();
	            cal.set(Calendar.DAY_OF_WEEK, 1);
	            cal.add(Calendar.DAY_OF_WEEK, -14);
	            meta.setStartDate(cal.getTime());
	            cal.add(Calendar.DAY_OF_WEEK, 6);
	            meta.setEndDate(cal.getTime());
	        }
	        break;
	        case RPTD_ALL:
	        {
	            meta.setStartDate(null);
	            meta.setEndDate(null);
	        }
	        break;
	        case RPTD_CUSTOM:
	        {
	            Calendar today = this.getCurrentBusinessDay();
	            meta.setStartDate(today.getTime());
	            meta.setEndDate(today.getTime());
	        }
	            break;
	        default:
	            throw new RuntimeException(Messages.getString("ReportDateUtil.12") + meta); //$NON-NLS-1$
        }
    }
    
    public int	getLastQuarter() {
        int quarter = this.getCurrentQuarter();
        quarter -= 3;
        if (quarter < 0) {
            quarter += 12;
        }
        return quarter;
    }
    
    public int	getCurrentQuarter() {
        Calendar cal = this.getCurrentBusinessDay();
        int month = cal.get(Calendar.MONTH);
        // internal calcs require quarters to be zero based indexed
        int quarter = this.quarterStart - 1;
        for (int i = 0; i < 4; i++) {
            if ((quarter + 3) > 12) {
                if (quarter <= (month + 12) && (month + 12) < (quarter + 3)) {
                    break;
                }                
            }
            if (quarter <= month && month < (quarter + 3)) {
                break;
            }
            quarter = (quarter + 3) % 12;
        }
        // GUI and prefs require quarter to be 1 based indexed
        return quarter + 1;
    }
    
    /**
     * @return Returns the currentBusinessDay.
     */
    public Calendar getCurrentBusinessDay() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(this.currentBusinessDay.getTime());
        return cal;
    }
    /**
     * @return Returns the quarterStart.
     */
    public int getQuarterStart() {
        return quarterStart;
    }
    /**
     * @param quarterStart The quarterStart to set.
     */
    public void setQuarterStart(int quarterStart) {
        if (quarterStart > 0 && quarterStart <= 12) {
            this.quarterStart = quarterStart;
        } else {
            throw new RuntimeException(Messages.getString("ReportDateUtil.13")); //$NON-NLS-1$
        }
    }
    /**
     * @return Returns the yearStart.
     */
    public int getYearStart() {
        return yearStart;
    }
    /**
     * @param yearStart The yearStart to set.
     */
    public void setYearStart(int yearStart) {
        if (yearStart > 0 && yearStart <= 12) {
            this.yearStart = yearStart;
        } else {
            throw new RuntimeException(Messages.getString("ReportDateUtil.14")); //$NON-NLS-1$
        }
    }
}
