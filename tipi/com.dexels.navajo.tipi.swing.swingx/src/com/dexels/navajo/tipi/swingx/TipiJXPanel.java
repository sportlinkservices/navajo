/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.swingx;

import org.jdesktop.swingx.*;

import com.dexels.navajo.tipi.components.swingimpl.*;

public class TipiJXPanel extends TipiSwingDataComponentImpl {

	private static final long serialVersionUID = 495952398319709388L;

	@Override
	public Object createContainer() {
		JXPanel p = new JXPanel();
		return p;
	}

}
