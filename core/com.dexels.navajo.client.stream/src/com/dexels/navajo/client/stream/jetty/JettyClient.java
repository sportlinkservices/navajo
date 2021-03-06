/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.client.stream.jetty;

import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.reactive.client.ContentChunk;
import org.eclipse.jetty.reactive.client.ReactiveRequest;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.client.stream.ReactiveReply;

import io.reactivex.Emitter;
import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;
import io.reactivex.Single;

public class JettyClient {

	private final HttpClient httpClient = new HttpClient(new SslContextFactory.Client());
	private AtomicLong sent = new AtomicLong();
	private AtomicLong received = new AtomicLong();

	
	private static final Logger logger = LoggerFactory.getLogger(JettyClient.class);

	public JettyClient() throws Exception {
		httpClient.start();
	}

	public Single<ReactiveReply> callWithoutBody(String uri, UnaryOperator<Request> buildRequest) {
		return call(uri,buildRequest, Optional.empty(), Optional.empty()).firstOrError();
	}

	public Flowable<byte[]> callWithoutBodyToStream(String uri, UnaryOperator<Request> buildRequest) {
		return call(uri,buildRequest, Optional.empty(), Optional.empty())
				.compose(this.responseStream());
	}
	
	public Flowable<ReactiveReply> callWithBody(String uri, UnaryOperator<Request> buildRequest,Flowable<byte[]> requestBody,String requestContentType) {
		return call(uri,buildRequest,Optional.of(requestBody),Optional.of(requestContentType));
	}
	public Flowable<byte[]> callWithBodyToStream(String uri, UnaryOperator<Request> buildRequest,Flowable<byte[]> requestBody,String requestContentType) {
		return call(uri,buildRequest,Optional.of(requestBody),Optional.of(requestContentType))
				.compose(this.responseStream())
				
				;
	}
	public  Flowable<ReactiveReply> call(String uri,UnaryOperator<Request> buildRequest,Optional<Flowable<byte[]>> requestBody,Optional<String> requestContentType) {
//		Reque
		Request req = httpClient.newRequest(uri);
		Request reqProcessed = buildRequest.apply(req);
		if(requestContentType.isPresent()) {
			reqProcessed = reqProcessed.header("Content-Type", requestContentType.get());
		}
		ReactiveRequest.Builder requestBuilder = ReactiveRequest.newBuilder(reqProcessed);
		if(requestBody.isPresent()) {
			Publisher<ContentChunk> bb = requestBody.get()
					.doOnNext(b->this.sent.addAndGet(b.length))
					.map(e->new ContentChunk(ByteBuffer.wrap(e)));
			requestBuilder = requestBuilder.content(ReactiveRequest.Content.fromPublisher(bb, requestContentType.orElse("application/octet-stream")));
		}
		ReactiveRequest request = requestBuilder.build();
		return Flowable.fromPublisher(request.response((response, content) -> Flowable.just(new ReactiveReply(response,content,b->this.sent.addAndGet(b.length)))))
				.doOnComplete(
						()->logger.info("HTTP Client to {}: sent: {} received: {}",uri,sent.get(),received.get())
					);
	}

	public FlowableTransformer<ReactiveReply, byte[]> responseStream() {
		return single->single.flatMap(e->e.content).map(c->this.streamResponse(c)	).flatMap(e->e);
	}

	
	private Flowable<byte[]> streamResponse(ContentChunk chunk) {
		
		return Flowable.generate((Emitter<byte[]> emitter) -> {
            ByteBuffer buffer = chunk.buffer;
            if (buffer.hasRemaining()) {
                emitter.onNext(getByteArrayFromByteBuffer(buffer));
            } else {
                chunk.callback.succeeded();
                emitter.onComplete();
            }
        });
	}

	public void close() throws Exception {
		this.httpClient.stop();
	}
	


    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

}
