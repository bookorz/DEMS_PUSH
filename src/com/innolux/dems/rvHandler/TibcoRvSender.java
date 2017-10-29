package com.innolux.dems.rvHandler;

import org.apache.log4j.Logger;

import com.tibco.tibrv.*;

public class TibcoRvSender {
	private Logger logger = Logger.getLogger(this.getClass());

	private TibrvTransport transport = null;
	private String mesSubject;

	// public RVHandle(RvMsgEvent rvMsgEvt){
	// this.rvMsgEvt=rvMsgEvt;
	// }

	public TibcoRvSender(String daemon, String subject, String service, String network) {
		this.mesSubject = subject;
		// open Tibrv in native implementation
		try {
			Tibrv.open(Tibrv.IMPL_NATIVE);
		} catch (TibrvException e) {
			logger.error("Failed to open Tibrv in native implementation: " + e);
			// return false;
		}

		// Create RVD transport
		try {
			transport = new TibrvRvdTransport(service, network, daemon);
		} catch (TibrvException e) {
			logger.error("Failed to create TibrvRvdTransport: " + e);
			// return false;
		}
	}

	public void sendMessage(String data) {
		TibrvMsg msg = new TibrvMsg();
		String newSubject = mesSubject;

		try {
			String sendSubject = newSubject; // +".COMM_SRV."+eqpID;
			logger.debug("Send subject=" + sendSubject);

			msg.setSendSubject(sendSubject);
			// msg.setReplySubject(localSubject);
			msg.update("DATA", data);
			logger.info(msg);
			transport.send(msg);
			transport.destroy();
		} catch (TibrvException e) {
			logger.error(e.getMessage());
		}
	}
}
