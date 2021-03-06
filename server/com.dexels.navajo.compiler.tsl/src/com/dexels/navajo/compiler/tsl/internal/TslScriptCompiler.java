/*
This file is part of the Navajo Project.
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt.
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler.tsl.internal;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.ScriptCompiler;
import com.dexels.navajo.compiler.tsl.custom.PackageReportingClassLoader;
import com.dexels.navajo.document.ExpressionEvaluator;
import com.dexels.navajo.mapping.compiler.SkipCompilationException;
import com.dexels.navajo.mapping.compiler.TslCompiler;
import com.dexels.navajo.script.api.CompilationException;
import com.dexels.navajo.script.api.Dependency;
import com.dexels.navajo.server.NavajoIOConfig;

public class TslScriptCompiler extends ScriptCompiler {

    private static final Logger logger = LoggerFactory.getLogger(TslScriptCompiler.class);

    private static final String SCRIPT_PATH = "scripts";
    private static final String SCRIPT_EXTENSION = ".xml";

    private ClassLoader classLoader = null;
    private TslCompiler compiler;

    String[] standardPackages = new String[] {
            "com.dexels.navajo.document",
            "com.dexels.navajo.document.types",
            "com.dexels.navajo.enterprise.entity;resolution:=optional",
            "com.dexels.navajo.enterprise.entity.impl;resolution:=optional",
            "com.dexels.navajo.loader",
            "com.dexels.navajo.mapping",
            "com.dexels.navajo.mapping.compiler.meta",
            "com.dexels.navajo.parser",
            "com.dexels.navajo.script.api",
            "com.dexels.navajo.server",
            "com.dexels.navajo.server.enterprise.tribe",
            "com.dexels.navajo.server.resource;resolution:=optional",
            "org.osgi.framework"
    };

    public void activate() {
        logger.debug("Activating TSL compiler");
        compiler = new TslCompiler(classLoader, navajoIOConfig);
    }

    public void deactivate() {
        logger.debug("Deactivating TSL compiler");
    }


    @Override
    public Set<String> compileScript(File scriptPath, String script, String packagePath, List<Dependency> dependencies,
            String tenant, boolean hasTenantSpecificFile, boolean forceTenant) throws CompilationException, SkipCompilationException {

        final Set<String> packages = new HashSet<>();
        for (String pkg : standardPackages) {
            packages.add(pkg);
        }
        PackageReportingClassLoader prc = new PackageReportingClassLoader(classLoader);
        prc.addPackageListener(name -> packages.add(name));

        compiler.compileToJava(script, navajoIOConfig.getScriptPath(), navajoIOConfig.getCompiledScriptPath(),
                packagePath, packagePath, prc, navajoIOConfig, dependencies, tenant, hasTenantSpecificFile,
                forceTenant);

        return packages;
    }

    public void setClassLoader(ClassLoader cls) {
        this.classLoader = cls;
    }

    public void clearClassLoader(ClassLoader cls) {
        this.classLoader = null;
    }

    public void setIOConfig(NavajoIOConfig config) {
        this.navajoIOConfig = config;
    }

    public void clearIOConfig(NavajoIOConfig config) {
        this.navajoIOConfig = null;
    }

    void setExpressionEvaluator(ExpressionEvaluator e) {
        this.expressionEvaluator = e;
    }

    void clearExpressionEvaluator(ExpressionEvaluator e) {
        this.expressionEvaluator = null;
    }

    @Override
    public boolean scriptNeedsCompilation() {
        return true;
    }


    @Override
    public String getScriptExtension() {
        return SCRIPT_EXTENSION;
    }

    @Override
    public String getRelativeScriptPath() {
        return SCRIPT_PATH;
    }

	@Override
	public Set<String> getRequiredBundles() {
		return Collections.<String>emptySet();

	}

	@Override
	public boolean supportTslDependencies() {
		return true;
	}

}
