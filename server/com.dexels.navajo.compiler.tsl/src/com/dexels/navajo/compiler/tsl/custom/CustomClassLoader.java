/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler.tsl.custom;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomClassLoader extends ClassLoader {

	private final JavaFileManager fileManager;

	private final static Logger logger = LoggerFactory
			.getLogger(CustomClassLoader.class);

	// private final static Logger logger = LoggerFactory
	// .getLogger(CustomClassLoader.class);
	//
	public CustomClassLoader(
			JavaFileManager customJavaFileManager) {
		this.fileManager = customJavaFileManager;
	}

	@Override
	protected Class<?> findClass(String className)
			throws ClassNotFoundException {
		byte[] b = loadClassData(className);
		if (b == null) {
			return null;
		}
		return defineClass(className, b, 0, b.length);
	}

	private byte[] loadClassData(String className) {

		String name = className.replaceAll("\\.", "/");
		try {
			JavaFileObject jfo = fileManager.getJavaFileForInput(
					StandardLocation.CLASS_OUTPUT, name, Kind.CLASS);
			if (jfo == null) {
				return null;
			}
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(jfo.openInputStream(), baos);
			return baos.toByteArray();
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
		return null;
	}

	@Override
	protected URL findResource(String res) {
		String name = res.replaceAll("\\.", "/");
		try {
			// fileManager.getFo
			JavaFileObject jfo = fileManager.getJavaFileForInput(
					StandardLocation.CLASS_OUTPUT, name, Kind.CLASS);
			if (jfo == null) {
				return null;
			}
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
		return super.findResource(res);
	}

	@Override
	protected synchronized Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		return super.loadClass(name, resolve);
	}

}
