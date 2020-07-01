/**
 * Created on 30-Oct-2005
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

/**
 * misc helper methods for Meta layer
 * @author david.tran@transcraft.co.uk
 */
public class MetaHelper {

    private MetaHelper() {
    }

    /**
     * returns an optional MetaProvider implementation to support a generic
     * object. The naming convention in order for this method to succeed is
     * {package name of MetaProvider} + "." + {class name of obj} + "Meta"
     * @param obj
     * @return
     */
    public static final	Class<?> getMetaProviderPrototype(Object obj) {
        try {
            String className = obj.getClass().getName();
            int idx = className.lastIndexOf("."); //$NON-NLS-1$
            if (idx >= 0) {
                // get the base class name without the package
                className = className.substring(idx + 1);
            }
            /*
             * the naming convention for a MetaProvider for class a.b.c.Foo is
             * {package name of MetaProvider}.FooMeta
             */
            String metaProviderClass = MetaHelper.class.getPackage().getName() + "." + className + "Meta"; //$NON-NLS-1$ //$NON-NLS-2$
            return Class.forName(metaProviderClass);
        } catch (Exception e) {
            // ignore this exception. This is just a test to see if an optional
            // MetaProvider exists
        }
        return null;
    }
}
