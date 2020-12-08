/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.events.types;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.events.NavajoEvent;

public class ServerTooBusyEvent implements NavajoEvent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7547786342264688745L;

	@Override
	public Navajo getEventNavajo() {
		return null;
	}
	
	@Override
    public boolean isSynchronousEvent() {
        return false;
    }

}
