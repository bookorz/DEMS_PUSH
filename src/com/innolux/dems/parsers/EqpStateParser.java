package com.innolux.dems.parsers;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.innolux.dems.interfaces.CallBackInterface;

public class EqpStateParser implements CallBackInterface {
	private CallBackInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());

	public EqpStateParser(CallBackInterface _sourceObj, String _fab) {
		sourceObj = _sourceObj;
		fab = _fab;
	}

	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		try{
		if (msg.indexOf("subEqpID=\"") == -1) {
			return;
		}
		}catch(Exception e){
			logger.error("Try to find subEqpID error " + e.getMessage());
		}
		String jsonString = parseMsg(msg);
		if (!jsonString.equals("")) {
			sourceObj.onRvMsg(jsonString);
		}
	}

	private String parseMsg(String orgMsg) {

		try {
			int idxEqp = orgMsg.indexOf("subEqpID=\"") + 10;
			int idxEnd = orgMsg.indexOf("\"", idxEqp + 1);
			String ID = orgMsg.substring(idxEqp, idxEnd);

			String eqpStatus = "";
			if (orgMsg.indexOf("newState=\"") != -1) {
				String target2 = "newState=\"";

				int target2_startIdx = orgMsg.indexOf(target2) + target2.length();
				int target2_endIdx = orgMsg.indexOf("\"", target2_startIdx);
				eqpStatus = orgMsg.substring(target2_startIdx, target2_endIdx);
			}else{
				logger.error("parse error:newState is not exist, original Message:" + orgMsg);
				return "";
			}

			JSONObject SendJson = new JSONObject();
			SendJson.put("fab", fab);
			SendJson.put("eqpName", ID);
			SendJson.put("eqpState", eqpStatus);

			String SendStr = SendJson.toString();

			return SendStr;
		} catch (Exception e) {
			logger.error("parse error:" + e.getMessage() + " original Message:" + orgMsg);
			return "";
		}
	}

}
