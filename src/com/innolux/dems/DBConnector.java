package com.innolux.dems;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.innolux.dems.source.Tools;

public class DBConnector implements DBSource {

	private String url;
	private String user;
	private String passwd;
	private int max; // 連接池中最大Connection數目
	private List<ConnectionInfo> connections;
	private Tools tools = new Tools();
	private Logger logger = Logger.getLogger(this.getClass());

	public class ConnectionInfo {
		public long LastTime = 0;
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

	public synchronized Connection getConnection() throws SQLException {
		ConnectionInfo con = null;
		try {
			if (connections.size() == 0) {
				return DriverManager.getConnection(url, user, passwd);
			} else {
				int lastIndex = connections.size() - 1;
				con = connections.remove(lastIndex);
				if (!con.conn.isValid(5)) {

					con.conn = DriverManager.getConnection(url, user, passwd);
				}
				if (System.currentTimeMillis() - con.LastTime > 900000) {
					con.conn = DriverManager.getConnection(url, user, passwd);
				}
			}
		} catch (Exception e) {
			logger.error(url);
			logger.error(tools.StackTrace2String(e));
		}
		return con.conn;
	}

	public synchronized void closeConnection(Connection conn) throws SQLException {
		try {
			if (connections.size() == max) {
				conn.close();
			} else {
				ConnectionInfo conInfo = new ConnectionInfo();
				conInfo.conn = conn;
				conInfo.LastTime = System.currentTimeMillis();
				connections.add(conInfo);
			}
		} catch (Exception e) {
			logger.error(url);
			logger.error(tools.StackTrace2String(e));
		}
	}
}
