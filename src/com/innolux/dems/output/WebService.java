package com.innolux.dems.output;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import com.innolux.dems.interfaces.CallBackInterface;

public class WebService implements CallBackInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	Invocation.Builder invocationBuilder = null;

	public WebService() {
		// ClientConfig cfg = new ClientConfig().register(LoggingFilter.class);
		ClientConfig cfg = new ClientConfig();
		cfg.property(ClientProperties.CONNECT_TIMEOUT, 10000);
		cfg.property(ClientProperties.READ_TIMEOUT, 10000);
		Client client = ClientBuilder.newClient(cfg);
		// WebTarget webTarget =
		// client.target("http://localhost:14518").path("Home").path("SendSignalR");
		WebTarget webTarget = client.target("http://10.56.195.221").path("Home").path("SendSignalR");
		invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
	}

	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		send2WebService(msg);
	}

	private void send2WebService(String jsonStr) {
		int retryCount = 0;
		while (true) {
			try {
				logger.info("post json string:" + jsonStr);

				long processTime = System.currentTimeMillis();

				Response response = invocationBuilder.post(Entity.entity(jsonStr, MediaType.APPLICATION_JSON));
				logger.debug("Process time:" + (System.currentTimeMillis() - processTime));
				int statusCode = response.getStatus();
				String statusCodeDesc = response.getStatusInfo().getReasonPhrase();
				if (statusCode != 200) {
					logger.error("Send to webservice fail, response status:" + statusCode + " " + statusCodeDesc);
					if (statusCode == 500) {
						logger.error("Send to webservice fail, response status:" + statusCode + " " + statusCodeDesc);
					}
					Thread.sleep(3000);
				} else {
					break;
				}
				if (retryCount >= 3) {
					break;
				}
				retryCount++;
			} catch (Exception e) {
				logger.error("send2WebService error : " + e.getMessage());
			}
			logger.error("Send to webservice fail, retry");
			// System.out.println(response.getStatus());
			// System.out.println(response.readEntity(String.class));
		}
	}

}
