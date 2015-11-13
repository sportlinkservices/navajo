package com.dexels.navajo.tipi.echo.functions;

import java.io.InputStream;
import java.util.List;

import navajo.ExtensionDefinition;



public class EchoFunctionDefinition implements ExtensionDefinition {

	private static final long serialVersionUID = -3429274998043371128L;
	private transient ClassLoader extensionClassLoader = null;

	@Override
	public InputStream getDefinitionAsStream() {
		return getClass().getClassLoader().getResourceAsStream("com/dexels/navajo/tipi/echo/functions/echofunctions.xml");
	}

	@Override
	public String getConnectorId() {
		return null;
	}

	@Override
	public List<String> getDependingProjectUrls() {
		// list urls to open source projects here
		return null;
	}

	public String getDeploymentDescriptor() {
		return null;
	}

	@Override
	public String getDescription() {
		
		return "Echo navajo function library";
	}

	@Override
	public String getId() {
		return "tipiecho";
	}

	@Override
	public String[] getIncludes() {
		return new String[]{"com/dexels/navajo/functions/echo/echofunctions.xml"};
	}

	public List<String> getLibraryJars() {
		return null;
	}


	@Override
	public String getProjectName() {
		return "com.dexels.navajo.tipi.echo";
	}

	@Override
	public List<String> getRequiredExtensions() {
		return null;
	}

	@Override
	public boolean isMainImplementation() {
		return false;
	}

	@Override
	public String requiresMainImplementation() {
		// any will do
		return null;
	}
	@Override
	public ClassLoader getExtensionClassloader() {
		return extensionClassLoader;
	}

	@Override
	public void setExtensionClassloader(ClassLoader extClassloader) {
		extensionClassLoader =  extClassloader;
	}



}
