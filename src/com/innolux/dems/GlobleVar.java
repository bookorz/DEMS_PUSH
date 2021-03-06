package com.innolux.dems;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.source.StateColor;

public class GlobleVar {

	private static Logger logger = Logger.getLogger(GlobleVar.class);
	public static DBConnector DEMS = new DBConnector(
			"jdbc:oracle:thin:@ (DESCRIPTION = (ADDRESS_LIST = (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.8.68)(PORT = 1521)) ) (CONNECT_DATA = (SERVER = DEDICATED) (SERVICE_NAME = T2PDEMS) ) ) ",
			"DEMSPROD", "DEMSPROD", 3);

//	public static DBConnector DEMS = new DBConnector(
//			"jdbc:oracle:thin:@ (DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  ) ",
//			"l5cel", "l5cel", 3);

	public static DBConnector BC2FBML100 = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST =172.20.36.131)(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME =ORCL)))",
			"innolux", "innoluxabc123", 0);

	public static DBConnector ARRAYMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pary)    )  )",
			"aryprod", "aryprod", 0);
	public static DBConnector CFMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcft)    )  )",
			"cftprod", "cftprod", 0);

	public static DBConnector CELLMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  )",
			"celprod", "celprod", 0);

	private static Hashtable<String, StateColor> States = new Hashtable<String, StateColor>();

	private static Hashtable<String, ItemState> Status = new Hashtable<String, ItemState>();

	public static void InitialStates() {
		StateColor state = new StateColor();
		for (StateColor eachState : state.Get()) {
			if (!States.containsKey(eachState.State)) {
				States.put(eachState.State, eachState);
			}
		}
	}

	public static ItemState GetStatus(ItemState item) {
		ItemState result = new ItemState();
		String key = item.Fab + item.ItemName;
		synchronized (Status) {
			if (Status.containsKey(key)) {
				ItemState tmp = Status.get(key);
				result.Fab = tmp.Fab;
				result.ItemMode = tmp.ItemMode;
				result.ItemName = tmp.ItemName;
				result.ItemState = tmp.ItemState;
				result.ItemStateUpdateTime = tmp.ItemStateUpdateTime;
				result.ItemType = tmp.ItemType;
				result.UpdateType = tmp.UpdateType;
				result.UpdateValue = tmp.UpdateValue;

			}
		}
		return result;
	}

	public static void SetStatus(ItemState item) {
		String key = item.Fab + item.ItemName;
		synchronized (Status) {
			if (Status.containsKey(key)) {
				Status.replace(key, item);
			} else {
				Status.put(key, item);
			}
		}

	}

	public static synchronized boolean CheckState(ItemState item) {
		boolean result = false;

		if (States.containsKey(item.ItemState)) {
			result = true;

		} else {
			logger.debug("State is not in the list. " + item.toString());
		}

		return result;
	}
}
