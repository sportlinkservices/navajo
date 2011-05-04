/*
 * generated by Xtext
 */
package com.dexels.navajo.dsl.expression.ui;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingHelper;
import org.osgi.framework.BundleContext;

/**
 * Use this class to register components to be used within the IDE.
 */
public class NavajoExpressionUiModule extends com.dexels.navajo.dsl.expression.ui.AbstractNavajoExpressionUiModule {
	
//	private final AbstractUIPlugin myPlugin;
	
	private static NavajoExpressionUiModule instance;
	
	public NavajoExpressionUiModule(AbstractUIPlugin plugin) {
		super(plugin);
//		myPlugin = plugin;
		NavajoExpressionUiModule.instance = this;
		System.err.println("Registering UI module!");
		registerToOSGi(plugin);
	}
	
	public static NavajoExpressionUiModule getInstance() {
		return NavajoExpressionUiModule.instance;
	}

	private void registerToOSGi(AbstractUIPlugin plug) {
		BundleContext bc = plug.getBundle().getBundleContext();
		bc.registerService("com.dexels.navajo.dsl.expression.ui.AbstractNavajoExpressionUiModule", this, null);
		System.err.println("<<<<<<<< Registered ui module!");
	}

	@Override
	public Class<? extends IHighlightingHelper> bindIHighlightingHelper() {
		// TODO Auto-generated method stub
		return super.bindIHighlightingHelper();
	}


	
	
}
