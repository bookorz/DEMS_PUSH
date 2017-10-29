package com.innolux.dems.source;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.DBConnector.ConnectionInfo;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.output.UpdateState;
import com.innolux.dems.output.UpdateStatus;

public class CurrentState extends Thread{
	DBConnector MesDB = null;
	String Fab = "";
	private Tools tools = new Tools();
	private Logger logger = Logger.getLogger(this.getClass());

	private CheckTime ckTime = null;
	

	public CurrentState(DBConnector DB, String _Fab ,CheckTime _ckTime) {
		Fab = _Fab;
		MesDB = DB;
		ckTime = _ckTime;
	
		
	}
	
	public void run() {
		new UpdateState().onRvMsg(GetCurrentPortState());
		new UpdateStatus().onRvMsg(GetEQMode());
		
	}


	public Vector<ItemState> GetCurrentPortState() {
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		
		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.conn.createStatement();

			
			SQL = "select name EQUIPMENTNAME, state STATE, time from fweqpport"
					+" where time >= '"+ckTime.LastPortUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			ckTime.LastPortUpdateTime = "";
			while (rs.next()) {
				if(ckTime.LastPortUpdateTime.equals("")){
					ckTime.LastPortUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
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
		logger.debug(pushList.toString());
		return pushList;
	}

	public Vector<ItemState> getAllStates() {
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		
		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.conn.createStatement();

			SQL = "select t1.EQUIPMENTNAME EQUIPMENTNAME, t2.VALDATA STATE, t1.time"
					+"          from fweqpcurrentstate t1, fweqpcurrentstate_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+ckTime.LastEQPUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			ckTime.LastEQPUpdateTime = "";
			while (rs.next()) {
				if(ckTime.LastEQPUpdateTime.equals("")){
					ckTime.LastEQPUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			rs.close();
			SQL = "select t1.name EQUIPMENTNAME, t2.valdata STATE, t1.time"
					+"          from fweqpsubeqp t1, fweqpsubeqp_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+ckTime.LastSubEQPUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			ckTime.LastSubEQPUpdateTime = "";
			while (rs.next()) {
				if(ckTime.LastSubEQPUpdateTime.equals("")){
					ckTime.LastSubEQPUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			rs.close();
			SQL = "select t1.name EQUIPMENTNAME, t2.valdata STATE, t1.time"
					+"          from fweqpchamber t1, fweqpchamber_pn2m t2"
					+"         where t1.SYSID = t2.FROMID"
					+"           and t2.keydata = 'EQPREPORT'"
					+" and time >= '"+ckTime.LastChamberUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			ckTime.LastChamberUpdateTime = "";
			while (rs.next()) {
				if(ckTime.LastChamberUpdateTime.equals("")){
					ckTime.LastChamberUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
			}
			rs.close();
			SQL = "select name EQUIPMENTNAME, state STATE, time from fweqpport"
					+" where time >= '"+ckTime.LastPortUpdateTime+"'"
					+" order by time desc";
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);
			ckTime.LastPortUpdateTime = "";
			while (rs.next()) {
				if(ckTime.LastPortUpdateTime.equals("")){
					ckTime.LastPortUpdateTime = rs.getString("time");
					
				}
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time").substring(0,rs.getString("time").length()-3);				
			
				pushList.add(eachEqp);
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
		logger.debug(pushList.toString());
		return pushList;
	}
	
	public Vector<ItemState> GetEQMode() {
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		
		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.conn.createStatement();

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
		
		return pushList;

	}
}
