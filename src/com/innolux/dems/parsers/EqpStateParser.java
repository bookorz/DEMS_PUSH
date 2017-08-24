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

public class EqpStateParser implements CallBackInterface {
	private ParserInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public EqpStateParser(ParserInterface _sourceObj, String _fab) {
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
						return result;
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
					Tools tools = new Tools();
					logger.error("parse error:" + tools.StackTrace2String(e));
					return result;
				}
			}
			
			
			
			for(String eachKey:tmpStateCol.keySet()){
				ItemState eachEqp = new ItemState();
				String newStatus = tmpStateCol.get(eachKey);
				eachEqp.Fab = fab;
				eachEqp.ItemName = eachKey;
				eachEqp.ItemState = newStatus;
			
				
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
