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
public class Entity {
    private String name;
    private String description;
    private Currency currency;
    private double denomination;
    private double scalingFactor;
    private EntityType entityType;
    
    /**
     * @param name
     */
    public Entity(String name) {
        super();
        this.name = name;
        this.denomination = 1;
        this.scalingFactor = 1;
    }
    
    /**
     * @param name
     * @param description
     * @param currency
     * @param denomination
     * @param scalingFactor
     * @param entityType
     */
    public Entity(String name, String description, Currency currency,
            double denomination, double scalingFactor) {
        this(name);
        this.description = description;
        this.currency = currency;
        this.denomination = denomination;
        this.scalingFactor = scalingFactor;
    }
    /**
     * @param name
     * @param description
     * @param currency
     * @param denomination
     * @param scalingFactor
     * @param entityType
     */
    public Entity(String name, String description, Currency currency,
            double denomination, double scalingFactor, EntityType entityType) {
        this(name, description, currency, denomination, scalingFactor);
        this.entityType = entityType;
    }
    
    /**
     * @return Returns the currency.
     */
    public Currency getCurrency() {
        return currency;
    }
    /**
     * @param currency The currency to set.
     */
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    /**
     * @return Returns the denomination.
     */
    public double getDenomination() {
        return denomination;
    }
    /**
     * @param denomination The denomination to set.
     */
    public void setDenomination(double denomination) {
        this.denomination = denomination;
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
     * @return Returns the entityType.
     */
    public EntityType getEntityType() {
        return entityType;
    }
    /**
     * @param entityType The entityType to set.
     */
    public void setEntityType(EntityType entityType) {
        this.entityType = entityType;
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
            return this.name.equalsIgnoreCase(((Entity)arg0).name);
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
