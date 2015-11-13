package com.dexels.navajo.compiler.tsl.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreator;
import com.dexels.navajo.compiler.tsl.BundleQueue;
import com.dexels.navajo.dependency.Dependency;
import com.dexels.navajo.dependency.DependencyAnalyzer;
import com.dexels.navajo.repository.api.RepositoryInstance;
import com.dexels.navajo.repository.api.util.RepositoryEventParser;

public class BundleQueueComponent implements EventHandler, BundleQueue {

    private static final String SCRIPTS_FOLDER = "scripts" + File.separator;
    private static final List<String> SUPPORTED_EXTENSIONS = Arrays.asList(".xml", ".scala");
    private BundleCreator bundleCreator = null;
    private ExecutorService executor;
    private DependencyAnalyzer depanalyzer;
    
    private boolean keepIntermediateFiles = false;

    private final static Logger logger = LoggerFactory.getLogger(BundleQueueComponent.class);

    public void setBundleCreator(BundleCreator bundleCreator) {
        this.bundleCreator = bundleCreator;
    }

    public void activate() {
        this.executor = Executors.newFixedThreadPool(1);
        
        if ("true".equals(System.getenv("DEVELOP_MODE"))) {
            keepIntermediateFiles = true;
        }
    }

    public void deactivate() {
        executor.shutdown();
        executor = null;
    }

    public void setDependencyAnalyzer(DependencyAnalyzer depa) {
        depanalyzer = depa;
    }

    public void clearDependencyAnalyzer(DependencyAnalyzer depa) {
        depanalyzer = null;
    }

    /* (non-Javadoc)
     * @see com.dexels.navajo.compiler.tsl.internal.BundleQueue#enqueueScript(java .lang.String) */
    @Override
    public void enqueueScript(final String script, final String extension) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                List<String> failures = new ArrayList<String>();
                List<String> success = new ArrayList<String>();
                List<String> skipped = new ArrayList<String>();
                logger.info("Eagerly compiling: " + script);
                try {
                    bundleCreator.createBundle(script, new Date(), failures, success, skipped, true, keepIntermediateFiles, extension);
                    bundleCreator.installBundle(script, failures, success, skipped, true, extension);
                    if (!skipped.isEmpty()) {
                        logger.info("Script compilation skipped: " + script);
                    }
                    if (!failures.isEmpty()) {
                        logger.info("Script compilation failed: " + script);
                    }

                } catch (Throwable e) {
                    logger.error("Error: ", e);
                }
            }
        });
    }

    public void enqueueDeleteScript(final String script) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                // String tenant = script.
                logger.info("Uninstalling: " + script);
                try {
                    bundleCreator.uninstallBundle(script);
                } catch (Throwable e) {
                    logger.error("Error: ", e);
                }
            }
        });
    }

    /**
     * 
     * @param bundleCreator
     *            the bundlecreator to clear
     */
    public void clearBundleCreator(BundleCreator bundleCreator) {
        this.bundleCreator = null;
    }

    @Override
    public void handleEvent(Event e) {
        try {

            checkForChangedScripts(e);
            checkForRemovedScripts(e);
        } catch (Exception e1) {
            logger.error("Exception on handling event: {}", e);
        }

    }

    private void checkForRemovedScripts(Event e) {
        List<String> deletedScripts = RepositoryEventParser.filterDeleted(e, SCRIPTS_FOLDER);
        for (String deletedScript : deletedScripts) {
            // Uninstall bundle
            String stripped = deletedScript.substring(SCRIPTS_FOLDER.length());
            int dotIndex = stripped.lastIndexOf(".");
            if (dotIndex < 0) {
                logger.info("Scripts need an extension, and {} has none. Ignoring.");
                continue;
            }

            String extension = stripped.substring(dotIndex, stripped.length());
            if (!SUPPORTED_EXTENSIONS.contains(extension)) {
                logger.info("Ignoring file delete {} due to non-matching extension: {} ", deletedScript, extension);
                return;
            }

            String scriptName = stripped.substring(0, dotIndex);
            enqueueDeleteScript(scriptName);
        }
    }

    private void checkForChangedScripts(Event e) {
        RepositoryInstance ri = (RepositoryInstance) e.getProperty("repository");
        Set<String> changedScripts = new HashSet<String>(RepositoryEventParser.filterChanged(e, SCRIPTS_FOLDER));
        for (String changedScript : changedScripts) {
            // Replace windows backslashes with normal ones
            changedScript = changedScript.replace("\\", "/");
            try {
                File location = new File(ri.getRepositoryFolder(), changedScript);
                if (location.isFile()) {
                    String stripped = changedScript.substring(SCRIPTS_FOLDER.length());
                    int dotIndex = stripped.lastIndexOf(".");
                    if (dotIndex < 0) {
                        logger.info("Scripts need an extension, and {} has none. Ignoring update.", stripped);
                        continue;
                    }
                    String scriptName = stripped.substring(0, dotIndex);
                    String extension = stripped.substring(dotIndex, stripped.length());
                    if (!SUPPORTED_EXTENSIONS.contains(extension)) {
                        logger.info("Ignoring file update {} due to non-matching extension: {} ", scriptName, extension);
                        continue;
                    }
                    enqueueScript(scriptName, extension);
                    enqueueDependentScripts(scriptName);
                }
            } catch (IllegalArgumentException e1) {
                logger.warn("Error in handling changed script {}: {}", changedScript, e1);
            }
        }
    }

    private void enqueueDependentScripts(String script) {
        String rpcName = script;
        if (script.indexOf("_") > 0) {
            rpcName = script.substring(0, script.lastIndexOf("_"));
        }
        List<Dependency> dependencies = depanalyzer.getReverseDependencies(rpcName);
        // Use a set to prevent duplicates due to tenent-specific dependencies
        Set<String> dependentScripts = new HashSet<String>();
        for (Dependency dep : dependencies) {
            if (dep.getType() == Dependency.INCLUDE_DEPENDENCY) {
                dependentScripts.add(dep.getScript());
            }

        }
        for (String depScript : dependentScripts) {
            logger.debug("Compiling {}; the following script should be recompiled too: {}", script, depScript);
            enqueueScript(depScript, ".xml");
        }
    }

}
