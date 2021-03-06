/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions.util;

import java.util.HashMap;
import java.util.Map;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.expression.api.FunctionDefinition;
import com.dexels.navajo.expression.api.FunctionInterface;


public class OSGiFunctionFactoryFactory  {
    private static Map<String, FunctionDefinition> cache = new HashMap<>();
	private static final Logger logger = LoggerFactory.getLogger(OSGiFunctionFactoryFactory.class);
	
	private OSGiFunctionFactoryFactory() {
		// no instances
	}
	
	public static FunctionInterface getFunctionInterface(final String functionName)  {
	    
	    if (cache.containsKey(functionName)) {
            return cache.get(functionName).getFunctionInstance();
        }
		FunctionDefinition fd = (FunctionDefinition) getComponent(functionName,
				"functionName", FunctionDefinition.class);
		if(fd==null) {
			throw NavajoFactory.getInstance().createNavajoException("No such function: "+functionName);
		}
        
		cache.put(functionName, fd);
		FunctionInterface instance = fd.getFunctionInstance();
		instance.setDefinition(fd);
		return instance;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Object getComponent( final String name, String serviceKey, Class interfaceClass)  {
	    
		BundleContext context = navajocore.Version.getDefaultBundleContext();
		try {
			ServiceReference[] refs = context.getServiceReferences(interfaceClass.getName(), "("+serviceKey+"="+name+")");
			if(refs==null) {
				logger.error("Service resolution failed: Query: "+"({}={})"+" class: {}",serviceKey,name,interfaceClass.getName());
				return null;
			}
			return context.getService(refs[0]);
			
		} catch (InvalidSyntaxException e) {
			logger.error("Error: ", e);
		}
		logger.error("Service resolution failed: No references found for query: "+"({}={})"+" class: ",serviceKey,name,interfaceClass.getName());
		return null;
	}
}
