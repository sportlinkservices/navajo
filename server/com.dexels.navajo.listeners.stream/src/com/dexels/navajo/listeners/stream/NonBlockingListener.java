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
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.dexels.immutable.factory.ImmutableFactory;
import com.dexels.navajo.authentication.api.AuthenticationMethodBuilder;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.ReactiveScript;
import com.dexels.navajo.document.stream.StreamCompress;
import com.dexels.navajo.document.stream.StreamDocument;
import com.dexels.navajo.document.stream.api.Msg;
import com.dexels.navajo.document.stream.api.Prop;
import com.dexels.navajo.document.stream.api.ReactiveScriptRunner;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;
import com.dexels.navajo.document.stream.xml.XML;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.AuthorizationException;
import com.dexels.navajo.script.api.LocalClient;
import com.github.davidmoten.rx2.Bytes;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import nl.codemonkey.reactiveservlet.Servlets;

public class NonBlockingListener extends HttpServlet {
	private static final long serialVersionUID = -4381216748627396838L;

	private LocalClient localClient;
				
	private final static Logger logger = LoggerFactory.getLogger(NonBlockingListener.class);

	private AuthenticationMethodBuilder authMethodBuilder;

	private ReactiveScriptRunner reactiveScriptEnvironment;
	
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
    
    public void setReactiveScriptEnvironment(ReactiveScriptRunner env) {
    		this.reactiveScriptEnvironment = env;
    }

    public void clearReactiveScriptEnvironment(ReactiveScriptRunner env) {
		this.reactiveScriptEnvironment = null;
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
	
	private void handleInitialError(HttpServletResponse response, Throwable error,StreamScriptContext context) {
		try {
			response.sendError(502,error.getMessage());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@Override
	protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		AsyncContext ac = request.startAsync();
		StreamScriptContext context = determineContextFromRequest(ac);
		Optional<String> responseEncoding = decideEncoding(request.getHeader("Accept-Encoding"));
		Subscriber<byte[]> responseSubscriber = Servlets.createSubscriber(ac,(resp,error)->handleInitialError(resp, error, context));
		try {
			ac.setTimeout(10000000);
			ac.addListener(new AsyncListener() {
				
				@Override
				public void onTimeout(AsyncEvent ae) throws IOException {
					Throwable throwable = ae.getThrowable();
					if(throwable!=null) {
						responseSubscriber.onError(throwable);						
					}
				}
				
				@Override
				public void onStartAsync(AsyncEvent ae) throws IOException {
					
				}
				
				@Override
				public void onError(AsyncEvent ae) throws IOException {
					Throwable throwable = ae.getThrowable();
					if(throwable!=null) {
						responseSubscriber.onError(throwable);
						logger.error("Error in Async Listener: ", throwable);
					}
				}
				
				@Override
				public void onComplete(AsyncEvent ae) throws IOException {
				}
			});
			try {
				authenticate(context,(String)context.attributes.get("x-navajo-password"), (String)context.attributes.get("Authorization"),context.tenant);
			} catch (Exception e1) {
				logger.error("Authentication problem: ",e1);
				errorMessage(true,context, Optional.of(e1), e1.getMessage())
					.lift(StreamDocument.serialize())
					.compose(StreamCompress.compress(responseEncoding))
					.subscribe(responseSubscriber);	
				return;
			}
			// TODO Cache file exists result + Flush on change
			Function<? super Throwable,? extends Publisher<? extends DataItem>> cc = e->{
				logger.error("Error detected: {}",e);
				return errorMessage(true,context,Optional.of(e), e.getMessage()).map(DataItem::of);
			};
			String debugString = request.getHeader("X-Navajo-Debug");
			boolean debug = debugString != null;
			
			ReactiveScript rs = buildScript(context,debug);
			if(responseEncoding.isPresent()) {
				response.addHeader("Content-Encoding", responseEncoding.get());
			}
			if(rs.binaryMimeType().isPresent()) {
				response.addHeader("Content-Type", rs.binaryMimeType().get());
			} else {
				response.addHeader("Content-Type", "text/xml;charset=utf-8");
			}

			switch(rs.dataType()) {
			case DATA:
				rs.execute(context)
					.map(di->di.data())
					.compose(StreamCompress.compress(responseEncoding))
					.subscribe(responseSubscriber);
				break;
			case MESSAGE:
				rs.execute(context)
				.onErrorResumeNext(cc)
				.map(di->di.message())
				.map(msg->msg.toFlatString(ImmutableFactory.getInstance()).getBytes())
				.doOnCancel(()->System.err.println("Cancel at toplevel"))
				.subscribe(responseSubscriber);
				break;
			case EVENTSTREAM:
				rs.execute(context)
				.onErrorResumeNext(cc)
				.map(e->e.eventStream())
				.concatMap(e->e)
//				.map(di->di.message())
//				.map(msg->msg.toFlatString(ImmutableFactory.getInstance()).getBytes())
				.doOnCancel(()->System.err.println("Cancel at toplevel"))
				.compose(StreamDocument.inNavajo(context.service, context.username, Optional.empty()))
				.lift(StreamDocument.filterMessageIgnore())
				.lift(StreamDocument.serialize())
				.compose(StreamCompress.compress(responseEncoding))
				.subscribe(responseSubscriber);
				break;
			case EMPTY:
			case MSGSTREAM:
			case EVENT:
			default:
				rs.execute(context)
					.onErrorResumeNext(cc)
					.map(di->di.event())
//					.compose(StreamDocument.inNavajo(context.service, context.username, Optional.empty()))
					.lift(StreamDocument.filterMessageIgnore())
					.lift(StreamDocument.serialize())
					.compose(StreamCompress.compress(responseEncoding))
					.onErrorResumeNext(new Function<Throwable, Publisher<? extends byte[]>>() {
						@Override
						public Publisher<? extends byte[]> apply(Throwable e1) throws Exception {
							return errorMessage(true,context, Optional.of(e1), e1.getMessage())
							.lift(StreamDocument.serialize())
							.compose(StreamCompress.compress(responseEncoding));
						}
					})
					.subscribe(responseSubscriber);
			}
		} catch (Throwable e1) {
			logger.error("Error: ", e1);
			errorMessage(true, context, Optional.of(e1), e1.getMessage())
			.lift(StreamDocument.serialize())
			.compose(StreamCompress.compress(responseEncoding))
			.subscribe(responseSubscriber);	
//			ac.complete();
//			request.getInputStream().close();
//			response.setStatus(501);
//			response.getOutputStream().close();
//			throw new ServletException("Servlet problem", e1);
		}
	}


	private ReactiveScript buildScript(StreamScriptContext context, boolean debug) throws IOException {
		return reactiveScriptEnvironment.build(context.service, debug);

	}

	public void authenticate(StreamScriptContext context, String password, String authHeader, String tenant) throws AuthorizationException {
		Access a = new Access(-1,-1,context.username.orElse(null),context.service,"stream","ip","hostname",null,false,"access");
		if(tenant==null) {
			throw new AuthorizationException(true, false, context.username.orElse(null), "Can not authenticate without tenant!");
		}
		a.setTenant(tenant);
		a.setInDoc(NavajoFactory.getInstance().createNavajo());
		a.rpcPwd = password;
		authMethodBuilder.getInstanceForRequest(authHeader).process(a);
	}

	private static Flowable<NavajoStreamEvent> errorMessage(boolean wrapNavajo, StreamScriptContext context, Optional<Throwable> throwable, String message) {

		String service = context.service;
		Flowable<NavajoStreamEvent> result = Msg.create("error")
				.with(Prop.create("code","-1","integer" ))
				.with(Prop.create("description", message))
				.with(Prop.create("service", service))
				.with(Prop.create("exception", throwable.isPresent()?throwable.get().getMessage():""))
				.stream()
//				.concatWith(Observable.just(Events.done()))
				.toFlowable(BackpressureStrategy.BUFFER);
		if(wrapNavajo) {
			result = result.compose(StreamDocument.inNavajo(context.service, context.username,Optional.of("")));
		}
		return result;
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
		if (acceptedEncodings.contains("gzip")) {
			return Optional.of("gzip");
		}
		return Optional.empty();
	}

	private StreamScriptContext determineContextFromRequest(AsyncContext ac) throws IOException {
		final HttpServletRequest req = (HttpServletRequest) ac.getRequest();
		Map<String, Object> attributes = extractHeaders(req);
		String tenant = determineTenantFromRequest(req);
		String username = (String) attributes.get("X-Navajo-Username");
		String password = (String) attributes.get("X-Navajo-Password");
		String serviceHeader = (String) attributes.get("X-Navajo-Service");

		boolean isGet = "GET".equals(req.getMethod());
		if(serviceHeader == null) {
			throw new NullPointerException("Missing service header. Streaming Endpoint requires a 'X-Navajo-Service' header");
		}
		if(username == null) {
			throw new NullPointerException("Missing username header. Streaming Endpoint requires a 'X-Navajo-Username' header");
		}
		if(password == null) {
			throw new NullPointerException("Missing username header. Streaming Endpoint requires a 'X-Navajo-Password' header");
		}
		if(tenant == null) {
			throw new NullPointerException("Missing tenant header. Streaming Endpoint requires a tenant");
		}

		if(isGet) {
			return new StreamScriptContext(tenant,serviceHeader, Optional.ofNullable(username), Optional.ofNullable(password),attributes,Optional.of(Flowable.<NavajoStreamEvent>empty().compose(StreamDocument.inNavajo(serviceHeader, Optional.of(username), Optional.of(password)))),Optional.empty(), Optional.of((ReactiveScriptRunner)this.reactiveScriptEnvironment), Collections.emptyList());
		}
		String requestEncoding = (String) attributes.get("Content-Encoding");

//		Flowable<NavajoStreamEvent> input = Servlets.createFlowable(ac, 1000)
		Flowable<NavajoStreamEvent> input = getBlockingInput(req)
				.doOnComplete(()->{
					System.err.println("Blocking input complete");
				})
			.compose(StreamCompress.decompress(Optional.ofNullable(requestEncoding)))
			.doOnComplete(()->{
				System.err.println("Input decompress complete");
			})
			.lift(XML.parseFlowable(10))
			.concatMap(e->e)
			.lift(StreamDocument.parse())
			.concatMap(e->e);
		
		return new StreamScriptContext(tenant,serviceHeader, Optional.ofNullable(username), Optional.ofNullable(password),attributes,Optional.of(input),Optional.empty(), Optional.of(this.reactiveScriptEnvironment), Collections.emptyList());
	}

	private Flowable<byte[]> getBlockingInput(HttpServletRequest request) {
		String requestEncoding = (String) request.getHeader("Content-Encoding");

		System.err.println("Requestencoding: "+requestEncoding);
		try {
			return Bytes.from(request.getInputStream())
					.doOnComplete(()->System.err.println("COMPLETE input"));
		} catch (IOException e) {
			return Flowable.error(e);
		}
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
