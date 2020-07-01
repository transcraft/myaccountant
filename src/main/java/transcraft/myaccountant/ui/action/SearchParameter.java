/**
 * Created on 22-Oct-2005
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
package transcraft.myaccountant.ui.action;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * domain object for describing a search
 * 
 * @author david.tran@transcraft.co.uk
 */
public class SearchParameter {
    private Object scope;
    private Class<?> prototype;
    private Map<Object, Object> parameters = Maps.newHashMap();
    private boolean forward = false;
    private boolean matchAll = true;
    private boolean caseSensitive = false;
    
    /**
     * 
     */
    public SearchParameter() {
    }

    /**
     * convenience constructor to pass in scope and prototype
     * @param scope
     * @param prototype
     */
    public SearchParameter(Object scope, Class<?> prototype) {
        this.prototype = prototype;
        this.scope = scope;
    }
    /**
     * blank forward/reverse search
     * @param forward
     */
    public SearchParameter(boolean forward) {
        this.forward = forward;
    }
    /**
     * prototype indicates the type of object the search is intended for
     * @return Returns the prototype.
     */
    public Class<?> getPrototype() {
        return this.prototype;
    }
    /**
     * prototype indicates the type of object the search is intended for
     * @param prototype The prototype to set.
     */
    public void setPrototype(Class<?> prototype) {
        this.prototype = prototype;
    }
    /**
     * scope can be set to indicate the constraint of the search
     * @return Returns the scope.
     */
    public Object getScope() {
        return scope;
    }
    /**
     * scope can be set to indicate the constraint of the search
     * @param scope The scope to set.
     */
    public void setScope(Object scope) {
        this.scope = scope;
    }
    /**
     * @return Returns the caseSensitive.
     */
    public boolean isCaseSensitive() {
        return caseSensitive;
    }
    /**
     * @param caseSensitive The caseSensitive to set.
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }
    /**
     * @return Returns the forward.
     */
    public boolean isForward() {
        return forward;
    }
    /**
     * @param forward The forward to set.
     */
    public void setForward(boolean forward) {
        this.forward = forward;
    }
    /**
     * @return Returns the matchAll.
     */
    public boolean isMatchAll() {
        return matchAll;
    }
    /**
     * @param matchAll The matchAll to set.
     */
    public void setMatchAll(boolean matchAll) {
        this.matchAll = matchAll;
    }
    /**
     * @return Returns the search criteria.
     */
    public Object [][] getCriteria() {
        if (this.parameters.size() < 1) {
            // make sure we never return null
            return new Object[1][2];
        }
        Object [][] params = new Object[this.parameters.size()][2];
        int i = 0;
        for (Map.Entry<Object, Object> entry : this.parameters.entrySet()) {
            params[i][0] = entry.getKey();
            params[i][1] = entry.getValue();
            i++;
        }
        return params;
    }
    /**
     * add a new search criterium
     * @param key
     * @param value
     */
    public void	addCriterium(Object key, Object value) {
        this.parameters.put(key, value);
    }
}
