/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.resource.binarystore;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BinaryStoreFactory {
	
	private static BinaryStoreFactory instance = null;
	
	private final Map<String,BinaryStore> defaultStores = new HashMap<>();
	private final Map<String,Map<String,BinaryStore>> tenantStores = new HashMap<>();
	
	private final static Logger logger = LoggerFactory.getLogger(BinaryStoreFactory.class);
	
	public void activate() {
		instance = this;
	}

	public void deactivate() {
		instance = null;
	}
	
	public static BinaryStoreFactory getInstance() {
		return instance;
	}

	public void addBinaryStore(BinaryStore store, Map<String,Object> settings) {
		String name = (String) settings.get("name");
		String tenant = (String) settings.get("instance");
		logger.info("Adding Openstack store name: {} for tenant {}",name,tenant);
		logger.info("");
		if(tenant==null) {
			defaultStores.put(name, store);
			return;
		}
		Map<String,BinaryStore> myTenantStore = tenantStores.get(tenant);
		if(myTenantStore==null) {
			myTenantStore = new HashMap<>();
			tenantStores.put(tenant, myTenantStore);
		}
		myTenantStore.put(name, store);
	}
	
	public void removeBinaryStore(BinaryStore store, Map<String,Object> settings) {
		String name = (String) settings.get("name");
		String tenant = (String) settings.get("tenant");
		if(tenant==null) {
			defaultStores.remove(name);
			return;
		}
		Map<String,BinaryStore> myTenantStore = tenantStores.get(tenant);
		if(myTenantStore==null) {
			myTenantStore = new HashMap<>();
			tenantStores.put(tenant, myTenantStore);
		}
		myTenantStore.remove(name);
	}

	public BinaryStore getBinaryStore(String name, String tenant) {
		Map<String,BinaryStore> myTenantStore = tenantStores.get(tenant);
		if(myTenantStore!=null) {
			BinaryStore os = myTenantStore.get(name);
			if(os!=null) {
				return os;
			}
		}
		return defaultStores.get(name);
	}

	
	
}

