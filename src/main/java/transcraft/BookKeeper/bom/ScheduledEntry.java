/**
 * Created on 07-Jun-2005
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
package transcraft.BookKeeper.bom;

import java.util.Calendar;
import java.util.Date;

/**
 * @author david.tran@transcraft.co.uk
 */
public class ScheduledEntry extends Entry {
    /**
	 * 
	 */
	private static final long serialVersionUID = -8716101086227314431L;
	private int scheduleType = Calendar.MONTH;
    private int scheduleCount = 0;
    private int scheduleVDIncr = 1;
    private int scheduleFrequency = 1;
    
    public ScheduledEntry(Integer entryId) {
        super(entryId);
    }
    /**
     * @param entryId
     * @param reference
     * @param description
     * @param fromAccount
     * @param postingDate
     * @param amount
     */
    public ScheduledEntry(Integer entryId, String reference,
            String description, String fromAccount, Date postingDate,
            double amount, int scheduleType, int scheduleInterval, int scheduleVDIncr) {
        super(entryId, reference, description, fromAccount, postingDate, amount);
        this.scheduleCount = scheduleInterval;
        this.scheduleType = scheduleType;
        this.scheduleVDIncr = scheduleVDIncr;
     }
    /**
     * @return Returns the scheduleCount.
     */
    public int getScheduleCount() {
        return scheduleCount;
    }
    /**
     * @param scheduleCount The scheduleCount to set.
     */
    public void setScheduleCount(int scheduleInterval) {
        this.scheduleCount = scheduleInterval;
    }
    /**
     * @return Returns the type of schedule. Value should be from Calendar enumerated values e.g. Calendar.MONTH
     */
    public int getScheduleType() {
        return scheduleType;
    }
    /**
     * @param scheduleType The scheduleType to set. Value should be from Calendar enumerated values e.g. Calendar.MONTH
     */
    public void setScheduleType(int scheduleType) {
        this.scheduleType = scheduleType;
    }
    /**
     * @return Returns the scheduleFrequency.
     */
    public int getScheduleFrequency() {
        return scheduleFrequency;
    }
    /**
     * @param scheduleFrequency The scheduleFrequency to set.
     */
    public void setScheduleFrequency(int scheduleFrequency) {
        this.scheduleFrequency = scheduleFrequency;
    }
    /**
     * @return Returns the increment to be added to the posting date to calculate the value date.
     */
    public int getScheduleVDIncr() {
        return scheduleVDIncr;
    }
    /**
     * @param scheduleVDIncr The increment to be added to the posting date to calculate the value date.
     */
    public void setScheduleVDIncr(int scheduleVDIncr) {
        this.scheduleVDIncr = scheduleVDIncr;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getDescription() != null ? this.getDescription() : Messages.getString("ScheduledEntry.0"); //$NON-NLS-1$
    }
}
