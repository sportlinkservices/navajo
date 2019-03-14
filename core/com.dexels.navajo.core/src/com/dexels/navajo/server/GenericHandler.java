package com.dexels.navajo.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreatorFactory;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.events.NavajoEventRegistry;
import com.dexels.navajo.events.types.NavajoCompileScriptEvent;
import com.dexels.navajo.loader.NavajoClassLoader;
import com.dexels.navajo.mapping.CompiledScript;
import com.dexels.navajo.mapping.MappingUtils;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.CompiledScriptInterface;
import com.dexels.navajo.script.api.Dependency;
import com.dexels.navajo.script.api.NavajoClassSupplier;
import com.dexels.navajo.script.api.SystemException;
import com.dexels.navajo.script.api.UserException;
import com.dexels.navajo.server.scriptengine.GenericScriptEngine;
import com.dexels.navajo.util.AuditLog;

import navajocore.Version;

/**
 * Title:        Navajo
 * Description:
 * Copyright:    Copyright (c) 2001 - 2011
 * Company:      Dexels
 * @author Arjen Schoneveld en Martin Bergman
 * @version $Id$
 *
 * This class is used to run Navajo script web services.
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DEXELS BV OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */

public class GenericHandler extends ServiceHandler {

    private static ConcurrentHashMap<String,NavajoClassSupplier> loadedClasses = null;

    private static Object mutex1 = new Object();
    private static Object mutex2 = new Object();
   
    
	private static final Logger logger = LoggerFactory
			.getLogger(GenericHandler.class);
    
    private NavajoConfigInterface tenantConfig;

	protected NavajoConfigInterface navajoConfig;
    
    public GenericHandler() {
    	if(!Version.osgiActive()) {
    		logger.warn("Warning: using OSGi constructor for GenericHandler");
    	}
    }
    
    public GenericHandler(NavajoConfigInterface tenantConfig) {
    	this.tenantConfig = tenantConfig;
    	boolean finishedSync = false;
    	
    	if (loadedClasses == null)
    		synchronized ( mutex1 ) {
    			if ( !finishedSync ) {
    				loadedClasses = new ConcurrentHashMap<>();
    				finishedSync = true;
    			}
    		}
    }

	public static void doClearCache() {
    	// fix ugly shutdown npe in some cases
    	if(loadedClasses!=null) {
        	loadedClasses.clear();
    	}
       loadedClasses = null;
       loadedClasses = new ConcurrentHashMap<String, NavajoClassSupplier>();
    }

    private static final CompiledScript getCompiledScript(Access a, String className,File scriptFile,String scriptName) throws Exception {
    	NavajoClassSupplier loader = getScriptLoader(a.betaUser, className);
    	Class<?> cs = loader.getCompiledNavaScript(className);
    	if ( cs != null ) {
    		com.dexels.navajo.mapping.CompiledScript cso = (com.dexels.navajo.mapping.CompiledScript) cs.getDeclaredConstructor().newInstance();
    		if(cso instanceof GenericScriptEngine) {
    			GenericScriptEngine gse = (GenericScriptEngine)cso;
    			gse.setScriptFile(scriptFile);
    			gse.setScriptName(scriptName);
    			gse.setAccess(a);
    			
    		}
    		cso.setClassLoader(loader);
    		return cso;
    	}
    	return null;
    }
    
    private static final boolean hasDirtyDepedencies(Access a, String className) {
    	CompiledScript cso = null;
    	try {
    		NavajoClassLoader loader = (NavajoClassLoader) loadedClasses.get(className);
    		if ( loader == null ) { // Script does not yet exist.
    			return false;
    		}
    		Class<?> cs = loader.getCompiledNavaScript(className);
        	if ( cs != null ) {
        		cso = (CompiledScript) cs.getDeclaredConstructor().newInstance();
        		cso.setClassLoader(loader);
        	}	
    	} catch (Exception e) {
    		logger.error("Error: ", e);
    		return false;
    	}
    	if ( cso != null ) {
    		return cso.hasDirtyDependencies(a);
    	} else {
    		return false;
    	}

    }
    
    private final Object[] getScriptPathServiceNameAndScriptFile(String rpcName, boolean betaUser) throws Exception {
    	String scriptPath = DispatcherFactory.getInstance().getNavajoConfig().getScriptPath();
    	int strip = rpcName.lastIndexOf("/");
        String pathPrefix = "";
        String serviceName = rpcName;
        if (strip != -1) {
          serviceName = rpcName.substring(strip+1);
          pathPrefix = rpcName.substring(0, strip) + "/";
        }
        final String applicationGroup;
        if (access.getTenant()==null) {
        	applicationGroup = this.tenantConfig.getInstanceGroup();
		} else {
			applicationGroup = access.getTenant();
		}
        
    	File scriptFile = new File(scriptPath + "/" + rpcName + "_" + applicationGroup + ".xml");
    	if (scriptFile.exists()) {
    		serviceName += "_" + applicationGroup;
    	} else {
    		scriptFile = new File(scriptPath + "/" + rpcName + ( betaUser ? "_beta" : "" ) + ".xml" );
    		if ( betaUser && !scriptFile.exists() ) {
    			// Try normal webservice.
    			scriptFile = new File(scriptPath + "/" + rpcName + ".xml" );
    		} else if ( betaUser ) {
    			serviceName += "_beta";
    		} 
    		// Check if scriptFile exists.
    	}
    	
    	if(!scriptFile.exists()) {
       	//-------------------------------
       	// Now, check the context folder:
       	//-------------------------------
       	String sp = "scripts/";
       	File scrPath = new File(DispatcherFactory.getInstance().getNavajoConfig().getContextRoot(),sp);
       	scriptFile = new File(scrPath + rpcName + "_" + applicationGroup + ".xml");
       	if (scriptFile.exists()) {
       		serviceName += "_" + applicationGroup;
       	} else {
       		scriptFile = new File(scrPath, rpcName + ( betaUser ? "_beta" : "" ) + ".xml" );
       		if ( betaUser && !scriptFile.exists() ) {
       			// Try normal webservice.
       			scriptFile = new File(scrPath, rpcName + ".xml" );
       		} else if ( betaUser ) {
       			serviceName += "_beta";
       		} 
       		// Check if scriptFile exists.
   			if ( !scriptFile.exists() ) {
   			}
       	}

    	}
    	
    	
    	
    	
    	String sourceFileName = null;
    	if(scriptFile.exists()) {
    		sourceFileName = DispatcherFactory.getInstance().getNavajoConfig().getCompiledScriptPath()+ "/" + pathPrefix + serviceName + ".java";
    		// regular scriptfile, 
    	} else {
    		// pure java scriptfile
    		String temp =DispatcherFactory.getInstance().getNavajoConfig().getScriptPath()+ "/" + pathPrefix + serviceName + ".java";
    		File javaScriptfile = new File(temp);
    		if(javaScriptfile.exists()) {
       		sourceFileName = temp;    			    			
			}
		}
    	// set when either normal script or pure java, will skip if its neither (-> a jsr223 script)
		if (sourceFileName != null) {
			String classFileName = DispatcherFactory.getInstance().getNavajoConfig().getCompiledScriptPath() + "/" + pathPrefix
					+ serviceName + ".class";
			String className = (pathPrefix.equals("") ? serviceName : MappingUtils.createPackageName(pathPrefix) + "."
					+ serviceName);
			return new Object[] { pathPrefix, serviceName, scriptFile, sourceFileName, new File(sourceFileName), className,
					classFileName, new File(classFileName),true };
		}
    	
    	
		File currentScriptDir = new File(DispatcherFactory.getInstance().getNavajoConfig().getScriptPath() + "/" + pathPrefix);
		final String servName = serviceName;
		File[] scripts = currentScriptDir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.startsWith(servName);
			}
		});
		if(scripts==null) {
			return null;
		}
		
		if (scripts.length > 1) {
			logger.warn("Warning, multiple candidates. Assuming the first: " + scripts[0].getName());
		}
		if(scripts.length==0) {
			logger.warn("Not found. dir: "+currentScriptDir.getAbsolutePath());
			return null;
		}
		String classFileName = null;
		String className = "com.dexels.navajo.server.scriptengine.GenericScriptEngine";    	
    	
    	return new Object[]{pathPrefix,serviceName,scriptFile,sourceFileName,scripts[0],className,classFileName,null,false};

    }
    
    /**
     * Check whether Navajo script file needs to be recompiled.
     * 
     * @param scriptFile
     * @param sourceFile
     * @return
     */
    private static final boolean checkScriptRecompile(File scriptFile, File sourceFile) {
    	if ( scriptFile == null ) {
    		return false;
    	}
    	return (!sourceFile.exists() || (scriptFile.lastModified() > sourceFile.lastModified()) || sourceFile.length() == 0);
    }
    
    /**
     * Check whether Java class file needs to be recompiled.
     * 
     * @param serviceName
     * @param sourceFile
     * @param targetFile
     * @return
     */
    private static final boolean checkJavaRecompile(File sourceFile, File targetFile) {
    	return !targetFile.exists() || (sourceFile.lastModified() > targetFile.lastModified());
    }
    
    private static final NavajoClassSupplier getScriptLoader(boolean isBetaUser, String className) {
    	NavajoClassSupplier newLoader = loadedClasses.get(className);
         if (newLoader == null ) {
         	newLoader = new NavajoClassLoader(null, DispatcherFactory.getInstance().getNavajoConfig().getCompiledScriptPath(), 
         			DispatcherFactory.getInstance().getNavajoConfig().getClassloader() );
         	// Use concurrent hashmap: if key exists, return existing classloader.
         	NavajoClassSupplier ncs = loadedClasses.putIfAbsent(className, newLoader);
         	if ( ncs != null ) {
         		return ncs;
         	}
         }
         return newLoader;
    }
    
    /**
     * Check whether web service Access needs recompile.
     * Can be used by interested parties, e.g. Dispatcher.
     * 
     * @param a
     * @return
     */
    public final boolean needsRecompileForScript(Access a) throws Exception {
    	Object [] all = getScriptPathServiceNameAndScriptFile(a.rpcName, a.betaUser);
 		if(all==null) {
 			return false;
 		}

    	File scriptFile = (File) all[2];
    	File sourceFile = (File) all[4];
    	String className = (String) all[5];
    	File targetFile = (File) all[7];
    	boolean isCompilable = (Boolean)all[8];

    	return isCompilable && (checkScriptRecompile(scriptFile, sourceFile) || 
    	             checkJavaRecompile(sourceFile, targetFile) ||
    	             hasDirtyDepedencies(a, className));
    }
    
    @Override
	public boolean needsRecompile() throws Exception {
    	return needsRecompileForScript(this.access);
    }
    
    /**
     * Non-OSGi only
     * @param a
     * @param compilerErrors
     * @return
     * @throws Exception
     */
    public CompiledScript compileScript(Access a, StringBuilder compilerErrors) throws Exception {
    	
    	NavajoConfigInterface properties = DispatcherFactory.getInstance().getNavajoConfig();
    	List<Dependency> deps = new ArrayList<>();
    	String scriptPath = properties.getScriptPath();
    	
    		Object [] all = getScriptPathServiceNameAndScriptFile(a.rpcName, a.betaUser);
    		if(all==null) {
    			throw new FileNotFoundException("No script found for: "+a.rpcName);
    		}
    		String pathPrefix = (String) all[0];
    		String serviceName = (String) all[1];
    		File scriptFile = (File) all[2];
    		String sourceFileName = (String) all[3];
    		File sourceFile = (File) all[4];
    		String className = (String) all[5];
    		File targetFile = (File) all[7];

    		if (properties.isCompileScripts()) {

    			if ( scriptFile != null && scriptFile.exists()) { // We have a script that exists.

    				if ( checkScriptRecompile(scriptFile, sourceFile) || hasDirtyDepedencies(a, className) ) {

    					synchronized (mutex1) { // Check for outdated compiled script Java source.

    						if ( checkScriptRecompile(scriptFile, sourceFile) || hasDirtyDepedencies(a, className) ) {

    							com.dexels.navajo.mapping.compiler.TslCompiler tslCompiler = new
    							com.dexels.navajo.mapping.compiler.TslCompiler(properties.getClassloader());

    							try {
    								final String tenant = tenantConfig.getInstanceGroup();
									tslCompiler.compileScript(serviceName, 
    										scriptPath,
    										properties.getCompiledScriptPath(),
    										pathPrefix,properties.getOutputWriter(properties.getCompiledScriptPath(), pathPrefix, serviceName, ".java"),deps,tenant,tenantConfig.hasTenantScriptFile(serviceName, tenant, null), false);
    							} catch (SystemException ex) {
    								Files.delete(sourceFile.toPath());
    								AuditLog.log(AuditLog.AUDIT_MESSAGE_SCRIPTCOMPILER , ex.getMessage(), Level.SEVERE, a.accessID);
    								throw ex;
    							}
    						}
    					}
    				} // end of sync block.

    				// Java recompile.
    				compilerErrors.append(recompileJava(a, sourceFileName, sourceFile, className, targetFile,serviceName));

    			} else {

    				// Maybe the jave file exists in the script path.
    				if ( !sourceFile.exists() ) { // There is no java file present.
    					AuditLog.log(AuditLog.AUDIT_MESSAGE_SCRIPTCOMPILER, "SCRIPT FILE DOES NOT EXISTS, I WILL TRY TO LOAD THE CLASS FILE ANYWAY....", Level.WARNING, a.accessID);
    				} else {
    					// Compile java file using normal java compiler.
    					if(sourceFile.getName().endsWith("java")) {
       					compilerErrors.append(recompileJava(a, sourceFileName, sourceFile, className, targetFile,serviceName));
    					}
    				}
    			}
    		}
    
    		return getCompiledScript(a, className,sourceFile,serviceName);
    }
    
    /**
     * doService() is called by Dispatcher to perform web service.
     *
     * @return
     * @throws NavajoException
     * @throws UserException
     * @throws SystemException
     * @throws AuthorizationException
     */
	@Override
    public final Navajo doService() throws UserException, SystemException, AuthorizationException {

        // Check whether break-was-set for access from 'the-outside'. If so, do NOT perform service and return
        // current value of outputdoc.

        if (access.isBreakWasSet()) {
            if (access.getOutputDoc() == null) {
                Navajo outDoc = NavajoFactory.getInstance().createNavajo();
                access.setOutputDoc(outDoc);
            }
            return access.getOutputDoc();
        }

        Navajo outDoc = null;
        StringBuilder compilerErrors = new StringBuilder();
        outDoc = NavajoFactory.getInstance().createNavajo();
        CompiledScriptInterface cso = null;
        try {
            cso = loadOnDemand(access.rpcName);
        } catch (Throwable e) {
            logger.error("Exception on getting compiledscript", e);
            if (e instanceof FileNotFoundException) {
                access.setExitCode(Access.EXIT_SCRIPT_NOT_FOUND);
            }
            throw new SystemException(-1, e.getMessage(), e);
        }
        try {

            // (access.rpcName);
            if (cso == null) {
                if (Version.osgiActive()) {
                    logger.warn("Script not found from OSGi registry while OSGi is active");
                } else {
                    cso = compileScript(access, compilerErrors);
                    if (cso == null) {
                        logger.error("Can not find OSGi script for rpc: {} ", access.rpcName);
                        throw new RuntimeException("Can not resolve script (non OSGi): " + access.rpcName);
                    }
                }
            }
            if (cso == null) {
                logger.error("No compiled script found, proceeding further is useless.");
                throw new RuntimeException("Can not resolve script: " + access.rpcName);
            }
            access.setOutputDoc(outDoc);
            access.setCompiledScript(cso);
            if (cso.getClassLoader() == null) {
                logger.error("No classloader present!");
            }

            cso.run(access);

            return access.getOutputDoc();
        } catch (Throwable e) {

            if (e instanceof com.dexels.navajo.mapping.BreakEvent) {
                outDoc = access.getOutputDoc(); // Outdoc might have been changed by running script
                // Create dummy header to set breakwasset attribute.

                Header h = NavajoFactory.getInstance().createHeader(outDoc, "", "", "", -1);
                outDoc.addHeader(h);
                outDoc.getHeader().setHeaderAttribute("breakwasset", "true");
                return outDoc;
            } else if (e instanceof com.dexels.navajo.server.ConditionErrorException) {
                return ((com.dexels.navajo.server.ConditionErrorException) e).getNavajo();
            } else if (e instanceof UserException) {
                throw (UserException) e;
            }
            throw new SystemException(-1, e.getMessage(), e);
        }
    }

	// THIS rpcName seems to have a tenant suffix
	private CompiledScriptInterface loadOnDemand(String rpcName) throws Exception {
		
		
		final String tenant;
		if (access.getTenant()==null) {
			tenant = tenantConfig.getInstanceGroup();
		} else {
			tenant = access.getTenant();
		}
		return BundleCreatorFactory.getInstance().getOnDemandScriptService(rpcName, tenant);
	}

	@SuppressWarnings("unused")
	@Deprecated
	private static String recompileJava(
			Access a,
			String sourceFileName,
			File sourceFile, String className, File targetFile,String scriptName) {
		
		String compilerErrors = "";
		
		
		if ( checkJavaRecompile(sourceFile, targetFile) ) { // Create class file

			synchronized(mutex2) {

				if ( checkJavaRecompile(sourceFile, targetFile) ) {

					NavajoClassSupplier loader = null;
					if ( ( loader = loadedClasses.get(className) ) != null) {
						// Get previous version of CompiledScript.
						try {
							CompiledScriptInterface prev = getCompiledScript(a, className,sourceFile,scriptName);
							prev.releaseCompiledScript();
						} catch (Exception e) {
						}
						loadedClasses.remove(className);
						loader = null;
					}

					com.dexels.navajo.compiler.internal.NavajoCompiler compiler = new com.dexels.navajo.compiler.internal.NavajoCompiler();
					try {
						compiler.compile(DispatcherFactory.getInstance().getNavajoConfig(), sourceFileName);
						compilerErrors = compiler.errors;
						NavajoEventRegistry.getInstance().publishEvent(new NavajoCompileScriptEvent(a.rpcName));
					}
					catch (Throwable t) {
						AuditLog.log(AuditLog.AUDIT_MESSAGE_SCRIPTCOMPILER, "Could not run-time compile Java file: " + sourceFileName + " (" + t.getMessage() + "). It may be compiled already", Level.WARNING, a.accessID);
					}
				}
			}
		} // end of sync block.
		return compilerErrors;
	}
    
    /**
     * Return load script class count.
     * @return
     */
    public static int getLoadedClassesSize() {
    	if ( loadedClasses != null ) {
    		return loadedClasses.size();
    	} else {
    		return 0;
    	}
    }

}
