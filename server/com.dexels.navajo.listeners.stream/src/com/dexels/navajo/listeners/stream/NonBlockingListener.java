package com.dexels.navajo.listeners.stream;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import javax.servlet.AsyncContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.dexels.navajo.authentication.api.AuthenticationMethodBuilder;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.stream.StreamDocument;
import com.dexels.navajo.document.stream.api.Msg;
import com.dexels.navajo.document.stream.api.NavajoHead;
import com.dexels.navajo.document.stream.api.Prop;
import com.dexels.navajo.document.stream.api.ReactiveScriptRunner;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.document.stream.events.Events;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;
import com.dexels.navajo.document.stream.xml.XML;
import com.dexels.navajo.reactive.ReactiveScriptEnvironment;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.FatalException;
import com.dexels.navajo.script.api.LocalClient;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import nl.codemonkey.reactiveservlet.Servlets;

public class NonBlockingListener extends HttpServlet {
	private static final long serialVersionUID = -4381216748627396838L;

	private LocalClient localClient;
				
	private final static Logger logger = LoggerFactory.getLogger(NonBlockingListener.class);

	private AuthenticationMethodBuilder authMethodBuilder;

	private ReactiveScriptEnvironment reactiveScriptEnvironment;
	
	public LocalClient getLocalClient() {
		return localClient;
	}

	public void setLocalClient(LocalClient localClient) {
		this.localClient = localClient;
	}

	public void clearLocalClient(LocalClient localClient) {
		this.localClient = null;
	}

    public void setAuthenticationMethodBuilder(AuthenticationMethodBuilder amb) {
        this.authMethodBuilder = amb;
    }

    public void clearAuthenticationMethodBuilder(AuthenticationMethodBuilder eventAdmin) {
        this.authMethodBuilder = null;
    }
    
    public void setReactiveScriptEnvironment(ReactiveScriptEnvironment env) {
    		this.reactiveScriptEnvironment = env;
    }

    public void clearReactiveScriptEnvironment(ReactiveScriptEnvironment env) {
		this.reactiveScriptEnvironment = null;
    }

	private static Flowable<NavajoStreamEvent> emptyDocument(StreamScriptContext context) {
		return Flowable.just(
				Events.started(NavajoHead.createSimple(context.service, context.username, context.password))
				, Events.done()
				);
	}
	
	private static Map<String, Object> extractHeaders(HttpServletRequest req) {
		Map<String, Object> attributes = 
			    new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);
		Enumeration<String> en = req.getHeaderNames();
		while (en.hasMoreElements()) {
			String headerName = en.nextElement();
			String headerValue = req.getHeader(headerName);
			attributes.put(headerName, headerValue);
		}
		return Collections.unmodifiableMap(attributes);
	}
	
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			boolean isGet = "GET".equals(request.getMethod());
			AsyncContext ac = request.startAsync();
			ac.setTimeout(3600000);
			StreamScriptContext context = determineContextFromRequest(request);
			String requestEncoding = (String) context.attributes.get("Content-Encoding");
			Optional<String> responseEncoding = decideEncoding(request.getHeader("Accept-Encoding"));

			Subscriber<byte[]> responseSubscriber = Servlets.createSubscriber(ac);
			
			try {
				authenticate(context,(String)context.attributes.get("x-navajo-password"), (String)context.attributes.get("Authorization"),context.tenant);
			} catch (Exception e1) {
				logger.error("Authentication problem: ",e1);
				errorMessage(context.service, context.username, -1, e1.getMessage())
					.lift(StreamDocument.serialize())
					.compose(StreamDocument.compress(responseEncoding))
					.subscribe(responseSubscriber);
				return;
			}
			
			Flowable<NavajoStreamEvent> input = isGet ? emptyDocument(context) : 
				Servlets.createFlowable(ac, 1000)
				.observeOn(Schedulers.io(),false,10)
				.compose(StreamDocument.decompress2(requestEncoding))
				.lift(XML.parseFlowable(10))
				.flatMap(e->e)
				.lift(StreamDocument.parse())
				.concatMap(e->e);
			
			context.setInputFlowable(input);
			
			// TODO Cache file exists result + Flush on change
			if(reactiveScriptEnvironment.isReactiveScript(context.service)) {
				runScript(context)
//				processStreamingScript(request,input)
					.lift(StreamDocument.filterMessageIgnore())
					.lift(StreamDocument.serialize())
					.compose(StreamDocument.compress(responseEncoding))
					.subscribe(responseSubscriber);
				return;
			}
			
			processLegacyScript(request,input)
				.lift(StreamDocument.filterMessageIgnore())
				.lift(StreamDocument.serialize())
				.compose(StreamDocument.compress(responseEncoding))
				.subscribe(responseSubscriber);
		} catch (Exception e1) {
			throw new IOException("Servlet problem", e1);
		}

	}


	private Flowable<NavajoStreamEvent> runScript(StreamScriptContext context) {
		return reactiveScriptEnvironment.run(context);

	}



	private Flowable<NavajoStreamEvent> processLegacyScript(HttpServletRequest request,Flowable<NavajoStreamEvent> eventStream) throws Exception {
		StreamScriptContext context = determineContextFromRequest(request);
		return eventStream
				.lift(StreamDocument.collectFlowable())
				.flatMap(inputNav->executeLegacy(context.service, context.username, context.tenant, inputNav));
	}
	

	public void authenticate(StreamScriptContext context, String password, String authHeader, String tenant) throws AuthorizationException {
		Access a = new Access(-1,-1,context.username.orElse(null),context.service,"stream","ip","hostname",null,false,"access");
		a.setTenant(tenant);
		a.setInDoc(NavajoFactory.getInstance().createNavajo());
		a.rpcPwd = password;
		authMethodBuilder.getInstanceForRequest(authHeader).process(a);
	}

//	private FlowableTransformer<NavajoStreamEvent, NavajoStreamEvent> createTransformerScript(StreamScriptContext context) {
//		Script s = scripts.get(context.service);
//		if(s!=null) {
//			return s.call(context);
//		}
//		SimpleScript ss = simpleScripts.get(context.service);
//		if(ss!=null) {
//			
//			return ss.apply(context);
//		}
//		return s.call(context);
//	}

	private static Flowable<NavajoStreamEvent> errorMessage(String service, Optional<String> user, int code, String message) {
		return Msg.create("error")
				.with(Prop.create("code",code))
				.with(Prop.create("description", message))
				.stream()
				.toFlowable(BackpressureStrategy.BUFFER)
				.compose(StreamDocument.inNavajo(service, user, Optional.empty()));
	}
	
	public static Optional<String> decideEncoding(String accept) {
		if (accept == null) {
			return Optional.empty();
		}
		String[] encodings = accept.split(",");
		Set<String> acceptedEncodings = new HashSet<>();
		for (String encoding : encodings) {
			acceptedEncodings.add(encoding.trim());
		}
		if (acceptedEncodings.contains("deflate")) {
			return Optional.of("deflate");
		}
		if (acceptedEncodings.contains("jzlib")) {
			return Optional.of("jzlib");
		}
		return null;
	}

	private final Flowable<NavajoStreamEvent> executeLegacy(String service, Optional<String> user, String tenant,Navajo in) {
		Navajo result;
		try {
			result = execute(tenant, in);
		} catch (Throwable e) {
			logger.error("Error: ", e);
			return errorMessage(service,user,101,"Could not resolve script: "+service);
		}
		return Observable.just(result)
			.lift(StreamDocument.domStream())
			.toFlowable(BackpressureStrategy.BUFFER);
	}
	
	private final Navajo execute(String tenant, Navajo in) throws IOException, ServletException {

		if (tenant != null) {
			MDC.put("instance", tenant);
		}
		try {
			in.getHeader().setHeaderAttribute("useComet", "true");
			if (in.getHeader().getHeaderAttribute("callback") != null) {

				String callback = in.getHeader().getHeaderAttribute("callback");

				Navajo callbackNavajo = getLocalClient().handleCallback(tenant, in, callback);
				return callbackNavajo;

			} else {
				Navajo outDoc = getLocalClient().handleInternal(tenant, in, null, null);
				return outDoc;
			}
		} catch (Throwable e) {
			if (e instanceof FatalException) {
				FatalException fe = (FatalException) e;
				if (fe.getMessage().equals("500.13")) {
					// Server too busy.
					throw new ServletException("500.13");
				}
			}
			throw new ServletException(e);
		} finally {
			MDC.remove("instance");
		}
	}

	private StreamScriptContext  determineContextFromRequest(final HttpServletRequest req) {
		Map<String, Object> attributes = extractHeaders(req);
		String tenant = determineTenantFromRequest(req);
		String username = (String) attributes.get("X-Navajo-Username");
		String password = (String) attributes.get("X-Navajo-Password");
		String serviceHeader = (String) attributes.get("X-Navajo-Service");
		return new StreamScriptContext(tenant,serviceHeader, Optional.ofNullable(username), Optional.ofNullable(password),attributes,Optional.of((ReactiveScriptRunner)this.reactiveScriptEnvironment));
	}


	// warn: Duplicated code
	private static String determineTenantFromRequest(final HttpServletRequest req) {
		String requestInstance = req.getHeader("X-Navajo-Instance");
		if (requestInstance != null) {
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
		if (pathinfo.indexOf('/') != -1) {
			instance = pathinfo.substring(0, pathinfo.indexOf('/'));
		} else {
			instance = pathinfo;
		}
		return instance;
	}
}
