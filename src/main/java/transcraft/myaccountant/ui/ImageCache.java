/**
 * Created on 14-Jun-2005
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
package transcraft.myaccountant.ui;

import java.util.Map;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import com.google.common.collect.Maps;

/**
 * similar to ImageRegistry, refactored from ImageLoader
 * 
 * @author david.tran@transcraft.co.uk
 */
public class ImageCache {
    private static final Map<String, Image> cache = Maps.newHashMap();
    private static Display theDisplay;
    
    public static void	init(Display dpy) {
        theDisplay = dpy;
    }
    public static final Image	get(String id) {
        if (theDisplay != null && ! cache.containsKey(id)) {
            String path = "/images/" + id; //$NON-NLS-1$
            Image image = null;
            try {
                image = new Image(theDisplay, ImageCache.class.getResourceAsStream(path + ".gif")); //$NON-NLS-1$
            } catch (Exception e) {
                try {
                    image = new Image(theDisplay, ImageCache.class.getResourceAsStream(path + ".jpg")); //$NON-NLS-1$
                } catch (Exception e1) {
                    throw new RuntimeException("Can not locate resource '" + id + "'"); //$NON-NLS-1$ //$NON-NLS-2$
                }
            }
            cache.put(id, image);
        }
        return (Image)cache.get(id);
    }
}
