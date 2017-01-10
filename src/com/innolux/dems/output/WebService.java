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
import org.glassfish.jersey.filter.LoggingFilter;

import com.innolux.dems.interfaces.CallBackInterface;

public class WebService implements CallBackInterface {

	private Logger logger = Logger.getLogger(this.getClass());
	Client client = ClientBuilder.newClient(new ClientConfig().register(LoggingFilter.class));
	 //WebTarget webTarget = client.target("http://localhost:14518").path("Home").path("SendSignalR");
	WebTarget webTarget = client.target("http://10.56.195.221").path("Home").path("SendSignalR");
	Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

	public WebService() {

	}

	@Override
	public void onRvMsg(String msg) {
		// TODO Auto-generated method stub
		send2WebService(msg);
	}

	private void send2WebService(String jsonStr) {
		
		while (true) {
			try {
				logger.info("post json string:" + jsonStr);

				long processTime = System.currentTimeMillis();
				Response response = invocationBuilder.post(Entity.entity(jsonStr, MediaType.APPLICATION_JSON));
				logger.debug("Process time:" + (System.currentTimeMillis() - processTime) + " jsonStr:" + jsonStr);
				int statusCode = response.getStatus();
				String statusCodeDesc = response.getStatusInfo().getReasonPhrase();
				if (statusCode != 200) {
					logger.error("Send to webservice fail, response status:" + statusCode + " " + statusCodeDesc
							+ " request json string:" + jsonStr);
					Thread.sleep(3000);
				}else{
					break;
				}
				
			} catch (Exception e) {
				logger.error("send2WebService error : " + e.getMessage() + " str:" +jsonStr);
			}
			logger.error("Send to webservice fail, retry:"+jsonStr);
			// System.out.println(response.getStatus());
			// System.out.println(response.readEntity(String.class));
		}
	}

}