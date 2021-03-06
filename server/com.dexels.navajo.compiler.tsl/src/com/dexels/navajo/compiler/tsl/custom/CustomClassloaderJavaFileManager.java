/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.compiler.tsl.custom;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author atamur
 * @since 15-Oct-2009
 */
public class CustomClassloaderJavaFileManager extends
		ForwardingJavaFileManager<JavaFileManager> implements JavaFileManager,
		BundleListener {
	private final ClassLoader classLoader;

	private final Map<String, CustomJavaFileFolder> folderMap = new HashMap<>();
	private final Map<String, CustomJavaFileObject> fileMap = new HashMap<>();
	private final Set<Bundle> loadedBundles = new HashSet<>();
	private Optional<BundleContext> bundleContext;
	
	private static final Logger logger = LoggerFactory
			.getLogger(CustomClassloaderJavaFileManager.class);
	

	public CustomClassloaderJavaFileManager(Optional<BundleContext> context,
			ClassLoader classLoader, JavaFileManager standardFileManager) {
		super(standardFileManager);
		this.classLoader = classLoader;
		this.bundleContext = context;
		this.bundleContext.ifPresent(ctx->{
			ctx.addBundleListener(this);
			enumerateBundles(ctx);
		});
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		return classLoader;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof CustomJavaFileObject) {
			String binaryName = ((CustomJavaFileObject) file).binaryName();
			if (binaryName.indexOf('/') >= 0) {
				binaryName = binaryName
						.substring(binaryName.lastIndexOf('/') + 1);
			}
			if (binaryName.indexOf('.') >= 0) {
				binaryName = binaryName.substring(0, binaryName.indexOf('.'));
			}
			return binaryName;
		} else {
			return super.inferBinaryName(location, file);
		}
	}

	@Override
	public boolean hasLocation(Location location) {
		return location.equals(StandardLocation.SOURCE_PATH);
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location,
			String className, JavaFileObject.Kind kind) throws IOException {
		if(!StandardLocation.CLASS_OUTPUT.equals(location) && !StandardLocation.SOURCE_PATH.equals(location)) {
			JavaFileObject sjfo = super.getJavaFileForInput(location, className, kind);
			if(sjfo!=null) {
				return sjfo;
			}
		}
		String binaryName = className.replaceAll("\\.", "/");
		if (kind.equals(Kind.CLASS)) {
			binaryName = binaryName + ".class";
		} else {
			binaryName = binaryName + ".java";
		}
		CustomJavaFileObject cjfo = fileMap.get(binaryName);
		if (cjfo != null) {
			return cjfo;
		}
		String packageName = null;
		if (className.indexOf('/') >= 0) {
			packageName = className.substring(0, className.lastIndexOf('/'));
		} else {
			packageName = "";
		}
		CustomJavaFileFolder cjf = getNode(packageName);

		return cjf.getFile(binaryName);
	}



	@Override
	public JavaFileObject getJavaFileForOutput(Location location,
			String className, JavaFileObject.Kind kind, FileObject sibling)
			throws IOException {
		String binaryName = className.replaceAll("\\.", "/") + kind.extension;
		URI uri = URI.create("file:///" + binaryName);
		CustomJavaFileObject cjfo = fileMap.get(binaryName); 
		if (cjfo == null) {
			cjfo = new CustomJavaFileObject(binaryName, uri,
					(InputStream) null, kind);
			fileMap.put(binaryName, cjfo);

		}
		return cjfo;
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName,
			String relativeName) throws IOException {
		JavaFileObject jf = fileMap.get(location.getName());
		if (jf != null) {
			return jf;
		}
		return super.getFileForInput(location, packageName, relativeName);
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName,
			Set<JavaFileObject.Kind> kinds, boolean recurse) throws IOException {

		if (location == StandardLocation.PLATFORM_CLASS_PATH) {
			try {
				return super.list(location, packageName, kinds,recurse);
			} catch (Throwable e) {
				logger.error("Platform class loader failed while trying to load: "+packageName,e);
			}
		} 
		if (location == StandardLocation.CLASS_PATH
				&& kinds.contains(JavaFileObject.Kind.CLASS)) {
			CustomJavaFileFolder folder = getNode(packageName);
			if(recurse) {
				return folder.getRecursiveEntries();
			}
			return folder.getEntries();
		}
		return Collections.emptyList();

	}

	private CustomJavaFileFolder getNode(String origPackageName) {
		CustomJavaFileFolder cjf = findNode(origPackageName);
		if(cjf!=null) {
			return cjf;
		}
		String packageName = origPackageName.replaceAll("\\.", "/");
		String[] path = packageName.split("/");
		if(path.length==1) {
			// root node
			cjf = createPackageNode(packageName);
			folderMap.put(packageName, cjf);
			return cjf;
		} else {
			String parentPath = packageName.substring(0,packageName.lastIndexOf('/'));
			String name = packageName.substring(packageName.lastIndexOf('/')+1,packageName.length());
			cjf = createPackageNode(packageName);

			CustomJavaFileFolder parent = getNode(parentPath);
			parent.addSubFolder(name,cjf);
			return cjf;
		}
	}
	
	private CustomJavaFileFolder findNode(String origPackageName) {
		String packageName = origPackageName.replaceAll("\\.", "/");
		String[] paths = packageName.split("/");
		CustomJavaFileFolder cjf = folderMap.get(paths[0]);
		if(cjf==null) {
			cjf = createPackageNode(paths[0]);
			folderMap.put(paths[0], cjf);
		}
		return findChild(cjf, paths, 1);
	}

	private CustomJavaFileFolder findChild(CustomJavaFileFolder current, String[] paths, int index) {
		if(index>=paths.length) {
			return current;
		}
		CustomJavaFileFolder child = current.getSubFolder(paths[index]);
		if(child==null) {
			String joined = StringUtils.join(paths, ".",0,index+1);
			child = createPackageNode(joined);
			current.addSubFolder(paths[index], child);
		}
	
		return findChild(child, paths, index+1);
	}
	private CustomJavaFileFolder createPackageNode(String packageName) {
		return new CustomJavaFileFolder(packageName);
	}

	@Override
	public int isSupportedOption(String option) {
		return -1;
	}

	@Override
	public synchronized void bundleChanged(BundleEvent be) {
		final Bundle bundle = be.getBundle();
		switch (be.getType()) {
			case BundleEvent.UNRESOLVED:
			case BundleEvent.UNINSTALLED:
			case BundleEvent.STOPPED:
				unloadBundle(bundle);
				break;
			case BundleEvent.RESOLVED:
			case BundleEvent.STARTING:
			case BundleEvent.STARTED:
				loadBundle(bundle);
				break;
			default:
				break;
	
		}
	}

	private void enumerateBundles(BundleContext context) {
		Bundle[] bndls = context.getBundles();
		for (Bundle bundle : bndls) {
			loadBundle(bundle);
		}
	}
	
	private void loadBundle(Bundle bundle) {
			if (!loadedBundles.contains(bundle)) {
				BundleWiring bw = bundle.adapt(BundleWiring.class);
				if(bw==null) {
					return;
				}
				Iterable<String> pkgs = getAffectedPackages(bw);
				for (String pkg : pkgs) {
					CustomJavaFileFolder cjf = getNode(pkg);
					cjf.linkBundle(bundle);
				}
				loadedBundles.add(bundle);
			}
	}

	private void unloadBundle(Bundle bundle) {
		if(loadedBundles.contains(bundle)) {
					BundleWiring bw = bundle.adapt(BundleWiring.class);
					if(bw==null) {
						return;
					}
					Iterable<String> pkgs = getAffectedPackages(bw);
					for (String pkg : pkgs) {
						CustomJavaFileFolder cjf = getNode(pkg);
						cjf.unlinkBundle(bundle);
					}
			loadedBundles.remove(bundle);
		}

	}

	private Iterable<String> getAffectedPackages(BundleWiring bw) {
		List<String> result = new ArrayList<>();
		if (bw == null) {
			return result;
		}
		List<BundleCapability> l = bw.getCapabilities("osgi.wiring.package");
		if (l == null) {
			return result;
		}
		for (BundleCapability bundleCapability : l) {
			String pkg = (String) bundleCapability.getAttributes().get(
					"osgi.wiring.package");
			result.add(pkg);
		}
		return result;
	}

	@Override
	public void close() throws IOException {
		super.close();
		bundleContext.ifPresent(ctx->ctx.removeBundleListener(this));
		fileMap.clear();
	}

}