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
import com.innolux.dems.output.UpdateStatus;

import type.StockerInfo;

public class StockerData extends Thread{
	private Logger logger = Logger.getLogger(this.getClass());
	private Hashtable<String, StockerInfo> StockerInfoList = new Hashtable<String, StockerInfo>();
	DBConnector MesDB = null;
	private Tools tools = new Tools();
	private String Fab = "";
	
	
	public StockerData(DBConnector DB,String _Fab){
		Fab = _Fab;
	
		MesDB= DB;
	}
	
	public void run() {
		Vector<ItemState> ItemStateList = new Vector<ItemState>();
		updateStockerData();
		for (String eachStocker : getStockerInfoKeys()) {
			StockerInfo eachStockerInfo = getStockerInfo(eachStocker);
			if (eachStockerInfo != null) {
				ItemState eachStk = new ItemState();
				eachStk.Fab = Fab;
				eachStk.ItemName = eachStockerInfo.EquipmentName;
				eachStk.ItemState = "I:" + eachStockerInfo.transferInstk + " O:"
						+ eachStockerInfo.transferOutstk + " E:" + eachStockerInfo.Emptycst + " "
						+ eachStockerInfo.fullratio;
				ItemStateList.add(eachStk);

			}

		}
		new UpdateState().onRvMsg(ItemStateList);
		
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
		ConnectionInfo conn = null;
		Statement stmt = null;
		ResultSet rs = null;

		String SQL = "";
		try {
			conn = MesDB.getConnection();
			stmt = conn.conn.createStatement();

			SQL = "select t.equipmentname || '.STKINFO' equipmentname,"+
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
			logger.debug(SQL);

			rs = stmt.executeQuery(SQL);

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
