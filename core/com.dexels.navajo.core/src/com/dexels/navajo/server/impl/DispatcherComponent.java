package com.dexels.navajo.server.impl;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreator;
import com.dexels.navajo.events.NavajoEventRegistry;
import com.dexels.navajo.server.Dispatcher;
import com.dexels.navajo.server.DispatcherFactory;
import com.dexels.navajo.server.DispatcherInterface;
import com.dexels.navajo.server.NavajoConfigInterface;
import com.dexels.navajo.util.AuditLog;

public class DispatcherComponent extends Dispatcher implements
		DispatcherInterface {

	AuditLog auditLog = null;
	BundleContext myContext = null;
	NavajoEventRegistry myRegistry = null;
	
	private final static Logger logger = LoggerFactory
			.getLogger(DispatcherComponent.class);
	private ServiceRegistration<AuditLog> auditLogSr;
	
	public void activate(BundleContext context) {
		logger.info("Activate dispatcher!");
		myContext = context;
		// Register AuditLog.
		AuditLog al = new AuditLog(getNavajoConfig().getInstanceName(), myRegistry);
		auditLog = al;
		auditLogSr = myContext.registerService(AuditLog.class, al, null);
		new DispatcherFactory(this);
	}
	public void deactivate() {
		logger.info("Deactivate dispatcher!");
		myContext = null;
		auditLogSr.unregister();
	}

	public void setNavajoConfig(NavajoConfigInterface nci) {
		logger.info("setNavajoConfig dispatcher!");
		super.setNavajoConfig(nci);
	}
	public void clearNavajoConfig(NavajoConfigInterface nci) {
		logger.info("clearNavajoConfig dispatcher!");
		super.clearNavajoConfig(nci);
	}

	public void setBundleCreator(BundleCreator nci) {
		logger.info("setBundleCreator dispatcher!");
		super.setBundleCreator(nci);
	}
	public void clearBundleCreator(BundleCreator nci) {
		logger.info("clearBundleCreater dispatcher!");
		super.clearBundleCreator(nci);
	}

	public void setEventRegistry(NavajoEventRegistry ner) {
		myRegistry = ner;
	}
	
	public void clearEventRegistry(NavajoEventRegistry ner) {
		myRegistry = null;
	}
}
