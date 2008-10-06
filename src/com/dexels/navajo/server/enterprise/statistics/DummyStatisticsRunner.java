package com.dexels.navajo.server.enterprise.statistics;

import com.dexels.navajo.events.NavajoEvent;
import com.dexels.navajo.events.NavajoEventRegistry;
import com.dexels.navajo.events.NavajoListener;
import com.dexels.navajo.events.types.AuditLogEvent;
import com.dexels.navajo.mapping.AsyncMappable;
import com.dexels.navajo.server.Access;

public class DummyStatisticsRunner implements StatisticsRunnerInterface, NavajoListener {

	private static final DummyStatisticsRunner instance;
	
	static {
		instance = new DummyStatisticsRunner();
		NavajoEventRegistry.getInstance().addListener(AuditLogEvent.class, instance);
	}
	
	public final static DummyStatisticsRunner getInstance() {
		return instance;
	}
	
	public boolean isEnabled() {
		return false;
	}

	public void setEnabled(boolean b) {
		
	}

	public void addAccess(Access a, Throwable e, AsyncMappable am) {
		// Do nothing.
		a = null;
		e = null;
		am = null;
	}

	public void onNavajoEvent(NavajoEvent ne) {
		if ( ne instanceof AuditLogEvent ) {
			//System.err.println("DummyStatisticsRunner: " + ne);
		}
		ne = null;
	}

	public void addAuditLog(String subsystem, String message, String level) {
		// TODO Auto-generated method stub
		
	}

	public int getAuditLevel() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setAuditLevel(int l) {
		// TODO Auto-generated method stub
		
	}

}
