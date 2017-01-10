package com.innolux.dems.parsers;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.innolux.dems.interfaces.CallBackInterface;

public class MMSResistParser implements CallBackInterface {
	private CallBackInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());
	
	public MMSResistParser(CallBackInterface _sourceObj, String _fab){
		sourceObj = _sourceObj;
		fab = _fab;
	}
	
	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		if (msg.indexOf("class=MMSResistInUseRep") == -1) {
			return;
		}
		String jsonString = parseMsg(msg);
		if (!jsonString.equals("")) {
			sourceObj.onRvMsg(jsonString);
		}
	}
	
	private String parseMsg(String orgMsg) {
		try {
			String eqpID = "";
			String target1 = "SubEqpID=\"";
			if (orgMsg.indexOf(target1) != -1) {
				
				int target1_startIdx = orgMsg.indexOf(target1) + target1.length();
				int target1_endIdx = orgMsg.indexOf("\"", target1_startIdx);
				eqpID = orgMsg.substring(target1_startIdx, target1_endIdx);
			}else{
				logger.error("parse error: SubEqpID is not exist, original Message:" + orgMsg);
				return "";
			}

			String MaterialType = "";
			String target2 = "MaterialType=\"";
			if (orgMsg.indexOf(target2) != -1) {
				
				int target2_startIdx = orgMsg.indexOf(target2) + target2.length();
				int target2_endIdx = orgMsg.indexOf("\"", target2_startIdx);
				MaterialType = orgMsg.substring(target2_startIdx, target2_endIdx);
				switch(MaterialType){
				case "0":
					MaterialType = "NoUse";
					break;
				case "1":
					MaterialType = "Mount";
					break;
				case "2":
					MaterialType = "UnMount";
					break;
				case "3":
					MaterialType = "InUse";
					break;
				default:
					MaterialType += "UnDefined";
				}
			}else{
				logger.error("parse error: MaterialType is not exist, original Message:" + orgMsg);
				return "";
			}
			String ResistID = "";
			String target3 = "ResistID=\"";
			if (orgMsg.indexOf(target3) != -1) {
				
				int target3_startIdx = orgMsg.indexOf(target3) + target3.length();
				int target3_endIdx = orgMsg.indexOf("\"", target3_startIdx);
				ResistID = orgMsg.substring(target3_startIdx, target3_endIdx);
			}else{
				logger.error("parse error: ResistID is not exist, original Message:" + orgMsg);
				return "";
			}
			
			String MainEqpID = "";
			String target4 = "eqpId=\"";
			if (orgMsg.indexOf(target4) != -1) {
				
				int target4_startIdx = orgMsg.indexOf(target4) + target4.length();
				int target4_endIdx = orgMsg.indexOf("\"", target4_startIdx);
				MainEqpID = orgMsg.substring(target4_startIdx, target4_endIdx);
			}else{
				logger.error("parse error: eqpId is not exist, original Message:" + orgMsg);
				return "";
			}

			JSONObject SendJson = new JSONObject();
			SendJson.put("fab", fab);
			SendJson.put("eqpName", eqpID);
			SendJson.put("status", MaterialType);
			SendJson.put("resistID", ResistID);
			SendJson.put("mainEqpID", MainEqpID);

			String SendStr = SendJson.toString();

			return SendStr;
		} catch (Exception e) {
			logger.error("parse error:" + e.getMessage() + " original Message:" + orgMsg);
			return "";
		}
	}

}
