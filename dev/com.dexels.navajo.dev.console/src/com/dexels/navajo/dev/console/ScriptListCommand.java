/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.dev.console;
import org.apache.felix.service.command.CommandSession;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

public class ScriptListCommand implements ConsoleCommand {

	private final BundleContext bundleContext;

	public ScriptListCommand(BundleContext bc) {
		this.bundleContext = bc;
	}
	
	 public void scripts(CommandSession session) {
		 this.scripts(session,null);
	 }
	 
	public void scripts(CommandSession session, String filter) {
			 String serviceFilter = null;
			 if(filter!=null) {
				 serviceFilter="(navajo.scriptName="+filter+")";
			 } else {
				 serviceFilter="(navajo.scriptName=*)";
			}
			 try {
				ServiceReference<?>[] result = bundleContext.getServiceReferences("com.dexels.navajo.script.api.CompiledScriptFactory", serviceFilter);
				for (ServiceReference<?> serviceReference : result) {
					String name = (String) serviceReference.getProperty("symbolicName");
//					String adapterClass = (String) serviceReference.getProperty("adapterClass");
					session.getConsole().println(""+name+" class: (Not impl)");
				}
			 } catch (InvalidSyntaxException e) {
				 e.printStackTrace(session.getConsole());
			}

	 }

	@Override
	public String showUsage() {
		return null;
	}
}
