/**
 * Created on 20-Oct-2005
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

/**
 * tax band entry
 * 
 * @author david.tran@transcraft.co.uk
 */
public class TaxBandEntry {

    private String code;
    private String description = ""; //$NON-NLS-1$
    private double rate;
    private boolean defaultBand = false;
    
    public TaxBandEntry() {
    }
    
    /**
     * used for quick instantiation
     * @param description
     * @param rate
     */
    public TaxBandEntry(String code, String description, double rate) {
        this.code = code;
        this.description = description;
        this.rate = rate;
    }
    /**
     * 
     * @param code
     * @param description
     * @param rate
     * @param defaultBand
     */
    public TaxBandEntry(String code, String description, double rate, boolean defaultBand) {
        this(code, description, rate);
        this.defaultBand = defaultBand;
    }
    /**
     * @return Returns the code.
     */
    public String getCode() {
        return code;
    }
    /**
     * @param code The code to set.
     */
    public void setCode(String code) {
        this.code = code;
    }
    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }
    /**
     * @param description The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * @return Returns the rate.
     */
    public double getRate() {
        return rate;
    }
    /**
     * @param rate The rate to set.
     */
    public void setRate(double rate) {
        this.rate = rate;
    }
    /**
     * @return Returns the defaultBand.
     */
    public boolean isDefaultBand() {
        return defaultBand;
    }
    /**
     * @param defaultBand The defaultBand to set.
     */
    public void setDefaultBand(boolean defaultBand) {
        this.defaultBand = defaultBand;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        try {
            return this.code.equals(((TaxBandEntry)arg0).code);
        } catch (Exception e) {}
        return false;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.code != null ? this.code.hashCode() : super.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "{" + this.code + "=" + this.rate + "}"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}