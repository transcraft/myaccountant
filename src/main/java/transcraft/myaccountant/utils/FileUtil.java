/**
 * Created on 27-Jun-2020
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
package transcraft.myaccountant.utils;

import java.io.File;
import java.net.URL;

public class FileUtil {

	/**
	 * return the root path, regardless of whether the app is in a jar file
	 * or in a folder
	 * 
	 * @return root location for the application
	 */
	public static final File getAppLocation(String path) {
		URL applicationRootPathURL = FileUtil.class.getProtectionDomain().getCodeSource().getLocation();
		File applicationRootPath = new File(applicationRootPathURL.getPath());
		File myFile;
		if(applicationRootPath.isDirectory()){
		    myFile = new File(applicationRootPath, path);
		}
		else{
			// this is a jar file, so return ../.. (we assume the jar file is in $rootDir/lib)
		    myFile = new File(applicationRootPath.getParentFile().getParentFile(), path);
		}
		return myFile;
	}
}
