package navajoadapters;

import org.dexels.grus.GrusManager;
import org.osgi.framework.BundleContext;

import com.dexels.navajo.adapter.MailMap;
import com.dexels.navajo.adapter.SQLMap;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;

/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Dexels BV</p>
 * @author
 * @version $Id$.
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DEXELS BV OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

/**
 * VERSION HISTORY
 *
 * 3.1.1 Added support for SOAP attachments
 * 
 * 3.1.0 Now using ALTER Session to set default schema instead of using different connection brokers per username 
 *       (NOTE THAT THIS SOLUTION IS ORACLE SPECIFIC)
 *       
 * 3.0.0 Introduced Grus Manager as a Single Threaded Broker solution.
 * 		 Removed unnecessary synchronized blocks in ConnectionBrokerManager.
 * 
 * 2.2.8 Use Acces object private Console writer instead of System.err for logging. Used in SQLMap and SPMap.
 *       OracleStore now writes additional console value into navajolog table.
 *       
 * 2.1.0 Added generic property support to NavajoMap. You can now use $property('xyz') and field name="property" inside
 * scripts instead of its typed counter parts.
 *
 * 2.1.1 Null release (experimented with open resultsets problem in Oracle when autocommit is false and
 * using SPMap (CallableStatement).
 *
 * 2.2.0 NavajoMap When speciying useCurrentOutdoc the __globals__ and __parms__ messages also get copied.
 *       MailMap, refactored attachment stuff, now always use AttachmentMap
 *
 * 2.2.1 SQLMap with new support for getting 'binary' content of a recordset.
 *
 * 2.2.2 Added FTPMap. NavajoMap now has appendParms to append result to current params block.
 *
 * 2.2.3 Fixed security issue in MailMap for empty text body.
 *
 * 2.2.4 TEMPORARY FIX! In it's current form the CV32 does not work properly with the previous version
 *                      Therefore version 1.19 of MailMap.java was restored (instead of 1.21) and the
 *                      fix of 2.2.3 was applied to version 1.19.
 *                      When CV32 new style is introduced version 1.21 can overwrite version 1.22 of
 *                      the file MailMap.java
 * 
 * 2.2.5 Implemented setContent() in FileMap.
 * 
 * 2.2.6 Supports kill (due to time-out) connection brokers and reviving those.
 * 
 * 2.2.7 Added support for non-broker mode of SQLMap.
 *
 */
public class Version extends com.dexels.navajo.version.Version {

	public static final int    MAJOR       = 3;
	public static final int    MINOR       = 1;
	public static final int    PATCHLEVEL  = 1;
	public static final String VENDOR      = "Dexels";
	public static final String PRODUCTNAME = "Navajo Adapter Library";
	public static final String RELEASEDATE = "2010-09-17";

	//	Included packages.

	public Version() {
		setReleaseDate(RELEASEDATE);
	}

	public int getMajor() {
		return MAJOR;
	}

	public int getMinor() {
		return MINOR;
	}

	public int getPatchLevel() {
		return PATCHLEVEL;
	}

	public String getVendor() {
		return VENDOR;
	}

	public String getProductName() {
		return PRODUCTNAME;
	}
	@Override
	public void start(BundleContext bc) throws Exception {
		super.start(bc);
		MailMap m = new MailMap();
	}
	
	@Override
	public void shutdown() {
		GrusManager.getInstance().shutdown();
		SQLMap.terminateFixedBroker();
	}
	
	public static void main(String [] args) {
		Version v = new Version();
		System.err.println(v.toString());
		dexels.Version [] d = (dexels.Version [] ) v.getIncludePackages();
		for (int i = 0; i < d.length; i++) {
			System.err.println("\t"+d[i].toString());
		}
	}
}
