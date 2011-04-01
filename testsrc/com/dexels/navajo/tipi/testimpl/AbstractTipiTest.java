package com.dexels.navajo.tipi.testimpl;
import java.io.*;
import java.util.*;

import junit.framework.*;

import com.dexels.navajo.tipi.*;
import com.dexels.navajo.tipi.headless.*;
import com.dexels.navajo.tipi.internal.*;

public class AbstractTipiTest extends TestCase {

	private HeadlessTipiContext myContext = null;
	
	public HeadlessTipiContext getContext() {
		return myContext;
	}

	public AbstractTipiTest(String name) {
		super(name);
	}
	
	public void setContext(String definition, File tipiDir) throws Exception {
		setContext(definition,tipiDir,new String[]{});
	}	

	public void setContext(String definition, File tipiDir, String[] properties) throws Exception {
		myContext = (HeadlessTipiContext) HeadlessApplicationInstance.initialize(definition,tipiDir,properties,null);
			System.err.println("Resource loader set: "+tipiDir.getAbsolutePath());
	}
	
	public void injectEvent(String componentPath, String eventName) throws TipiException {
		getContext().getTipiComponentByPath(componentPath).performTipiEvent(eventName, null, true);
	}
	
	public void injectEvent(String componentPath, String eventName, Map<String,Object> eventParams, boolean sync) throws TipiException {
		getContext().getTipiComponentByPath(componentPath).performTipiEvent(eventName, eventParams, sync);
		
	}
	public void doTestTipi(String expectInfoBuffer, int waitingTime) {
		try {
//			setName("Monkey");
//			HeadlessTipiContext xxx = (HeadlessTipiContext) HeadlessApplicationInstance.initialize(definition,tipiDir);
			Thread.sleep(waitingTime);
			String xx = myContext.getInfoBuffer();
			assertEquals(xx, expectInfoBuffer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
