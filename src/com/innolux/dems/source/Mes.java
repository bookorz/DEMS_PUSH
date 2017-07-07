package com.innolux.dems.source;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;

import type.PortCST;
import type.StockerInfo;

public class Mes extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private CallBackInterface sourceObj;
	private String Fab = "";
	private DBConnector MesDB = null;

	public Mes(CallBackInterface _sourceObj, String _Fab) {
		sourceObj = _sourceObj;
		Fab = _Fab;
		switch (_Fab) {
		case "ARRAY":
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pary)    )  )",
					"aryprod", "aryprod");
			break;
		case "CF":
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcft)    )  )",
					"cftprod", "cftprod");
			break;
		case "CELL":
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  )",
					"celprod", "celprod");
			break;
		}
	}
	
	

	public void run() {

		StockerData stockerData = new StockerData(MesDB);
		PortCSTInfo portCSTInfo = new PortCSTInfo(MesDB);

		while (true) {
			JSONArray pushList = new JSONArray();
			if (Fab.equals("ARRAY")) {
				stockerData.updateStockerData();
				for (String eachStocker : stockerData.getStockerInfoKeys()) {
					StockerInfo eachStockerInfo = stockerData.getStockerInfo(eachStocker);
					if (eachStockerInfo != null) {
						JSONObject eachStk = new JSONObject();
						eachStk.put("EquipmentName", eachStockerInfo.EquipmentName);
						eachStk.put("State",
								"I:" + eachStockerInfo.transferInstk + " O:" + eachStockerInfo.transferOutstk + " E:"
										+ eachStockerInfo.Emptycst + " " + eachStockerInfo.fullratio);
						pushList.put(eachStk);
					}

				}
			}
			portCSTInfo.updatePortCSTInfo();
			for (String eachPort : portCSTInfo.getPortCSTInfoKeys()) {
				PortCST eachPortCSTInfo = portCSTInfo.getPortCSTInfo(eachPort);
				if (eachPortCSTInfo != null) {
					JSONObject eachPortInfo = new JSONObject();
					eachPortInfo.put("EquipmentName", eachPortCSTInfo.PortID);
					eachPortInfo.put("State",eachPortCSTInfo.CassetteID);
					pushList.put(eachPortInfo);
				}

			}
			
			

			JSONObject SendJson = new JSONObject();
			SendJson.put("fab", Fab);
			SendJson.put("eqpStateList", pushList);
			sourceObj.onRvMsg(SendJson.toString());
			try {
				Thread.sleep(30000);
			} catch (Exception e) {
				logger.error("run Mes.sleep error:" + e.getMessage());
			}
		}
	}
}
