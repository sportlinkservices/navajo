/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.adapter.sqlmap;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.adapter.SQLMap;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.Mappable;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.UserException;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
@Deprecated
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class DatabaseInfo implements Mappable {

  public String vendor = "";
  public String version = "";
  public DbCatalog [] catalogs;
  public String catalogName = null;
  public String schemaName = null;
  public DbSchema schema = null;
  public String datasource;

  private static final Logger logger = LoggerFactory
		.getLogger(DatabaseInfo.class);


  public DatabaseInfo() {
	  logger.warn("DatabaseInfo won't work for now");
  }
  public DatabaseInfo(DatabaseMetaData dbmd, String datasource) {

    try {
      this.vendor = dbmd.getDatabaseProductName();
      this.datasource = datasource;
      //setCatalogs(dbmd);
    }
    catch (SQLException ex) {
    	logger.warn("Error querying database version",ex);
    }

    try {
      this.version = dbmd.getDatabaseProductVersion();
    }
    catch (SQLException ex1) {
    	logger.warn("Error querying database version",ex1);
    }
    logger.info("vendor = " + vendor);
    logger.info("version = " + version);
  }

  public DbCatalog [] getCatalogs() {
    setCatalogs();
    return catalogs;
  }

  private void setCatalogs() {

    if (catalogs == null) {

      SQLMap sqlMap = new SQLMap();
      DatabaseMetaData metaData = null;

      try {
        sqlMap.load(null);
        sqlMap.setDatasource(datasource);
        Connection c = sqlMap.getConnection();
        metaData = c.getMetaData();
      }
      catch (Exception ex) {
      } finally {
        try {
          sqlMap.store();
        }
        catch (Exception ex1) {
        	logger.error("Error: ", ex1);
        }
      }

      if (metaData == null) {
        return;
      }

      boolean found = false;
      ArrayList list = new ArrayList();
      try {
        ResultSet rs = metaData.getCatalogs();
        while (rs.next()) {
          found = true;
          ResultSetMetaData rsmd = rs.getMetaData();
          int count = rsmd.getColumnCount();
          for (int i = 1; i <= count; i++) {
            String columnName = rsmd.getColumnName(i);
            String value = rs.getString(i);
            DbCatalog c = new DbCatalog();
            c.name = value;
            c.setSchemas(getAllSchemas(value, metaData));
            list.add(c);
          }
        }
        rs.close();

        if (!found) {
          DbCatalog c = new DbCatalog();
          c.name = "default";
          c.dummy = true;
          c.setSchemas(getAllSchemas(null, metaData));
          list.add(c);
        }

        catalogs = new DbCatalog[list.size()];
        catalogs = (DbCatalog[]) list.toArray(catalogs);
      }
      catch (SQLException sqle) {
    	  logger.error("Error: ", sqle);
      }
    }

  }

  private final ArrayList getAllSchemas(String catSchema, DatabaseMetaData metaData) {

    boolean found = false;
    ArrayList l = new ArrayList();
    try {
      ResultSet rs =metaData.getSchemas();
      while (rs.next()) {
        found = true;
        ResultSetMetaData rsmd = rs.getMetaData();
        int count = rsmd.getColumnCount();
        for (int i = 1; i <= count; i++) {
          String columnName = rsmd.getColumnName(i);
          String value = rs.getString(i);
          DbSchema s = new DbSchema();
          s.name = value;
          s.setTables(getAllTables(value, catSchema, metaData));
          l.add(s);
        }
      }
      rs.close();

      if (!found) {
        DbSchema s = new DbSchema();
        s.name = "default";
        s.dummy = true;
        s.setTables(getAllTables(null, null, metaData));
        l.add(s);
      }

    } catch (SQLException sqle) {
    	logger.error("Error: ", sqle);
    }

    return l;

  }

  private final ArrayList getAllTables(String tableSchema, String catSchema, DatabaseMetaData metaData) {
    ArrayList l = new ArrayList();
    try {
      ResultSet rs = metaData.getTables(catSchema, tableSchema, null, new String[]{"TABLE"});
      while (rs.next()) {
        ResultSetMetaData rsmd = rs.getMetaData();
        String columnName = rsmd.getColumnName(3);
        String value = rs.getString(3);
        DbTable t = new DbTable();
        t.name = value;
        t.catalogName = catSchema;
        t.schemaName = tableSchema;
        t.datasource = datasource;
        l.add(t);
      }
      rs.close();
    }
    catch (Exception ex) {
    	logger.error("Error: ", ex);
    }
    return l;
  }

  public String getVendor() {
    return this.vendor;
  }

  public String getVersion() {
    return this.version;
  }

  @Override
public void load(Access access) throws MappableException, UserException {
  }

  @Override
public void store() throws MappableException, UserException {
  }

  @Override
public void kill() {
  }

  public void setCatalogName(String catalogName) {
    this.catalogName = catalogName;
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  public DbSchema getSchema() throws UserException {

    DbCatalog [] all = getCatalogs();
    for (int i = 0; i < all.length; i++) {
      if (all[i].name.equals(catalogName == null ? "default" : catalogName)) {
        DbSchema [] schemas = all[i].getSchemas();
        for (int j = 0; j < schemas.length; j++) {
          if (schemas[j].name.equals(schemaName == null ? "default" : schemaName)) {
            return schemas[j];
          }
        }
      }
    }
    return null;
  }
}
