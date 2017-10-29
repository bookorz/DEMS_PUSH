package com.innolux.dems.source;

import java.util.Vector;

import org.apache.log4j.Logger;
import com.innolux.dems.DBConnector;
import com.innolux.dems.GlobleVar;
import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.interfaces.UpdateInterface;

import type.PortCST;
import type.StockerInfo;

public class Mes extends Thread {
	private Logger logger = Logger.getLogger(this.getClass());
	private UpdateInterface State;
	private UpdateInterface Status;
	private String Fab = "";
	private DBConnector MesDB = null;

	public Mes(UpdateInterface _State, UpdateInterface _Status, String _Fab) {
		Status = _Status;
		State = _State;
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
	    
		try {
			//StockerData stockerData = new StockerData(MesDB);
			//PortCSTInfo portCSTInfo = new PortCSTInfo(MesDB);
			//CurrentState eqpDate = new CurrentState(MesDB, Fab);
			//State.onRvMsg(eqpDate.getAllStates(LastEQPUpdateTime,LastSubEQPUpdateTime,LastChamberUpdateTime,LastPortUpdateTime)); // initial
			while (true) {
				try {
					Vector<ItemState> ItemStateList = new Vector<ItemState>();

					//State.onRvMsg(eqpDate.GetCurrentPortState(LastPortUpdateTime));

					if (Fab.equals("ARRAY")) {
//						stockerData.updateStockerData();
//						for (String eachStocker : stockerData.getStockerInfoKeys()) {
//							StockerInfo eachStockerInfo = stockerData.getStockerInfo(eachStocker);
//							if (eachStockerInfo != null) {
//								ItemState eachStk = new ItemState();
//								eachStk.Fab = Fab;
//								eachStk.ItemName = eachStockerInfo.EquipmentName;
//								eachStk.ItemState = "I:" + eachStockerInfo.transferInstk + " O:"
//										+ eachStockerInfo.transferOutstk + " E:" + eachStockerInfo.Emptycst + " "
//										+ eachStockerInfo.fullratio;
//								ItemStateList.add(eachStk);
//
//							}
//
//						}

						//Status.onRvMsg(eqpDate.GetEQMode());

					}
//					portCSTInfo.updatePortCSTInfo();
//					for (String eachPort : portCSTInfo.getPortCSTInfoKeys()) {
//						PortCST eachPortCSTInfo = portCSTInfo.getPortCSTInfo(eachPort);
//						if (eachPortCSTInfo != null) {
//							ItemState eachPortInfo = new ItemState();
//							eachPortInfo.Fab = Fab;
//							eachPortInfo.ItemName = eachPortCSTInfo.PortID;
//							eachPortInfo.ItemState = eachPortCSTInfo.CassetteID;
//							ItemStateList.add(eachPortInfo);
//
//						}
//
//					}

					State.onRvMsg(ItemStateList);
					
					Thread.sleep(30000);
				} catch (Exception e) {
					Tools tools = new Tools();
					logger.error(tools.StackTrace2String(e));
				}
			}
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error(tools.StackTrace2String(e));
		}

	}
}
