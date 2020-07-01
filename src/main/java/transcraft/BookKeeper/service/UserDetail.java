/**
 * Created on 05-Nov-2005
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
package transcraft.BookKeeper.service;

/**
 * Domain model for a user. This class can not be instantiated outside
 * this package as it is used to store authentication data
 * 
 * @author david.tran@transcraft.co.uk
 */
public class UserDetail {

    private String name;
    private byte[] password;
    private String realName;
    private String role;
    
    /**
     * 
     */
    public UserDetail(String name) {
        this.name = name;
    }

    /**
     * @return Returns the password.
     */
    byte[] getPassword() {
        return password;
    }
    /*
     * convenience method to check if user has password set
     */
    boolean hasNoPassword() {
        return this.password == null || this.password.length == 0;
    }
    /**
     * @param password The password to set.
     */
    void setPassword(byte[] password) {
        this.password = password;
    }
    /**
     * @return Returns the realName.
     */
    public String getRealName() {
        return realName;
    }
    /**
     * @param realName The realName to set.
     */
    public void setRealName(String realName) {
        this.realName = realName;
    }
    /**
     * @return Returns the role.
     */
    public String getRole() {
        return role;
    }
    /**
     * @param role The role to set.
     */
    public void setRole(String role) {
        this.role = role;
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
    public boolean equals(Object obj) {
        try {
            return this.name.equalsIgnoreCase(((UserDetail)obj).name);
        } catch (Exception e) {}
        return false;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return this.name.hashCode();
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.name;
    }
}
