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
package transcraft.BookKeeper.bom;

/**
 * @author david.tran@transcraft.co.uk
 */
public class Currency {
    private String name;
    private String description;
    private double scalingFactor = 1;
    private double decimalPlaces = 2;
    
    public Currency(String name) {
        this.name = name;
    }
    /**
     * @param name
     * @param description
     * @param scalingFactor
     * @param decimalPlaces
     */
    public Currency(String name, String description, double scalingFactor,
            double decimalPlaces) {
        this(name);
        this.description = description;
        this.scalingFactor = scalingFactor;
        this.decimalPlaces = decimalPlaces;
    }
    /**
     * @return Returns the decimalPlaces.
     */
    public double getDecimalPlaces() {
        return decimalPlaces;
    }
    /**
     * @param decimalPlaces The decimalPlaces to set.
     */
    public void setDecimalPlaces(double decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
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
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object arg0) {
        try {
            return this.name.equalsIgnoreCase(((Currency)arg0).name);
        } catch (Exception e) {}
        return super.equals(arg0);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        try {
            return this.name.hashCode();
        } catch (Exception e) {}
        return super.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.name;
    }
    /**
     * @return Returns the scalingFactor.
     */
    public double getScalingFactor() {
        return scalingFactor;
    }
    /**
     * @param scalingFactor The scalingFactor to set.
     */
    public void setScalingFactor(double scalingFactor) {
        this.scalingFactor = scalingFactor;
    }
}
