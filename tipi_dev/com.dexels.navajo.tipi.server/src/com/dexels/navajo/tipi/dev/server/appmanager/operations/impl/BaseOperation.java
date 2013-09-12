package com.dexels.navajo.tipi.dev.server.appmanager.operations.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.dexels.navajo.tipi.dev.server.appmanager.AppStoreOperation;
import com.dexels.navajo.tipi.dev.server.appmanager.ApplicationManager;
import com.dexels.navajo.tipi.dev.server.appmanager.ApplicationStatus;

public abstract class BaseOperation implements AppStoreOperation {


	protected final Map<String,ApplicationStatus> applications = new HashMap<String, ApplicationStatus>();
	protected ApplicationManager applicationManager = null;

	
	public void setApplicationManager(ApplicationManager am) {
		this.applicationManager = am;
	}

	public void clearApplicationManager(ApplicationManager am) {
		this.applicationManager = null;
	}

	public void addApplicationStatus(ApplicationStatus a) {
		applications.put(a.getApplicationName(), a);
	}
	
	public void removeApplicationStatus(ApplicationStatus a) {
		applications.remove(a.getApplicationName());
	}

	protected Set<String> listApplications() {
		 return applicationManager.listApplications();
	}
}
