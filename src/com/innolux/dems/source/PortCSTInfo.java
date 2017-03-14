package com.innolux.dems.source;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;

import type.PortCST;

public class PortCSTInfo {
	DBConnector MesDB = null;
	private Hashtable<String, PortCST> PortCSTInfoList = new Hashtable<String, PortCST>();
	private Logger logger = Logger.getLogger(this.getClass());
	
public PortCSTInfo(DBConnector DB){
		
		MesDB= DB;
	}
public Set<String> getPortCSTInfoKeys(){
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

public void updatePortCSTInfo(){
	ResultSet rs = null;
	String SQL = "select t.name || '.CSTID' name,t.carrierid from fweqpport t where t.name not like '%.P99'";

	rs = MesDB.Query(SQL);
	try {
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
}
}
