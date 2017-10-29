package com.innolux.dems;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.innolux.dems.output.*;
import com.innolux.dems.parsers.*;
import com.innolux.dems.rvHandler.TibcoRvListener;
import com.innolux.dems.source.BCCampsite;
import com.innolux.dems.source.CheckTime;
import com.innolux.dems.source.CurrentState;
import com.innolux.dems.source.PortCSTInfo;
import com.innolux.dems.source.StockerData;

public class Push_Server {

	private static CheckTime ckTimeA = new CheckTime();
	private static CheckTime ckTimeF = new CheckTime();
	private static CheckTime ckTimeC = new CheckTime();

	public static void main(String[] args) {
		// GlobleVar.InitialStates();

		TibcoRvListener FromARYMESMsg = new TibcoRvListener("172.20.8.13:8585",
				"INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.ARRAY", "8585", "",
				new EqpStateParser(new UpdateState(), "ARRAY"));
		FromARYMESMsg.start();

		TibcoRvListener FromCFMESMsg = new TibcoRvListener("172.20.8.13:8585", "INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CF",
				"8585", "", new EqpStateParser(new UpdateState(), "CF"));
		FromCFMESMsg.start();

		TibcoRvListener FromCELLMESMsg = new TibcoRvListener("172.20.8.13:8585",
				"INNOLUX.T2.PROD.OEE.MESSTATUSREPORT.CELL", "8585", "", new EqpStateParser(new UpdateState(), "CELL"));
		FromCELLMESMsg.start();

		TibcoRvListener FromMCDMsg = new TibcoRvListener("172.20.8.13:8585", "INNOLUX.T2.PROD.MES.ARRAY.COMM_SRV.>",
				"8585", "", new MCDParser(new UpdateStatus(), "ARRAY"));
		FromMCDMsg.start();

		TibcoRvListener FromBCMMSMsg = new TibcoRvListener("172.20.8.13:8585", "INNOLUX.T2.PROD.MMS.CF.>", "8585", "",
				new MMSResistParser(new UpdateResistStatus(), "CF"));
		FromBCMMSMsg.start();
		//
		//
		// Mes Array = new Mes(new UpdateState(),new UpdateStatus(),"ARRAY");
		// Array.start();
		//
		// Mes CF = new Mes(new UpdateState(),new UpdateStatus(),"CF");
		// CF.start();
		//
		// Mes CELL = new Mes(new UpdateState(),new UpdateStatus(),"CELL");
		// CELL.start();
		//
		//
//		new BCCampsite("CF");
//
//		new BCCampsite("CELL");

		UpdatePort();
		UpdateStk();
		UpdatePortCST();
	}

	private static void UpdatePortCST() {

		Timer timer = new Timer();

		long period = 60 * 1000;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				new PortCSTInfo(GlobleVar.ARRAYMesDB, "ARRAY").start();
				new PortCSTInfo(GlobleVar.CFMesDB, "CF").start();
				new PortCSTInfo(GlobleVar.CELLMesDB, "CELL").start();
			}
		};

		timer.scheduleAtFixedRate(task, new Date(), period);
	}

	private static void UpdateStk() {

		Timer timer = new Timer();

		long period = 120 * 1000;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				new StockerData(GlobleVar.ARRAYMesDB, "ARRAY").start();
			}
		};

		timer.scheduleAtFixedRate(task, new Date(), period);
	}

	private static void UpdatePort() {
		// initial
		new UpdateState().onRvMsg(new CurrentState(GlobleVar.ARRAYMesDB, "ARRAY", ckTimeA).getAllStates());
		new UpdateState().onRvMsg(new CurrentState(GlobleVar.CFMesDB, "CF", ckTimeF).getAllStates());
		new UpdateState().onRvMsg(new CurrentState(GlobleVar.CELLMesDB, "CELL", ckTimeC).getAllStates());

		Timer timer = new Timer();

		long period = 30 * 1000;
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				new CurrentState(GlobleVar.ARRAYMesDB, "ARRAY", ckTimeA).start();
				new CurrentState(GlobleVar.CFMesDB, "CF", ckTimeF).start();
				new CurrentState(GlobleVar.CELLMesDB, "CELL", ckTimeC).start();
			}
		};

		timer.scheduleAtFixedRate(task, new Date(), period);
	}

}
