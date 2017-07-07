package com.innolux.dems.parsers;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.CurrentState;
import com.innolux.dems.source.Mes;

public class EqpStateParser implements ParserInterface {
	private CallBackInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public EqpStateParser(CallBackInterface _sourceObj, String _fab) {
		sourceObj = _sourceObj;
		fab = _fab;
		
		
		
	}

	@Override
	public void onRvMsg(Vector<String> msg) {
		// TODO Auto-generated method stub
		
		String jsonString = parseMsg(msg);
		if (!jsonString.equals("")) {
			sourceObj.onRvMsg(jsonString);
		}
	}

	private String parseMsg(Vector<String> orgMsgList) {

		try {
			
		    Hashtable<String,String> tmpStateCol = new Hashtable<String,String>();
			for (String orgMsg : orgMsgList) {
				try {
					if (orgMsg.indexOf("subEqpID=\"") == -1) {
						continue;
					}
					int idxEqp = orgMsg.indexOf("subEqpID=\"") + 10;
					int idxEnd = orgMsg.indexOf("\"", idxEqp + 1);
					String ID = orgMsg.substring(idxEqp, idxEnd);

					String eqpStatus = "";
					if (orgMsg.indexOf("newState=\"") != -1) {
						String target2 = "newState=\"";

						int target2_startIdx = orgMsg.indexOf(target2) + target2.length();
						int target2_endIdx = orgMsg.indexOf("\"", target2_startIdx);
						eqpStatus = orgMsg.substring(target2_startIdx, target2_endIdx);
					} else {
						logger.error("parse error:newState is not exist, original Message:" + orgMsg);
						return "";
					}

					if(tmpStateCol.containsKey(ID)){
						String lastState = tmpStateCol.get(ID);
						if(!lastState.equals(eqpStatus)){
							tmpStateCol.remove(ID);
							tmpStateCol.put(ID, eqpStatus);
						}
					}else{
						tmpStateCol.put(ID, eqpStatus);
					}

					
				} catch (Exception e) {
					logger.error("parse error:" + e.getMessage());
					return "";
				}
			}
			
			JSONArray pushList = new JSONArray();
			
			for(String eachKey:tmpStateCol.keySet()){
				JSONObject eachEqp = new JSONObject();
				String newStatus = tmpStateCol.get(eachKey);
				eachEqp.put("EquipmentName", eachKey);
				eachEqp.put("State", newStatus);
				pushList.put(eachEqp);
			}
			JSONObject SendJson = new JSONObject();
			SendJson.put("fab", fab);
			SendJson.put("eqpStateList", pushList);

			String SendStr = SendJson.toString();

			return SendStr;
		} catch (Exception e) {
			logger.error("parse error:" + e.getMessage());
			return "";
		}
	}

}
