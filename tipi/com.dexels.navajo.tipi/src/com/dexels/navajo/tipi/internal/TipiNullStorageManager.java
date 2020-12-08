/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on Feb 10, 2006
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package com.dexels.navajo.tipi.internal;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.tipi.TipiContext;
import com.dexels.navajo.tipi.TipiException;
import com.dexels.navajo.tipi.TipiStorageManager;

public class TipiNullStorageManager implements TipiStorageManager, Serializable {

	private static final long serialVersionUID = -5812574497740827700L;
	
	private final static Logger logger = LoggerFactory
			.getLogger(TipiNullStorageManager.class);
	
	@Override
	public Navajo getStorageDocument(String id) throws TipiException {
		logger.warn("TipiNullStorageManager: Asked for: " + id);
		return null;
	}

	@Override
	public void setStorageDocument(String id, Navajo n) throws TipiException {
		logger.warn("TipiNullStorageManager: Stored: " + id);
	}

	@Override
	public void setInstanceId(String id) {
		// whatever
	}

	@Override
	public void setContext(TipiContext tc) {

	}

}
