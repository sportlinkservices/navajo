/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.server.enterprise.integrity;

import java.lang.reflect.Method;

import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import navajocore.Version;

public class WorkerFactory {

	private static volatile WorkerInterface instance = null;
	private static Object semaphore = new Object();
	private static final Logger logger = LoggerFactory
			.getLogger(WorkerFactory.class);

	private static WorkerInterface getOSGiIntegrityWorker() {
		ServiceReference<WorkerInterface> sr = Version.getDefaultBundleContext().getServiceReference(WorkerInterface.class);
		if(sr==null) {
			logger.warn("No JabberWorker implementation found");
			return null;
		}
		WorkerInterface result = Version.getDefaultBundleContext().getService(sr);
		Version.getDefaultBundleContext().ungetService(sr);
		return result;
	}

	
	/**
	 * Beware, this functions should only be called from the authorized class that can enable this thread(!).
	 * 
	 * @return
	 */
	
	public static final WorkerInterface getInstance() {

		if(Version.osgiActive()) {
			return getOSGiIntegrityWorker();
		}
		if ( instance != null ) {
			return instance;
		} else {

			synchronized (semaphore) {
				
				if ( instance == null ) {
					try {
						@SuppressWarnings("unchecked")
						Class<? extends WorkerInterface> c = (Class<? extends WorkerInterface>) Class.forName("com.dexels.navajo.integrity.TribalWorker");
						WorkerInterface dummy = c.getDeclaredConstructor().newInstance();
						Method m = c.getMethod("getInstance", (Class[])null);
						instance = (WorkerInterface) m.invoke(dummy, (Object[])null);
					} catch (Exception e) {
						logger.warn("Could not instantiate integrity worker: ",e);
						instance = new DummyWorker();
					}	
				}
				
				return instance;
			}
		}
	}
}
