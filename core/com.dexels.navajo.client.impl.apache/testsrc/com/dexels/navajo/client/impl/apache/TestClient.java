package com.dexels.navajo.client.impl.apache;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.config.runtime.TestConfig;
import com.dexels.navajo.client.ClientException;
import com.dexels.navajo.client.NavajoClient;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;

public class TestClient {

	
	private static final Logger logger = LoggerFactory.getLogger(TestClient.class);
	
	public TestClient() {
	}

	@Test(timeout=5000)
	public void testClient() throws ClientException {
		NavajoClient cl = new ApacheNavajoClientImpl();
		cl.setAllowCompression(true);
		cl.setForceGzip(true);
		cl.useBasicAuthentication(true);
		cl.setServerUrls(new String[] {TestConfig.NAVAJO_TEST_SERVER.getValue()});
		cl.setUsername(TestConfig.NAVAJO_TEST_USER.getValue());
		cl.setPassword(TestConfig.NAVAJO_TEST_PASS.getValue());
		Navajo nc = NavajoFactory.getInstance().createNavajo();
		Navajo result = cl.doSimpleSend(nc, "single");
		result.write(System.err);
		Assert.assertTrue(result.getErrorDescription()==null);
	}
	
	@Test (timeout=20000)
	public void testClientBig() throws ClientException {
		NavajoClient cl = new ApacheNavajoClientImpl();
		cl.setAllowCompression(true);
		cl.setForceGzip(true);
		cl.setServerUrls(new String[] {TestConfig.NAVAJO_TEST_SERVER.getValue()});
		cl.setUsername(TestConfig.NAVAJO_TEST_USER.getValue());
		cl.setPassword(TestConfig.NAVAJO_TEST_PASS.getValue());
		cl.useBasicAuthentication(true);
		Navajo nc = NavajoFactory.getInstance().createNavajo();
		Navajo result = cl.doSimpleSend(nc, "club/InitUpdateClub");
		result.getMessage("Club").getProperty("ClubIdentifier").setAnyValue("BBFX31R");
		Navajo result2 = cl.doSimpleSend(result, "club/ProcessQueryClub");
		result2.write(System.err);
	}

}
