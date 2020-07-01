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
package transcraft.BookKeeper.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.config.ObjectClass;
import com.db4o.ext.StoredClass;
import com.db4o.internal.config.EmbeddedConfigurationImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.myaccountant.report.BatchReport;
import transcraft.myaccountant.report.GenericReport;
import transcraft.myaccountant.ui.schema.SchemaInfo;

/**
 * @author david.tran@transcraft.co.uk
 */
public class DAOService {
    private static final Logger LOG = LoggerFactory.getLogger(DAOService.class);
	
    private static final String DB_FILE = "book.db"; //$NON-NLS-1$
    
    static private ObjectContainer dbServer;

    static private final Class<?> [] deepClasses = new Class [] {
            Entry.class,
            AllocationRule.class,
            GenericReport.class,
            BatchReport.class,
            Invoice.class
    };
    
    static EmbeddedConfiguration getConfig() {
    	EmbeddedConfiguration theConfig = Db4oEmbedded.newConfiguration();

    	EmbeddedConfigurationImpl config = (EmbeddedConfigurationImpl)theConfig;
    	config.legacy().allowVersionUpdates(true);
    	config.legacy().messageLevel(2);
    	for (int i = 0; i < deepClasses.length; i++) {
    		ObjectClass objectClass = config.legacy().objectClass(deepClasses[i]);
    		objectClass.cascadeOnUpdate(true);
    		objectClass.cascadeOnActivate(true);
    		objectClass.cascadeOnDelete(true);
    	}
    	return theConfig;
    }
    
    /**
     * @return Returns the dbServer.
     */
    public static ObjectContainer getDbServer() {
        if (dbServer == null || dbServer.ext().isClosed()) {
            dbServer = openConnection(DB_FILE);
        }
        return dbServer;
    }
    
    public static File	getUserDbPath(String fileName) {
        String userHomeDir = System.getProperty("user.dir"); //$NON-NLS-1$
        if (userHomeDir == null) {
            userHomeDir = "."; //$NON-NLS-1$
        }
        userHomeDir += "/"; //$NON-NLS-1$
        File fp = new File(userHomeDir + DB_FILE);
        return fp;
    }
    
    public static ObjectContainer	openConnection(String fileName) {
        File fp = getUserDbPath(fileName);
        return openConnection(fp);
    }
    
    /**
     * @return Returns the ObjectContainer.
     */
    public static ObjectContainer openConnection(File fp) {
    	LOG.info("Open " + fp.getAbsolutePath());
        ObjectContainer conn = Db4oEmbedded.openFile(getConfig(), fp.getAbsolutePath());
        return conn;
    }
    
    /**
     * DB4O specific method to return object graph of an object. Pass in
     * null to get back all objects
     * @param obj
     * @return
     */
    public static Map<Class<?>, List<SchemaInfo>>	getObjectTree(ObjectContainer container, Object obj) {
        HashMap<Class<?>, List<SchemaInfo>> map = Maps.newHashMap();
        if (container != null) {
	        ObjectSet<?> os = container.ext().query(obj != null ? obj.getClass() : null);
	        while (os.hasNext()) {
	            Object data = os.next();
	            Class<?> theClass = data.getClass();
	            ArrayList<SchemaInfo> list = (ArrayList<SchemaInfo>)map.get(theClass);
	            if (list == null) {
	                list = Lists.newArrayList();
	                map.put(theClass, list);
	            }
	            SchemaInfo info = new SchemaInfo(Long.valueOf(container.ext().getID(data)), data);
	            list.add(info);
	        }
        }
        return map;
    }
    
    public static Class<?> [] getHierachy(ObjectContainer container, Object obj) {
        ArrayList<Class<?>> list = Lists.newArrayList();
        StoredClass storedClass = container.ext().storedClass(obj instanceof SchemaInfo ? ((SchemaInfo)obj).getData() : obj);
        while (storedClass != null) {
            try {
                list.add(Class.forName(storedClass.getName()));
            } catch (ClassNotFoundException e) {
                LOG.error("getHierachy(): " + storedClass.getName() + " does not exist", e); //$NON-NLS-1$ //$NON-NLS-2$
            }
            storedClass = storedClass.getParentStoredClass();
        }
        // Java 8 compatibility mode, so Class<?>[]::new is not available
        return list.toArray(new Class<?>[list.size()]);
    }
}
