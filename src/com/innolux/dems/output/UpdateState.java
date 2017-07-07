package com.innolux.dems.output;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;

public class UpdateState implements CallBackInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector CELLEDCXSpec = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    ))",
			"L5CEL", "L5CEL");

	public UpdateState() {
		
	}

	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		Update2DB(msg);
	}

	private void Update2DB(String jsonStr) {
		try {
			JSONObject orgMsg = new JSONObject(jsonStr);
			String Fab = orgMsg.getString("fab");
			JSONArray revJson = orgMsg.getJSONArray("eqpStateList");
			for(int i = 0;i<revJson.length();i++){
				JSONObject each = revJson.getJSONObject(i);
				String EquipmentName = each.getString("EquipmentName");
				String State = "";
				try{
					State = each.getString("State");
				}catch(Exception e){
					logger.error("Update2DB error: " + e.getMessage() + " each:" + each.toString());
				}
				
				String UpdateTime = "";
				try{
					UpdateTime = each.getString("UpdateTime");
				}catch(Exception e){
					UpdateTime = "";
				}
				if(!UpdateTime.equals("")){
					UpdateTime = UpdateTime.substring(0,15);
				}
				ResultSet rs = null;
				int rowCount = 0;
				String SQL = "select * from dems_current_state t where t.fab = '"+Fab+"' and t.item_name = '"+EquipmentName+"'";
				logger.info(SQL);
				rs = CELLEDCXSpec.Query(SQL);

				try {
					while (rs.next()) {
						rowCount++;

					}
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					logger.error("Functon:Update2DB rs.next() error, exception=" + e.getMessage());
				} finally {
					try {
						rs.getStatement().close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
					}
				}

				if (rowCount != 0) {
					if(UpdateTime.equals("")){
						SQL = "update dems_current_state t set t.item_state='"+State+"',t.updatetime=sysdate where t.fab = '"+Fab+"' and t.item_name = '"+EquipmentName+"'";
					}else{
						SQL = "update dems_current_state t set t.item_state='"+State+"',t.updatetime=to_date('"+UpdateTime+"','yyyymmdd hh24miss') where t.fab = '"+Fab+"' and t.item_name = '"+EquipmentName+"'";
					}
					logger.info(SQL);
					CELLEDCXSpec.Execute(SQL);
				} else {
					if(UpdateTime.equals("")){
						SQL = "insert into dems_current_state (fab,item_name,item_state) values('"+Fab+"','"+EquipmentName+"','"+State+"')";
					}else{
						SQL = "insert into dems_current_state (fab,item_name,item_state,updatetime) values('"+Fab+"','"+EquipmentName+"','"+State+"',to_date('"+UpdateTime+"','yyyymmdd hh24miss'))";
					}
					logger.info(SQL);
					CELLEDCXSpec.Execute(SQL);
				}
			
			}
		
			
		} catch (Exception e) {
			logger.error("Update2DB error: " + e.getMessage() + " jsonMsg:" + jsonStr);
		}

	}

}
