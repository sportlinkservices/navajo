/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.script.api;

/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version 1.0
 */

public class AuthorizationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2640288692823196154L;
	
	public static final String AUTHORIZATION_ERROR_MESSAGE = "AuthorizationError";
	public static final String AUTHENTICATION_ERROR_MESSAGE = "AuthenticationError";

	private final String message;
	private final String user;
	private final boolean authenticationError;
	private final boolean authorizationError;

	public AuthorizationException(boolean authenticationError,boolean authorizationError, String user, String message, Throwable rootCause) {
		super(rootCause);
		this.message = message;
		this.user = user;
		this.authenticationError = authenticationError;
		this.authorizationError = authorizationError;
	}

  public AuthorizationException(boolean authenticationError, boolean authorizationError, String user, String message) {
    this.message = message;
    this.user = user;
    this.authenticationError = authenticationError;
    this.authorizationError = authorizationError;
  }

  @Override
public String getMessage() {
    return this.message;
  }

  public String getUser() {
    return this.user;
  }

  public boolean isNotAuthenticated() {
    return this.authenticationError;
  }

  public boolean isNotAuthorized() {
    return this.authorizationError;
  }
}