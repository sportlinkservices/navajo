package com.dexels.navajo.tipi.components.core.parsers;

import com.dexels.navajo.tipi.TipiComponent;
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
public class ComponentParser extends BaseTipiParser {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5584565910100210484L;

	public Object parse(TipiComponent source, String expression, TipiEvent event) {
		return getTipiComponent(source, expression);
	}

	public String toString(Object o, TipiComponent source) {
		return "Not possible";
	}

}
