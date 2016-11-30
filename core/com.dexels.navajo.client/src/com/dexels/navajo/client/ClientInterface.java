package com.dexels.navajo.client;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;

import com.dexels.navajo.document.Navajo;

/**
 * See NavajoClient for an explanation of all methods
 */
public interface ClientInterface {
	public final static String LOCALE_HEADER_KEY = "locale";
	public final static String SUBLOCALE_HEADER_KEY = "sublocale";
	public final static String ORG_HEADER_KEY = "organization";
	public final static String APP_HEADER_KEY = "application";

    public Navajo doSimpleSend(Navajo out, String method) throws ClientException;

    public Navajo doSimpleSend(Navajo out, String method, Integer retries) throws ClientException;


    
    public void setUsername(String s);

    public void setKeyStore(KeyStore keystore);

    public void setPassword(String pw);

    public void setServerUrl(String url);

    public void setServerUrls(String[] servers);

    /*
     * sets the locale for the client, it will be appended to the header
     */
    public void setHeader(String key, Object value);
    
    public void setNavajoHeader(String key, Object value);
    
    public void setCurrentHost(String host);

    public void setAllowCompression(boolean allowCompression);

    /**
     * Handle common connection exceptions as ValidationError, instead of throwing the exception itself.
     * Default is true.
     */
    public void setGenerateConditionErrors(boolean set);

    /**
     * set the SSL socket factory to use whenever an HTTPS call is made.
     * 
     * @param algorithm, the algorithm to use, for example: SunX509
     * @param type Type of the keystore, for example PKCS12 or JKS
     * @param source InputStream of the client certificate, supply null to reset the socketfactory to default
     * @param password the keystore password
     */

    public void setClientCertificate(String algorithm, String type, InputStream is, char[] password) throws IOException;

    /**
     * Created to force the client to encode the request using Gzip (GAE related)
     * 
     * @param forceGzip
     */
    public void setForceGzip(boolean forceGzip);

}
