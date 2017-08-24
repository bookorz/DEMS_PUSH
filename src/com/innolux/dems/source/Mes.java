package com.innolux.dems.source;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.innolux.dems.DBConnector;
import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.ParserInterface;

import type.PortCST;
import type.StockerInfo;

public class Mes extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private ParserInterface sourceObj;
	private String Fab = "";
	private DBConnector MesDB = null;

	public Mes(ParserInterface _sourceObj, String _Fab) {
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
		CurrentState eqpDate = new CurrentState(MesDB,Fab);

		while (true) {
			Vector<ItemState> pushList = new Vector<ItemState>();
			if (Fab.equals("ARRAY")) {
				stockerData.updateStockerData();
				for (String eachStocker : stockerData.getStockerInfoKeys()) {
					StockerInfo eachStockerInfo = stockerData.getStockerInfo(eachStocker);
					if (eachStockerInfo != null) {
						ItemState eachStk = new ItemState();
						eachStk.Fab = Fab;
						eachStk.ItemName = eachStockerInfo.EquipmentName;
						eachStk.ItemState = "I:" + eachStockerInfo.transferInstk + " O:" + eachStockerInfo.transferOutstk + " E:"
								+ eachStockerInfo.Emptycst + " " + eachStockerInfo.fullratio;

						pushList.add(eachStk);
					}

				}
				
				
				eqpDate.GetEQMode();
				
			}
			portCSTInfo.updatePortCSTInfo();
			for (String eachPort : portCSTInfo.getPortCSTInfoKeys()) {
				PortCST eachPortCSTInfo = portCSTInfo.getPortCSTInfo(eachPort);
				if (eachPortCSTInfo != null) {
					ItemState eachPortInfo = new ItemState();
					eachPortInfo.Fab = Fab;
					eachPortInfo.ItemName = eachPortCSTInfo.PortID;
					eachPortInfo.ItemState = eachPortCSTInfo.CassetteID;
								
					pushList.add(eachPortInfo);
				}

			}
			
			

			
			sourceObj.onRvMsg(pushList);
			try {
				Thread.sleep(30000);
			} catch (Exception e) {
				Tools tools = new Tools();
				logger.error(tools.StackTrace2String(e));
			}
		}
	}
}
