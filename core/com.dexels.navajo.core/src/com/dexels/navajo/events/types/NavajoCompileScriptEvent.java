/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.events.types;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.events.NavajoEvent;

public class NavajoCompileScriptEvent implements NavajoEvent {

	private static final long serialVersionUID = 1224320416969244502L;
	private static final Logger logger = LoggerFactory.getLogger(NavajoCompileScriptEvent.class);
	private String webservice;
	
	public NavajoCompileScriptEvent(String webservice) {
		this.webservice = webservice;
	}

	/**
	 * Get the name of the webservice that was compiled.
	 * 
	 * @return
	 */
	public String getWebservice() {
		return webservice;
	}
	
	/**
	 * Return the event parameters as a Navajo object with a message __event__.
	 * 
	 */
	@Override
	public Navajo getEventNavajo() {
		Navajo input = NavajoFactory.getInstance().createNavajo();
		Message event = NavajoFactory.getInstance().createMessage(input, "__event__");
		try {
			input.addMessage(event);
			Property eventWebService = NavajoFactory.getInstance().createProperty(input, "Webservice", 
					Property.STRING_PROPERTY, getWebservice(), 0, "", Property.DIR_OUT);
			event.addProperty(eventWebService);
		} catch (NavajoException e) {
			logger.error("Error: ", e);
		}
		return input;
	}
	@Override
    public boolean isSynchronousEvent() {
        return false;
    }
}
