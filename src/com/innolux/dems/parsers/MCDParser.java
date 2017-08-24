package com.innolux.dems.parsers;

import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.CurrentState;
import com.innolux.dems.source.Mes;
import com.innolux.dems.source.Tools;

public class MCDParser implements CallBackInterface {
	private ParserInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public MCDParser(ParserInterface _sourceObj, String _fab) {
		sourceObj = _sourceObj;
		fab = _fab;
		
		
		
	}

	@Override
	public void onRvMsg(Vector<String> msg) {
		// TODO Auto-generated method stub
		
		Vector<ItemState> result = parseMsg(msg);
		if (result.size()!=0) {
			sourceObj.onRvMsg(result);
		}
	}

	private Vector<ItemState> parseMsg(Vector<String> orgMsgList) {

		try {
			Vector<ItemState> result = new Vector<ItemState>();
		    Hashtable<String,String> tmpStateCol = new Hashtable<String,String>();
			for (String orgMsg : orgMsgList) {
				try {
					if (orgMsg.indexOf("class=EapReportAlarmInt") == -1) {
						continue;
					}
					int idxEqp = orgMsg.indexOf("eqpId=\"") + 7;
					int idxEnd = orgMsg.indexOf("\"", idxEqp + 1);
					String ID = orgMsg.substring(idxEqp, idxEnd);
					if (!(ID.indexOf("MCD") != -1 || ID.indexOf("MCL") != -1)) {
						continue;
					}

					String eqpStatus = "";
					if (orgMsg.indexOf("alarmText=\"") != -1) {
						String target2 = "alarmText=\"";

						int target2_startIdx = orgMsg.indexOf(target2) + target2.length();
						int target2_endIdx = orgMsg.indexOf("\"", target2_startIdx);
						eqpStatus = orgMsg.substring(target2_startIdx, target2_endIdx);
					} else {
						logger.error("parse error:newState is not exist, original Message:" + orgMsg);
						return result;
					}
					if(eqpStatus.indexOf("LW Measure error") != -1){
						if(!tmpStateCol.containsKey(ID)){

							tmpStateCol.put(ID, "ALERT");
						}
					}
					
				} catch (Exception e) {
					Tools tools = new Tools();
					logger.error("parse error:" + tools.StackTrace2String(e));
					return result;
				}
			}
			
		
			
			for(String eachKey:tmpStateCol.keySet()){
				ItemState eachEqp = new ItemState();
				eachEqp.Fab = fab;
				eachEqp.ItemName = eachKey;
				eachEqp.UpdateValue = "true";
				eachEqp.UpdateType = "Item_Alert";
				result.add(eachEqp);
			}
			

			return result;
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("parse error:" + tools.StackTrace2String(e));
			return new Vector<ItemState>();
		}
	}

}
