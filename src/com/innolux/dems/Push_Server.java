package com.innolux.dems;

import com.innolux.dems.output.*;
import com.innolux.dems.parsers.*;
import com.innolux.dems.rvHandler.TibcoRvListener;
import com.innolux.dems.source.BCCampsite;
import com.innolux.dems.source.CurrentState;
import com.innolux.dems.source.Mes;

public class Push_Server{
	public static void main(String[] args) {
		DBConnector MesDB = null;
		CurrentState currentState = null;
		UpdateState Initial = new UpdateState();
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pary)    )  )",
					"aryprod", "aryprod");
			currentState = new CurrentState(MesDB,"ARRAY");
			Initial.onRvMsg(currentState.getAllStates());
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcft)    )  )",
					"cftprod", "cftprod");
			currentState = new CurrentState(MesDB,"CF");
			Initial.onRvMsg(currentState.getAllStates());
			MesDB = new DBConnector(
					"jdbc:oracle:thin:@(DESCRIPTION =    (ADDRESS_LIST =      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.1)(PORT = 1521))      (ADDRESS = (PROTOCOL = TCP)(HOST = 172.20.9.2)(PORT = 1521))      (LOAD_BALANCE = yes)    )    (CONNECT_DATA =      (SERVER = DEDICATED)      (SERVICE_NAME = t2pcel)    )  )",
					"celprod", "celprod");
			currentState = new CurrentState(MesDB,"CELL");
			Initial.onRvMsg(currentState.getAllStates());
		
		TibcoRvListener FromMCDMsg = new TibcoRvListener("172.20.8.14:8585","INNOLUX.T2.PROD.MES.ARRAY.COMM_SRV.>","8585","",new MCDParser(new UpdateStatus(),"ARRAY"));
		FromMCDMsg.start();
		
		TibcoRvListener FromCFMESMsg = new TibcoRvListener("172.20.8.14:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CF","8585","",new EqpStateParser(new UpdateState(),"CF"));
		FromCFMESMsg.start();
		TibcoRvListener FromARYMESMsg = new TibcoRvListener("172.20.8.14:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.ARRAY","8585","",new EqpStateParser(new UpdateState(),"ARRAY"));
		FromARYMESMsg.start();
		TibcoRvListener FromCELLMESMsg = new TibcoRvListener("172.20.8.14:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CELL","8585","",new EqpStateParser(new UpdateState(),"CELL"));
		FromCELLMESMsg.start();
		TibcoRvListener FromBCMMSMsg = new TibcoRvListener("172.20.8.14:8585","INNOLUX.T2.PROD.MMS.CF.>","8585","",new MMSResistParser(new UpdateResistStatus(),"CF"));
		FromBCMMSMsg.start();
		BCCampsite CFBC = new BCCampsite("CF");
		
		BCCampsite CELLBC = new BCCampsite("CELL");
		
		Mes ArrayMes = new Mes(new UpdateState(),"ARRAY");
		ArrayMes.start();
		
	}

	
}
