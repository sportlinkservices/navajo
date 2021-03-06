/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.adapter;

/**
 * Title:        Navajo Product Project
 * Description:  This is the official source for the Navajo server
 * Copyright:    Copyright (c) 2002
 * Company:      Dexels BV
 * @author Arjen Schoneveld
 * @version 1.0
 *
 * $Id$
 *
 *
 */

import java.io.InputStream;
import java.sql.CallableStatement;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.adapter.sqlmap.ResultSetMap;
import com.dexels.navajo.adapter.sqlmap.SQLMapConstants;
import com.dexels.navajo.adapter.sqlmap.SQLMapHelper;
import com.dexels.navajo.document.types.ClockTime;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.UserException;
import com.dexels.navajo.server.DispatcherFactory;
import com.dexels.navajo.util.AuditLog;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class SPMap extends SQLMap {

	
  private static final Logger logger = LoggerFactory.getLogger(SPMap.class);
  public String outputParameter;
  public String outputParameterType;

  protected static final int INPUT_PARAM = 0;
  protected static final int OUTPUT_PARAM = 1;
  protected static final int INOUT_PARAM = 2;
  private static int openCallStatements = 0;
  private boolean isLegacyMode;

  protected ArrayList parameterTypes = new ArrayList();

  protected static HashMap lookupTable = null;

  protected CallableStatement callStatement = null;

  private static Object semaphore = new Object();
  
  public SPMap() {
	  this.isLegacyMode = SQLMapConstants.isLegacyMode();
  }
  
  @Override
public void load(Access access) throws UserException, MappableException {
	  
	  super.load(access);
	  synchronized ( semaphore  ) {
		  if (lookupTable == null) {
			  lookupTable = new HashMap();
			  lookupTable.put("VARCHAR", Integer.valueOf(java.sql.Types.VARCHAR));
			  lookupTable.put("DOUBLE", Integer.valueOf(java.sql.Types.DOUBLE));
			  lookupTable.put("BIT", Integer.valueOf(java.sql.Types.BIT));
			  lookupTable.put("INTEGER", Integer.valueOf(java.sql.Types.INTEGER));
			  lookupTable.put("TINYINT", Integer.valueOf(java.sql.Types.TINYINT));
			  lookupTable.put("DATE", Integer.valueOf(java.sql.Types.TIMESTAMP));
			  lookupTable.put("SMALLINT", Integer.valueOf(java.sql.Types.SMALLINT));
			  lookupTable.put("NUMBER", Integer.valueOf(java.sql.Types.NUMERIC));
		  }
	  }
	  // logger.info("lookupTable = " + lookupTable);
  }

 

@SuppressWarnings("deprecation")
@Override
  protected ResultSetMap[] getResultSet(boolean updateOnly) throws UserException {
    if (debug) {
     logger.info("TIMING SPMAP, start query... : " + update);

    }
    long start = System.currentTimeMillis();

    requestCount++;
    ResultSet rs = null;

    try {

      createConnection();

      if (con == null) {
        throw new UserException( -1,
            "in SQLMap. Could not open database connection [driver = " + driver +
                                ", url = " + url + ", username = '" + username +
                                "', password = '" + password + "']");
      }

      if (resultSet == null) {
        String spName = "";

        // Close previously open call statements:
        if (callStatement != null) {

			try {
				callStatement.close();
			} catch (Exception e) {
				logger.warn("Non fatal error closing statement", e);
			}
     	callStatement = null;
        	openCallStatements--;
        }
        
        if (query != null) {
        
          callStatement = con.prepareCall(query);
          openCallStatements++;
          if (query.indexOf("Call") != -1 && query.indexOf("(") != -1) {
            spName = query.substring(query.indexOf("Call") + 5,
                                     query.indexOf("("));
          }
        }
        else {
          callStatement = con.prepareCall(update);
          openCallStatements++;
          if (update.indexOf("Call") != -1
              && update.indexOf("(") != -1) {
            spName = update.substring(update.indexOf("Call") + 5,
                                      update.indexOf("("));
          }
        }

        if (debug) {
          logger.info("callStatement = " + callStatement.toString());
        }
        if (debug) {
          logger.info("parameters = " + parameters);

        }
        if (parameters != null) {
        	int spIndex = 0;
			for (int i = 0; i < parameters.size(); i++) {
				Object param = parameters.get(i);
	            int type = ( (Integer) parameterTypes.get(i)).intValue();
	            if (debug) {
	              logger.info("Setting parameter: " + param + "(" + (param != null ? param.getClass().toString() : "") + "), type = " + type);
	            }
	            if (type == INPUT_PARAM) {
	                spIndex++;
					SQLMapHelper.setParameter(callStatement,
    										  param, 
    										  i, 
    										  this,
    										  this.getDbIdentifier(), 
    										  this.isLegacyMode,
    										  this.debug, 
    										  this.myAccess);
	            } else {
	              int sqlType = ( (Integer) lookupTable.get( param)).intValue();
	              callStatement.registerOutParameter(i + 1, sqlType);
	            }
			}
        }

        if (query != null) {
          // logger.info("\nCalling query - callStatement.query()");
          rs = callStatement.executeQuery();
          // logger.info("\nCalled query");
        }
        else {
          // logger.info("\nCalling update - callStatement.execute()");
          callStatement.execute();
          // logger.info("\nCalled update");
        }
      }

      if (rs != null) {
        ResultSetMetaData meta = rs.getMetaData();
        int columns = meta.getColumnCount();
        ArrayList dummy = new ArrayList();
        int index = 1;

        remainCount = 0;
        while (rs.next()) {
          if ( (index >= startIndex) && ( index <= endIndex) ) {
            ResultSetMap rm = new ResultSetMap();

            for (int i = 1; i < (columns + 1); i++) {
              String param = meta.getColumnName(i);
              int type = meta.getColumnType(i);
              // logger.info(param + " has type " + getType(type));
              Object value = null;
              java.util.Calendar c = java.util.Calendar.getInstance();

              if (rs.getString(i) != null) {
            	  value = SQLMapHelper.getColumnValue(rs, type, i);
              }
              rm.addValue(param.toUpperCase(), value);
            }
            dummy.add(rm);
            viewCount++;
          }
          else if (index >= startIndex) {
            remainCount++;
          }
          rowCount++;
          index++;
        }
        resultSet = new ResultSetMap[dummy.size()];
        resultSet = (ResultSetMap[]) dummy.toArray(resultSet);
      }
    }
    catch (SQLException sqle) {
      AuditLog.log( sqle.getLocalizedMessage() + "/" + sqle.getSQLState(), "SPMap",sqle,Level.SEVERE, myAccess.accessID);
      throw new UserException( -1,  sqle.getLocalizedMessage() + "/" + sqle.getSQLState(),sqle);
    }
    finally {
    	 if (rs != null) {
       	  try {
       	   rs.close();
       	  } catch (Exception e) { e.printStackTrace(Access.getConsoleWriter(myAccess)); }
       	  rs = null;
         }
         this.resetAll();
    }
    long end = System.currentTimeMillis();
    double total = (end - start) / 1000.0;

    totaltiming += total;
    if (debug) {
        logger.info("finished " + total + " seconds. Average query time: " + (totaltiming/requestCount) + " (" + requestCount + ")");
    }
    return resultSet;
  }

  
    private boolean isLegacyMode() {
		//Repository r = DispatcherFactory.getInstance().getNavajoConfig().getRepository();
		return  DispatcherFactory.getInstance().getNavajoConfig().useLegacyDateMode();
	}

  @Override
public void setQuery(String newQuery) throws UserException {
//    if ( (this.query != null) || (this.update != null)) {
//      throw new UserException( -1,
//          "SPMap does not allow for multiple queries/updates, use a new SPMap");
//    }
    update = null;
    super.setQuery(newQuery);
    parameterTypes = new ArrayList();
  }

  @Override
  public void setUpdate(String newUpdate) throws UserException {
    if ( (this.update != null) || (this.query != null)) {
      throw new UserException( -1,
          "SPMap does not allow for multiple queries/updates, use a new SPMap");
    }
    // logger.info("in setUpdate(), newUpdate = " + newUpdate);
    // Close previous callStatement if it exists.
    try {
      	if (callStatement != null) {
      		callStatement.close();
      		callStatement = null;
      		openCallStatements--;
      	}
      } catch (Exception e) { e.printStackTrace(Access.getConsoleWriter(myAccess)); }
    super.setUpdate(newUpdate);
    parameterTypes = new ArrayList();
  }

  @Override
public void setParameter(Object param) {
    // logger.info("in setParameter(),");
    super.setParameter(param);
    parameterTypes.add(Integer.valueOf(INPUT_PARAM));
    // logger.info("Leaving setParameter() in SPMap");
  }

  public Object getOutputParameter() {
    return "";
  }

  public void setOutputParameterType(String type) {
    super.setParameter( type);
    parameterTypes.add(Integer.valueOf(OUTPUT_PARAM));
  }

  public Object getOutputParameter(Integer i) throws UserException {

    int index = i.intValue();
    // logger.info("in getOutputParameter("+index+")");
    Object value = null;

    if (callStatement != null) {
      try {
        // logger.info("parameters = " + parameters);
        if ( index  > parameters.size() ) {
          throw new UserException(-1, "Outputparameter index out of range: " + i.intValue() );
        }
        String type = (String) parameters.get(index - 1);
        if (lookupTable.get(type) == null) {
          throw new UserException(-1, "Outputparameter index out of range, trying to read a normal parameter as an output parameter: " + i.intValue() );
        }
        int sqlType = ( (Integer) lookupTable.get(type)).intValue();

        java.util.Calendar c = java.util.Calendar.getInstance();

        switch (sqlType) {
          case Types.VARCHAR:
          case Types.CHAR:
            value = callStatement.getString(index);
            break;

          case Types.BIT:
            value =  Boolean.valueOf(callStatement.getBoolean(index));
            break;

          case Types.DATE:
            if (callStatement.getDate(index) != null) {
              Date d = callStatement.getDate(index, c);
              long l = d.getTime();

              value = new java.util.Date(l);
            }
            break;

          case -101: // For Oracle; timestamp with timezone, treat this as clocktime.
                    if (callStatement.getTimestamp(index) != null) {
                      Timestamp ts = callStatement.getTimestamp(index, c);
                      long l = ts.getTime();
                      value = new ClockTime(new java.util.Date(l));
                    }
                    break;

          case Types.TIMESTAMP:
            if (callStatement.getTimestamp(index) != null) {
              Timestamp ts = callStatement.getTimestamp(index, c);
              long l = ts.getTime();

              value = new java.util.Date(l);
            }
            break;

          case Types.INTEGER:
            value = Integer.valueOf(callStatement.getInt(index));
            break;

          case Types.NUMERIC:

            ResultSetMetaData meta = callStatement.getMetaData();
            int prec = meta.getPrecision(index);
            int scale = meta.getScale(index);

            if (scale == 0) {
              value = Integer.valueOf(callStatement.getInt(index));
            }
            else {
              value = Double.valueOf(callStatement.getString(index));
            }
            break;

          case Types.SMALLINT:
          case Types.TINYINT:
            value = Integer.valueOf(callStatement.getInt(index));
            break;

          case Types.DOUBLE:
          case Types.FLOAT:
            value = Double.valueOf(callStatement.getDouble(index));
            break;

          default:
            value = callStatement.getString(index);
            break;
        }
      }
      catch (SQLException sqle) {
    	  AuditLog.log("SPMap", sqle.getLocalizedMessage() + "/" + sqle.getSQLState(),sqle, Level.SEVERE, myAccess.accessID);
        throw new UserException( -1, sqle.getMessage(),sqle);
      }
      return value;
    }
    else {
      return "";
    }
  }

  @Override
  public void store() throws UserException,
      MappableException {
    try {
      if (callStatement != null) {
        callStatement.close();
        callStatement = null;
        openCallStatements--;
      }
    }
    catch (SQLException sqle) {
    	AuditLog.log("SPMap", sqle.getLocalizedMessage() + "/" + sqle.getSQLState(),sqle, Level.SEVERE, myAccess.accessID);
    }
    super.store();
  }

  @Override
public void kill() {
    try {
      if (callStatement != null) {
        callStatement.close();
        callStatement = null;
        openCallStatements--;
      }
    }
    catch (SQLException sqle) {
    	AuditLog.log( "SPMap", sqle.getLocalizedMessage() + "/" + sqle.getSQLState(),sqle,Level.SEVERE, myAccess.accessID);
    }
    super.kill();
  }
  
 
}
