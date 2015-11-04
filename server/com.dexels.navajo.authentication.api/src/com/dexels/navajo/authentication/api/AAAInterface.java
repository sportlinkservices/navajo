package com.dexels.navajo.authentication.api;

import com.dexels.navajo.document.Navajo;
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

public interface AAAInterface {


    public static final String AUTH_PASSWORD = "password";
    public static final String AUTH_TOKEN = "token";
    public static final String AUTH_CERTIFICATE = "certificate";

    public static final String UNKNOWN_USER = "Unknown user";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String INVALID_TOKEN = "Invalid token";
    public static final String INVALID_CERTIFICATE = "Invalid certificate";
    public static final String EMPTY_CERTIFICATE = "Empty certificate";

    public static final String EMPTY_DISTRICTS = "No districts specified in database";

    
    public Access performUserAuthorisation(String username, String password, String service, Navajo inMessage,
            Object certificate, String accessID) throws SystemException, AuthorizationException;


    
    /**
     * Return all valid districts for a given user.
     *
     * @param username
     * @param password
     * @return
     * @throws AAAException
     */
    public String [] checkUser(String username, String password) throws AAAException;

    


    /**
     * Determine the number of days left before the password will expire.
     *
     * @param username
     * @return the number of days left before the password expires
     * @throws AAAException
     */
	public int checkExpiration(String username) throws AAAException;
	
    /**
     * Determine whether an account is blocked.
     *
     * @param username
     * @return true if account is blocked, and false otherwise
     * @throws AAAException
     */
	public boolean checkBlocked(String username) throws AAAException;
	
    /**
     * Determine whether an account is being used for the first time.
     *
     * @param username
     * @return true if account is being accessed for the first time, and false otherwise
     * @throws AAAException
     */
	public boolean checkFirstTimeUse(String username) throws AAAException;
	


    /**
     * Get the roles of user
     *
     * @param username
     * @param password
     * @return the names of all the roles
     */
    public String [] getUserRoles(String username, String organizationId) throws AAAException;



    /**
     * Reset AAA module, i.e. re-load all configuration data.
     *
     */
    public void destroy();

    /**
     * Reset user credentials for specified username.
     *
     * @param username
     */
    public void resetCachedUserCredential(String username);


    public void clearActionObjects();


}