/**
 * Created on 19-Aug-2005
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
package transcraft.myaccountant.ui.vc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.InputStreamReader;

import javax.activation.FileDataSource;

import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.TransferData;

/**
 * Drag and drop utility class for LedgerEntryViewer
 * 
 * @author david.tran@transcraft.co.uk
 */
public class LedgerEntryDropAdapter extends ViewerDropAdapter {
    private LedgerEntryViewer viewer;
    
    /**
     * @param viewer
     */
    public LedgerEntryDropAdapter(LedgerEntryViewer viewer) {
        super(viewer.getTableViewer());
        this.viewer = viewer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#performDrop(java.lang.Object)
     */
    public boolean performDrop(Object data) {
        if (data == null) {
            return false;
        }
        if (data instanceof String) {
            this.viewer.doPaste(data);
            return true;
        }
        if (data instanceof String[]) {
            String [] list = (String[])data;
            if (list.length > 0) {
                File fp = new File(list[0]);
                try {
                    FileDataSource fds = new FileDataSource(fp);
                    String mimeType = fds.getContentType();
                    if (mimeType.toLowerCase().startsWith("text")) { //$NON-NLS-1$
                        DataInputStream dis = new DataInputStream(fds.getInputStream());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(dis));
	                    StringBuilder buf = new StringBuilder();
	                    String str = reader.readLine();
	                    buf.append(str + "\n"); //$NON-NLS-1$
	                    dis.close();
	                    this.viewer.doPaste(buf.toString());
                    }
                    return true;
                } catch (Exception e) {}
            }
        }
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerDropAdapter#validateDrop(java.lang.Object, int, org.eclipse.swt.dnd.TransferData)
     */
    public boolean validateDrop(Object target, int operation,
            TransferData transferType) {
        return target != null;
    }

}
