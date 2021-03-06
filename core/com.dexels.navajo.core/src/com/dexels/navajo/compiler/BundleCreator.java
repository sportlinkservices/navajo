/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler;

import java.util.List;

import com.dexels.navajo.script.api.CompilationException;
import com.dexels.navajo.script.api.CompiledScriptInterface;

/**
 * Interface for TSL compiling service
 * @author frank
 *
 */
public interface BundleCreator {

	public void createBundle(String script, List<String> failures,
			List<String> success, List<String> skipped, boolean force, boolean keepIntermediateFiles) throws CompilationException;

	public void installBundle(String scriptPath, List<String> failures, List<String> success, List<String> skipped, boolean force);

	/**
	 * Same as getCompiledScript, only will try to install (and compile if needed) bundle if it isn't there.
	 * @param rpcName
	 * @return
	 * @throws CompilationException 
	 * @throws Exception
	 */
	public CompiledScriptInterface getOnDemandScriptService(String rpcName, String tenant) throws CompilationException;

	/**
	 * Uninstall the bundle belonging to <code>scriptName</code>. If <code>scriptName</code> is tenant-specific, only that 
	 * bundle will be uninstalled. Otherwise only the generic bundle will be uninstalled; leaving tenant-specific versions intact
	 */
	public void uninstallBundle(String scriptName);

}
