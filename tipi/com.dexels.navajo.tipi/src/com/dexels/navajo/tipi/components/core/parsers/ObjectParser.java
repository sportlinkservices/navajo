package com.dexels.navajo.tipi.components.core.parsers;

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

public class ObjectParser extends TipiTypeParser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3606732073441759936L;

	public ObjectParser() {
	}

	@Override
	public Object parse(TipiComponent source, String expression, TipiEvent event) {
		throw new RuntimeException("Objects are unparsable");
	}

}
