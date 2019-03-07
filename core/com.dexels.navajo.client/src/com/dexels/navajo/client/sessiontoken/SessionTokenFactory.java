package com.dexels.navajo.client.sessiontoken;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionTokenFactory {

    private static SessionTokenProvider instance = null;

    @SuppressWarnings("unused")
    private static final Logger logger = LoggerFactory.getLogger(SessionTokenFactory.class);

    private SessionTokenFactory() {

    }

    public static synchronized SessionTokenProvider getSessionTokenProvider() {
        if (instance == null) {
            instance = new DefaultSessionTokenProvider();
        }
        return instance;
    }

    public static synchronized void setSessionTokenProvider(SessionTokenProvider provider) {
        instance = provider;
    }

    public static void main(String[] args) {
        SessionTokenFactory.getSessionTokenProvider().reset();
    }

    public static void clearInstance() {
        instance = null;
    }
}
