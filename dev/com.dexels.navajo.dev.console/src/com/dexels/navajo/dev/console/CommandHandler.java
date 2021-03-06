/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.dev.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreator;
import com.dexels.navajo.script.api.LocalClient;

public class CommandHandler {
	private static final Logger logger = LoggerFactory .getLogger(CommandHandler.class);
	
	private BundleCreator bundleCreator = null;
	protected final Collection<ServiceRegistration<?>> registeredCommands = new ArrayList<>();
	protected BundleContext bundleContext;
	
	private LocalClient localClient;
	
	public void setBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = bundleCreator;
	}

	/**
	 * 
	 * @param bundleCreator the bundlecreator to clear
	 */
	public void clearBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = null;
	}

	public void setLocalClient(LocalClient lc) {
		localClient  = lc;
	}

	/**
	 *
	 * @param lc the local client to clear
	 */
	public void clearLocalClient(LocalClient lc) {
		localClient  = null;
	}
	
	public void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		logger.debug("Command handler activated");

		CompileCommand c = new CompileCommand();
		c.setBundleCreator(bundleCreator);
		registerCommand(c,"compile");
	
		LoadCommand load = new LoadCommand();
		load.setBundleCreator(bundleCreator);
		registerCommand(load,"loadbundle");

		FunctionListCommand func = new FunctionListCommand(bundleContext);
		registerCommand(func,"functions");

		AdapterListCommand adapter = new AdapterListCommand(bundleContext);
		registerCommand(adapter,"adapters");

		ScriptListCommand script = new ScriptListCommand(bundleContext);
		registerCommand(script,"scripts");
		
		CallCommand cc = new CallCommand();
		cc.setLocalClient(localClient);
		registerCommand(cc, "call");
		
		SharedStore_ls ls = new SharedStore_ls();
		registerCommand(ls, "ls");
		
		SharedStore_cd changedir = new SharedStore_cd();
		registerCommand(changedir, "cd");
		
		SharedStore_cat cat = new SharedStore_cat();
		registerCommand(cat, "cat");
		
		SharedStore_get get = new SharedStore_get();
		registerCommand(get, "get");
		
		SharedStore_put put = new SharedStore_put();
		registerCommand(put, "put");
		
		SharedStore_mkdir mkdir = new SharedStore_mkdir();
		registerCommand(mkdir, "mkdir");
		
		SharedStore_rmdir rmdir = new SharedStore_rmdir();
		registerCommand(rmdir, "rmdir");
		
		SharedStore_pwd pwd = new SharedStore_pwd();
		registerCommand(pwd, "pwd");
		
		SharedStore_rm rm = new SharedStore_rm();
		registerCommand(rm, "rm");
		
        SharedStore_rmdate rmdate = new SharedStore_rmdate();
        registerCommand(rmdate, "rmdate");
		
		SharedStore_sharedstore sharedstore = new SharedStore_sharedstore();
		registerCommand(sharedstore, "sharedstore");
		
		NavajoStatusCommand navajoStatus = new NavajoStatusCommand();
		registerCommand(navajoStatus, "status");
		
		HelpCommand help = new HelpCommand(this);
		registerCommand(help, "help");

		
	}

	private void registerCommand(ConsoleCommand c, String command) {
		Dictionary<String,String> dd = new Hashtable<>();
		dd.put("osgi.command.scope", "navajo");
		dd.put("osgi.command.function", command);
		ServiceRegistration<?> sr = bundleContext.registerService(c.getClass().getName(), c,dd );
		registeredCommands.add(sr);
		logger.debug("registered: {} with class: {}",command,c.getClass().getName());

	}

	public void deactivate() {
		logger.info("Deactivating command handler");
		for (ServiceRegistration<?> sr : registeredCommands) {
			sr.unregister();
		}
	}
}
