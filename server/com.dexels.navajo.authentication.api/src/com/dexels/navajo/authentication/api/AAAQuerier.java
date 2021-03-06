/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.authentication.api;

import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;

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
    public static final String UNKNOWN_USER = "Unknown user";
    public static final String INVALID_PASSWORD = "Invalid password";
    public static final String ACCOUNT_BLOCKED = "Account blocked";
    public static final String ACCOUNT_INACTIVE = "Account not active";

    public static final String FAILED_LOGIN_TOPIC = "aaa/failedlogin";
    
    
    public AuthenticationResult authenticateUsernamePassword(Access access);
    
    public AuthenticationType type();
    
    /**
     * Perform the full authentication and authorization stack
     */
    public void process(Access access) throws AuthorizationException;
    
    /**
     * Skips authentication 
     */
    public void authorize(Access access, Integer userid) throws AuthorizationException;

    
    /**
     * Reset AAA module, i.e. re-load all configuration data.
     *
     */
    public void reset();

    public void resetCachedUserCredential(String tenant, String username);
    
    public Integer getUserId(Access a);

    
    public boolean isFirstUseAccount(Access access);
    
    public int getDaysUntilExpiration(Access access);



}