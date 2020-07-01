/* Copyright (C) 2004 - 2005  db4objects Inc.  http://www.db4o.com

This file is part of the db4o open source object database.

db4o is free software; you can redistribute it and/or modify it under
the terms of version 2 of the GNU General Public License as published
by the Free Software Foundation and as clarified by db4objects' GPL 
interpretation policy, available at
http://www.db4o.com/about/company/legalpolicies/gplinterpretation/
Alternatively you can write to db4objects, Inc., 1900 S Norfolk Street,
Suite 350, San Mateo, CA 94403, USA.

db4o is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
for more details.

You should have received a copy of the GNU General Public License along
with this program; if not, write to the Free Software Foundation, Inc.,
59 Temple Place - Suite 330, Boston, MA  02111-1307, USA. */

/**
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

import static transcraft.myaccountant.ui.GUIUtil.updateProgressMonitor;

import java.io.File;
import java.io.RandomAccessFile;
import java.lang.reflect.Modifier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.ext.StoredClass;
import com.db4o.internal.config.EmbeddedConfigurationImpl;
import com.db4o.types.TransientClass;

import transcraft.BookKeeper.bom.AllocationRule;
import transcraft.myaccountant.ui.GUIUtil;


/**
 * defragments a database file.
 * <br><br><b>This class is not part of db4o.jar!</b><br>
 * It is delivered as sourcecode in the
 * path ../com/db4o/tools/<br><br> <b>Prerequites:</b><br> - The database file may not be
 * in use.<br> - All stored classes need to be available.<br> - If you use yor own special
 * Db4o translators, they need to be installed before starting Defragment.<br><br>
 * <b>Performed tasks:</b><br> - Free filespace is removed.<br> - Deleted IDs are
 * removed.<br> - Unavailable classes are removed.<br> - Unavailable class members are
 * removed.<br> - Class indices are restored.<br> - Previous rename tasks are removed.<br>
 * <br>
 * <b>Backup:</b><br>
 * Defragment creates a backup file with the name [filename].bak. If
 * a file with this name is already present, Defragment will not run
 * for safety reasons.<br><br>
 * <b>Recommendations:</b><br>
 * - Keep the backup copy of your database file.<br>
 * - <b>Always</b> back up your class files with your database files also.<br>
 * You will need them to restore the full data of all objects from old database file versions.<br>
 * - Scan the output log for "Class not available" messages.<br><br>
 * You may also run this task programmatically on a scheduled basis.
 * In this case note that <code>Defragment</code> modifies db4o
 * configuration parameters. You may have to restore them for your
 * application. See the private methods Defragment#configureDb4o() and
 * Db4o#restoreConfiguration() in the sourcecode of
 * com.db4o.tools.Defragment.java for the exact changed parameters that
 * may need to be restored.
 */
public class Defragment {
	private static final Logger LOG = LoggerFactory.getLogger(Defragment.class);
	
	/**
	 * the main method is the only entry point
	 */
	public Defragment() {
	}

	/**
	 * programmatic interface to run Defragment with a forced delete of a possible
	 * old Defragment backup.
	 * <br>This method is supplied for regression tests only. It is not recommended
	 * to be used by application programmers.
	 * @param filename the database file. 
	 * @param forceBackupDelete forces deleting an old backup. <b>Not recommended.</b>
	 */
	public void run(String filename, boolean forceBackupDelete) {
	    this.run(filename, forceBackupDelete, null);
	}
	
	public void run(String filename, boolean forceBackupDelete, IProgressMonitor monitor) {
		File file = new File(filename);
		if (file.exists()) {
			boolean canRun = true;
			ExtFile backupTest = new ExtFile(file.getAbsolutePath() + ".bak"); //$NON-NLS-1$
			if (backupTest.exists()) {
				if (forceBackupDelete) {
					backupTest.delete();
				} else {
					canRun = false;
					String str = Messages.getString("Defragment.1") + backupTest.getAbsolutePath() + Messages.getString("Defragment.2") + //$NON-NLS-1$ //$NON-NLS-2$
						Messages.getString("Defragment.3") + //$NON-NLS-1$
						Messages.getString("Defragment.4"); //$NON-NLS-1$
					GUIUtil.showError(Messages.getString("Defragment.5"), str, null); //$NON-NLS-1$
				}
			}
			if (canRun) {
			    try {
					file.renameTo(backupTest);
					this.migrate(backupTest.getAbsolutePath(), filename, monitor);
			    } catch (Exception e) {
					LOG.error(String.format("run(%s)", filename), e); //$NON-NLS-1$
					GUIUtil.showError(Messages.getString("Defragment.6"), Messages.getString("Defragment.7"), e); //$NON-NLS-1$ //$NON-NLS-2$
					try {
						new File(filename).delete();
						backupTest.copy(filename);
					} catch (Exception ex) {
						GUIUtil.showError(Messages.getString("Defragment.8"), Messages.getString("Defragment.9") + backupTest.getAbsolutePath() + "'", ex); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						return;
					}
					GUIUtil.showError(Messages.getString("Defragment.11"), Messages.getString("Defragment.12"), null); //$NON-NLS-1$ //$NON-NLS-2$
					try {
						new File(backupTest.getAbsolutePath()).delete();
					} catch (Exception ex) {
					}
			    }
			}
		} else {
			LOG.info("{} does not exist", file.getAbsolutePath());
		}
	}

	public void	migrate(String fromPath, String toPath, IProgressMonitor monitor)
	{
		File fromFile = new File(fromPath);
		migrate(fromFile, null, toPath, monitor);
	}
	
	/**
	 * for copying data from one file to another
	 * @param fromPath
	 * @param toPath
	 * @param monitor
	 * @throws ClassNotFoundException
	 */
	public void	migrate(File fromFile, ObjectContainer readFrom, String toPath, IProgressMonitor monitor)
	{
		ObjectContainer writeTo = null;
		boolean closeFrom = false;
		EmbeddedConfiguration srcConfig = Db4oEmbedded.newConfiguration();
		try {
			if (readFrom == null) {
				readFrom = Db4oEmbedded.openFile(srcConfig, fromFile.getAbsolutePath());
				closeFrom = true;
			}
		    File toFile = new File(toPath);
		    if (toFile.exists()) {
		        throw new RuntimeException(Messages.getString("Defragment.15") + toPath + Messages.getString("Defragment.16")); //$NON-NLS-1$ //$NON-NLS-2$
		    }
		    if (fromFile.getCanonicalPath().equals(toFile.getCanonicalPath())) {
		        throw new RuntimeException(Messages.getString("Defragment.17") + fromFile + Messages.getString("Defragment.18") + toPath + Messages.getString("Defragment.19")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		    }
		    
			configureDb4o(srcConfig);
			EmbeddedConfiguration dstConfig = Db4oEmbedded.newConfiguration();
			configureDb4o(dstConfig);
			writeTo = Db4oEmbedded.openFile(dstConfig, toPath);
			//writeTo.ext().migrateFrom(readFrom);
			this.migrate(readFrom, writeTo, monitor);
			if (monitor == null) {
			    GUIUtil.showError(Messages.getString("Defragment.20"), Messages.getString("Defragment.21"), null); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} catch (Throwable e) {
		    GUIUtil.showError(Messages.getString("Defragment.22"), Messages.getString("Defragment.23"), e); //$NON-NLS-1$ //$NON-NLS-2$
		} finally{
			restoreConfiguration(srcConfig);
			if (readFrom != null && closeFrom) {
			    try {
			        readFrom.close();
			    } catch (Exception e) {}
			}
			if (writeTo != null) {
			    try {
			        writeTo.close();
			    } catch (Exception e) {}
			}
            if (monitor != null) {
                monitor.done();
            }
		}
	}
	private void configureDb4o(EmbeddedConfiguration config) {
		EmbeddedConfigurationImpl configImpl = (EmbeddedConfigurationImpl)config;
		configImpl.legacy().activationDepth(0);
		configImpl.legacy().callbacks(false);
		configImpl.legacy().classActivationDepthConfigurable(false);
		configImpl.legacy().weakReferences(false);
	}
	
	private void restoreConfiguration(EmbeddedConfiguration config){
		EmbeddedConfigurationImpl configImpl = (EmbeddedConfigurationImpl)config;
		configImpl.legacy().activationDepth(5);
		configImpl.legacy().callbacks(true);
		configImpl.legacy().classActivationDepthConfigurable(true);
		configImpl.legacy().weakReferences(true);
	}

	private void migrate(ObjectContainer origin, ObjectContainer destination, IProgressMonitor monitor)
		throws ClassNotFoundException {

		// get all stored classes
		StoredClass[] classes = origin.ext().storedClasses();
		removeUnavailableSecondAndAbstractClasses(classes, monitor);
		removeSubclasses(classes, monitor);		
		migrateClasses(origin, destination, classes, monitor);
	}

	private void migrateClasses(ObjectContainer origin, ObjectContainer destination, StoredClass[] classes, IProgressMonitor monitor) {
		if (monitor != null) {
		    monitor.beginTask(Messages.getString("Defragment.24"), classes.length); //$NON-NLS-1$
		}
		for (int i = 0; i < classes.length; i++) {
			if (classes[i] != null) {
				if (classes[i].getName().startsWith("java.util")) { //$NON-NLS-1$
					LOG.info("Ignoring " + classes[i].getName()); //$NON-NLS-1$
					continue;
				}
				LOG.info("Migrating {} ...", classes[i].getName()); //$NON-NLS-1$
				long[] ids = classes[i].getIDs();
				for (int j = 0; j < ids.length; j++) {
					Object obj = origin.ext().getByID(ids[j]);

					// prevent possible constructor side effects
					origin.activate(obj, 1);
					origin.deactivate(obj, 2);

					origin.activate(obj, 3);
					
					if (obj instanceof AllocationRule) {
						((AllocationRule)obj).optimiseForDb();
					}
					destination.store(obj);

					// Both Containers keep track of state individually,
					// so we need to make sure, both know, the object is deactivated
					origin.deactivate(obj, 1);
					destination.deactivate(obj, 1);
				}
				destination.ext().purge();
				destination.commit();
			}
			updateProgressMonitor(monitor, i);
		}
	}

	private void removeSubclasses(StoredClass[] classes, IProgressMonitor monitor) throws ClassNotFoundException {
		// rule out inheritance dependancies
		if (monitor != null) {
		    monitor.beginTask(Messages.getString("Defragment.25"), classes.length); //$NON-NLS-1$
		}
		for (int i = 0; i < classes.length; i++) {
			if (classes[i] != null) {
				Class<?> javaClass = Class.forName(classes[i].getName());
				for (int j = 0; j < classes.length; j++) {
					if (classes[j] != null && classes[i] != classes[j]) {
						Class<?> superClass = Class.forName(classes[j].getName());
						if (superClass.isAssignableFrom(javaClass)) {
							LOG.info("Sub-classees of {} removed", classes[i].getName()); //$NON-NLS-1$
							classes[i] = null;
							break;
						}
					}
				}
			}
			updateProgressMonitor(monitor, i);
		}
	}

	private void removeUnavailableSecondAndAbstractClasses(StoredClass[] classes, IProgressMonitor monitor) {
		// remove classes that are currently not available,
		// abstract classes and all transient class objects
		if (monitor != null) {
		    monitor.beginTask(Messages.getString("Defragment.26"), classes.length); //$NON-NLS-1$
		}
		for (int i = 0; i < classes.length; i++) {
			try {
				Class<?> javaClass = Class.forName(classes[i].getName());
				if (javaClass == null
					|| TransientClass.class.isAssignableFrom(javaClass)
					|| Modifier.isAbstract(javaClass.getModifiers())) {
					LOG.info("Unavailable/Abstract {} removed", classes[i].getName()); //$NON-NLS-1$
					classes[i] = null;
				}
			} catch (Throwable t) {
				classes[i] = null;
			}
			updateProgressMonitor(monitor, i);
		}
	}

	private class ExtFile extends File {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public ExtFile(String path) {
			super(path);
		}

		public ExtFile copy(String toPath) throws Exception {
			try {
				new ExtFile(toPath).mkdirs();
				new ExtFile(toPath).delete();
				final int bufferSize = 64000;

				RandomAccessFile rafIn = new RandomAccessFile(getAbsolutePath(), "r"); //$NON-NLS-1$
				RandomAccessFile rafOut = new RandomAccessFile(toPath, "rw"); //$NON-NLS-1$
				long len = rafIn.length();
				byte[] bytes = new byte[bufferSize];

				while (len > 0) {
					len -= bufferSize;
					if (len < 0) {
						bytes = new byte[(int) (len + bufferSize)];
					}
					rafIn.read(bytes);
					rafOut.write(bytes);
				}
				rafIn.close();
				rafOut.close();
				return new ExtFile(toPath);
			} catch (Exception e) {
				LOG.error(String.format("copy(%s, args)", toPath), e); //$NON-NLS-1$
				throw e;
			}
		}
	}

}
