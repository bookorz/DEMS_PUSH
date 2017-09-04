package com.innolux.dems.source;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.GlobleVar;
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
			MesDB = GlobleVar.ARRAYMesDB;
			break;
		case "CF":
			MesDB = GlobleVar.CFMesDB;
			break;
		case "CELL":
			MesDB = GlobleVar.CELLMesDB;
			break;
		}
	}
	
	

	public void run() {

		StockerData stockerData = new StockerData(MesDB);
		PortCSTInfo portCSTInfo = new PortCSTInfo(MesDB);
		CurrentState eqpDate = new CurrentState(MesDB,Fab);

		while (true) {
			
			for(ItemState eachstate:eqpDate.getAllStates()){
				sourceObj.onRvMsg(eachstate);
			}
			
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
						sourceObj.onRvMsg(eachStk);
						
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
					sourceObj.onRvMsg(eachPortInfo);
					
				}

			}
			
			
			

			
			
			try {
				Thread.sleep(60000);
			} catch (Exception e) {
				Tools tools = new Tools();
				logger.error(tools.StackTrace2String(e));
			}
		}
	}
}
