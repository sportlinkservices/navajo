/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.dev.servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.compiler.BundleCreator;
import com.dexels.navajo.dependency.Dependency;
import com.dexels.navajo.dependency.DependencyAnalyzer;

public class CompileServlet extends HttpServlet {

	private static final long serialVersionUID = 1696342524348410364L;
	private BundleCreator bundleCreator = null;
	private DependencyAnalyzer dependencyAnalyzer;
	
	private final static Logger logger = LoggerFactory
			.getLogger(CompileServlet.class);
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String script = req.getParameter("script");
		Thread.currentThread().setName("Compile");
		if(script==null) {
			resp.sendError(400,"No script parameter supplied");
			return;
		}
		String tenant = req.getParameter("tenant");
		if(tenant==null) {
			tenant = "default";
		}
		boolean force = true;
		Boolean keepIntermediateFiles = false;
		if (req.getParameter("keepIntermediateFiles") != null) {
		    keepIntermediateFiles = Boolean.valueOf(req.getParameter("keepIntermediateFiles"));
		} else if ("true".equals(System.getenv("DEVELOP_MODE"))) {
            keepIntermediateFiles = true;
        }
		
		List<String> success = new ArrayList<String>();
		List<String> failures = new ArrayList<String>();
		List<String> skipped = new ArrayList<String>();
		String failedReason = null;
		long tsStart=0;
		long compileDuration=0;
		long tsInstall=0;
		try {
			tsStart = System.currentTimeMillis();
			if(script.equals("/")) {
				script = "";
			}
			bundleCreator.createBundle(script,failures,success,skipped, force,keepIntermediateFiles);
			long ts1 = System.currentTimeMillis();
			compileDuration = ts1 - tsStart;
			logger.info("Compiling java complete. took: {}ms. Succeeded: {} failed: {} skipped: {}", compileDuration, success.size(),failures.size(), skipped.size());
			logger.warn("Failed compiling: {}", failures);
			
		    bundleCreator.installBundle(script,failures, success, skipped, true);
			tsInstall = System.currentTimeMillis() - ts1;
			logger.info("Installing bundles took {}ms", tsInstall);
			
		} catch (Throwable e) {
			logger.error("Error compiling scripts form servlet:",e);
			failedReason = e.getMessage();
			
		}
		
		if(req.getParameter("redirect")!=null) {
			resp.sendRedirect("/index.jsp");
		} else {
		    resp.setContentType("text/plain");
			resp.getWriter().write("Compiling java complete. took: "+compileDuration+" millis.");
			resp.getWriter().write(" Succeeded: "+success.size()+" failed: "+failures.size()+" skipped: "+skipped.size());
			
			if (! script.equals("")) {
				resp.getWriter().write(" Broken deps: " + containsBrokenDependencies(script));
			}
			for (String failed : failures) {
				resp.getWriter().write(" Failed: "+failed);
			}
			if (failedReason != null) {
			    resp.getWriter().write(" failreason=" + failedReason);
			}
			resp.getWriter().write(" Avg: "+(1000 * (float)success.size() / compileDuration)+" scripts / sec");
		}
	}

    private boolean containsBrokenDependencies(String scriptPath) {
        List<Dependency> deps = dependencyAnalyzer.getDependencies(scriptPath);
        if (deps == null) {
            return false;
        }

        for (Dependency dep : deps) {
            if (dep.isBroken()) {
                return true;
            }
        }
        return false;
}
	
	
	public void setBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = bundleCreator;
	}

	public void clearBundleCreator(BundleCreator bundleCreator) {
		this.bundleCreator = null;
	}

	public void setDependencyAnalyzer(DependencyAnalyzer d) {
		this.dependencyAnalyzer = d;
	}
	
	public void clearDependencyAnalyzer(DependencyAnalyzer d) {
		this.dependencyAnalyzer = null;
	}

	
}
