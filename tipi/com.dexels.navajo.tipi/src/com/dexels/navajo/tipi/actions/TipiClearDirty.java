/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.actions;

import com.dexels.navajo.tipi.TipiComponent;
import com.dexels.navajo.tipi.TipiException;
import com.dexels.navajo.tipi.internal.Dirtyable;
import com.dexels.navajo.tipi.internal.TipiAction;
import com.dexels.navajo.tipi.internal.TipiEvent;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */
public final class TipiClearDirty extends TipiAction {

	private static final long serialVersionUID = 472131866710083014L;

	@Override
	public final void execute(TipiEvent event)
			throws com.dexels.navajo.tipi.TipiException,
			com.dexels.navajo.tipi.TipiBreakException {
		// Set<String> ss = getParameterNames();

		Object p = getEvaluatedParameterValue("rootComponent", event);
		if (p == null) {
			throw new TipiException(
					"TipiClearDirty: rootComponent missing ");

		}
		if (!(p instanceof TipiComponent)) {
			throw new TipiException(
					"TipiClearDirty: rootComponent wrong type");
		}
		TipiComponent tc = (TipiComponent) p;
		processAllComponents(tc);
		
	}
	
	private void processAllComponents(TipiComponent root)
	{
		if (root.getChildCount() == 0)
		{
			if (root instanceof Dirtyable)
			{
				((Dirtyable) root).setDirty(Boolean.FALSE);
			}
		}
		else
		{
			for (TipiComponent child : root.getChildren())
			{
				processAllComponents(child);
			}
		}

	}
}
