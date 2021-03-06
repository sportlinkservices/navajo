/*
This file is part of the Navajo Project.
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt.
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler.scala;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.ScriptCompiler;
import com.dexels.navajo.compiler.tsl.custom.PackageReportingClassLoader;
import com.dexels.navajo.script.api.CompilationException;
import com.dexels.navajo.script.api.Dependency;
import com.dexels.navajo.server.NavajoIOConfig;

import scala.collection.mutable.ListBuffer;
import scala.tools.nsc.Global;
import scala.tools.nsc.Settings;
import scala.tools.nsc.reporters.ConsoleReporter;
import scala.tools.reflect.ReflectGlobal;

public class ScalaCompiler extends ScriptCompiler {

    private static final Logger logger = LoggerFactory.getLogger(ScalaCompiler.class);

	private static final String SCRIPT_PATH = "scripts";
	private static final String SCRIPTEXTENSION = ".scala";

    private ClassLoader navajoScriptClassLoader;
    private File commonDir;

    Set<String> whitelist = new HashSet<>();

    String[] standardPackages = new String[] {
            "com.dexels.navajo.document",
            "com.dexels.navajo.document.types",
            "com.dexels.navajo.enterprise.entity;resolution:=optional",
            "com.dexels.navajo.enterprise.entity.impl;resolution:=optional",
            "com.dexels.navajo.expression.api",
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

    String[] standardReqBundles = new String[] {
            "com.dexels.immutable.api",
            "com.dexels.navajo.adapters",
            "com.dexels.navajo.function",
            "com.dexels.navajo.mongo",
            "com.dexels.navajo.enterprise.adapters",
            "com.dexels.navajo.enterprise.pubsub",
            "com.dexels.replication.api",
            "com.sportlink.adapters",
            "com.sportlink.financial.adapters",
            "org.scala-lang.scala-library;bundle-version=\"2.11.2\""
    };

    public void activate(Map<String, Object> osgisettings) {
       if (osgisettings.containsKey("whitelist")) {

           String whitelistString = (String) osgisettings.get("whitelist");
           String[] splitted  = whitelistString.split(",");
           for (String anImport : splitted) {
               if (anImport.indexOf(';') > 0) {
                   anImport =    anImport.substring(0,anImport.indexOf(';'));
               }
               whitelist.add(anImport);
           }
       }

    }


    @Override
    protected Set<String> compileScript(File scriptFile, String script, String packagePath, List<Dependency> dependencies, String tenant,
            boolean hasTenantSpecificFile, boolean forceTenant) throws CompilationException {
    	final Set<String> packages = new HashSet<>();

        for (String pkg : standardPackages) {
            packages.add(pkg);
        }

        PackageReportingClassLoader prc = new PackageReportingClassLoader(navajoScriptClassLoader);
        prc.addPackageListener(name -> {
		    if (whitelist.contains(name)) {
		        packages.add(name);
		    }
		});


        File targetDir = new File(navajoIOConfig.getCompiledScriptPath(), packagePath + File.separator + script);
        targetDir.mkdirs();

        Settings settings = new Settings();
        settings.outputDirs().setSingleOutput(targetDir.getAbsolutePath()) ;
        ConsoleReporter reporter = new ConsoleReporter(settings);

        ReflectGlobal g = new ReflectGlobal(settings, reporter, prc);
        Global.Run compiler = g.new Run();

        ListBuffer<String> files = new ListBuffer<>();
        String file = scriptFile.getAbsolutePath();

        addScalaCommon(files);
        files.$plus$eq(file);

        try {
            compiler.compile(files.toList());
        } catch (Exception e) {
            logger.error("Exception on getting scala code! {}", e);
        }

        logger.debug("finished compiling scala for the following files: {}", compiler.compiledFiles());
        return packages;
    }

    private void addScalaCommon(ListBuffer<String> files) {
        try {
            Files.walk(Paths.get(commonDir.toURI()))
	            .filter(Files::isRegularFile)
	            .filter(f -> f.normalize().toString().endsWith(".scala"))
	            .forEach(f -> files.$plus$eq(f.normalize().toString()));
        } catch (IOException e) {
            logger.error("IOException on adding scala common!", e);
        }
    }


    @Override
    public boolean scriptNeedsCompilation() {
        return false;
    }

    @Override
    public String getScriptExtension() {
        return SCRIPTEXTENSION;
    }

    public void setNavajoConfig(NavajoIOConfig config) {
        this.navajoIOConfig = config;
        commonDir = new File(navajoIOConfig.getRootPath(), "scalacommon");
    }

    public void clearNavajoConfig(NavajoIOConfig config) {
        this.navajoIOConfig = null;
        commonDir = null;
    }

    public void setClassLoader(ClassLoader navajoScriptClassLoader) {
        this.navajoScriptClassLoader = navajoScriptClassLoader;
    }

    public void clearClassLoader(ClassLoader navajoScriptClassLoader) {
        this.navajoScriptClassLoader = null;
    }



	@Override
	public Set<String> getRequiredBundles() {
		return new HashSet<>(Arrays.asList(standardReqBundles));
	}



    @Override
    public String getRelativeScriptPath() {
        return SCRIPT_PATH;
    }


	@Override
	public boolean supportTslDependencies() {
		return false;
	}
}
