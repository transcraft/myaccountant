/**
 * Created on 12-Jun-2005
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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.naming.AuthenticationException;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import transcraft.BookKeeper.service.BOMService;
import transcraft.myaccountant.ui.MainApp;
import transcraft.myaccountant.ui.PasswordDialog;
import transcraft.myaccountant.ui.pref.PreferenceFactory;

/**
 * @author david.tran@transcraft.co.uk
 */
public class BookExplorer extends Composite {
	private static final Logger LOG = LoggerFactory.getLogger(BookExplorer.class);
	
    private BOMService bomService;
    private File dbFile;
    private CTabFolder tabFolder;
    private LedgerTreeViewer leTreeViewer;
    
    /**
     * @param parent
     * @param dbFile
     */
    public BookExplorer(Composite parent, File dbFile) {
        super(parent, SWT.BORDER);
        this.dbFile = dbFile;
        this.createContents();
    }
 
    public String	getBookName() {
        return this.dbFile != null ? this.dbFile.getName() : ""; //$NON-NLS-1$
    }
    
    public String	getBookAbsolutePath() {
        return this.dbFile != null ? this.dbFile.getAbsolutePath() : ""; //$NON-NLS-1$
    }
    
    protected void	createContents() {
        this.setLayout(new FillLayout());
        this.bomService = new BOMService(dbFile);
        if (this.bomService.isPasswordProtected()) {
            try {
                this.authenticate();
            } catch (AuthenticationException e) {
                /*
                 * have to translate the AuthenticationException into a RuntimeException
                 * as we can not declare it with createContents()
                 */
                throw new RuntimeException(e.getMessage());
            }
        }
        
        final SashForm sashForm = new SashForm(this, SWT.HORIZONTAL);
        
        this.leTreeViewer = new LedgerTreeViewer(sashForm, bomService, dbFile);
        if (! this.leTreeViewer.isValid()) {
            return;
        }
        this.tabFolder = new CTabFolder(sashForm, SWT.TOP);
        sashForm.setWeights(new int[] { 20, 80 });
        sashForm.SASH_WIDTH = 5;
        this.leTreeViewer.setTabFolder(this.tabFolder);
    }
    
    public boolean isValid() {
        return this.leTreeViewer.isValid();
    }
    
    /**
     * provide direct access to the BOMService
     * @return
     */
    public boolean isAdminMode() {
        return this.bomService.isAdminMode();
    }
    
    /**
     * @return Returns the bomService.
     */
    public BOMService getBomService() {
        return bomService;
    }
    
    /**
     * provide direct access to the BOMService
     * @return
     */
    public String	getCurrentUserName() {
        return this.bomService.getCurrentUserName();
    }
    
    /**
     * authentication support
     *
     */
    public void	authenticate()
    	throws AuthenticationException
    {
        try {
	        PasswordDialog dialog = new PasswordDialog(this.getShell());
	        if (dialog.open() != Dialog.OK) {
	            throw new AuthenticationException(this.dbFile.getName() + Messages.getString("BookExplorer.2")); //$NON-NLS-1$
	        }
	        byte [] encryptedPassword = this.bomService.encrypt(dialog.getPassword());
	        byte [] encryptedOldPassword = this.bomService.encrypt(dialog.getOldPassword());
	        this.bomService.setUser(dialog.getUserName(), encryptedPassword, encryptedOldPassword);
        } catch (AuthenticationException e) {
            // make sure we close the DB or it will be locked forever
            this.bomService.done();
            throw e;
        }
    }
    
    public void	done() {
        this.bomService.done();
        List<String> memorisedDBs = PreferenceFactory.getAsList(MainApp.PROP_MEMORISED_DB);
        String path = this.dbFile.getAbsolutePath();
        if (memorisedDBs.contains(path)) {
            memorisedDBs.remove(path);
        }
        memorisedDBs.add(0, path);
        if (memorisedDBs.size() > MainApp.MAX_MEMORISED_DB) {
            memorisedDBs.remove(memorisedDBs.size() - 1);
        }
        try {
            PreferenceFactory.storeList(MainApp.PROP_MEMORISED_DB, memorisedDBs);
        } catch (IOException e) {
            LOG.error("BookExplorer.close():", e); //$NON-NLS-1$
        }
        LOG.debug("{} all done", this.getBookName()); //$NON-NLS-1$
    }
    
    public void	initDB() {
        this.bomService.initDB();
    }
}
