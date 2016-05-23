package com.dexels.navajo.authentication.api;

import java.util.List;

import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.SystemException;

/**
 * <p>Title: <h3>SportLink Services</h3><br></p>
 * <p>Description: Web Services for the SportLink Project<br><br></p>
 * <p>Copyright: Copyright (c) 2002<br></p>
 * <p>Company: Dexels.com<br></p>
 * <br>
 * This interface specifies the requirements of the AAA (Authorisation, Authentication and Audit)
 * document created by the "werkgroep Autorisatie/Authenticatie" d.d. 20-12-2002.
 *
 * @author Arjen Schoneveld
 * @version $Id$
 */

public interface AAAQuerier {
    public static final int AUTHENTICATION_OK = 0;
    public static final int AUTHENTICATION_FAILED = 1;
    public static final int AUTHENTICATION_FAILED_BLOCKED = 2;
    public static final int AUTHENTICATION_FAILED_EXPIRED= 3;
    
    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_TOKEN = "token";
    public static final String AUTH_CERTIFICATE = "certificate";
    public static final String AUTH_SECRETKEY = "secretkey";

    public static final String UNKNOWN_USER = "Unknown user";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String INVALID_CERTIFICATE = "Invalid certificate";
    public static final String EMPTY_CERTIFICATE = "Empty certificate";
    public static final String ACCOUNT_BLOCKED = "Account blocked";
    public static final String EMPTY_DISTRICTS = "No districts specified in database";
    public static final String ACCOUNT_INACTIVE = "Account not active";

    public static final String FAILED_LOGIN_TOPIC = "aaa/failedlogin";
    
    public static final String REGION_WILDCARD = "%";
    
    public int authenticateUsernamePassword(Access access, String username, String password);
    
    
    /**
     * Perform the full authentication and authorization stack
     */
    public void process(Access access) throws SystemException, AuthorizationException;

    public List<String> getUserDistricts(String tenant, String username) throws AAAException;
    
    public String getRegion(String tenant, String username);

    /**
     * Reset AAA module, i.e. re-load all configuration data.
     *
     */
    public void reset(String tenant);

    public void resetCachedUserCredential(String tenant, String username);
    
    public Integer getUserId(String tenant, String username);
    public String getPersonId(String tenant, String username);
    
    
    public String getUserAuthMethod(String tenant, String username);
    
    public boolean isFirstUseAccount(String tenant, String username);
    
    public int getDaysUntilExpiration(String tenant, String username);

    
}