package com.dexels.navajo.resource.swift.impl;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.stream.StreamDocument;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.document.types.BinaryDigest;
import com.dexels.navajo.resource.binarystore.BinaryStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import hu.akarnokd.rxjava.interop.RxJavaInterop;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.HttpMethod;
import io.reactivex.Flowable;
import io.reactivex.netty.protocol.http.client.HttpClient;
import io.reactivex.netty.protocol.http.client.HttpClientResponse;
import rx.Observable;
import rx.Observable.Operator;
import rx.Subscriber;

public class SwiftReactiveImpl implements BinaryStore {

	private static final ObjectMapper mapper = new ObjectMapper();
	private HttpClient<ByteBuf, ByteBuf> client;
	private String accessToken;
	private String tenantId;
	private String container;
	private URL endpointURL;

	
	private final static Logger logger = LoggerFactory.getLogger(SwiftReactiveImpl.class);

	
	@Override
	public Binary resolve(BinaryDigest digest) {
		String path = "/v1/"+tenantId+"/"+container+"/"+digestToPath(digest);
		return requestWithResponse(path, HttpMethod.GET)
				.doOnError(e->e.printStackTrace())
				.toObservable()
				.lift(StreamDocument.createBinary())
				.blockingFirst();
	}

	private void ensureContainerExists() {
		String path = "/v1/"+tenantId+"/"+container;
		Map<String, String> result =  request(path, HttpMethod.GET).toBlocking().first();
		String codeString = result.get("code");
		int code = Integer.parseInt(codeString);
		if(code>=200 && code < 300) {
			return;
		}
//		createContainer();
	}
	
	public void createContainer() {
		String path = "/v1/"+tenantId+"/"+container;
		Map<String, String> result =  requestWithBody(path, HttpMethod.PUT,rx.Observable.just(new byte[]{}))
				.doOnError(e->e.printStackTrace())
				.toBlocking()
				.first();
		logger.info("Result: "+result);
	}

	@Override
	public void store(Binary b) {
		String path = "/v1/"+tenantId+"/"+container+"/"+digestToPath(b.getDigest());
		Map<String, String> result = requestWithBinaryBody(path,HttpMethod.PUT,b,1024).toBlocking().first();
		logger.info("Result: "+result);
		String code = result.get("code");
		logger.info("Code: "+code);
	}

	@Override
	public void delete(BinaryDigest b) {
		Map<String,String> result = request(digestToPath(b), HttpMethod.DELETE)
				.toBlocking()
				.first();
		result.get("code");
	}


	@Override
	public Map<String, String> metadata(BinaryDigest b) {
		return request(digestToPath(b), HttpMethod.HEAD).toBlocking().first();
	}

	public void activate(Map<String,Object> settings) throws IOException {
		String endpoint = (String) settings.get("endpoint");
		String username = (String) settings.get("username");
		String apiKey = (String) settings.get("apiKey");
		String container = (String) settings.get("container");
		configure(endpoint, username, apiKey, container);
	}
	
	public void configure(String endpoint, String username, String apiKey, String container) throws IOException {
		ObjectNode authNode = authenticate(endpoint, username, apiKey);
        
		Optional<String> swiftEndpoint = findObjectStoreURL(authNode,false);
		this.container = container;
		this.tenantId = findTenantId(authNode).get();
        this.accessToken = getAuthToken(authNode);
        logger.info("Swift endpoint: "+swiftEndpoint.get());
        this.client = createClientFromURL(swiftEndpoint.get());
        ensureContainerExists();
	}

	// HEAD / DELETE
	private Observable<Map<String,String>> request(String path, HttpMethod method) {
		return this.client
			.createRequest(method, path)
		    .addHeader("X-Auth-Token", accessToken)
			.asObservable()
			.doOnNext(e->logger.info("Some kind of response: "+e))
			.map(this::responseHeaders);
	}

	private Flowable<byte[]> requestWithResponse(String path, HttpMethod method) {
		return RxJavaInterop.toV2Flowable(this.client
			.createRequest(method, path)
		    .addHeader("X-Auth-Token", accessToken)
			.asObservable()
			.concatMapEager(e->e.getContent().asObservable())
			.map(buf->{
				byte[] bytes = new byte[buf.readableBytes()];
				buf.readBytes(bytes);
				return bytes;
			}))
//			.lift(StreamDocument.createBinary())
				
				;
			
			
//			.lift(parseBinary());
	}

	private Observable<Map<String,String>> requestWithBinaryBody(String path, HttpMethod method, Binary input, int bufferSize) {
		Observable<byte[]> body = Observable.from(input.getDataAsIterable(bufferSize));
		return requestWithBody(path, method, body);
	}

	private Observable<Map<String, String>> requestWithBody(String path, HttpMethod method, rx.Observable<byte[]> body) {
		return this.client
				.createRequest(method, path)
			    .addHeader("X-Auth-Token", accessToken)
			    .writeBytesContent(body)
			    .asObservable()
				.map(this::responseHeaders);
	}

	private String digestToPath(BinaryDigest digest) {
		String path = digest.hex();
		return path.substring(0, 2) + "/" + path;
	}
	
	private Map<String,String> responseHeaders(HttpClientResponse<ByteBuf> response) {
		Map<String,String> result = new HashMap<>();
		String resultCode = Integer.toString(response.getStatus().code());
		result.put("code", resultCode);
		for (String header : response.getHeaderNames()) {
			result.put(header, response.getHeader(header));
		} 
		int code = Integer.parseInt(resultCode); 
		if(code>400) {
			throw new RuntimeException("Error performing request. Response headers: "+result);
		}
		return result;
	}

//	private Observable<Binary> getBinaryByPath(String path, Optional<String> swiftEndpoint, Optional<String> tenantId,
//			String accessToken) {
//		return createClientFromURL(swiftEndpoint.get())
//		    .createGet("/v1/"+tenantId.get()+"/"+path)
//		    .addHeader("X-Auth-Token", accessToken)
//		    .lift(parseBinary())
//		    ;
//	}

	
	private static String getAuthToken(ObjectNode authNode) {
		return authNode.get("access").get("token").get("id").asText();
	}

	private static Optional<String> findObjectStoreURL(ObjectNode authNode, boolean internal) {
		ArrayNode services = (ArrayNode) authNode.get("access").get("serviceCatalog");
		for (JsonNode jsonNode : services) {
			ObjectNode e = (ObjectNode)jsonNode;
			if("object-store".equals( e.get("type").asText())) {
				logger.info("FOUND!");
				return Optional.of(((ArrayNode)e.get("endpoints")).get(0).get(internal?"internalURL":"publicURL").asText());
			}
		}
		return Optional.empty();
	}
	
	private static Optional<String> findTenantId(ObjectNode authNode) {
		ArrayNode services = (ArrayNode) authNode.get("access").get("serviceCatalog");
		for (JsonNode jsonNode : services) {
			ObjectNode e = (ObjectNode)jsonNode;
			if("object-store".equals( e.get("type").asText())) {
				logger.info("FOUND!");
				return Optional.of(((ArrayNode)e.get("endpoints")).get(0).get("tenantId").asText());
			}
		}
		return Optional.empty();
	}
	private ObjectNode authenticate(String endpoint, String username, String apiKey)
			throws JsonProcessingException {
		HttpClient<ByteBuf, ByteBuf> authClient = createClientFromURL(endpoint);

		
		ObjectNode node = mapper.createObjectNode();
		ObjectNode auth = mapper.createObjectNode();
		ObjectNode passwordCredentials = mapper.createObjectNode();
		passwordCredentials.put("username", username);
		passwordCredentials.put("apiKey", apiKey);
		auth.set("RAX-KSKEY:apiKeyCredentials", passwordCredentials);
		node.set("auth", auth);

        ObjectNode authNode = authClient 
        	.createPost("/v2.0/tokens")
		    .addHeader("Content-Type", "application/json")
		    .writeStringContent(Observable.just(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node)))
		    .doOnNext(re->logger.info(":: "+re.toString()))
		    .concatMapEager(resp->resp.getContent().map(a->a.toString(Charset.defaultCharset())))
		    .reduce(new StringBuffer(),(sb,s)->sb.append(s))
        	.map(sb->extractKey(sb.toString()))
        	.toBlocking()
        	.first();
		return authNode;
	}

	private HttpClient<ByteBuf, ByteBuf> createClientFromURL(String endpoint) {
		try {
			endpointURL = new URL(endpoint);
		} catch (Exception e) {
			throw new UnsupportedOperationException("Unsupported URL: "+endpoint,e);
		}
		boolean secure = "https".equals(endpointURL.getProtocol());
		String host = endpointURL.getHost();
		int port = endpointURL.getPort();
		if(port==-1) {
			switch (endpointURL.getProtocol()) {
			case "https":
				port = 443;
				break;
			case "http":
				port = 80;
				break;
			default:
				throw new UnsupportedOperationException("Unsupported protocol: "+endpointURL.getProtocol());
			}
		}
		HttpClient<ByteBuf,ByteBuf> authClient = secure? HttpClient.newClient(host,port).unsafeSecure() : HttpClient.newClient(host, port);
//		HttpClient<ByteBuf,ByteBuf> authClient = secure? HttpClient.newClient(host,port).secure(defaultSSLEngineForClient(host,port)) : HttpClient.newClient(host, port);
		return authClient;
	}
	
	private static Operator<Binary,HttpClientResponse<ByteBuf>> parseBinary() {
		return new Operator<Binary,HttpClientResponse<ByteBuf>>(){

			@Override
			public Subscriber<? super HttpClientResponse<ByteBuf>> call(Subscriber<? super Binary> out) {
				return new Subscriber<HttpClientResponse<ByteBuf>>(){

					@Override
					public void onCompleted() {
//						out.onCompleted();
					}

					@Override
					public void onError(Throwable e) {
						out.onError(e);
					}

					@Override
					public void onNext(HttpClientResponse<ByteBuf> response) {
						Map<String,String> headers = new HashMap<>();
						for (String name : response.getHeaderNames()) {
							headers.put(name, response.getHeader(name));
						}
						logger.info("Code: "+headers.get("code")+" all headers: "+headers+" status: "+response.getStatus());
						int cc = response.getStatus().code();
						if(cc>=400) {
							out.onError(new RuntimeException("Error code in HTTP: "+cc+" headers: "+headers));
							
						}
						Binary result = new Binary();
						result.startBinaryPush();
						result.setMimeType(response.getHeader("Content-Type"));
						response.getContent()
							.map(SwiftReactiveImpl::byteBufToByteArray)
							.subscribe(
								b->result.pushContent(b)
							, e->out.onError(e)
							, ()->{
								try {
									result.finishPushContent();
									out.onNext(result);
								} catch (Exception e) {
									out.onError(e);
								}
							}
							);
//						out.onNext(result);
					}};
			}};
	}

	private static ObjectNode extractKey(String response) {
		ObjectNode node;
		try {
			node = (ObjectNode) mapper.readTree(response);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return node;
		
	}
	
	private static byte[] byteBufToByteArray(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		int readerIndex = buf.readerIndex();
		buf.getBytes(readerIndex, bytes);
		return bytes;
	}
//    private static SSLEngine defaultSSLEngineForClient(String host, int port) {
//        try {
//        		SSLContext sslCtx = SSLContext.getDefault();
//		    SSLEngine sslEngine = sslCtx.createSSLEngine(host, port);
//			sslEngine.setUseClientMode(true);
//			return sslEngine;
//		} catch (NoSuchAlgorithmException e) {
//			e.printStackTrace();
//			return null;
//		}
//    }
}
