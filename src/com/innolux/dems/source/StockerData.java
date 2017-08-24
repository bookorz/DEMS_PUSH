package com.innolux.dems.source;

import java.sql.ResultSet;
import java.util.Hashtable;
import java.util.Set;

import org.apache.log4j.Logger;

import com.innolux.dems.DBConnector;

import type.StockerInfo;

public class StockerData {
	private Logger logger = Logger.getLogger(this.getClass());
	private Hashtable<String, StockerInfo> StockerInfoList = new Hashtable<String, StockerInfo>();
	DBConnector MesDB = null;
	
	
	public StockerData(DBConnector DB){
		
		MesDB= DB;
	}
	
	public Set<String> getStockerInfoKeys(){
		return StockerInfoList.keySet();
	}
	
	public StockerInfo getStockerInfo(String key) {
		
		StockerInfo result = null;
		synchronized (StockerInfoList) {
			if (StockerInfoList.containsKey(key)) {
				result = StockerInfoList.get(key);
			}
		}
		return result;
	} 
	
	public void updateStockerData(){
		ResultSet rs = null;
		String SQL = "select t.equipmentname || '.STKINFO' equipmentname,"+
					"       sum(case"+
					"             when t1.tonode = t.equipmentname then 1 else 0 end) transferInstk,"+
					"       sum(case"+
					"             when t1.fromnode = t.equipmentname then 1 else 0 end) transferOutstk,"+
					"       count(t2.carrierid) Emptycst,"+
					"       t3.fullratio"+
					"  from fweqpcurrentstate t, fwmhstransferqueue t1, fwcarrier t2, mcs_stkinfo t3"+
					" where eqptype in ('STOCKER', 'AGV', 'RGV')"+
					"   and (t1.fromnode = t.equipmentname or t1.tonode = t.equipmentname)"+
					"   and t2.location(+) = t.equipmentname"+
					"   and t2.carrierstatus(+) = 'Processing_Empty'"+
					"   and t2.carrierstate(+) = 'STOCKER'"+
					"   and t3.stkid(+) = t.equipmentname"+
					" group by t.equipmentname,t3.fullratio";

		rs = MesDB.Query(SQL);
		try {
			while (rs.next()) {
				StockerInfo eachStocker = new StockerInfo();
				String key = rs.getString("equipmentname");
				eachStocker.EquipmentName = key;
				eachStocker.transferInstk = rs.getString("transferInstk");
				eachStocker.transferOutstk = rs.getString("transferOutstk");
				eachStocker.Emptycst = rs.getString("Emptycst");
				eachStocker.fullratio = rs.getString("fullratio");
			   
				synchronized (StockerInfoList) {
					if (StockerInfoList.containsKey(key)) {
						StockerInfoList.remove(key);
						StockerInfoList.put(key, eachStocker);
					} else {
						StockerInfoList.put(key, eachStocker);
					}
				}
			}
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
	}
}
