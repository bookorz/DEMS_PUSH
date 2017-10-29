package com.innolux.dems.parsers;

import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.UpdateInterface;
import com.innolux.dems.source.Tools;

public class EqpStateParser implements CallBackInterface {
	private UpdateInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public EqpStateParser(UpdateInterface _sourceObj, String _fab) {
		sourceObj = _sourceObj;
		fab = _fab;

	}

	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub

		Vector<ItemState> result = parseMsg(msg);
		// if (result.size()!=0) {
		if (result != null) {

			sourceObj.onRvMsg(result);

		}

		// }
	}

	private Vector<ItemState> parseMsg(String orgMsg) {

		try {
			Vector<ItemState> result = new Vector<ItemState>();
			// Hashtable<String,String> tmpStateCol = new
			// Hashtable<String,String>();
			// for (String orgMsg : orgMsgList) {
			try {

				if (orgMsg.indexOf("MESStatusReport_E14") == -1) {
					return null;
				}
				if (orgMsg.indexOf("alleqstateinfo=\"") == -1) {
					return null;
				}
				
				String startText = "subEqpID=\"";
				int idxBegin = orgMsg.indexOf(startText) + startText.length();
				int idxEnd = orgMsg.indexOf("\"", idxBegin + 1);
				String subEqpID = orgMsg.substring(idxBegin, idxEnd);
				
				startText = "newState=\"";
				idxBegin = orgMsg.indexOf(startText) + startText.length();
				idxEnd = orgMsg.indexOf("\"", idxBegin + 1);
				String newState = orgMsg.substring(idxBegin, idxEnd);
				
				
				ItemState state = new ItemState();
				state.Fab = fab;
				state.ItemName = subEqpID;
				state.ItemState = newState;
				result.add(state);

			    idxBegin = orgMsg.indexOf("alleqstateinfo=\"") + 16;
				idxEnd = orgMsg.indexOf("\"", idxBegin + 1);
				String stateStr = orgMsg.substring(idxBegin, idxEnd);
				String[] stateAry = stateStr.split(",");
				
				for(String eachPair : stateAry){
					String[] eachState = eachPair.split("=");
					if(eachState.length==2){
						ItemState subState = new ItemState();
						subState.Fab = fab;
						subState.ItemName = eachState[0];
						if(subState.ItemName.indexOf("00")!= -1){
							continue;
						}else if(subEqpID.equals(state.ItemName)){
							continue;
						}
						result.add(state);
					}
				}

				

			} catch (Exception e) {
				Tools tools = new Tools();
				logger.error("parse error:" + tools.StackTrace2String(e));
				return null;
			}
			// }

			return result;
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("parse error:" + tools.StackTrace2String(e));
			return null;
		}
	}

}
