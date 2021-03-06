/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.server.listener.http.continuation;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dexels.navajo.api.util.NavajoRequestConfig;
import com.dexels.navajo.document.stream.api.ReactiveScriptRunner;
import com.dexels.navajo.script.api.LocalClient;
import com.dexels.navajo.script.api.SchedulableServlet;
import com.dexels.navajo.script.api.TmlRunnable;
import com.dexels.navajo.script.api.TmlScheduler;

public class TmlContinuationMultitenantServlet extends HttpServlet implements
		SchedulableServlet {

	private static final long serialVersionUID = -8645365233991777113L;

	private static final Logger logger = LoggerFactory
			.getLogger(TmlContinuationMultitenantServlet.class);

	public static final String COMPRESS_GZIP = "gzip";
	public static final String COMPRESS_JZLIB = "jzlib";
	public static final String COMPRESS_NONE = "";

	private LocalClient localClient;
	private TmlScheduler tmlScheduler;
	private HttpServlet reactiveHttpServlet;
	private ReactiveScriptRunner reactiveScriptEnvironment;
	private long requestTimeout;

    public void setReactiveScriptEnvironment(ReactiveScriptRunner env) {
		this.reactiveScriptEnvironment = env;
}

	public void clearReactiveScriptEnvironment(ReactiveScriptRunner env) {
		this.reactiveScriptEnvironment = null;
	}

	public void setReactiveServlet(HttpServlet servlet) {
		this.reactiveHttpServlet = servlet;
	}

	public void clearReactiveServlet(HttpServlet servlet) {
		this.reactiveHttpServlet = null;
	}

	public LocalClient getLocalClient() {
		return localClient;
	}

	public void setLocalClient(LocalClient localClient) {
		this.localClient = localClient;
	}

	public void clearLocalClient(LocalClient localClient) {
		this.localClient = null;
	}

	public void setTmlScheduler(TmlScheduler scheduler) {
		this.tmlScheduler = scheduler;
	}

	public void clearTmlScheduler(TmlScheduler scheduler) {
		this.tmlScheduler = null;
	}

    public void activate() {

        logger.info("Continuation servlet component activated");
        requestTimeout = NavajoRequestConfig.getRequestTimeout(10_000_000L);
        logger.info("Using timeout in continuation: {}", requestTimeout);
    }

	public void deactivate() {
		logger.info("Continuation servlet component deactivated");
	}

    @Override
    protected void service(final HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            if (useReactiveEndpoint(req)) {
                reactiveHttpServlet.service(req, resp);
                return;
            }
            String instance = determineTenantFromRequest(req);
            LocalClient localClient = getLocalClient();
            if (localClient == null) {
                localClient = getLocalClient(req);
            }
            TmlRunnable runner = TmlRunnableBuilder.prepareRunnable(req, resp, localClient,
                    instance, requestTimeout);

            if (runner != null) {
                getTmlScheduler().submit(runner, false);
            }
        } catch (Throwable e) {
            if (e instanceof ServletException) {
                throw (ServletException) e;
            }
            logger.error("Servlet call failed dramatically", e);
        }
    }

	private boolean useReactiveEndpoint(final HttpServletRequest req) {
		String serviceHeader = req.getHeader("X-Navajo-Service");
		if(this.reactiveScriptEnvironment!=null && this.reactiveHttpServlet!=null && serviceHeader!=null) {
			return this.reactiveScriptEnvironment.acceptsScript(serviceHeader);
		}
		return false;
	}

	private String determineTenantFromRequest(final HttpServletRequest req) {
		String requestInstance = req.getHeader("X-Navajo-Instance");
		if(requestInstance!=null) {
			return requestInstance;
		}
		String pathinfo = req.getPathInfo();
		if(pathinfo==null) {
			return null;
		}
		if (pathinfo.length() > 0 && pathinfo.charAt(0) == '/') {
			pathinfo = pathinfo.substring(1);
		}
		String instance = null;
		if(pathinfo.indexOf('/')!=-1) {
			instance = pathinfo.substring(0, pathinfo.indexOf('/'));
		} else {
			instance = pathinfo;
		}
		return instance;
	}



	protected LocalClient getLocalClient(final HttpServletRequest req)
			throws ServletException {
		LocalClient tempClient = localClient;
		if (localClient == null) {
			tempClient = (LocalClient) req.getServletContext()
					.getAttribute("localClient");
		}

		final LocalClient lc = tempClient;
		if (lc == null) {
			logger.error("No localclient found");
			throw new ServletException("No local client registered in servlet context");
		}
		return lc;
	}

	@Override
	public TmlScheduler getTmlScheduler() throws ServletException {
		if (tmlScheduler != null) {
			return tmlScheduler;
		}
		TmlScheduler attribute = (TmlScheduler) getServletContext()
				.getAttribute("tmlScheduler");
		if(attribute==null) {
			throw new ServletException("Can not use scheduling servlet: No scheduler found.");
		}
		return attribute;
	}



}
