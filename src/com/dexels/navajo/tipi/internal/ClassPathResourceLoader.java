package com.dexels.navajo.tipi.internal;

import java.io.*;
import java.net.*;
import java.util.*;

public class ClassPathResourceLoader implements TipiResourceLoader {

	public URL getResourceURL(String location) throws IOException {

		return getClassResourceURL(location);
	}

	private URL getClassResourceURL(String location) {
		ClassLoader classLoader = getClass().getClassLoader();
		if(classLoader==null) {
			System.err.println("Null classloader detected!");
			classLoader = ClassLoader.getSystemClassLoader();
			
		}
		return classLoader.getResource(location);
	}

	public InputStream getResourceStream(String location) throws IOException {
		URL u = getClassResourceURL(location);
		if (u == null) {
			return null;
		}
		return u.openStream();
	}

	public OutputStream writeResource(String resourceName) throws IOException {
		throw new UnsupportedOperationException("The classpath resource loader is unable to write to the resource path");

	}

	public List<File> getAllResources() throws IOException {
		throw new UnsupportedOperationException("The classpath resource loader is unable to enumerate resources");
	}

	public boolean isReadOnly() {
		return true;
	}

	public void flushCache() {
		// DO NOTHING
	}
}
