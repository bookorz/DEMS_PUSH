package com.innolux.dems.parsers;

import org.apache.log4j.Logger;
import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.ParserInterface;
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
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		
		ItemState result = parseMsg(msg);
		if (result!=null) {
			sourceObj.onRvMsg(result);
		}
	}

	private ItemState parseMsg(String orgMsg) {

		try {
			ItemState result = new ItemState();
		    
			
				try {
					if (orgMsg.indexOf("class=EapReportAlarmInt") == -1) {
						return null;
					}
					int idxEqp = orgMsg.indexOf("eqpId=\"") + 7;
					int idxEnd = orgMsg.indexOf("\"", idxEqp + 1);
					String ID = orgMsg.substring(idxEqp, idxEnd);
					if (!(ID.indexOf("MCD") != -1 || ID.indexOf("MCL") != -1)) {
						return null;
					}

					String eqpStatus = "";
					if (orgMsg.indexOf("alarmText=\"") != -1) {
						String target2 = "alarmText=\"";

						int target2_startIdx = orgMsg.indexOf(target2) + target2.length();
						int target2_endIdx = orgMsg.indexOf("\"", target2_startIdx);
						eqpStatus = orgMsg.substring(target2_startIdx, target2_endIdx);
					} else {
						logger.error("parse error:newState is not exist, original Message:" + orgMsg);
						return null;
					}
					if(eqpStatus.indexOf("LW Measure error") != -1){
						result.Fab = fab;
						result.ItemName = ID;
						result.UpdateValue = "true";
						result.UpdateType = "Item_Alert";
					}
					
				} catch (Exception e) {
					Tools tools = new Tools();
					logger.error("parse error:" + tools.StackTrace2String(e));
					return null;
				}

			return result;
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("parse error:" + tools.StackTrace2String(e));
			return null;
		}
	}

}
