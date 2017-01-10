package com.innolux.dems;
import java.sql.*;

import org.apache.log4j.Logger;
public class DBConnector {
	public Connection con = null;
    public String _connectionStr="";
    public String _User="";
    public String _PWD="";
    
    
	public Logger logger = Logger.getLogger(this.getClass());

	public DBConnector(String connectionStr, String User, String PWD) {
		_connectionStr = connectionStr;
		_User=User;
		_PWD=PWD;
		try {
			// step1 load the driver class
			Class.forName("oracle.jdbc.driver.OracleDriver");
			
			// step2 create the connection object
		
			con = DriverManager.getConnection(connectionStr, User, PWD);
			// "jdbc:oracle:thin:@172.20.9.32:1521:t2prpt"
			
		} catch (Exception e) {
			logger.error("get Connection failed, conStr=" + connectionStr + " User=" + User + " PWD=" + PWD
					+ "exception=" + e.getMessage());
		}
	}

	public ResultSet Query(String SQL) {

		// String strSQL;
		// step3 create the statement object
		Statement stmt = null;
		ResultSet rs = null;
		try {
			
			stmt = con.createStatement();

			// step4 execute query
			// SQL="select * from rfidcurrentsts t ";
		    rs = stmt.executeQuery(SQL);

			return rs;
		} catch (Exception ex) {
			logger.error("SQL Query failed, exception=" + ex);
			logger.error("SQL Query failed, SQL=" + SQL);
//			try {
//				if(con!=null)
//				con.close();
//				con = DriverManager.getConnection(_connectionStr, _User, _PWD);
//				
//				stmt = con.createStatement();
//				stmt.setQueryTimeout(10000);
//				rs = stmt.executeQuery(SQL);
//				return rs;
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				logger.error("SQL Query failed, exception=" + e);
				return null;
//			}
			
			
		} finally {
//			try {
//				
//				stmt.close();
//				
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				logger.error("Statement close failed, exception=" + e.getMessage());
//			}
		}
	}

	@SuppressWarnings("resource")
	public void Execute(String SQL) {
		// Type:insert,Updata
		Statement stmt = null;
		try {
			
			stmt = con.createStatement();
//			stmt = con.createStatement();
			stmt.execute(SQL);

		} catch (Exception ex) {
			logger.error("SQL Execute failed, exception=" + ex);
			logger.error("SQL Query failed, SQL=" + SQL);
			try {
				con.close();
				con = DriverManager.getConnection(_connectionStr, _User, _PWD);
				
				stmt = con.createStatement();
				stmt.execute(SQL);
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("SQL Query failed, exception=" + e);
				
			}
		} finally {
			try {
				stmt.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("Statement close failed, exception=" + e.getMessage());
			}
		}
		

	}
}
