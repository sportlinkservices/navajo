/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*******************************************************************************
 * Copyright (c) 2008, Original authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo ZERR <angelo.zerr@gmail.com>
 *******************************************************************************/
package org.akrogen.tkui.css.core.impl.engine;

import org.akrogen.tkui.css.core.engine.CSSErrorHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic implementation for CSS Engine error handlers which print stack trace of
 * the exception throwed.
 * 
 * @version 1.0.0
 * @author <a href="mailto:angelo.zerr@gmail.com">Angelo ZERR</a>
 * 
 */
public class CSSErrorHandlerImpl implements CSSErrorHandler {

	public static final CSSErrorHandler INSTANCE = new CSSErrorHandlerImpl();
	
	private final static Logger logger = LoggerFactory
			.getLogger(CSSErrorHandlerImpl.class);
	
	/*
	 * (non-Javadoc)
	 * @see org.akrogen.tkui.css.core.engine.CSSErrorHandler#error(java.lang.Exception)
	 */
	public void error(Exception e) {
		logger.error("Error: ",e);
	}
}
