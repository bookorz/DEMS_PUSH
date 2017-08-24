package com.innolux.dems.output;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

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
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.Tools;

public class UpdateState implements ParserInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	private DBConnector CELLEDCXSpec = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    ))",
			"L5CEL", "L5CEL");

	public UpdateState() {
		
	}

	@Override
	public void onRvMsg(Vector<ItemState> msg) {
		// TODO Auto-generated method stub
		Update2DB(msg);
	}

	private void Update2DB(Vector<ItemState> msg) {
		try {
			
			for(int i = 0;i<msg.size();i++){
				ItemState eachItem = msg.get(i);
				boolean isChange = false;

		
				ResultSet rs = null;
				int rowCount = 0;
				String SQL = "select * from dems_current_state t where t.fab = '"+eachItem.Fab+"' and t.item_name = '"+eachItem.ItemName+"'";
				logger.debug(SQL);
				rs = CELLEDCXSpec.Query(SQL);

				try {
					while (rs.next()) {
						rowCount++;
						if(eachItem.ItemState == null){
							eachItem.ItemState = "";
						}
						if(!eachItem.ItemState.equals(rs.getString("item_state"))){
							isChange = true;
						}
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					Tools tools = new Tools();
					logger.error(eachItem.toString());
					logger.error( tools.StackTrace2String(e));
				} finally {
					try {
						rs.getStatement().close();
					} catch (SQLException e) {
						// TODO Auto-generated catch block
					}
				}

				if (rowCount != 0) {
					if(isChange){
						if(eachItem.ItemStateUpdateTime.equals("")){
							SQL = "update dems_current_state t set t.item_state='"+eachItem.ItemState+"',t.ITEM_STATE_UPDATETIME=sysdate,t.updatetime=sysdate where t.fab = '"+eachItem.Fab+"' and t.item_name = '"+eachItem.ItemName+"'";
						}else{
							eachItem.ItemStateUpdateTime = eachItem.ItemStateUpdateTime.substring(0, 15);
							SQL = "update dems_current_state t set t.item_state='"+eachItem.ItemState+"',t.ITEM_STATE_UPDATETIME=to_date('"+eachItem.ItemStateUpdateTime+"','yyyymmdd hh24miss'),t.updatetime=sysdate where t.fab = '"+eachItem.Fab+"' and t.item_name = '"+eachItem.ItemName+"'";
						}
						logger.debug(SQL);
						CELLEDCXSpec.Execute(SQL);
					}
				} else {
					
					
					if(eachItem.ItemStateUpdateTime.equals("")){
						SQL = "insert into dems_current_state (fab,item_name,item_state) values('"+eachItem.Fab+"','"+eachItem.ItemName+"','"+eachItem.ItemState+"')";
					}else{
						SQL = "insert into dems_current_state (fab,item_name,item_state,ITEM_STATE_UPDATETIME) values('"+eachItem.Fab+"','"+eachItem.ItemName+"','"+eachItem.ItemState+"',to_date('"+eachItem.ItemStateUpdateTime+"','yyyymmdd hh24miss'))";
					}
					logger.debug(SQL);
					CELLEDCXSpec.Execute(SQL);
				}
			
			}
		
			
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error(msg.toString());
			logger.error("Update2DB error: " + tools.StackTrace2String(e));
		}

	}

}
