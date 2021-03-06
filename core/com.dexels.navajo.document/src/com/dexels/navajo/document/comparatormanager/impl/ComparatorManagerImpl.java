/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.comparatormanager.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.comparator.ComparatorFactory;
import com.dexels.navajo.document.comparatormanager.ComparatorManager;
import com.dexels.navajo.document.comparatormanager.ComparatorManagerFactory;

public class ComparatorManagerImpl implements ComparatorManager {

	private final Map<String,ComparatorFactory> factories = new HashMap<>();
	
	private static final Logger logger = LoggerFactory
			.getLogger(ComparatorManagerImpl.class);
	
	public void activate() {
		ComparatorManagerFactory.setInstance(this);
	}

	public void deactivate() {
		ComparatorManagerFactory.setInstance(null);
	}

	@Override
	public Comparator<Message> getComparator(String name) {
		ComparatorFactory cf = factories.get(name);
		if(cf==null) {
			logger.warn("Unable to find ComparatorFactory with name: {}",name);
			return null;
		}
		return cf.createComparator();
	}

	public void addComparatorFactory(ComparatorFactory cf) {
		factories.put(cf.getName(), cf);
	}

	public void removeComparatorFactory(ComparatorFactory cf) {
		factories.remove(cf.getName());
	}

}
