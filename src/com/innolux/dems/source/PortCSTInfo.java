package com.innolux.dems.source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;

import type.PortCST;

public class PortCSTInfo {
	DBConnector MesDB = null;
	private Hashtable<String, PortCST> PortCSTInfoList = new Hashtable<String, PortCST>();
	private Logger logger = Logger.getLogger(this.getClass());
	private Tools tools = new Tools();

	public PortCSTInfo(DBConnector DB) {

		MesDB = DB;
	}

	public Set<String> getPortCSTInfoKeys() {
		return PortCSTInfoList.keySet();
	}

	public PortCST getPortCSTInfo(String key) {

		PortCST result = null;
		synchronized (PortCSTInfoList) {
			if (PortCSTInfoList.containsKey(key)) {
				result = PortCSTInfoList.get(key);
			}
		}
		return result;
	}

	public void updatePortCSTInfo() {

		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select t.name || '.CSTID' name,t.carrierid from fweqpport t where t.name not like '%.P99'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				PortCST eachPort = new PortCST();
				String key = rs.getString("name");
				eachPort.PortID = key;
				eachPort.CassetteID = rs.getString("carrierid");

				synchronized (PortCSTInfoList) {
					if (PortCSTInfoList.containsKey(key)) {
						PortCSTInfoList.remove(key);
						PortCSTInfoList.put(key, eachPort);
					} else {
						PortCSTInfoList.put(key, eachPort);
					}
				}
			}
		} catch (Exception e) {
			logger.error(tools.StackTrace2String(e));

		} finally {
			if (stmt != null) {
				try {
					stmt.close();
				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
			if (conn != null) {
				try {

					MesDB.closeConnection(conn);

				} catch (SQLException e) {
					logger.error(tools.StackTrace2String(e));
				}
			}
		}

	}
}
