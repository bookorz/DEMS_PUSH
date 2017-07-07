package com.innolux.dems.source;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;

class FunctionAttribute{
	public String EventName = "";
	public String SubKey = "";
	public String ItemName ="";
    public String Position ="";
    public String Lentgh ="";
    public int StartIdx = 0;
    public int EndIdx = 0;
    public Hashtable<String, String> StringMapping = new Hashtable<String, String>();
}


public class BCCampsite extends Thread{
	private Hashtable<String, BC> BCList = new Hashtable<String, BC>();
	private Logger logger = Logger.getLogger(this.getClass());
	private Vector<FunctionAttribute> PushEventList = new Vector<FunctionAttribute>();
	private CallBackInterface sourceObj;
	private String Fab = "";

	public BCCampsite(CallBackInterface _sourceObj,String _Fab){
		sourceObj = _sourceObj;
		Fab = _Fab;
	}
	
	public void run() {
		GetBC(Fab);
		GetPushEventList();
		while(true){
			for(String eachBCName:BCList.keySet()){
				BC eachBC = BCList.get(eachBCName);
				Vector<String> currentSubEqpList = eachBC.BCInfo.getSubEqpList();
				eachBC.BCInfo.updateEvent();
				eachBC.BCInfo.updateWIPCount();
				eachBC.BCInfo.updateHostMode();
				eachBC.BCInfo.updateInlineMode();
				eachBC.BCInfo.updatePortInfo();
				JSONObject SendJson = null;

				JSONArray pushList = new JSONArray();
				for(String key:eachBC.BCInfo.getLineInfoKeys()){
					String value = eachBC.BCInfo.getLineInfo(key);
					
					JSONObject eachEqp = new JSONObject();
					eachEqp.put("EquipmentName", key);
					eachEqp.put("State",value);
					pushList.put(eachEqp);			
					if(pushList.length() > 50){
						SendJson = new JSONObject();
						SendJson.put("fab", Fab);
						SendJson.put("eqpStateList", pushList);
						sourceObj.onRvMsg(SendJson.toString());
						pushList = new JSONArray();
					}
			    }			
				
				
				for(int i = 0; i < PushEventList.size(); i++){
					FunctionAttribute eachEvent = PushEventList.get(i);
					for(int k = 0; k < currentSubEqpList.size();k++){
						String eachSubEQP = currentSubEqpList.get(k);
						String EventData = eachBC.BCInfo.getEventData(eachSubEQP, eachEvent.EventName, eachEvent.SubKey);
						String ItemData = "";
						if(EventData.equals("")){
							continue;
						}
						try{
							ItemData = EventData.substring(eachEvent.StartIdx, eachEvent.EndIdx);
						}catch(Exception e){
							logger.error("run EventData.substring error:" + e.getMessage()+ " ---- "+eachEvent.EventName+eachEvent.SubKey+"-"+eachSubEQP+"("+eachEvent.Position+","+eachEvent.Lentgh+"):"+eachBC.BCInfo.getEventData(eachSubEQP, eachEvent.EventName, eachEvent.SubKey));
							continue;
						}
						ItemData = ""+Integer.parseInt(ItemData, 2);
						if(!eachEvent.StringMapping.containsKey("NUM")){
							if(eachEvent.StringMapping.containsKey(ItemData)){
								ItemData = eachEvent.StringMapping.get(ItemData);
							}
						}
						
						
						JSONObject eachEqp = new JSONObject();
						eachEqp.put("EquipmentName", eachSubEQP+"."+eachEvent.ItemName);
						eachEqp.put("State",ItemData);
						pushList.put(eachEqp);		
						
						String WIPInfo = eachBC.BCInfo.getWIP(eachSubEQP);
						if(!WIPInfo.equals("")){

							eachEqp = new JSONObject();
							eachEqp.put("EquipmentName", eachSubEQP+".WIP");
							eachEqp.put("State",WIPInfo);
							pushList.put(eachEqp);			
						}
						if(pushList.length() > 50){
							SendJson = new JSONObject();
							SendJson.put("fab", Fab);
							SendJson.put("eqpStateList", pushList);
							sourceObj.onRvMsg(SendJson.toString());
							pushList = new JSONArray();
						}
						
					}
				}				
			
				SendJson = new JSONObject();
				SendJson.put("fab", Fab);
				SendJson.put("eqpStateList", pushList);
				sourceObj.onRvMsg(SendJson.toString());
				try{
					Thread.sleep(10000);
				}catch(Exception e){
					logger.error("run EventData.sleep error:" + e.getMessage());
				}
			}
		}
		
	}
	
	
	
	private void GetPushEventList(){
		
		try{
			String filePath = "PushEventList.json";
			FileReader FileStream=new FileReader(filePath);
	  	  
	  	  	BufferedReader BufferedStream=new BufferedReader(FileStream);
	       
	        String data;
	       // Vector<FunctionAttribute> FuncList = new Vector<FunctionAttribute>();
	        do{
	      	  data=BufferedStream.readLine();
	      	  if(data==null ){
	                 break;          
	      	  }
	      	  
	      	  try{
	      		
	      		JSONObject revJson = new JSONObject(data);
	      		
				String EventName = revJson.getString("EventName");
				String SubKey = revJson.getString("SubKey");
				JSONArray Items = revJson.getJSONArray("Items");
				for(int i = 0; i<Items.length();i++){
					FunctionAttribute eachFuncAttr = new FunctionAttribute();
					JSONObject eachItem = Items.getJSONObject(i);
					String FunctionName = eachItem.getString("FunctionName");
					String Position = eachItem.getString("Position");
					int startIdx = Integer.parseInt(Position);
					String Length = eachItem.getString("Length");
					int endIdx = startIdx+Integer.parseInt(Length);
					eachFuncAttr.ItemName = FunctionName;
					eachFuncAttr.Position=Position;
					eachFuncAttr.Lentgh=Length;
					eachFuncAttr.EventName = EventName;
					eachFuncAttr.SubKey=SubKey;
					eachFuncAttr.StartIdx = startIdx;
					eachFuncAttr.EndIdx = endIdx;
					JSONArray StringMappingList = eachItem.getJSONArray("StringMapping");
					for(int k=0;k<StringMappingList.length();k++){
						JSONObject eachObject = StringMappingList.getJSONObject(k);
						
							String keyStr = eachObject.getString("Key");
							String valueStr = eachObject.getString("Value");
							eachFuncAttr.StringMapping.put(keyStr, valueStr);
						
					}
					PushEventList.add(eachFuncAttr);
				}
				
				
				
	      	  }catch(Exception e1){
	      		logger.error("GetPushEventList parse json error:" + e1.getMessage());	
	      	  } 
	      	  
	        }while(true);
	        BufferedStream.close();
	        
		}catch(Exception e){
			
			logger.error("GetPushEventList read config file error :" + e.getMessage());
		}
	}
	
	private void GetBC(String fab){
		DBConnector MonDB = new DBConnector(
				"jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.11.2)(PORT = 1521)))(CONNECT_DATA =(SERVER = DEDICATED)(SERVICE_NAME = ORCL)))",
				"monuser", "monuser");
		
		ResultSet rs = null;

		String SQL = "select t.apid bc,substr(t.infodata,instr(t.infodata,'BCServerIPAddress=',1)+18,instr(t.infodata,',',instr(t.infodata,'BCServerIPAddress=',1)+18) - (instr(t.infodata,'BCServerIPAddress=',1)+18)) ip from sysinfo_last t where t.sysid = 'BC' and t.fab = '"+fab+"'";
		rs = MonDB.Query(SQL);
		try {
			while (rs.next()) {
				if (!BCList.containsKey(rs.getString("bc"))) {
					BC eachBC = new BC(rs.getString("bc"),rs.getString("ip"));
					//eachBC.PLCData.updateEvent();
					BCList.put(rs.getString("bc"), eachBC);
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			logger.error("getCFBCInfo while (rs.next()) error, exception=" + e.getMessage());

		} finally {
			try {
				rs.getStatement().close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				logger.error("getCFBCInfo rs.close error :" + e.getMessage());
			}
		}
	}



	
}


