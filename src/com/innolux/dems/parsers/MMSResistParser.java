package com.innolux.dems.parsers;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.MMSMsg;
import com.innolux.dems.interfaces.MMSParserInterface;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.Tools;

public class MMSResistParser implements CallBackInterface {
	private MMSParserInterface sourceObj;
	private String fab = "";
	private Logger logger = Logger.getLogger(this.getClass());
	
	public MMSResistParser(MMSParserInterface _sourceObj, String _fab){
		sourceObj = _sourceObj;
		fab = _fab;
	}
	
	@Override
	public void onRvMsg(Vector<String> msgList) {
		// TODO Auto-generated method stub
		for(String msg:msgList){
			if (msg.indexOf("class=MMSResistInUseRep") == -1) {
				return;
			}
			MMSMsg result = parseMsg(msg);
			
			sourceObj.onRvMsg(result);
			
		}
	}
	
	private MMSMsg parseMsg(String orgMsg) {
		try {
			MMSMsg result = new MMSMsg();
			String eqpID = "";
			String target1 = "SubEqpID=\"";
			if (orgMsg.indexOf(target1) != -1) {
				
				int target1_startIdx = orgMsg.indexOf(target1) + target1.length();
				int target1_endIdx = orgMsg.indexOf("\"", target1_startIdx);
				eqpID = orgMsg.substring(target1_startIdx, target1_endIdx);
			}else{
				logger.error("parse error: SubEqpID is not exist, original Message:" + orgMsg);
				return result;
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
				return result;
			}
			String ResistID = "";
			String target3 = "ResistID=\"";
			if (orgMsg.indexOf(target3) != -1) {
				
				int target3_startIdx = orgMsg.indexOf(target3) + target3.length();
				int target3_endIdx = orgMsg.indexOf("\"", target3_startIdx);
				ResistID = orgMsg.substring(target3_startIdx, target3_endIdx);
			}else{
				logger.error("parse error: ResistID is not exist, original Message:" + orgMsg);
				return result;
			}
			
			String MainEqpID = "";
			String target4 = "eqpId=\"";
			if (orgMsg.indexOf(target4) != -1) {
				
				int target4_startIdx = orgMsg.indexOf(target4) + target4.length();
				int target4_endIdx = orgMsg.indexOf("\"", target4_startIdx);
				MainEqpID = orgMsg.substring(target4_startIdx, target4_endIdx);
			}else{
				logger.error("parse error: eqpId is not exist, original Message:" + orgMsg);
				return result;
			}

			
			result.Fab = fab;
			result.EqpID = eqpID;
			result.MaterialType = MaterialType;
			result.ResistID = ResistID;
			result.MainEqpID = MainEqpID;
			
			

			return result;
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("parse error:" + e.getMessage() + " original Message:" + orgMsg);
			logger.error(tools.StackTrace2String(e));
			return new MMSMsg();
		}
	}

}
