/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.client.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.client.ClientException;
import com.dexels.navajo.client.ClientInterface;
import com.dexels.navajo.client.NavajoClientFactory;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;

public class NavajoRemoteContext extends NavajoContext {

	private ClientInterface myClient;
	private boolean debugAll;

	private static final Logger logger = LoggerFactory
			.getLogger(NavajoRemoteContext.class);
	
	public void useCompression(boolean b) {
		myClient.setAllowCompression(b);
	}
	
	
	public void setForceGzip(boolean forceGzip) {
		myClient.setForceGzip(forceGzip);
	}
	
	
	@Override
	public void callService(String service,String tenant) throws ClientException {
		callService(service, tenant,null,null,null);
	}


	/* (non-Javadoc)
	 * @see com.dexels.navajo.client.context.ClientContext#callService(java.lang.String, com.dexels.navajo.document.Navajo)
	 */
	@Override
	public void callService(String service,String tenant,String username, String password, Navajo input)
			throws ClientException {
		if(myClient==null) {
			throw new ClientException(1,-1,"No client has been set up!");
		}
		if(input==null) {
			input = NavajoFactory.getInstance().createNavajo();
		}
		Header outHeader = input.getHeader();
		if(outHeader==null) {
			outHeader = NavajoFactory.getInstance().createHeader(input, service, username,password, -1);
			input.addHeader(outHeader);
		}
		
		if(debugAll) {
			outHeader.setHeaderAttribute("fullLog", "true");
		}
		
		long time = System.currentTimeMillis();
		Navajo n = myClient.doSimpleSend(input, service);

		logger.debug("Send complete!");
		n.getHeader().setRPCName(service);
		putNavajo(service, n);
		logger.debug("Call took: {} millis!", System.currentTimeMillis() - time);
	}


	public String getDefaultPostman(String serverName, int serverPort,String contextPath,String postmanPath) {
		StringBuilder requestBuffer = new StringBuilder();
		requestBuffer.append(serverName);
		if (serverPort > 0) {
			requestBuffer.append(":");
			requestBuffer.append(serverPort);
		}
		requestBuffer.append(contextPath);
		requestBuffer.append(postmanPath);
		return requestBuffer.toString();
	}
	public void setupClient(String server, String username, String password,String requestServerName,int requestServerPort, String requestContextPath,String postmanPath) {

		setupClient(server, username, password,requestServerName,requestServerPort,requestContextPath,postmanPath, false);
	}

	public void setupClient(String server, String username, String password) {
		setupClient(server,username,password,null,-1,"/Postman",null);
	}
	
	/**
	 * All request* params are only used when server is not supplied.
	 * @param server
	 * @param username
	 * @param password
	 * @param requestServerName
	 * @param requestServerPort
	 * @param requestContextPath
	 * @param debugAll
	 */
	public void setupClient(String server, String username, String password,String requestServerName,int requestServerPort, String requestContextPath, String postmanPath, boolean debugAll) {
		NavajoClientFactory.resetClient();
		 myClient = NavajoClientFactory.getClient();
		 if (username == null) {
			username = "demo";
		}
		myClient.setUsername(username);
		if (password == null) {
			password = "demo";
		}
		myClient.setPassword(password);
		if (server == null) {
			server = getDefaultPostman(requestServerName,requestServerPort,requestContextPath,postmanPath);
			logger.info("No server supplied. Creating default server url: {}",server);
		}
		myClient.setServerUrl(server);		
		this.debugAll = debugAll;
	}


}
