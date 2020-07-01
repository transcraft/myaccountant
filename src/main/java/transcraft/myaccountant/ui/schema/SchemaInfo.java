/**
 * Created on 15-Oct-2005
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
package transcraft.myaccountant.ui.schema;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

/**
 * @author david.tran@transcraft.co.uk
 */
public class SchemaInfo {
	
    private final Object data;
    private final Long id;
    
	private static final String ROOT_PACKAGE_NAME = StringUtils.split(SchemaInfo.class.getPackage().getName(), '.')[0];

    public SchemaInfo(Long id, Object data) {
        this.id = id;
        this.data = data;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @return Returns the obj.
     */
    public Object getData() {
        return this.data;
    }

	/* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object arg0) {
        try {
            return this.id.equals(((SchemaInfo)arg0).id);
        } catch (Exception e) {}
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        String str = Long.valueOf(this.id).toString();
        if (this.data != null) {
            String displayName = this.getDisplayName("getDescription"); //$NON-NLS-1$
            if (displayName == null) {
                displayName = this.getDisplayName("getName"); //$NON-NLS-1$
                if (displayName == null) {
                    displayName = this.getDisplayName("getReference"); //$NON-NLS-1$
                }
            }
            if (displayName != null) {
                str = displayName;
            }
        }
        return str;
    }
    
    protected String	getDisplayName(String methodName) {
        try {
        	if (!data.getClass().getName().startsWith(ROOT_PACKAGE_NAME)) {
        		return String.valueOf(data.toString());
        	}
            Method method = this.data.getClass().getDeclaredMethod(methodName, (Class<?>[])null);
            Object obj = method.invoke(this.data, (Object[])null);
            if (obj != null) {
                return obj.toString();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
        }
        return null;
    }
}
