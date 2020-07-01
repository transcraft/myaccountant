/**
 * Created on 05-Jun-2005
 *
 * Copyrights (c) Transcraft Trading Limited 2003-2004. All rights reserved.
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
package transcraft.myaccountant.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author david.tran@transcraft.co.uk
 */
public class DateUtil {
    private static final DateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy"); //$NON-NLS-1$
    private static final Logger LOG = LoggerFactory.getLogger(DateUtil.class);
    
    public static Date	datePart(Date ts) {
        Calendar cal = getTodayStart();
        cal.setTime(ts);
        return cal.getTime();
    }
    public static String format(Date dt) {
        return formatter.format(dt);
    }
    public static Date	parse(Object value) throws ParseException {
        try {
            if (value instanceof Date) {
                return (Date)value;
            } else if (value instanceof Calendar) {
                return ((Calendar)value).getTime();
            } else if (value instanceof String) {
            	String [] parts = ((String)value).split("\\W"); //$NON-NLS-1$
                Calendar cal = getTodayStart();
            	if (parts.length >= 1) {
                    try {
                    	cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(parts[0]));
                    	if (parts.length == 1) {
	                        LOG.debug("parse({}) => {}", value, cal.getTime()); //$NON-NLS-1$
	                    	return cal.getTime();
                    	}
                    } catch (Exception e) {
                    	LOG.error(String.format("Failed to parse date part '%s'", parts[0], e)); //$NON-NLS-1$
                    }
            	}
            	if (parts.length >= 2) {
                    try {
                    	cal.set(Calendar.MONTH, Integer.parseInt(parts[1]) - 1);
                    	if (parts.length == 2) {
                    		LOG.debug("parse({}) date and month => {}", value, cal.getTime()); //$NON-NLS-1$
                    		return cal.getTime();
                    	}
                    } catch (Exception e) {
                    	LOG.error(String.format("Failed to parse month '%s'", parts[1], e)); //$NON-NLS-1$
                    }
            	}
                try {
                	DateFormat fmt = DateFormat.getDateInstance(DateFormat.SHORT);
                	fmt.setLenient(true);
                	Date dt = fmt.parse(value.toString());
                	LOG.debug("parse({}) short => {}", value, dt); //$NON-NLS-1$
                	return dt;
                } catch (Exception e) {
                	LOG.error(String.format("Failed to parse with short formatter '%s'", value, e)); //$NON-NLS-1$
                    try {
                    	Date dt = formatter.parse(value.toString());
                    	LOG.debug("parse({}) predefined => {}", value, dt); //$NON-NLS-1$
                    	return dt;
                    } catch (Exception e1) {
                    	LOG.error(String.format("Failed to parse with predefined formatter '%s'", value, e1)); //$NON-NLS-1$
                    }
                }
                return cal.getTime();
            }
        } catch (Exception e) {
        	LOG.error(String.format("parse(%s)", value, e)); //$NON-NLS-1$
        }
        return formatter.parse(value.toString());
    }
    
    public static void main(String [] args) {
        DateFormat dtf = new SimpleDateFormat("yyyy-MM-dd"); //$NON-NLS-1$
        try {
            Date dt = dtf.parse(args[0]);
            LOG.debug("You entered '{}'", datePart(dt)); //$NON-NLS-1$
        } catch (Exception e) {
            System.err.println(e);
        }
    }
    public static Calendar	getCalendar(Object value) throws ParseException {
        if (value != null && value instanceof String) {
            Date dt = DateUtil.parse(value);
            Calendar cal = Calendar.getInstance();
            cal.setTime(dt);
            value = cal;
        }
        return (Calendar)value;
    }
    public static Date	getDate(Object value) throws ParseException {
        Calendar cal = getCalendar(value);
        if (cal != null) {
            return cal.getTime();
        }
        return (Date)value;
    }
    /**
     * return start of today
     * @return
     */
    public static Calendar	getTodayStart() {
        Calendar today = Calendar.getInstance();
        //today.add(Calendar.DAY_OF_MONTH, 1);
        today.set(Calendar.HOUR, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        today.set(Calendar.AM_PM, Calendar.AM);
        return today;
    }
}
