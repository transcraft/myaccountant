/**
 * Created on 29-Jul-2005
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
package transcraft.myaccountant.utils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * @author david.tran@transcraft.co.uk
 */
public class Formatters {
    private static final NumberFormat nfmt = NumberFormat.getInstance();
    private static final NumberFormat ifmt = NumberFormat.getIntegerInstance();
    private static final DateFormat dfmt = DateFormat.getDateInstance(DateFormat.SHORT);
    
    static {
        nfmt.setMinimumFractionDigits(2);
        nfmt.setMaximumFractionDigits(2);
        nfmt.setGroupingUsed(true);
        ifmt.setGroupingUsed(true);
    }
    
    public static String format(double value) {
        return nfmt.format(value);
    }
    public static String format(double value, int precision) {
        NumberFormat nf = new DecimalFormat();
        nf.setMinimumFractionDigits(precision);
        return nf.format(value);
    }
    public static String format(int value) {
        return ifmt.format(value);
    }
    public static String format(Object value) {
        if (value == null) {
            return ""; //$NON-NLS-1$
        }
        if (value instanceof Integer) {
            return ifmt.format(((Integer)value).intValue());
        } else if (value instanceof Number) {
            return nfmt.format(((Number)value).doubleValue());
        } else if (value instanceof Date) {
            return dfmt.format((Date)value);
        }
        return value.toString();
    }
    
    public static Number parse(String str) throws ParseException {
        return nfmt.parse(str);
    }
    
    public static Object	nullIfEmpty(String str) {
        return str != null && str.length() > 0 ? str : null;
    }
    
    public static String	emptyIfNull(Object obj) {
        return obj != null && obj.toString().length() > 0 ? obj.toString() : ""; //$NON-NLS-1$
    }
}
