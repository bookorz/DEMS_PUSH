package com.innolux.dems.source;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;

import type.PortCST;

public class CurrentState {
	DBConnector MesDB = null;
	String Fab = "";
	private Logger logger = Logger.getLogger(this.getClass());
	
public CurrentState(DBConnector DB,String _Fab){
		Fab = _Fab;
		MesDB= DB;
	}
public String getAllStates(){
	return CurrentState();
}

private String CurrentState(){
	ResultSet rs = null;
	String SQL = " select t1.EQUIPMENTNAME EQUIPMENTNAME, t2.VALDATA STATE, t1.time"+
					"  from fweqpcurrentstate t1, fweqpcurrentstate_pn2m t2"+
					" where t1.SYSID = t2.FROMID"+
					"   and t2.keydata = 'EQPREPORT'"+
					" union"+
					" select t1.name, t2.valdata, t1.time"+
					"  from fweqpsubeqp t1, fweqpsubeqp_pn2m t2"+
					" where t1.SYSID = t2.FROMID"+
					"   and t2.keydata = 'EQPREPORT'"+
					" union"+
					" select t1.name, t2.valdata, t1.time"+
					"  from fweqpchamber t1, fweqpchamber_pn2m t2"+
					" where t1.SYSID = t2.FROMID"+
					"   and t2.keydata = 'EQPREPORT'"+
					" union"+
					" select name, state, time from fweqpport";

	rs = MesDB.Query(SQL);
	try {
		JSONArray pushList = new JSONArray();
		while (rs.next()) {
			
			JSONObject eachEqp = new JSONObject();
			eachEqp.put("EquipmentName", rs.getString("EQUIPMENTNAME"));
			eachEqp.put("State", rs.getString("STATE"));
			eachEqp.put("UpdateTime", rs.getString("time"));
			pushList.put(eachEqp);
		   
			
		}
		JSONObject SendJson = new JSONObject();
		SendJson.put("fab", Fab);
		SendJson.put("eqpStateList", pushList);
		return SendJson.toString();
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
	return "";
}
}
