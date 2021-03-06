/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.events;

import java.io.Serializable;

import com.dexels.navajo.document.Navajo;

/**
 * A NavajoEvent is used as a means of communicating between different parts (modules) of a Navajo server.
 * A NavajoEvents enables a flexible a modularized architecture for the Navajo 2.0 system.
 * NavajoEvents are defined as light-weight "internal" events. They differ from e.g. web service events which are communicated
 * to all members of a Navajo cluster.
 * 
 * @author arjen
 *
 */
public interface NavajoEvent extends Serializable {

	/**
	 * Returns a Navajo object with the relevant event parameters as properties in
	 * a message named "__event__".
	 * 
	 * @return
	 */
	public Navajo getEventNavajo();
	
	/** Indicates whether instances of this event should be handled synchronous 
	 */
	public boolean isSynchronousEvent();
	
}
