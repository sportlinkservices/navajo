package com.dexels.navajo.tipi.actions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Operand;
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
public class TipiSetLocale extends TipiAction {
    private final static Logger logger = LoggerFactory.getLogger(TipiSetLocale.class);
    
	private static final long serialVersionUID = 979757447437751360L;

	@Override
	public void execute(TipiEvent event)
			throws com.dexels.navajo.tipi.TipiException,
			com.dexels.navajo.tipi.TipiBreakException {
	    
		// TODO Add support for multi-servers
		final Operand locale = getEvaluatedParameter("locale", event);
		// final Operand context = getEvaluatedParameter("context", event);
		// myContext.initRemoteDescriptionProvider((String)context.value,
		// (String)locale.value);
		if ( locale.value == null) {
		    logger.warn("Setting null locale - ignoring!");
		    return;
		}
		final String currentLocale = myContext.getApplicationInstance().getLocaleCode();
		Boolean localeChanged = currentLocale==null || !currentLocale.equals("" + locale.value); 
		myContext.clearLazyDefinitionCache();
		myContext.getApplicationInstance().setLocaleCode("" + locale.value);
		// check/change subLocale if given
		if (hasParameter("subLocale"))
		{
			final Operand subLocale = getEvaluatedParameter("subLocale", event);
			localeChanged = localeChanged || !myContext.getApplicationInstance().getSubLocaleCode().equals("" + subLocale.value); 
			myContext.getApplicationInstance().setSubLocaleCode("" + subLocale.value);
		}
		
		//if (localeChanged)
		//{
			getContext().resetErrorHandler();
			// TODO re-enable
//			myContext.reloadCssDefinitions();
		//}
	}
}