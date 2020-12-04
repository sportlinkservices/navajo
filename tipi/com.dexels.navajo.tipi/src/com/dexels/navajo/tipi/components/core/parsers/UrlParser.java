/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.components.core.parsers;

import java.net.MalformedURLException;
import java.net.URL;

import com.dexels.navajo.tipi.TipiComponent;
import com.dexels.navajo.tipi.TipiTypeParser;
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
public class UrlParser extends TipiTypeParser {
	private static final long serialVersionUID = -7297017153371186707L;

	@Override
	public Object parse(TipiComponent source, String expression, TipiEvent event) {
		return getUrl(expression);
	}

	private URL getUrl(String path) {
		try {
			return new URL(path);
		} catch (MalformedURLException ex) {
			throw new IllegalArgumentException("supplied url not valid for: "
					+ path);
		}
	}

}
