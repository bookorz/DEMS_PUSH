package com.innolux.dems.source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.output.UpdateStatus;

public class CurrentState {
	DBConnector MesDB = null;
	String Fab = "";
	private Tools tools = new Tools();
	private Logger logger = Logger.getLogger(this.getClass());
	private String LastUpdateTime = "20010101 120000000";

	public CurrentState(DBConnector DB, String _Fab) {
		Fab = _Fab;
		MesDB = DB;
	}

	public Vector<ItemState> getAllStates() {
		return GetCurrentState();
	}

	private Vector<ItemState> GetCurrentState() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		
		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select *"
					+"  from (select t1.EQUIPMENTNAME EQUIPMENTNAME, t2.VALDATA STATE, t1.time"
					+"          from fweqpcurrentstate t1, fweqpcurrentstate_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+"        union"
					+"        select t1.name, t2.valdata, t1.time"
					+"          from fweqpsubeqp t1, fweqpsubeqp_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+"        union"
					+"        select t1.name, t2.valdata, t1.time"
					+"          from fweqpchamber t1, fweqpchamber_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+"        union"
					+"        select name, state, time from fweqpport)"
					+" where time >= '"+LastUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			boolean isGetTime = false;
			while (rs.next()) {
				if(!isGetTime){
					this.LastUpdateTime = rs.getString("time");
					isGetTime = true;
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
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
		logger.debug(pushList.toString());
		return pushList;
	}
	
	public void GetEQMode() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		
		
		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.createStatement();

			SQL = "select t.name, t1.capability"
					 + " from fweqpequipment t, fweqpcurrentstate t1"
					+ " where t.name not in ('test','CIM-SYS')"
					+ " and t1.capability is not null"
					+ " and  t.currentstate ="
					+ " t1.sysid";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			UpdateStatus updateStatus = new UpdateStatus();
			while (rs.next()) {
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("name");
				eachEqp.UpdateType = "ITEM_MODE";
				eachEqp.UpdateValue = rs.getString("capability");
				
			
				
				updateStatus.Update2DB(eachEqp);
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
