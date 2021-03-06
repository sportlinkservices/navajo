/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.sharedstore.map;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.dexels.navajo.server.enterprise.tribe.TribeManagerFactory;
import com.dexels.navajo.sharedstore.SharedStoreFactory;
import com.dexels.navajo.sharedstore.SharedStoreInterface;
import com.dexels.navajo.sharedstore.SharedStoreLock;

@SuppressWarnings({"rawtypes","unchecked"})

public class SharedTribalMap<K,V> extends HashMap {

	public int size;
	public String id;
	
	/**
	 * Statistics.
	 */
	protected long insertCount;
	protected long deleteCount;
	protected long getCount;
	
	private static final long serialVersionUID = -1122073018927967102L;
	
	private static volatile Map<String,SharedTribalMap> registeredMaps = new HashMap<>();
	private boolean tribalSafe = false;
	
	private static Object semaphore = new String();
	private Object semaphoreLocal = new String();
	
	protected SharedTribalMap() {
	}
	
	/**
	 * Create a SharedTribalMap, note that the map is only shared after calling the registerMap method.
	 * 
	 * @param id
	 */
	public SharedTribalMap(String id) {
		super();
		this.id = id;
	}
	

	public static Collection<SharedTribalMap> getAllTribalMaps() {
		return registeredMaps.values();
	}
	
	/**
	 * Registers a SharedTribalMap.
	 * 
	 * @param stm
	 * @param overwrite if set to true, an existing SharedTribalMap is NOT overwritten
	 * @return the SharedTribalMap that is registered.
	 */
	public static SharedTribalMap registerMap(SharedTribalMap stm, boolean overwrite) {
		synchronized (semaphore) {
			if ( registeredMaps.get(stm.getId()) == null || overwrite ) {
				registerMapLocal(stm);
				TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.CREATEMAP, stm.getId());
				TribeManagerFactory.getInstance().broadcast(tms);
				return stm;
			} else {
				return registeredMaps.get(stm.getId());
			}
		}
	}
	
	protected static void registerMapLocal(SharedTribalMap stm) {
		synchronized (semaphore) {
			if ( registeredMaps.get(stm.getId()) != null ) {
				SharedTribalMap existing = registeredMaps.get(stm.getId());
				existing.clearLocal();
				existing.putAll(stm);
			} else {
				registeredMaps.put(stm.getId(), stm);
			}
		}
	}
	
	/**
	 * Deregisters a shared tribal map.
	 * 
	 * @param id
	 */
	public static void deregisterMap(String id) {
		synchronized (semaphore) {
			deregisterMapLocal(id);
			TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.DELETEMAP, id);
			TribeManagerFactory.getInstance().broadcast(tms);
		}
	}
	
	protected static void deregisterMapLocal(String id) {
		synchronized (semaphore) {
			if ( registeredMaps.get(id) != null ) {
				registeredMaps.get(id).clearLocal();
				registeredMaps.remove(id);
			}
		}
	}
	/**
	 * Deregisters a shared tribal map.
	 * 
	 * @param stm
	 */
	static void deregisterMap(SharedTribalMap stm) {
		synchronized (semaphore) {
			deregisterMapLocal(stm.getId());
			TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.DELETEMAP, stm.getId());
			TribeManagerFactory.getInstance().broadcast(tms);
		}
	}
	
	static SharedTribalMap getMap(String id) {
		return registeredMaps.get(id);
	}
	
	private final String getLockName(Object key) {
		if ( key != null ) {
			return id + "-" + key.hashCode();
		} else {
			return id;
		}
	}
	
	@Override
	public Object put(Object key, Object value) {
		
		SharedStoreLock ssl = null;
		
		if ( tribalSafe ) {
			ssl = SharedStoreFactory.getInstance().lock("", getLockName(key) , SharedStoreInterface.READ_WRITE_LOCK, true);
		}
		
		try {
			Object o = putLocal(key, value);

			SharedTribalElement ste = new SharedTribalElement(getId(), key, value);
			TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.PUT, ste);
			TribeManagerFactory.getInstance().broadcast(tms);

			return o;
			
		} finally {
			if ( tribalSafe && ssl != null ) {
				SharedStoreFactory.getInstance().release(ssl);
			}
		}
	}
	

	@Override
	public boolean containsKey(Object key) {
		SharedStoreLock ssl = null;
		if ( tribalSafe ) {
			ssl = SharedStoreFactory.getInstance().lock("", getLockName(key) , SharedStoreInterface.READ_WRITE_LOCK, true);
		}
		try {
			return super.containsKey(key);
		} finally {
			if ( tribalSafe && ssl != null ) {
				SharedStoreFactory.getInstance().release(ssl);
			}
		}
	}
	
	@Override
	public Object get(Object key) {

		SharedStoreLock ssl = null;
		if ( tribalSafe ) {
			ssl = SharedStoreFactory.getInstance().lock("", getLockName(key) , SharedStoreInterface.READ_WRITE_LOCK, true);
		}
		try {
			getCount++;
			return super.get(key);
		} finally {
			if ( tribalSafe && ssl != null ) {
				SharedStoreFactory.getInstance().release(ssl);
			}
		}
	}
	
	@Override
	public void clear() {
		clearLocal();
		SharedTribalElement ste = new SharedTribalElement(getId(), null, null);
		TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.CLEAR, ste);
		TribeManagerFactory.getInstance().broadcast(tms);
	}
	
	protected void clearLocal() {
		synchronized (semaphoreLocal) {
			super.clear();
		}
	}
	
	protected Object putLocal(Object key, Object value) {
		synchronized (semaphoreLocal) {
			Object o = super.put(key, value);
			insertCount++;
			return o;
		}
	}
	
	@Override
	public Object remove(Object key) {

		SharedStoreLock ssl = null;
		if ( tribalSafe ) {
			ssl = SharedStoreFactory.getInstance().lock("", getLockName(key) , SharedStoreInterface.READ_WRITE_LOCK, true);
		}

		try {
			Object o = removeLocal(key);
			SharedTribalElement ste = new SharedTribalElement(getId(), key, null);
			TribalMapSignal tms = new TribalMapSignal(TribalMapSignal.REMOVE, ste);
			TribeManagerFactory.getInstance().broadcast(tms);
			
			return o;
		} finally {
			if ( tribalSafe && ssl != null ) {
				SharedStoreFactory.getInstance().release(ssl);
			}
		}
	}
	
	protected Object removeLocal(Object key) {
		synchronized (semaphoreLocal) {
			Object o = super.remove(key);
			semaphoreLocal.notifyAll();
			if ( o != null ) {
				deleteCount++;
			}
			return o;
		}
	}
	
	public int getSize() {
		return this.size();
	}
	
	public String getId() {
		return id;
	}

	public boolean isTribalSafe() {
		return tribalSafe;
	}

	public void setTribalSafe(boolean tribalSafe) {
		this.tribalSafe = tribalSafe;
	}
	
}
