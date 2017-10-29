package com.innolux.dems.source;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.output.UpdateState;

import type.PortCST;
import type.StockerInfo;

public class PortCSTInfo extends Thread{
	DBConnector MesDB = null;
	private Hashtable<String, PortCST> PortCSTInfoList = new Hashtable<String, PortCST>();
	private Logger logger = Logger.getLogger(this.getClass());
	private Tools tools = new Tools();
	private String Fab = "";

	public PortCSTInfo(DBConnector DB,String _Fab) {
		Fab = _Fab;
		MesDB = DB;
	}

	public void run() {
		Vector<ItemState> ItemStateList = new Vector<ItemState>();
		updatePortCSTInfo();
		for (String eachPort : getPortCSTInfoKeys()) {
			PortCST eachPortCSTInfo = getPortCSTInfo(eachPort);
			if (eachPortCSTInfo != null) {
				ItemState eachPortInfo = new ItemState();
				eachPortInfo.Fab = Fab;
				eachPortInfo.ItemName = eachPortCSTInfo.PortID;
				eachPortInfo.ItemState = eachPortCSTInfo.CassetteID;
				ItemStateList.add(eachPortInfo);

			}

		}
		new UpdateState().onRvMsg(ItemStateList);
		
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

		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.conn.createStatement();

			SQL = "select t.name || '.CSTID' name,t.carrierid from fweqpport t where t.name not like '%.P99'";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

			while (rs.next()) {
				PortCST eachPort = new PortCST();
				String key = rs.getString("name");
				eachPort.PortID = key;
				if(rs.getString("carrierid")==null){
					eachPort.CassetteID = "";
				}else{
					eachPort.CassetteID = rs.getString("carrierid");
				}
				synchronized (PortCSTInfoList) {
					if (PortCSTInfoList.containsKey(key)) {
						PortCSTInfoList.remove(key);
						PortCSTInfoList.put(key, eachPort);
					} else {
						PortCSTInfoList.put(key, eachPort);
					}
				}
			}
			rs.close();
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
