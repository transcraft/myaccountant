/**
 * Created on 05-Jul-2005
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
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;

import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.ScheduledEntry;
import transcraft.myaccountant.utils.DateUtil;
import transcraft.myaccountant.utils.Formatters;

/**
 * descriptor for fields in the ScheduledEntryViewer object.
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ScheduledEntryMeta extends BaseMetaProvider<ScheduledEntry> {
    
    public static final String ID_PROP = "entryId"; //$NON-NLS-1$
    public static final String PD_PROP = "postingDate"; //$NON-NLS-1$
    public static final String REF_PROP = "reference"; //$NON-NLS-1$
    public static final String DESC_PROP = "description"; //$NON-NLS-1$
    public static final String FR_ACCT_PROP = "fromAccount"; //$NON-NLS-1$
    public static final String TO_ACCT_PROP = "toAccount"; //$NON-NLS-1$
    public static final String AMT_PROP = "amount"; //$NON-NLS-1$
    public static final String SCH_TYPE_PROP = "scheduleType"; //$NON-NLS-1$
    public static final String SCH_COUNT_PROP = "scheduleCount"; //$NON-NLS-1$
    public static final String SCH_FREQ_PROP = "scheduleFrequency"; //$NON-NLS-1$
    public static final String SCH_VDINCR_PROP = "scheduleVDIncr"; //$NON-NLS-1$
    public static final String ALLOC_PROP = "allocationRule"; //$NON-NLS-1$
    
    public static final String ST_NONE = Messages.getString("ScheduledEntryMeta.12"); //$NON-NLS-1$
    public static final String ST_DAY = Messages.getString("ScheduledEntryMeta.13"); //$NON-NLS-1$
    public static final String ST_MONTH = Messages.getString("ScheduledEntryMeta.14"); //$NON-NLS-1$
    public static final String ST_WEEK = Messages.getString("ScheduledEntryMeta.15"); //$NON-NLS-1$
    public static final String ST_YEAR = Messages.getString("ScheduledEntryMeta.16"); //$NON-NLS-1$

    private String [] scheduleTypes = new String [] {
            ST_NONE,
            ST_DAY,
            ST_WEEK,
            ST_MONTH,
            ST_YEAR
    };

    public ScheduledEntryMeta() {
        this.columns = new MetaColumn [] {
                new MetaColumn<Date>(PD_PROP, Messages.getString("ScheduledEntryMeta.17"), Date.class, 130), //$NON-NLS-1$
                new MetaColumn<String>(REF_PROP, Messages.getString("ScheduledEntryMeta.18"), String.class, 70, Lists.newArrayList(), true, SWT.NONE), //$NON-NLS-1$
                new MetaColumn<String>(DESC_PROP, Messages.getString("ScheduledEntryMeta.19"), String.class, 220, Lists.newArrayList(), true, SWT.NONE), //$NON-NLS-1$
                new MetaColumn<String>(FR_ACCT_PROP, Messages.getString("ScheduledEntryMeta.20"), String.class, 110, Lists.newArrayList()), //$NON-NLS-1$
                new MetaColumn<String>(TO_ACCT_PROP, Messages.getString("ScheduledEntryMeta.21"), String.class, 110, Lists.newArrayList(), true, SWT.NONE), //$NON-NLS-1$
                new MetaColumn<Double>(AMT_PROP, Messages.getString("ScheduledEntryMeta.22"), Double.class, 100, SWT.RIGHT), //$NON-NLS-1$
                new MetaColumn<Integer>(SCH_TYPE_PROP, Messages.getString("ScheduledEntryMeta.23"), Integer.class, 100, scheduleTypes), //$NON-NLS-1$
                new MetaColumn<Integer>(SCH_FREQ_PROP, Messages.getString("ScheduledEntryMeta.24"), Integer.class, 150, SWT.RIGHT), //$NON-NLS-1$
                new MetaColumn<Integer>(SCH_COUNT_PROP, Messages.getString("ScheduledEntryMeta.25"), Integer.class, 120, SWT.RIGHT), //$NON-NLS-1$
                new MetaColumn<Integer>(SCH_VDINCR_PROP, Messages.getString("ScheduledEntryMeta.26"), Integer.class, 150, SWT.RIGHT), //$NON-NLS-1$
                new MetaColumn<AllocationRule>(ALLOC_PROP, Messages.getString("ScheduledEntryMeta.27"), AllocationRule.class, 100, Lists.newArrayList(), true, SWT.NONE) //$NON-NLS-1$
        };
    }
    /* (non-Javadoc)
     * @see transcraft.myaccountant.meta.MetaProvider#getPrimaryKey()
     */
    @Override
    public String getPrimaryKey() {
        return ID_PROP;
    }
    
    @Override
    public Object getValue(ScheduledEntry se, String columnName, String accountReference) {
        if (ID_PROP.equals(columnName)) {
            return se.getEntryId();
        } else if (PD_PROP.equals(columnName)) {
            return se.getPostingDate();
        } else if (REF_PROP.equals(columnName)) {
            return se.getReference() != null ? se.getReference() : ""; //$NON-NLS-1$
        } else if (DESC_PROP.equals(columnName)) {
            return se.getDescription() != null ? se.getDescription() : ""; //$NON-NLS-1$
        } else if (FR_ACCT_PROP.equals(columnName)) {
            return se.getFromAccount();
        } else if (TO_ACCT_PROP.equals(columnName)) {
            return se.getToAccount();
        } else if (AMT_PROP.equals(columnName)) {
            return Double.valueOf(se.getAmount());
        } else if (SCH_TYPE_PROP.equals(columnName)) {
            switch(se.getScheduleType()) {
            case Calendar.DAY_OF_MONTH:
                return ST_DAY;
            case Calendar.WEEK_OF_MONTH:
                return ST_WEEK;
            case Calendar.MONTH:
                return ST_MONTH;
            case Calendar.YEAR:
                return ST_YEAR;
            default:
                return ST_NONE;
            }
        } else if (SCH_FREQ_PROP.equals(columnName)) {
            return Integer.valueOf(se.getScheduleFrequency());
        } else if (SCH_COUNT_PROP.equals(columnName)) {
            return Integer.valueOf(se.getScheduleCount());
        } else if (SCH_VDINCR_PROP.equals(columnName)) {
            return Integer.valueOf(se.getScheduleVDIncr());
        } else if (ALLOC_PROP.equals(columnName)) {
            return se.getName();
        }
        return ""; //$NON-NLS-1$
    }
    
    @Override
    public void	setValue(ScheduledEntry se, String accountReference, String columnName, Object value) throws ParseException {
        if (PD_PROP.equals(columnName)) {
            se.setPostingDate(DateUtil.getCalendar(value).getTime());
        } else if (REF_PROP.equals(columnName)) {
            se.setReference(value.toString());
        } else if (DESC_PROP.equals(columnName)) {
            se.setDescription(value.toString());
        } else if (FR_ACCT_PROP.equals(columnName)) {
            if (value != null && value.toString().length() > 0) {
                se.setFromAccount(value.toString());
            }
        } else if (TO_ACCT_PROP.equals(columnName)) {
            if (value != null && value.toString().length() > 0) {
                se.setToAccount(value.toString());
            }
        } else if (AMT_PROP.equals(columnName)) {
            if (value != null && value.toString().length() > 0) {
	            //value = value.toString().replaceAll(",", "");
	            double amount = Formatters.parse(value.toString()).doubleValue();
	            se.setAmount(amount);
            }
        } else if (SCH_TYPE_PROP.equals(columnName)) {
            int scheduleType = -1;
            if (value.equals(ST_DAY)) {
                scheduleType = Calendar.DAY_OF_MONTH;
            } else if (value.equals(ST_WEEK)) {
                scheduleType = Calendar.WEEK_OF_MONTH;
            } else if (value.equals(ST_MONTH)) {
                scheduleType = Calendar.MONTH;
            } else if (value.equals(ST_YEAR)) {
                scheduleType = Calendar.YEAR;
            }
            if (scheduleType != -1) {
                se.setScheduleType(scheduleType);
            }
        } else if (SCH_FREQ_PROP.equals(columnName)) {
            se.setScheduleFrequency(Integer.parseInt(value.toString()));
        } else if (SCH_COUNT_PROP.equals(columnName)) {
            se.setScheduleCount(Integer.parseInt(value.toString()));
        } else if (SCH_VDINCR_PROP.equals(columnName)) {
            se.setScheduleVDIncr(Integer.parseInt(value.toString()));
        } else if (ALLOC_PROP.equals(columnName)) {
            se.setName(value.toString());
        }
    }
}
