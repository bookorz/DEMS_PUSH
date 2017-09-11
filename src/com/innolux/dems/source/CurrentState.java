package com.innolux.dems.source;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.ItemState;

public class CurrentState {
	DBConnector MesDB = null;
	String Fab = "";
	private Tools tools = new Tools();
	private Logger logger = Logger.getLogger(this.getClass());
	private String LastEQPUpdateTime = "20010101 120000000";
	private String LastSubEQPUpdateTime = "20010101 120000000";
	private String LastChamberUpdateTime = "20010101 120000000";
	private String LastPortUpdateTime = "20010101 120000000";

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

			SQL = "select t1.EQUIPMENTNAME EQUIPMENTNAME, t2.VALDATA STATE, t1.time"
					+"          from fweqpcurrentstate t1, fweqpcurrentstate_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+this.LastEQPUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			this.LastEQPUpdateTime = "";
			while (rs.next()) {
				if(this.LastEQPUpdateTime.equals("")){
					this.LastEQPUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			
			SQL = "select t1.name EQUIPMENTNAME, t2.valdata STATE, t1.time"
					+"          from fweqpsubeqp t1, fweqpsubeqp_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+this.LastSubEQPUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			this.LastSubEQPUpdateTime = "";
			while (rs.next()) {
				if(this.LastSubEQPUpdateTime.equals("")){
					this.LastSubEQPUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			
			SQL = "select t1.name EQUIPMENTNAME, t2.valdata STATE, t1.time"
					+"          from fweqpchamber t1, fweqpchamber_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+this.LastChamberUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			this.LastChamberUpdateTime = "";
			while (rs.next()) {
				if(this.LastChamberUpdateTime.equals("")){
					this.LastChamberUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			
			SQL = "select name EQUIPMENTNAME, state STATE, time from fweqpport"
					+" where time >= '"+this.LastPortUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			this.LastPortUpdateTime = "";
			while (rs.next()) {
				if(this.LastPortUpdateTime.equals("")){
					this.LastPortUpdateTime = rs.getString("time");
					
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
	
	public Vector<ItemState> GetEQMode() {
		Connection conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		
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
			
			while (rs.next()) {
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("name");
				eachEqp.UpdateType = "ITEM_MODE";
				eachEqp.UpdateValue = rs.getString("capability");
				
			
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
		
		return pushList;

	}
}
