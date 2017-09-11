package com.innolux.dems.parsers;

import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.GlobleVar;
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
		
		ItemState result = parseMsg(msg);
		//if (result.size()!=0) {
		if(result!=null){
			if(GlobleVar.CheckState(result)){
				Vector<ItemState> ItemStateList = new Vector<ItemState>();
				ItemStateList.add(result);
				sourceObj.onRvMsg(ItemStateList);
				
			}
		}
		//}
	}

	private ItemState parseMsg(String orgMsg) {

		try {
			ItemState result = new ItemState();
		    //Hashtable<String,String> tmpStateCol = new Hashtable<String,String>();
			//for (String orgMsg : orgMsgList) {
				try {
					
						
					if (orgMsg.indexOf("subEqpID=\"") == -1) {
						return null;
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
						return null;
					}

					
					result.Fab = fab;
					result.ItemName = ID;
					result.ItemState = eqpStatus;

					
				} catch (Exception e) {
					Tools tools = new Tools();
					logger.error("parse error:" + tools.StackTrace2String(e));
					return null;
				}
			//}
			
			
			
			
			

			return result;
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("parse error:" + tools.StackTrace2String(e));
			return new ItemState();
		}
	}

}
