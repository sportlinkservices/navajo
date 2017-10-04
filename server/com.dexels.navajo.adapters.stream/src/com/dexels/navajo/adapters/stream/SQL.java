package com.dexels.navajo.adapters.stream;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.davidmoten.rx.jdbc.Database;
import org.dexels.grus.GrusProviderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.resource.jdbc.mysql.MySqlDataSourceComponent;
import com.dexels.replication.api.ReplicationMessage;

import io.reactivex.Flowable;
import oracle.jdbc.pool.OracleDataSource;

public class SQL {
	
	
	private final static Logger logger = LoggerFactory.getLogger(SQL.class);

	private static DataSource testDataSource = null;

	
	public static DataSource resolveDataSource(String dataSourceName, String tenant) {
		
		if(dataSourceName.equals("dummy")) {
			if(testDataSource!=null) {
				return testDataSource;
			}

		    OracleDataSource ds;
			try {
				ds = new oracle.jdbc.pool.OracleDataSource();
			    ds.setDriverType("thin");
			    ds.setServerName("localhost");
			    ds.setDatabaseName("SLTEST01");
			    ds.setPortNumber(1521);
			    ds.setUser("blib");
			    ds.setPassword("blob");
	        		testDataSource = ds;
				return testDataSource;
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		
		if(dataSourceName.equals("dummymysql")) {
			MySqlDataSourceComponent dsc = new MySqlDataSourceComponent();
	        Map<String,Object> props = new HashMap<>();
	        props.put("type", "mysql");
	        props.put("name", "authentication");
	        props.put("url", "jdbc:mysql://localhost/competition");
	        props.put("user", "username");
	        props.put("password", "password");
//	        dsc.activate(props);
	        Properties p = new Properties();
	        p.putAll(props);
	        try {
	        		testDataSource = dsc.createDataSource(p);
				return testDataSource;
			} catch (SQLException e) {
				e.printStackTrace();
			} 
	        return null;
		}
		logger.info("Resolving datasource {} for tenant {}",dataSourceName,tenant);
		DataSource source = GrusProviderFactory.getInstance().getInstanceDataSource(tenant, dataSourceName);
		return source;
	}
	
	public static Flowable<ReplicationMessage> query(String datasource, String tenant, String query, Object... params) {
		DataSource ds = resolveDataSource(datasource, tenant);
//		Pool<Connection> pool =  getPoolForDataSource(ds);

		return Database.fromBlocking(ds)
			.select(query)
			.parameterList(Arrays.asList(params))
			.get(SQL::resultSet);
	}
	


	public static ReplicationMessage defaultSqlResultToMsg(SQLResult result) {
		return result.toMessage();
	}
	
	
	
	public static ReplicationMessage resultSet(ResultSet rs)  {
			try {
				return new SQLResult(rs).toMessage();
			} catch (Exception e) {
				logger.error("Error: ", e);
			}
			return null;
	}
}
