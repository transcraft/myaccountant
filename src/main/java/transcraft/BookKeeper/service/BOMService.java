/**
 * 
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

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.naming.AuthenticationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Constraint;
import com.db4o.query.Query;
import com.google.common.collect.Lists;

import transcraft.BookKeeper.bom.Account;
import transcraft.BookKeeper.bom.Allocation;
import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.BookKeeper.bom.Categories;
import transcraft.BookKeeper.bom.Entity;
import transcraft.BookKeeper.bom.Entry;
import transcraft.BookKeeper.bom.Invoice;
import transcraft.BookKeeper.bom.LedgerAccount;
import transcraft.BookKeeper.bom.LedgerEntry;
import transcraft.BookKeeper.bom.RunningEntry;

/**
 * @author david.tran@transcraft.co.uk
 */
public class BOMService {
	private static final Logger LOG = LoggerFactory.getLogger(BOMService.class);
	
    public static final String TD_LEDGER = "TD"; //$NON-NLS-1$
    public static final String VD_LEDGER = "VD"; //$NON-NLS-1$

    /**
     * default admin user
     */
    public static final String ADMIN_USER = "admin"; //$NON-NLS-1$
    
    /**
     * default suffix for our DB files
     */
    public static final String DEF_DB_SUFFIX = "tdb"; //$NON-NLS-1$

    public static final String LT_TD = Messages.getString("BOMService.4"); //$NON-NLS-1$
    public static final String LT_VD = Messages.getString("BOMService.5"); //$NON-NLS-1$

    public static final String [] ledgerTypes = new String [] {
            LT_TD, LT_VD
    };
    
    // Entry types for inclusion in subreports
    public static final int ET_DRCR = 0;
    public static final int ET_DRONLY = 1;
    public static final int ET_CRONLY = 2;

    /**
     * wildcard for matching all categories
     */
    public static final String ALL_CATS = "allcats"; //$NON-NLS-1$

    // LedgerAccount categories
    public static final String LCAT_INCOME = Messages.getString("BOMService.3"); //$NON-NLS-1$
    public static final String LCAT_ASSET = Messages.getString("BOMService.2"); //$NON-NLS-1$
    public static final String LCAT_EXPENSE = Messages.getString("BOMService.9"); //$NON-NLS-1$
    public static final String LCAT_VAT = Messages.getString("BOMService.10"); //$NON-NLS-1$
    
    /**
     *  EntryAccount business category
     */
    public static final String ECAT_BUSINESS = Messages.getString("BOMService.1"); //$NON-NLS-1$
    /**
     *  EntryAccount personal category
     */
    public static final String ECAT_PERSONAL = Messages.getString("BOMService.12"); //$NON-NLS-1$

    ArrayList<BOMListener> listeners = Lists.newArrayList();
    
    ObjectContainer dbConn = null;
    File dbFile;
    
    private boolean publishEvent = true;

    /**
     * only set after authentication
     */
    private UserDetail currentUser = null;
    
    /**
     * use this constructor for auto-managed connections
     * @param fileName
     */
    public BOMService(String fileName) {
        this(new File(fileName));
    }
    
    /**
     * use this constructor for auto-managed connections
     * @param fp
     */
    public BOMService(File fp) {
        this.dbFile = fp;
    }
    
    /**
     * use this constructor for externally managed connections
     * @param conn
     */
    public BOMService(ObjectContainer conn) {
        this.dbConn = conn;
        this.dbFile = null;
    }
    
    public void	done() {
        if (dbFile != null) {
            try {
            	if (this.dbConn != null) {
            		LOG.info("Closing and committing database {}", dbFile);
            		this.getConn().commit();
            		this.getConn().close();
            	}
            } catch (Exception e) {
                LOG.error(Messages.getString("BOMService.13"), e); //$NON-NLS-1$
            }
            this.dbConn = null;
            this.currentUser = null;
        }
    }
    
    public void storeGeneric(Object obj, String key, Object value) {
        this.checkAuthentication(obj);
        try {
	        if (key != null && value != null) {
	            Object storedObj = this.getGeneric(obj.getClass(), key, value);
	            if (storedObj != null) {
	                this.getConn().delete(storedObj);
	            } else {
	                storedObj = this.getConn().queryByExample(obj);
	                if (storedObj != null) {
	                    this.getConn().delete(obj);
	                }
	            }
		        this.getConn().store(obj);
	            this.notifyListener(new BOMEvent(this, obj, storedObj, storedObj != null ? BOMEvent.OP_UPD : BOMEvent.OP_ADD));
	        } else {
	            throw new RuntimeException(Messages.getString("BOMService.14")); //$NON-NLS-1$
	        }
        } finally {
            if (this.getConn() != null) {
                this.getConn().commit();
            }
        }
    }
    
    /**
     * @return Returns whether user is the administrator
     */
    public boolean isAdminMode() {
        return this.currentUser != null && this.currentUser.getName().equals(ADMIN_USER);
    }
    
    /**
     * auto logon to the external service using the same id as us. This is designed to aid migration
     * of data between two services without compromising our credentials
     * @param service
     */
    public void	propagateCredentials(BOMService service) {
        /*
         * we can not use the getRefData() or getGeneric() methods here because
         * they are protected by password
         */
        ArrayList<UserDetail> myUDList = Lists.newArrayList();
        ArrayList<UserDetail> theirUDList = Lists.newArrayList();
        ObjectSet<UserDetail> os = this.getConn().query(UserDetail.class);
        while (os.hasNext()) {
            UserDetail ud = (UserDetail)os.next();
            myUDList.add(ud);
        }
        os = service.getConn().query(UserDetail.class);
        while (os.hasNext()) {
            UserDetail ud = (UserDetail)os.next();
            theirUDList.add(ud);
        }
        for (UserDetail ud : myUDList) {
            if (! theirUDList.contains(ud)) {
                theirUDList.add(ud);
                service.getConn().store(ud);
            }
            if (this.currentUser != null && ud.equals(this.currentUser)) {
                int idx = theirUDList.indexOf(new UserDetail(ud.getName()));
                if (idx >= 0) {
                    // auto logon to remote service
                    service.currentUser = (UserDetail)theirUDList.get(idx);
                }
            }
        }
        SecretKey key = this.getSecretKey();
        service.getConn().store(key);
        service.getConn().commit();
    }
    
    /**
     * set the user accessing this DB
     * @param user
     * @param password
     */
    public void	setUser(String user, String password, String oldPassword)
    	throws AuthenticationException
    {
        byte [] encryptedPassword = this.encrypt(password);
        byte [] encryptedOldPassword = this.encrypt(oldPassword);
        this.setUser(user, encryptedPassword, encryptedOldPassword);
    }
    
    /**
     * set the user accessing this DB. Note that we can not use any of the checkAuthentication() protected
     * methods here as it will be a catch-22 situtation
     * @param user
     * @param encryptedPassword
     * @param encryptedOldPassword
     */
    public void	setUser(String user, byte[] encryptedPassword, byte[] encryptedOldPassword)
    	throws AuthenticationException
    {
        try {
	        ObjectSet<UserDetail> os = this.getConn().query(UserDetail.class);
	        UserDetail userDetail = null;
	
	        if (os.isEmpty()) {
	            if (! user.equals(ADMIN_USER)) {
	                throw new RuntimeException(Messages.getString("BOMService.15")); //$NON-NLS-1$
	            }
	            userDetail = new UserDetail(user);
	        } else {
	            while (os.hasNext()) {
	                UserDetail ud = (UserDetail)os.next();
	                if (ud.getName().equals(user)) {
	                    userDetail = ud;
	                    break;
	                }
	            }
	            if (userDetail == null) {
	                // this user does not exist
	                if (this.currentUser == null) {
	                    throw new RuntimeException(Messages.getString("BOMService.16") + user + Messages.getString("BOMService.17")); //$NON-NLS-1$ //$NON-NLS-2$
	                } else if (! this.currentUser.getName().equals(ADMIN_USER)) {
	                    throw new RuntimeException(Messages.getString("BOMService.18")); //$NON-NLS-1$
	                }
	                userDetail = new UserDetail(user);
	            }
	        }
	
	        String oldUserName = null;
	        String newUserName = null;
	        
	        if (userDetail.hasNoPassword()) {
	            if (encryptedOldPassword == null) {
	                if (encryptedPassword != null) {
		                // first time password
		                userDetail.setPassword(encryptedPassword);
	                }
	                this.getConn().store(userDetail);
	                oldUserName = this.currentUser != null ? this.currentUser.getName() : null;
	                this.currentUser = userDetail;
	                newUserName = this.currentUser.getName();
	            } else {
	                throw new RuntimeException(Messages.getString("BOMService.19") + user + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	            }
	        } else {
	            if (encryptedOldPassword != null) {
	                // change password mode
	                if (userDetail.getPassword() == null || ! Arrays.equals(encryptedOldPassword, userDetail.getPassword())) {
	                    throw new RuntimeException(Messages.getString("BOMService.21") + user + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	                } else {
	                    // change to the new password as the old ones match
	                    userDetail.setPassword(encryptedPassword);
	                    this.getConn().store(userDetail);
	                    oldUserName = this.currentUser != null ? this.currentUser.getName() : null;
	                    this.currentUser = userDetail;
	                    newUserName = this.currentUser.getName();
	                }
	            } else {
	                // password matching mode
	                if (encryptedPassword != null && Arrays.equals(userDetail.getPassword(), encryptedPassword)) {
	                    oldUserName = this.currentUser != null ? this.currentUser.getName() : null;
	                    this.currentUser = userDetail;
	                    newUserName = this.currentUser.getName();
	                } else {
	                    throw new RuntimeException(Messages.getString("BOMService.23") + user + "'"); //$NON-NLS-1$ //$NON-NLS-2$
	                }
	            }
	        }
	        if (newUserName != null) {
	            this.notifyListener(new BOMEvent(this, newUserName, oldUserName, BOMEvent.OP_USER));
	        }
        } catch (Exception e) {
            // must close down the DB file or it will be locked forever
            this.done();
            throw new AuthenticationException(e.getMessage());
        }
    }
    
    /**
     * return the current logged in user
     * @return
     */
    public final String	getCurrentUserName() {
        return this.currentUser != null ? this.currentUser.getName() : null;
    }
    
    /**
     * provide client encryption in case they don't want the password transmitted in clear text
     * @param password
     * @return
     */
    public final	byte []	encrypt(String password) {
        byte [] encrypted = null;
        if (password != null && password.length() > 0) {
	        SecretKey key = this.getSecretKey();
	        if (key != null) {
	            try {
		            Cipher cipher = Cipher.getInstance("DES/ECB/PKCS5Padding"); //$NON-NLS-1$
		            cipher.init(Cipher.ENCRYPT_MODE, key);
		            encrypted =  cipher.doFinal(password.getBytes());
	            } catch (Exception e) {
	                LOG.error("BOMService.encrypt():", e); //$NON-NLS-1$
	            }
	        }
    	}
        return encrypted;
    }
    
    /**
     * check to see if this BOMService is password protected
     * @return
     */
    public boolean isPasswordProtected() {
        return this.dbFile != null && ! (this.getConn().query(UserDetail.class)).isEmpty();
    }
    
    /**
     * raw delete, this does not guarantee to delete the correct object, unless it has been retrieved
     * from us in the first place
     * @param obj
     */
    public void	delete(Object obj) {
        this.checkAuthentication(obj);
        this.getConn().delete(obj);
    }
    
    /**
     * delete a generic object using its primary key
     * @param obj
     * @param key
     * @param value
     * @return
     */
    public Object deleteGeneric(Object obj, String key, Object value) {
        this.checkAuthentication(obj);
        Class<?> theClass = obj instanceof Class ? (Class<?>)obj : obj.getClass();
        Object storedObj = this.getGeneric(theClass, key, value);
        if (storedObj != null) {
            this.getConn().delete(storedObj);
            this.notifyListener(new BOMEvent(this, storedObj, null, BOMEvent.OP_DEL));
        } else if (! (obj instanceof Class)){
            this.getConn().delete(obj);
        }
        return storedObj;
    }
    
    /**
     * get a generic object using its primary key
     * @param theClass
     * @param key
     * @param value
     * @return
     */
    public Object getGeneric(Class<?> theClass, String key, Object value) {
        this.checkAuthentication(theClass);
        if (key != null && value != null) {
	        Query query = this.getConn().query();
	        query.constrain(theClass);
            query.descend(key).constrain(value);
	        ObjectSet<Object> rs = query.execute();
	        while (rs.hasNext()) {
	            Object storedObj = rs.next();
	            if (storedObj.getClass().equals(theClass)) {
	                return storedObj;
	            }
	        }
        }
        return null;
    }
    
    public Integer	getNextId(Class<?> theClass) {
        return new FountainService().getNextId(this.getConn(), theClass.getName());
    }
    
    /**
     * special method to store an Entry, as we need to do a few additional things too
     * @param entry
     */
    public void	store(Entry entry) {
        this.checkAuthentication(entry);
        try {
            if (entry instanceof RunningEntry) {
                throw new RuntimeException("Can not save instances of RunningEntry"); //$NON-NLS-1$
            }
	        if (entry.getFromAccount() == null) {
	            throw new RuntimeException("Account not specified"); //$NON-NLS-1$
	        }
	        if (entry.getToAccount() != null && entry.getToAccount().equals(entry.getFromAccount())) {
	            // no point transfering to ourselves
	            entry.setToAccount(null);
	        }
	        Entry oldEntry = this.getEntry(entry.getEntryId());
	        if (oldEntry != null) {
	            oldEntry.setEntry(entry);
	            entry = oldEntry;
	        }
	        if (entry.getEntryId() == null) {
	            // new entry
	            entry.setEntryId(this.getNextId(Entry.class));
	        } else {
	            long internalID = this.getConn().ext().getID(entry);
	            if (internalID == 0L) {
	                throw new RuntimeException("Internal id for " + entry.getEntryId() + " must be non-zero"); //$NON-NLS-1$ //$NON-NLS-2$
	            }
	        }
	        this.getConn().store(entry);

	        this.updateAccount(entry.getFromAccount(), false);
	        this.updateAccount(entry.getToAccount(), false);
	        Allocation [] allocs = entry.getAllocations(false);
	        for (int i = 0; i < allocs.length; i++) {
	            this.updateAccount(allocs[i].getAccount(), true);
	        }
	        this.updateEntity(entry.getEntity());

            this.notifyListener(new BOMEvent(this, entry, oldEntry, oldEntry != null ? BOMEvent.OP_UPD : BOMEvent.OP_ADD));
        } finally {
            if (this.getConn() != null) {
                this.getConn().commit();
            }
        }
    }
    
    /**
     * delete an Entry object by its id
     * @param entryId
     */
    public void	delete(Integer entryId) {
        this.checkAuthentication(entryId);
        try {
            Entry entry = this.getEntry(entryId);
            while(entry != null) {
                this.getConn().delete(entry);
                this.notifyListener(new BOMEvent(this, entry, null, BOMEvent.OP_DEL));
                entry = this.getEntry(entryId);
            }
            Invoice invoice = this.getInvoiceForEntryId(entryId);
            if (invoice != null) {
                // have to immediately mark the invoice as unpaid
                LOG.debug("***** Marking invoice '" + invoice + "' as unpaid"); //$NON-NLS-1$ //$NON-NLS-2$
                invoice.setEntryId(null);
                this.getConn().store(invoice);
                this.notifyListener(new BOMEvent(this, invoice, invoice, BOMEvent.OP_UPD));
            }
        } finally {
            if (this.getConn() != null) {
                this.getConn().commit();
            }
        }
    }
    
    /**
     * get the associated Invoice for an Entry object
     * @param entryId
     * @return
     */
    public Invoice	getInvoiceForEntryId(Integer entryId) {
        this.checkAuthentication(entryId);
        Query query = this.getConn().query();
        query.constrain(Invoice.class);
        query.descend("entryId").constrain(entryId); //$NON-NLS-1$
        ObjectSet<Invoice> rs = query.execute();
        Invoice invoice = null;
        if (rs.hasNext()) {
            invoice = rs.next();
        }
        return invoice;
    }
    
    /**
     * the workhorse of the whole system, retrieving entries for a particular Account
     * @param ledgerName
     * @param account
     * @return
     */
    public List<Entry>	getLedgerEntries(String ledgerName, Account account) {
        return this.getLedgerEntries(ledgerName, account, null, null);
    }
    
    /**
     * the workhorse of the whole system, retrieving entries for a particular Account     * @param ledgerName
     * @param account
     * @param startDate
     * @param endDate
     * @return
     */
    public List<Entry>	getLedgerEntries(String ledgerName, Account account, Date startDate, Date endDate) {
        return this.getLedgerEntries(ledgerName, account, startDate, endDate, ET_DRCR);
    }
    
    /**
     * the workhorse of the whole system, retrieving entries for a particular Account
     * @param ledgerName
     * @param account
     * @param startDate
     * @param endDate
     * @param entryType
     * @return
     */
    public List<Entry>	getLedgerEntries(String ledgerName, Account account, Date startDate, Date endDate, int entryType) {
        return this._getRunningEntries(ledgerName, account, startDate, endDate, entryType);
    }
    
    private List<Entry>	_getRunningEntries(final String ledgerName, Account account, Date startDate, Date endDate, int entryType) {
        this.checkAuthentication(account);
        List<Entry> entryList = Lists.newArrayList();
        Query query = this.getConn().query();
        query.constrain(Entry.class);
        String dateField = ledgerName.equals(BOMService.TD_LEDGER) ? "postingDate" : "valueDate"; //$NON-NLS-1$ //$NON-NLS-2$
        if (startDate != null) {
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(startDate);
        	cal.set(Calendar.HOUR_OF_DAY, 0);
            query.descend(dateField).constrain(cal.getTime()).greater();
        }
        if (endDate != null) {
        	Calendar cal = Calendar.getInstance();
        	cal.setTime(endDate);
        	cal.add(Calendar.DATE, 1);
        	cal.set(Calendar.HOUR_OF_DAY, 0);
            query.descend(dateField).constrain(cal.getTime()).smaller();
        }
        
        ObjectSet<Entry> rs = query.execute();
        while (rs.hasNext()) {
            Entry entry = rs.next();
            if (! entry.getClass().equals(Entry.class)) {
                // we get back the subclasses too, like ScheduledEntry, so have to turn off leniency here
                continue;
            }
            if (! entry.isLinkedToAccount(account.getReference())) {
                continue;
            }
            RunningEntry re = new RunningEntry(entry, account);
            double amount = re.getAmount();
            switch (entryType) {
            case ET_DRONLY:
                if (amount > 0) {
                    continue;
                }
                break;
            case ET_CRONLY:
                if (amount < 0) {
                    continue;
                }
                break;
            }
            entryList.add(re);
        }
        entryList = entryList.stream().sorted((o1, o2) -> {
            RunningEntry re1 = (RunningEntry)o1;
            RunningEntry re2 = (RunningEntry)o2;
            int v = (ledgerName.equals(BOMService.TD_LEDGER)) ?
                re1.getPostingDate().compareTo(re2.getPostingDate()) :
                re1.getValueDate().compareTo(re2.getValueDate());
            return v == 0 ? (re1.getEntryId() - re2.getEntryId()) : v;
        }).collect(Collectors.toList());
        
        this.calculateRunningBalances(ledgerName, entryList, account);
        return entryList;
    }
    
    /**
     * this method makes the assumption that the entrySet parameter is a Set of
     * RunningEntry objects and is sorted depending on the ledgerName already
     * @param ledgerName
     * @param entrySet
     * @param account
     * @return
     */
    protected void	calculateRunningBalances(String ledgerName, List<Entry> entryList, Account account) {
        double openingBalance = 0.0;
        boolean obFound = false;
        for (Entry entry : entryList) {
            RunningEntry re = (RunningEntry)entry;
            Date acctDate = ledgerName.equals(BOMService.TD_LEDGER) ? re.getPostingDate() : re.getValueDate();
            double amount = re.getAmount();
            if (! (account instanceof LedgerAccount) && ! obFound) {
                if (! acctDate.before(account.getOpeningDate())) {
                    openingBalance = account.getOpeningBalance();
                    // back calculate the balances before the Opening Balance
                    for (int i = entryList.size() - 1; i >= 0; i--) {
                        RunningEntry rre = (RunningEntry)entryList.get(i);
                        rre.setBalance(openingBalance);
                        openingBalance -= rre.getAmount();
                    }
                    // now start going forward from the Opening Balance
                    openingBalance = account.getOpeningBalance() + amount;
                    obFound = true;
                }
            } else {
                openingBalance += amount;
            }
            re.setBalance(openingBalance);
        }
    }
    
    public <T> List<T> getRefData(Class<T> theClass) {
        return getRefData(theClass, false);
    }
    
    public <T> List<T> getRefData(Class<T> theClass, boolean isLenient) {
        this.checkAuthentication(theClass);
        Query query = this.getConn().query();
        query.constrain(theClass);
        ObjectSet<T> rs = query.execute();
        List<T> list = Lists.newArrayList();
        while(rs.hasNext()) {
            T obj = rs.next();
            if (isLenient || obj.getClass().equals(theClass)) {
                list.add(obj);
            }
        }
        return list;
    }
    
    public List<String>	getMemorisedList(String name) {
        this.checkAuthentication(name);
        List<String> list = Lists.newArrayList();
        Query query = this.getConn().query();
        try {
            if (name.toLowerCase().indexOf("account") >= 0) { //$NON-NLS-1$
                List<Account> accounts = this.getRefData(Account.class);
                for (Account account : accounts ) {
                    String ref = account.getReference();
                    if (ref != null && ref.length() > 0) {
                        list.add(ref);
                    }
                }
            } else if (name.toLowerCase().indexOf(Messages.getString("BOMService.0")) >= 0) { //$NON-NLS-1$
                List<AllocationRule> allocs = this.getRefData(AllocationRule.class);
                for (AllocationRule alloc : allocs) {
                    String allocName = alloc.getName();
                    if (allocName != null && allocName.length() > 0) {
                        list.add(allocName);
                    }
                }
            } else if (name.toLowerCase().indexOf(Messages.getString("BOMService.38")) >= 0) { //$NON-NLS-1$
                List<Invoice> invoices = this.getRefData(Invoice.class);
                for (Invoice invoice : invoices) {
                    String str = name.toLowerCase().indexOf(Messages.getString("BOMService.39")) >= 0 ? invoice.getClientAddress() : //$NON-NLS-1$
                        invoice.getClientName();
                    if (str != null && str.length() > 0 && ! list.contains(str)) {
                        list.add(str);
                    }
                }
            } else {
            	String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            	LOG.debug("methodName={}", methodName);
	            Method method = Entry.class.getDeclaredMethod(methodName, (Class<?>[])null); //$NON-NLS-1$
	            query.constrain(Entry.class);
	            ObjectSet<Entry> rs = query.execute();
	            while (rs.hasNext()) {
	                Entry entry = rs.next();
	                Object value = method.invoke(entry, (Object[])null);
	                if (value != null && ! list.contains(value)) {
	                    list.add(value.toString());
	                }
	            }
            }
            Collections.sort(list);
        } catch (Exception e) {
            LOG.error("getMemorisedList(" + name + "):", e); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return list;
    }
    
    public Entry	getMemorised(String name, String value) {
        return this.getMemorised(name, value, null);
    }
    
    /**
     * look for an {@link Entry} with the specified property matching the value
     * 
     * @param property
     * @param value
     * @param accountReference
     * @return the entry with matching value
     */
    public Entry	getMemorised(String property, String value, String accountReference) {
    	LOG.debug("getMemorised({},{},{})", property, value, accountReference);
        this.checkAuthentication(property);
        Query query = this.getConn().query();
        query.constrain(Entry.class);
        Constraint constraint = query
			.descend(property)
			.constrain(value)
			.like();
        if (accountReference != null && accountReference.length() > 0) {
            constraint = constraint
            	.and(
            		query
            			.descend("fromAccount")
            			.constrain(accountReference)
            			.equal()
            	);
        }
        
        ObjectSet<Entry> rs = query.execute();
        Entry entry = rs.hasNext() ? (Entry)rs.next() : null;
        return entry;
    }
    
    public void	addListener(BOMListener listener) {
        if (! this.listeners.contains(listener)) {
            this.listeners.add(listener);
        }
    }
    
    public void	removeListener(BOMListener listener) {
        this.listeners.remove(listener);
    }
    
    protected void	notifyListener(BOMEvent event) {
        if (! this.publishEvent) {
            return;
        }
        for (BOMListener listener : listeners) {
            try {
                listener.dataChanged(event);
            } catch (Exception e) {
                LOG.error("notifyListener(" + event + ")", e);
            }
        }
    }
    
    public int	getRefDataCount(Class<?> theClass) {
        return this.getRefData(theClass).size();
    }
    
    public Account	updateAccount(String accountName, boolean isLedger) {
        return this.updateAccount(accountName, isLedger, null);
    }
    
    public Account	updateAccount(String accountName, boolean isLedger, String category) {
        this.checkAuthentication(accountName);
        if (isBlank(accountName)) {
            return null;
        }
        Account account = null;
        if (isLedger) {
            LedgerAccount la = new LedgerAccount(accountName);
            la.setCategory(category);
            account = la;
        } else {
            account = new Account(accountName);
        }
        Query query = this.getConn().query();
        query.constrain(account.getClass());
        query.descend("reference").constrain(accountName); //$NON-NLS-1$
        ObjectSet<Account> rs = query.execute();
        if (! rs.hasNext()) {
            this.getConn().store(account);
            this.notifyListener(new BOMEvent(this, account, null, BOMEvent.OP_UPD));
            LOG.info("Creating new account " + accountName); //$NON-NLS-1$
        } else {
            Account storedAccount = rs.next();
            if (storedAccount.getClass().equals(account.getClass())) {
                return storedAccount;
            } else {
                this.getConn().store(account);
                this.notifyListener(new BOMEvent(this, account, null, BOMEvent.OP_UPD));
                LOG.info("Creating new account " + accountName); //$NON-NLS-1$
            }
        }
        return account;
    }
    
    public boolean isEmptyAccount(Account account) {
        this.checkAuthentication(account);
        Query query = this.getConn().query();
        query.constrain(LedgerEntry.class);
        query.descend("accountReference").constrain(account.getReference()); //$NON-NLS-1$
        ObjectSet<Account> rs = query.execute();
        return ! rs.hasNext();        
    }
    
    public boolean deleteAccount(Account account) {
        this.checkAuthentication(account);
        if (account.getClass().equals(Account.class) && ! this.isEmptyAccount(account)) {
            throw new RuntimeException(Messages.getString("BOMService.223") + account.getReference() + Messages.getString("BOMService.222")); //$NON-NLS-1$ //$NON-NLS-2$
        }
        boolean removed = false;
        Query query = this.getConn().query();
        query.constrain(account.getClass());
        query.descend("reference").constrain(account.getReference()); //$NON-NLS-1$
        ObjectSet<Account> rs = query.execute();
        if (rs.hasNext()) {
            Object acct = rs.next();
            this.getConn().delete(acct);
            this.notifyListener(new BOMEvent(this, acct, null, BOMEvent.OP_DEL));
            removed = true;
        }
        return removed;
    }
    
    public Entry	getEntry(Integer entryId) {
        this.checkAuthentication(entryId);
        if (entryId != null) {
	        Query query = this.getConn().query();
	        query.constrain(Entry.class);
	        query.descend("entryId").constrain(entryId); //$NON-NLS-1$
	        ObjectSet<Entry> rs = query.execute();
	        while (rs.hasNext()) {
		        Entry entry = rs.next();
		        if (entry.getClass().equals(Entry.class)) {
		            return entry;
		        }
	        }
        }
        return null;
    }
    
    public Entity	updateEntity(String entityName) {
        this.checkAuthentication(entityName);
        if (entityName == null) {
            return null;
        }
        Entity entity = new Entity(entityName);
        Query query = this.getConn().query();
        query.constrain(Entity.class);
        query.descend("name").constrain(entityName); //$NON-NLS-1$
        ObjectSet<Object> rs = query.execute();
        if (! rs.hasNext()) {
            this.getConn().store(entity);
            this.notifyListener(new BOMEvent(this, entity, null, BOMEvent.OP_ADD));
        } else {
            entity = (Entity)rs.next();
        }
        return entity;
    }
    
    public void	initDB() {
        this.checkAuthentication(null);
        AllocationRule rule = (AllocationRule)this.getGeneric(AllocationRule.class, "name", "AssetRule"); //$NON-NLS-1$ //$NON-NLS-2$
        if (rule == null) {
            rule = new AllocationRule("AssetRule"); //$NON-NLS-1$
            rule.addAllocation(BOMService.LCAT_ASSET, .85);
            rule.addAllocation(BOMService.LCAT_VAT, -.15);
            this.getConn().store(rule);
        }
        rule = (AllocationRule)this.getGeneric(AllocationRule.class, "name", "IncomeRule"); //$NON-NLS-1$ //$NON-NLS-2$
        if (rule == null) {
	        rule = new AllocationRule("IncomeRule"); //$NON-NLS-1$
	        rule.addAllocation(BOMService.LCAT_INCOME, .85);
	        rule.addAllocation(BOMService.LCAT_VAT, .15);
	        this.getConn().store(rule);
        }
        rule = (AllocationRule)this.getGeneric(AllocationRule.class, "name", "ExpenseRule"); //$NON-NLS-1$ //$NON-NLS-2$
        if (rule == null) {
	        rule = new AllocationRule("ExpenseRule"); //$NON-NLS-1$
	        rule.addAllocation(BOMService.LCAT_EXPENSE, .85);
	        rule.addAllocation(BOMService.LCAT_VAT, -.15);
	        this.getConn().store(rule);
        }

        this.updateAccount(Messages.getString("BOMService.224"), false); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.225"), false); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.64"), false); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.65"), false); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.66"), false); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.67"), false); //$NON-NLS-1$
        
        this.updateAccount(Messages.getString("BOMService.68"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.69"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.70"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.71"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.72"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.73"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.74"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.75"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.76"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.77"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.78"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.79"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.80"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.81"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.82"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.83"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.84"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.85"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.86"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.87"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.88"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.89"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.90"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.91"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.92"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.93"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.94"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.95"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.96"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.97"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.98"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.99"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.100"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.101"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.102"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.103"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.104"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.105"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.106"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.107"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.108"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.109"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.110"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.111"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.112"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.113"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.114"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.115"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.116"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.117"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.118"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.119"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.120"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.121"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.122"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.123"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.124"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.125"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.126"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.127"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.128"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.129"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.130"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.131"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.132"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.133"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.134"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.135"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.136"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.137"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.138"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.139"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.140"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.141"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.142"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.143"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.144"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.145"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.146"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.147"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.148"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.149"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.150"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.151"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.152"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.153"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.154"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.155"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.156"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.157"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.158"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.159"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.160"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.161"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.162"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.163"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.164"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.165"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.166"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.167"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.168"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.169"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.170"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.171"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.172"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.173"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.174"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.175"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.176"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.177"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.178"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.179"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.180"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.181"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.182"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.183"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.184"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.185"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.186"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.187"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.188"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.189"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.190"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.191"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.192"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.193"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.194"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.195"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.196"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.197"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.198"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.199"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.200"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.201"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.202"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.203"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.204"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.205"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.206"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.207"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.208"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.209"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.210"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.211"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.212"), true); //$NON-NLS-1$
        this.updateAccount(Messages.getString("BOMService.213"), true); //$NON-NLS-1$

        this.initCategories(Account.class.getName());
        this.initCategories(LedgerAccount.class.getName());
        this.getConn().ext().purge();
        this.getConn().commit();
    }
    
    public Categories	getCategories(Object obj) {
        return this.getCategories((obj instanceof Class ? ((Class<?>)obj) : obj.getClass()).getName());
    }
    
    public Categories	getCategories(String className) {
        this.checkAuthentication(className);
        Categories categories = (Categories)this.getGeneric(Categories.class, "forClass", className); //$NON-NLS-1$
        if (categories == null) {
            this.initCategories(className);
            categories = (Categories)this.getGeneric(Categories.class, "forClass", className); //$NON-NLS-1$
        }
        if (categories == null) {
            throw new RuntimeException(Messages.getString("BOMService.226") + className + "'"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return categories;
    }
    
    protected void	initCategories(String className) {
        Categories cat = new Categories(className);
        if (className.equals(Account.class.getName())) {
	        cat.addCategory(ECAT_BUSINESS);
	        cat.addCategory(ECAT_PERSONAL);
        } else if (className.equals(LedgerAccount.class.getName())) {
            cat.addCategory(LCAT_INCOME);
            cat.addCategory(LCAT_ASSET);
            cat.addCategory(LCAT_EXPENSE);
            cat.addCategory(LCAT_VAT);
        }
        this.getConn().store(cat);
    }
    
    ObjectContainer	getConn() {
        if (this.dbConn == null && this.dbFile != null) {
            boolean needInit = ! this.dbFile.exists();
            this.dbConn = DAOService.openConnection(this.dbFile);
            if (needInit) {
                initDB();
            }
        }
        return this.dbConn;
    }
    
    private void	checkAuthentication(Object obj) {
        if (this.isPasswordProtected() && this.currentUser == null) {
            this.getConn().close();
            throw new RuntimeException(Messages.getString("BOMService.218")); //$NON-NLS-1$
        }
        if (obj != null && (obj.equals(UserDetail.class) || obj instanceof UserDetail)) {
            throw new RuntimeException(obj.getClass() + Messages.getString("BOMService.219")); //$NON-NLS-1$
        }
    }
    
    private final SecretKey	getSecretKey() {
        ObjectSet<SecretKey> os = this.getConn().query(SecretKey.class);
        SecretKey key = null;
        if (os.isEmpty()) {
            try {
                KeyGenerator keyGen = KeyGenerator.getInstance("DES"); //$NON-NLS-1$
                key = keyGen.generateKey();
                this.getConn().store(key);
                this.getConn().commit();
            } catch (Exception e) {
                LOG.error("BOMService.getSecretKey():", e); //$NON-NLS-1$
            }
        } else {
            key = (SecretKey)os.get(0);            
        }
        return key;
    }

    /**
     * @return Returns the publishEvent.
     */
    public boolean isPublishEvent() {
        return publishEvent;
    }
    /**
     * @param publishEvent The publishEvent to set.
     */
    public void setPublishEvent(boolean publishEvent) {
        if (! this.publishEvent && publishEvent) {
            this.publishEvent = true;
            // wake up the world as we have just come out of a Batch mode
            this.notifyListener(new BOMEvent(this, this, null, BOMEvent.OP_BROADCAST));
            return;
        }
        this.publishEvent = publishEvent;
    }
}
