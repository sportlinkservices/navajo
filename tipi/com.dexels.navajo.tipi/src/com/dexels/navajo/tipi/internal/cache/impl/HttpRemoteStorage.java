/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.tipi.internal.cache.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.tipi.internal.cache.RemoteStorage;

public class HttpRemoteStorage implements RemoteStorage {
	private URL baseUrl = null;


	private final static Logger logger = LoggerFactory.getLogger(HttpRemoteStorage.class);
	
	public HttpRemoteStorage(URL base) {
		baseUrl = base;
	}

	@Override
	public InputStream getContents(String location, Map<String, Object> metadata)
			throws IOException {
		URL u = new URL(baseUrl, location);
		InputStream is = null;
		if(metadata==null) {
			metadata = new HashMap<String, Object>();
		}
		try {
			URLConnection uc = u.openConnection();
			uc.addRequestProperty("Accept-Encoding", "gzip");
			metadata.put("length", uc.getContentLength());
			metadata.put("encoding", uc.getContentEncoding());
			metadata.put("type", uc.getContentType());
			uc.connect();

			is = uc.getInputStream();
			String enc = uc.getHeaderField("Content-Encoding");
			if(enc!=null) {
				metadata.put("Content-Encoding", enc);
			}

			if("gzip".equals(enc)) {
				GZIPInputStream gzi = new GZIPInputStream(is);
				return gzi;
			}
			
		} catch (FileNotFoundException e) {
			logger.error("Remote location: " + location + " not found",e);
		}
		return is;
	}

	@SuppressWarnings("unused")
	@Override
	public long getRemoteModificationDate(String location) throws IOException {
		if(true) {
			return 0;
		}
		logger.debug("Checking modification date of location: "+location);
		URL u = new URL(baseUrl, location);
		URLConnection connection = u.openConnection();
		if (connection instanceof HttpURLConnection) {
			HttpURLConnection urlc = (HttpURLConnection) connection;
			urlc.setRequestMethod("HEAD");
		}
		return connection.getLastModified();

	}

	@Override
	public URL getURL(String location) throws IOException {
		return new URL(baseUrl, location);
	}

}
