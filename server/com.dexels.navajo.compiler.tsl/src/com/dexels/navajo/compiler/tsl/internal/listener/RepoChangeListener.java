/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler.tsl.internal.listener;

import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreator;

public class RepoChangeListener implements EventHandler {

	@SuppressWarnings("unused")
	private BundleCreator bundleCreator = null;

	private static final Logger logger = LoggerFactory
			.getLogger(RepoChangeListener.class);

	public void setBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = bundleCreator;
	}

	/**
	 * 
	 * @param bundleCreator
	 *            the bundle creator to clear
	 */
	public void clearBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = null;
	}

	@Override
	public void handleEvent(Event e) {
		logger.debug("EVENT FOUND {}", e);
		for (String p : e.getPropertyNames()) {
			final Object value = e.getProperty(p);
			if (value == null) {
				continue;
			}
			if (value instanceof List) {
				if (((List<?>) value).isEmpty()) {
					continue;
				}
			}
			logger.debug("Name: {} value: {}", p, value);

		}
	}

}
