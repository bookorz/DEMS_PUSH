package com.innolux.dems;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.innolux.dems.source.Tools;

import oracle.jdbc.OracleConnection;

public class DBConnector implements DBSource {

	private String url;
	private String user;
	private String passwd;
	private int max; // 連接池中最大Connection數目
	private List<ConnectionInfo> connections;
	private Tools tools = new Tools();
	private Logger logger = Logger.getLogger(this.getClass());

	public class ConnectionInfo {
		public long CreateTime = 0;
		public Connection conn = null;
	}

	public DBConnector(String connectionStr, String User, String PWD, int maxConn) {
		url = connectionStr;
		user = User;
		passwd = PWD;
		max = maxConn;
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
		} catch (Exception e) {
			logger.error(url);
			logger.error(tools.StackTrace2String(e));
		}

		connections = new ArrayList<ConnectionInfo>();
	}
	
	private ConnectionInfo GetConnectionFromDB() throws SQLException{
		Properties props = new Properties();
		props.setProperty("user", user);
		props.setProperty("password", passwd);
		props.setProperty(OracleConnection.CONNECTION_PROPERTY_THIN_NET_CONNECT_TIMEOUT, "10000");
		ConnectionInfo conInfo = new ConnectionInfo();
		conInfo.conn = DriverManager.getConnection(url, props);
		conInfo.CreateTime = System.currentTimeMillis();
		return conInfo;
	}

	public synchronized ConnectionInfo getConnection() throws SQLException {
		ConnectionInfo con = null;
		try {
			if (connections.size() == 0) {
				return GetConnectionFromDB();
			} else {
				int lastIndex = connections.size() - 1;
				con = connections.remove(lastIndex);

				if (System.currentTimeMillis() - con.CreateTime > 900000) {
					try {
						con.conn.close();
					} catch (Exception e1) {
						logger.error(tools.StackTrace2String(e1));
					}
					
					con = GetConnectionFromDB();
				} else {
					if (!con.conn.isValid(5)) {
						try {
							con.conn.close();
						} catch (Exception e2) {
							logger.error(tools.StackTrace2String(e2));
						}
						con = GetConnectionFromDB();
					}
				}
			}
		} catch (Exception e) {
			logger.error(url);
			logger.error(tools.StackTrace2String(e));
		}
		return con;
	}

	public synchronized void closeConnection(ConnectionInfo conn) throws SQLException {
		try {
			if (connections.size() == max) {
				conn.conn.close();
			} else {
				
				
				connections.add(conn);
			}
		} catch (Exception e) {
			logger.error(url);
			logger.error(tools.StackTrace2String(e));
		}
	}
}
