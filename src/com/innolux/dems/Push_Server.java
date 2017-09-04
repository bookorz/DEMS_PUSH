package com.innolux.dems;

import com.innolux.dems.output.*;
import com.innolux.dems.parsers.*;
import com.innolux.dems.rvHandler.TibcoRvListener;
import com.innolux.dems.source.BCCampsite;
import com.innolux.dems.source.Mes;

public class Push_Server{
	public static void main(String[] args) {
		GlobleVar.InitialStates();
		
		Mes ArrayMes = new Mes(new UpdateState(),"ARRAY");
		ArrayMes.start();
		
		Mes CFMes = new Mes(new UpdateState(),"CF");
		CFMes.start();
		
		Mes CELLMes = new Mes(new UpdateState(),"CELL");
		CELLMes.start();
		
		TibcoRvListener FromMCDMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.MES.ARRAY.COMM_SRV.>","8585","",new MCDParser(new UpdateStatus(),"ARRAY"));
		FromMCDMsg.start();
		

		TibcoRvListener FromBCMMSMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.MMS.CF.>","8585","",new MMSResistParser(new UpdateResistStatus(),"CF"));
		FromBCMMSMsg.start();
		@SuppressWarnings("unused")
		BCCampsite CFBC = new BCCampsite("CF");
		
		@SuppressWarnings("unused")
		BCCampsite CELLBC = new BCCampsite("CELL");
		
		
		
	}

	
}
