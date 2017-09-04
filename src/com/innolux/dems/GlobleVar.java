package com.innolux.dems;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.innolux.dems.interfaces.ItemState;
import com.innolux.dems.source.StateColor;

public class GlobleVar {
	
	private static Logger logger = Logger.getLogger(GlobleVar.class);
	public static DBConnector DEMS = new DBConnector(
			"jdbc:oracle:thin:@ (DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.8.68)(PORT = 1521))    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = T2PDEMS)    )  ) ",
			"DEMSPROD", "DEMSPROD", 6);
	
//	public static DBConnector DEMS = new DBConnector(
//	"jdbc:oracle:thin:@ (DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  ) ",
//	"l5cel", "l5cel", 6);

	public static DBConnector BC2FBML100 = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =(ADDRESS_LIST =(ADDRESS = (PROTOCOL = TCP)(HOST =172.20.36.131)(PORT = 1521)))(CONNECT_DATA =(SERVICE_NAME =ORCL)))",
			"innolux", "innoluxabc123", 3);

	public static DBConnector ARRAYMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pary)    )  )",
			"aryprod", "aryprod", 1);
	public static DBConnector CFMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcft)    )  )",
			"cftprod", "cftprod", 1);

	public static DBConnector CELLMesDB = new DBConnector(
			"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  )",
			"celprod", "celprod", 1);
	
	private static Hashtable<String,StateColor> States = new Hashtable<String,StateColor>();

	
	public static void InitialStates(){
		StateColor state = new StateColor();
		for(StateColor eachState : state.Get()){
			if(!States.containsKey(eachState.State)){
				States.put(eachState.State, eachState);
			}
		}
	}
	
	public static synchronized boolean CheckState(ItemState item){
		boolean result = false;
		
		if(States.containsKey(item.ItemState)){
			result = true;
			
		}else{
			logger.debug("State is not in the list. "+item.toString());
		}
		
		return result;
	}
}
