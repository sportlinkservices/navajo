package com.dexels.navajo.resource.sftp.impl;

import java.io.InputStream;
import java.util.List;

import navajo.ExtensionDefinition;



/**
 * Reference to the XML definition file for this set of adapters defined in this project.
 * 
 * @author arjen
 *
 */
public class SFTPAdapterLibrary implements ExtensionDefinition {

	
	private static final long serialVersionUID = 5195100848450458590L;
	private transient ClassLoader extensionClassLoader = null;
	
	@Override
	public InputStream getDefinitionAsStream() {
		return getClass().getResourceAsStream("adapters.xml");
	}

	@Override
	public String getConnectorId() {
		return null;
	}

	@Override
	public List<String> getDependingProjectUrls() {
		return null;
	}

	public String getDeploymentDescriptor() {
		return null;
	}

	@Override
	public String getDescription() {
		return "SFTP Adapter Library";
	}

	@Override
	public String getId() {
		return "SFTPAdapters";
	}

	@Override
	public String[] getIncludes() {
		return null;
	}

	public List<String> getLibraryJars() {
		return null;
	}

	public List<String> getMainJars() {
		return null;
	}

	@Override
	public String getProjectName() {
		return null;
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
