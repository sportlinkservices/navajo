/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.metadata;

import org.osgi.framework.BundleContext;

import com.dexels.navajo.document.BinaryOpenerFactory;

public class GenericBinaryOpenerComponent extends GenericBinaryOpener {

	public GenericBinaryOpenerComponent() {
		super();
	}
	
	public void activate(BundleContext bc) {
		BinaryOpenerFactory.setInstance(this);

	}
	
	public void deactivate() {
		BinaryOpenerFactory.setInstance(null);
	}
}
