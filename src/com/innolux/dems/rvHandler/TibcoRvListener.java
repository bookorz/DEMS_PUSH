package com.innolux.dems.rvHandler;

import java.util.Vector;

import org.apache.log4j.Logger;

import com.innolux.dems.interfaces.CallBackInterface;
import com.innolux.dems.interfaces.ParserInterface;
import com.innolux.dems.source.Tools;
import com.tibco.tibrv.*;

public class TibcoRvListener extends Thread implements TibrvMsgCallback {
	private Logger logger = Logger.getLogger(this.getClass());
	private String daemon;
	private String subject;
	private String service;
	private String network;
	private Vector<String> msgCol = new Vector<String>();

	private CallBackInterface sourceObj;

	public TibcoRvListener(String _daemon, String _subject, String _service, String _network, CallBackInterface _sourceObj) {
		daemon = _daemon;
		subject = _subject;
		service = _service;
		network = _network;
		sourceObj = _sourceObj;
	}

	public void run() {
		try {
			Tibrv.open(Tibrv.IMPL_NATIVE);
		} catch (TibrvException e) {
			System.err.println("Failed to open Tibrv in native implementation:");
			logger.error("Failed to open Tibrv in native implementation:");
			e.printStackTrace();
			// System.exit(0);
		}

		// Create RVD transport
		TibrvTransport transport = null;
		try {
			transport = new TibrvRvdTransport(service, network, daemon);
		} catch (TibrvException e) {
			System.err.println("Failed to create TibrvRvdTransport:");
			logger.error("Failed to create TibrvRvdTransport:");
			e.printStackTrace();
			// System.exit(0);
		}

		// Create listeners for specified subjects

		// create listener using default queue
		try {
			new TibrvListener(Tibrv.defaultQueue(), this, transport, subject, null);
			System.err.println("Listening on: " + subject);
		} catch (TibrvException e) {
			System.err.println("Failed to create listener:");
			logger.error("Failed to create listener:");
			e.printStackTrace();
			// System.exit(0);
		}

		// dispatch Tibrv events
		while (true) {
			try {
				if(Tibrv.defaultQueue().getCount()==0){
					
					Thread.sleep(10000);
					
				}
				Tibrv.defaultQueue().dispatch();
				logger.info("RV queue count: " + Tibrv.defaultQueue().getCount() + " subject:" + subject);
				
				
			} catch (TibrvException e) {
				logger.error("Exception dispatching default queue: " + e);
				// System.exit(0);
			} catch (InterruptedException ie) {
				// System.exit(0);
			}
		}
	}

	public void onMsg(TibrvListener listener, TibrvMsg message) {
		String data="";
		try {
			// logger.debug("Message="+message.getField("DATA").data);
			TibrvMsgField field = message.getField("DATA");
			if (field.type == TibrvMsg.STRING) {
			    data = (String) field.data;
				logger.debug("RVListener onMsg:" + data);

				//sourceObj.onRvMsg(data);
				msgCol.add(data);
				if(Tibrv.defaultQueue().getCount()==0){
					sourceObj.onRvMsg(msgCol);
				
					msgCol = new Vector<String>();
				}
			}
		} catch (Exception e) {
			Tools tools = new Tools();
			logger.error("subject:"+subject+" msg:"+data);
			logger.error(tools.StackTrace2String(e));
		}
	}
}
