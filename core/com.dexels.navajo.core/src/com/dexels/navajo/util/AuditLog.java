/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on 26-jan-2006
 *
 * @author Dexels developer
 * @version $Id$
 */
package com.dexels.navajo.util;

import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.events.NavajoEventRegistry;
import com.dexels.navajo.events.types.AuditLogEvent;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.Mappable;
import com.dexels.navajo.script.api.MappableException;
import com.dexels.navajo.script.api.UserException;

/**
 * @author Arjen
 *
 */
public final class AuditLog implements Mappable {

	public String message;
	public String level;
	
	public static final String AUDIT_MESSAGE_TASK_SCHEDULER = "Task Scheduler";
	public static final String AUDIT_MESSAGE_ASYNC_RUNNER = "Async Runner";
	public static final String AUDIT_MESSAGE_STAT_RUNNER = "Statistics Runner";
	public static final String AUDIT_MESSAGE_INTEGRITY_WORKER = "Integrity Worker";
	public static final String AUDIT_MESSAGE_LOCK_MANAGER = "Locking Manager";
	public static final String AUDIT_MESSAGE_DISPATCHER = "Dispatcher";
	public static final String AUDIT_MESSAGE_CLOCK = "Clock";
	public static final String AUDIT_MESSAGE_WORKFLOW = "Workflow";
	public static final String AUDIT_MESSAGE_TRIBEMANAGER = "TribeManager";
	public static final String AUDIT_MESSAGE_SHAREDSTORE = "SharedStore";
	public static final String AUDIT_MESSAGE_MONITOR = "Monitoring Agent";
	public static final String AUDIT_MESSAGE_QUEUEDADAPTERS = "Queued Adapters";
	public static final String AUDIT_MESSAGE_USER = "User Log";
	public static final String AUDIT_MESSAGE_SCRIPTCOMPILER = "Script Compiler";
	public static final String AUDIT_MESSAGE_AUTHORISATION = "Authorisation Repository";

	
	private static final Logger logger = LoggerFactory
			.getLogger(AuditLog.class);
	private String accessId;

	private static final void logToSlf(String message, String subsystem, Level l) {
		Logger instanceLog = LoggerFactory.getLogger("NavajoLog");
		if(Level.INFO.equals(l)) {
			instanceLog.info(message, subsystem);
			return;
		}
		if(Level.SEVERE.equals(l)) {
			instanceLog.error(message, subsystem);
			return;
		}
		if(Level.WARNING.equals(l)) {
			instanceLog.warn(message, subsystem);
			return;
		}
		instanceLog.info(message, subsystem);
	}
	

	private static final void logToSlf(String message, Throwable exception, Level l) {
		Logger instanceLog = LoggerFactory.getLogger("NavajoLog");
		if(Level.INFO.equals(l)) {
			instanceLog.info(message,exception);
			return;
		}
		if(Level.SEVERE.equals(l)) {
			instanceLog.error(message, exception);
			return;
		}
		if(Level.WARNING.equals(l)) {
			instanceLog.warn(message, exception);
			return;
		}
		instanceLog.info(message, exception);
	}

	public static final void log(String message, final Throwable exception, Level level) {
		logToSlf(message,exception,  level);
	}
	
	
	public static final void log(final String subsystem, final String message, Level level) {
		logToSlf(message, subsystem, level);
		publishEvent(new AuditLogEvent(subsystem.toUpperCase(), message, level));
	}
	
	public static final void log(final String subsystem, final String message) {
		logToSlf(message, subsystem, Level.INFO);

		publishEvent(new AuditLogEvent(subsystem.toUpperCase(), message, Level.INFO));
	}
	
	
	public static final void log(final String subsystem, final String message,  Level level, String accessId) {
		
		logToSlf(message, subsystem, level);
		AuditLogEvent ale = new AuditLogEvent(subsystem.toUpperCase(), message, level);
		ale.setAccessId(accessId);
		publishEvent(ale);
	}
	
	public static final void log( String subsystem,  final String message,Throwable t, Level level, String accessId) {
		
		logToSlf(message,t, level);
		AuditLogEvent ale = new AuditLogEvent(subsystem.toUpperCase(), message, level);
		ale.setAccessId(accessId);
		publishEvent(ale);
	}

	
	private static void publishEvent(AuditLogEvent ale) {
		NavajoEventRegistry ner = NavajoEventRegistry.getInstance();
		if(ner!=null) {
			ner.publishEvent(ale);
		} else {
			logger.warn("Can not publish event: No registered NavajoEventRegistry");
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Level getLevel() {
		if ( level == null) {
			return Level.INFO;
		} else if ( level.equals(Level.INFO.getLocalizedName() )) {
			return Level.INFO;
		} else if ( level.equals(Level.SEVERE.getLocalizedName() )) {
			return Level.SEVERE;
		} else if ( level.equals(Level.WARNING.getLocalizedName() )) {
			return Level.WARNING;
		} else {
			return Level.INFO;
		}
	}

	public void setLevel(String level) {
		this.level = level;
	}

	@Override
	public void kill() {
		
	}

	@Override
	public void load(Access access) throws MappableException, UserException {
		if ( access != null ) {
			accessId = access.accessID;
		}
	}

	@Override
	public void store() throws MappableException, UserException {
		AuditLogEvent ale = new AuditLogEvent(AUDIT_MESSAGE_USER, getMessage(), getLevel());
		ale.setAccessId(accessId);
		publishEvent(ale);
	}
}
