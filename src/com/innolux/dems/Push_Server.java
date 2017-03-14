package com.innolux.dems;

import com.innolux.dems.output.*;
import com.innolux.dems.parsers.*;
import com.innolux.dems.rvHandler.TibcoRvListener;
import com.innolux.dems.source.BCCampsite;
import com.innolux.dems.source.Mes;

public class Push_Server{
	public static void main(String[] args) {
		TibcoRvListener FromCFMESMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CF","8585","",new EqpStateParser(new WebService(),"CF"));
		FromCFMESMsg.start();
		TibcoRvListener FromARYMESMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.ARRAY","8585","",new EqpStateParser(new WebService(),"ARRAY"));
		FromARYMESMsg.start();
		TibcoRvListener FromCELLMESMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CELL","8585","",new EqpStateParser(new WebService(),"CELL"));
		FromCELLMESMsg.start();
		TibcoRvListener FromBCMMSMsg = new TibcoRvListener("172.20.8.13:8585","INNOLUX.T2.PROD.MMS.CF.>","8585","",new MMSResistParser(new UpdateResistStatus(),"CF"));
		FromBCMMSMsg.start();
		BCCampsite BC = new BCCampsite(new WebService(),"CF");
		BC.start();
		Mes ArrayMes = new Mes(new WebService(),"ARRAY");
		ArrayMes.start();
		
	}

	
}
