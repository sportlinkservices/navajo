package com.dexels.navajo.server;

import com.dexels.navajo.compiler.BundleCreator;
import com.dexels.navajo.script.api.Access;

public class HandlerFactory {

	public static ServiceHandler createHandler(NavajoConfigInterface navajoConfig, Access access, Boolean simulationMode, BundleCreator creator) {
	    ServiceHandler sh = null;
	    if (simulationMode) { 
	        sh = new StressTestHandler();
	    } else {
	        sh = new GenericHandler(navajoConfig,creator);
	    }
		sh.setInput(access);
		return sh;
	}
	
}
