/**
 * 
 */
package com.dexels.navajo.server;

import junit.framework.Assert;
import junit.framework.TestCase;

import com.dexels.navajo.client.ClientException;
import com.dexels.navajo.client.ClientInterface;
import com.dexels.navajo.client.ConditionErrorHandler;
import com.dexels.navajo.client.NavajoClient;
import com.dexels.navajo.client.NavajoClientFactory;
import com.dexels.navajo.client.ServerAsyncListener;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;

/**
 * @author arjen
 *
 */
public class BasicNavajoServerTests extends BasicTest implements ServerAsyncListener  {


	public void testAlive() throws Exception {
		Navajo n = myClient.doSimpleSend("navajo_ping");
		Assert.assertNotNull(n.getMessage("ping"));
	}
	
	public void testGarbage() throws Exception {
		Navajo n = myClient.doSimpleSend("navajo_pingetje");
		Assert.assertNull(n.getMessage("ping"));
	}
	
	public void testAsyncService() throws Exception {
		Navajo in = myClient.doSimpleSend("tests/InitAsync");
		myClient.doServerAsyncSend(in, "tests/ProcessAsyncTest", this, "test-client", 1000);
		while (!finished) {
			synchronized (myClient) {
		
			myClient.wait();
			}
		}
		Assert.assertNotNull(received.getMessage("Finished"));
	}
	
	public void testAsyncServiceWithSuddenlyUnavailableTribalMember() throws Exception {
		Navajo in = myClient.doSimpleSend("tests/InitAsync");
		myClient.doServerAsyncSend(in, "tests/ProcessAsyncTest", this, "test-client", 1000);
		
		// Switch to other server.
		myClient.setCurrentHost("localhost:8080/NavajoServer2/Postman");
		
		while (!finished) {
			synchronized (myClient) {
			myClient.wait();
			}
		}
		Assert.assertNotNull(received.getMessage("Finished"));
	}
	
	public void testDisabledServer() throws Exception {
		// Disable server first....
		myClient.setRetryAttempts(1);
		myClient.doSimpleSend("navajo/InitDisableServer");
		
		// Call service.
		Navajo n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNotNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getProperty("ConditionErrors@0/Id"));
		Assert.assertEquals(n.getProperty("ConditionErrors@0/Id").getValue(), "4444");
		
		// Enable server again....
		myClient.doSimpleSend("navajo/InitEnableServer");
		n = myClient.doSimpleSend("tests/InitUnit");
		n.write(System.err);
		Assert.assertNull(n.getMessage("ConditionErrors"));
	}
	
	public void testDisabledServerWithStaticLoadBalancing() throws Exception {
		// Disable server first....
		myClient.setLoadBalancingMode(NavajoClient.LBMODE_STATIC_MINLOAD);
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.setRetryAttempts(2);
		myClient.doSimpleSend("navajo/InitDisableServer");
		
		// Call service.
		Navajo n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getMessage("UnitMessage"));
		
		// Enable server again....
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.doSimpleSend("navajo/InitEnableServer");
		
		n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getMessage("UnitMessage"));
	}
	
	public void testDisabledServerWithDynamicLoadBalancing() throws Exception {
		// Disable server first....
		myClient.setLoadBalancingMode(NavajoClient.LBMODE_DYNAMIC_MINLOAD);
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.setRetryAttempts(2);
		myClient.doSimpleSend("navajo/InitDisableServer");
		
		// Call service.
		Navajo n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getMessage("UnitMessage"));
		
		// Enable server again....
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.doSimpleSend("navajo/InitEnableServer");
		
		n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getMessage("UnitMessage"));
	}
	
	public void testDisabledServerWithManualLoadBalancing() throws Exception {
		// Disable server first....
		myClient.setLoadBalancingMode(NavajoClient.LBMODE_MANUAL);
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.setRetryAttempts(2);
		myClient.doSimpleSend("navajo/InitDisableServer");
		
		// Call service.
		Navajo n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNotNull(n.getMessage("ConditionErrors"));
		Assert.assertNull(n.getMessage("UnitMessage"));
		
		// Call service on different server manually.
		myClient.setCurrentHost("localhost:8080/NavajoServer2/Postman");
		n = myClient.doSimpleSend("tests/InitUnit");
	    Assert.assertNull(n.getMessage("ConditionErrors"));
	    Assert.assertNotNull(n.getMessage("UnitMessage"));
			
		// Enable server again....
		myClient.setCurrentHost("localhost:8080/NavajoServer/Postman");
		myClient.doSimpleSend("navajo/InitEnableServer");
		
		n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getMessage("UnitMessage"));
		
	}
	
	public void testShutdown() throws Exception {
		// Async start sleep service to test proper termination...
		myClient.doAsyncSend(NavajoFactory.getInstance().createNavajo(), "tests/InitSleep", null, (ConditionErrorHandler) null);
		myClient.doSimpleSend("navajo/InitShutdownServer");
		// Call service.
		Navajo n = myClient.doSimpleSend("tests/InitUnit");
		Assert.assertNotNull(n.getMessage("ConditionErrors"));
		Assert.assertNotNull(n.getProperty("ConditionErrors@0/Id"));
		Assert.assertEquals(n.getProperty("ConditionErrors@0/Id").getValue(), "4444");
	}
	
	public void handleException(Exception e) {
		// TODO Auto-generated method stub
		
	}

	public void receiveServerAsync(Navajo n, String method, String serverId,
			String clientId) {
		System.err.println("Receive server async: " + method + ", serverId = " + serverId + ", clientId = " + clientId);
		try {
			n.write(System.err);
		} catch (NavajoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		received = n;
		synchronized (myClient) {
			finished = true;
			myClient.notifyAll();
		}
		
	}

	public void serviceStarted(String id) {
		System.err.println("Service started: " + id);
	}

	public void setProgress(String id, int d) {
		System.err.println("Progress: " + id + " = " + d);
		
	}

	public static void main(String [] args) throws Exception {
		BasicNavajoServerTests b = new BasicNavajoServerTests();
		b.setUp();
		b.testAsyncService();
		
	}
}
