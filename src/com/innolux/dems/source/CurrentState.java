package com.innolux.dems.source;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.output.UpdateStatus;

import type.PortCST;

public class CurrentState {
	DBConnector MesDB = null;
	String Fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public CurrentState(DBConnector DB, String _Fab) {
		Fab = _Fab;
		MesDB = DB;
	}

	public Vector<ItemState> getAllStates() {
		return GetCurrentState();
	}

	private Vector<ItemState> GetCurrentState() {
		ResultSet rs = null;
		Vector<ItemState> pushList = new Vector<ItemState>();
		String SQL = " select t1.EQUIPMENTNAME EQUIPMENTNAME, t2.VALDATA STATE, t1.time"
				+ "  from fweqpcurrentstate t1, fweqpcurrentstate_pn2m t2" + " where t1.SYSID = t2.FROMID"
				+ "   and t2.keydata = 'EQPREPORT'" + " union" + " select t1.name, t2.valdata, t1.time"
				+ "  from fweqpsubeqp t1, fweqpsubeqp_pn2m t2" + " where t1.SYSID = t2.FROMID"
				+ "   and t2.keydata = 'EQPREPORT'" + " union" + " select t1.name, t2.valdata, t1.time"
				+ "  from fweqpchamber t1, fweqpchamber_pn2m t2" + " where t1.SYSID = t2.FROMID"
				+ "   and t2.keydata = 'EQPREPORT'" + " union" + " select name, state, time from fweqpport";
		logger.debug(SQL);
		rs = MesDB.Query(SQL);
		try {
			
			while (rs.next()) {

				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("EQUIPMENTNAME");
				eachEqp.ItemState = rs.getString("STATE");
				eachEqp.ItemStateUpdateTime = rs.getString("time");				
			
				pushList.add(eachEqp);

			}
			
			return pushList;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error("updateInlineMode while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error("updateInlineMode rs.close error :" + e.getMessage());
			}
		}
		return pushList;
	}
	
	public void GetEQMode() {
		ResultSet rs = null;
		String SQL = "select t.name, t1.capability"
						 + " from fweqpequipment t, fweqpcurrentstate t1"
						+ " where t.name not in ('test','CIM-SYS')"
						+ " and t1.capability is not null"
						+ " and  t.currentstate ="
						+ " t1.sysid";

		
		UpdateStatus updateStatus = new UpdateStatus();
		rs = MesDB.Query(SQL);
		try {
		    Vector<ItemState> pushList = new Vector<ItemState>();
			while (rs.next()) {

				ItemState eachEqp = new ItemState();
				eachEqp.Fab = Fab;
				eachEqp.ItemName = rs.getString("name");
				eachEqp.UpdateType = "ITEM_MODE";
				eachEqp.UpdateValue = rs.getString("capability");
				
			
				pushList.add(eachEqp);

			}
			
			
			updateStatus.Update2DB(pushList);
		//return SendJson.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Tools tools = new Tools();
			logger.error(tools.StackTrace2String(e));

		} finally {
			try {

				rs.getStatement().close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				Tools tools = new Tools();
				logger.error(tools.StackTrace2String(e));
			}
		}
		//return "";
	}
}
