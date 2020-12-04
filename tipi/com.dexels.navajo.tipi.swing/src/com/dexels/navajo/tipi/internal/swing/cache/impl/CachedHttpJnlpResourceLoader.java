/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.internal.swing.cache.impl;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

import javax.jnlp.UnavailableServiceException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.internal.CachedResourceLoader;
import com.dexels.navajo.tipi.internal.cache.CacheManager;
import com.dexels.navajo.tipi.internal.cache.LocalStorage;
import com.dexels.navajo.tipi.internal.cache.impl.ClassLoaderStorageImpl;
import com.dexels.navajo.tipi.internal.cache.impl.FileLocalStorage;
import com.dexels.navajo.tipi.internal.cache.impl.GeneralCacheManager;
import com.dexels.navajo.tipi.internal.cache.impl.HttpRemoteStorage;
import com.dexels.navajo.tipi.internal.cache.impl.LocalDigestCacheValidator;
import com.dexels.navajo.tipi.internal.cookie.CookieManager;

public class CachedHttpJnlpResourceLoader extends CachedResourceLoader {

    private static final long serialVersionUID = -7140275852274687914L;
    protected final CacheManager cache;

    private final static Logger logger = LoggerFactory.getLogger(CachedHttpJnlpResourceLoader.class);

    public CachedHttpJnlpResourceLoader(String relativePath, URL baseUrl, CookieManager cm, String id) throws UnavailableServiceException {
        boolean useJnlpLocalStorage = true;

        String useJnlpLocalStorageString = getSystemProperty("jnlp.jnlpLocalStorage");
        if (useJnlpLocalStorageString != null && !useJnlpLocalStorageString.equals("")) {
            useJnlpLocalStorage = Boolean.valueOf(useJnlpLocalStorageString);
        }

        logger.info("Creating JNLP-backed local cache: relativePath: {} id: {} useJnlpLocalStorageString: {}", relativePath, id, useJnlpLocalStorageString);
        final LocalDigestCacheValidator cacheValidator = new LocalDigestCacheValidator();
        final LocalStorage localstore;
        if (useJnlpLocalStorage) {
            localstore = new JnlpLocalStorage(relativePath, cm, id);
        } else {
            File f = new File(getSystemProperty("deployment.user.tmp"), "tipicache");
            logger.info("Creating local storage in {}", f.getAbsolutePath());
            localstore = new FileLocalStorage(f);
        }

        final HttpRemoteStorage remoteStore = new HttpRemoteStorage(baseUrl);
        final ClassLoaderStorageImpl classLoaderStorage = new ClassLoaderStorageImpl(id);
        cache = new GeneralCacheManager(classLoaderStorage, localstore, remoteStore, cacheValidator, id);
        cacheValidator.setId(id);
        cacheValidator.setLocalStorage(localstore);
        cacheValidator.setRemoteStorage(remoteStore);
        try {
            cacheValidator.activate();
        } catch (IOException e) {
            logger.error("Error: ", e);
        }
    }

    @Override
    public CacheManager getCacheManager() {
        return cache;
    }

    private String getSystemProperty(String key) {
        final Map<String, String> env = System.getenv();
        if (env.containsKey(key)) {
            return env.get(key);
        }
        return System.getProperty(key);
    }

}
