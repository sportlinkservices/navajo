/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.adapter.sqlmap;

import java.sql.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.adapter.SQLMap;
import com.dexels.navajo.script.api.Access;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version $Id$
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

@SuppressWarnings({"unused"})
public final class SessionIdentification {

  private static final String oracleSid = "SELECT sid, serial# FROM v$session WHERE client_info = ?";
  private static final String killOracleSession = "ALTER SYSTEM KILL SESSION '?, ?'";
  
private static final Logger logger = LoggerFactory
		.getLogger(SessionIdentification.class);

  public static final void killSession(String dbIdentifier, Connection con, Access access) {

  }

  public static final String getSessionIdentification(String dbIdentifier, String datasource, Access access) {

    String sid = null;

    if (dbIdentifier.equals("Oracle") && access != null) {
      SQLMap sql = new SQLMap();
      try {
        sql.load(access);
        sql.setDatasource(datasource);
        sql.setQuery(oracleSid);
        sql.setParameter(access.accessID);
        if (sql.getRowCount() > 0) {
          sid = sql.getColumnValue("sid") + "/" + sql.getColumnValue("serial#");
        }
        sql.store();
      }
      catch (Exception ex) {
    	  logger.error("Error: ", ex);
    	  sql.kill();
      }
    }

    return sid;

  }

  public static final void setSessionId(String dbIdentifier, Connection con,
                                        Access access) {
  }

  public static final void clearSessionId(String dbIdentifier, Connection con) {

  }

}
