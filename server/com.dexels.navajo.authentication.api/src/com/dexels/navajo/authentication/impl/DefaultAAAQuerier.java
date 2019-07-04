package com.dexels.navajo.authentication.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.authentication.api.AAAQuerier;
import com.dexels.navajo.authentication.api.AuthenticationResult;
import com.dexels.navajo.authentication.api.AuthenticationType;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;

public class DefaultAAAQuerier implements AAAQuerier {

	
	private static final Logger logger = LoggerFactory.getLogger(DefaultAAAQuerier.class);

	@Override
	public AuthenticationResult authenticateUsernamePassword(Access access) {
		return AuthenticationResult.AUTHENTICATION_OK;
	}

	@Override
	public void process(Access access) throws AuthorizationException {
		logger.info("Processing using default authenticator using userid: {}  ",access.userID);
		

	}

    @Override
    public void authorize(Access access, Integer userid) throws AuthorizationException {
		logger.info("Authorizing using default authenticator using userid: {} and from accessid: {}",userid,access.userID);
    }

	@Override
	public void reset() {

	}

	@Override
	public void resetCachedUserCredential(String tenant, String username) {

	}

	@Override
	public Integer getUserId(Access a) {
		return null;
	}

	@Override
	public boolean isFirstUseAccount(Access access) {
		return false;
	}

	@Override
	public int getDaysUntilExpiration(Access access) {
		return 0;
	}

	@Override
	public AuthenticationType type() {
		return AuthenticationType.NONE;
	}


}
