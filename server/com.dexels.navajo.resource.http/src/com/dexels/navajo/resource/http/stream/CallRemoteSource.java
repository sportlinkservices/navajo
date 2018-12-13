package com.dexels.navajo.resource.http.stream;

import java.util.List;
import java.util.Optional;

import org.eclipse.jetty.http.HttpMethod;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.immutable.factory.ImmutableFactory;
import com.dexels.navajo.client.stream.jetty.JettyClient;
import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.document.stream.ReactiveParseProblem;
import com.dexels.navajo.document.stream.StreamDocument;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;
import com.dexels.navajo.document.stream.xml.XML;
import com.dexels.navajo.reactive.api.ReactiveMerger;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveResolvedParameters;
import com.dexels.navajo.reactive.api.ReactiveSource;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.SourceMetadata;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;

public class CallRemoteSource implements ReactiveSource {

	private final ReactiveParameters params;
	private final SourceMetadata metadata;
	private final JettyClient client;
	private final Type finalType;
	private List<ReactiveTransformer> transformers;

	public CallRemoteSource(SourceMetadata metadata, JettyClient client, String relativePath, String type, List<ReactiveParseProblem> problems,
			Optional<XMLElement> sourceElement, ReactiveParameters params, List<ReactiveTransformer> transformers, Type finalType,
			Function<String, ReactiveMerger> reducerSupplier) {
		this.metadata = metadata;
		this.client = client;
		this.params = params;
		this.finalType = finalType;
	}

	@Override
	public Flowable<DataItem> execute(StreamScriptContext context, Optional<ImmutableMessage> current,ImmutableMessage param) {
		ReactiveResolvedParameters resolved = params.resolve(context, current, ImmutableFactory.empty(), metadata);
		String server = resolved.paramString("server");
		String username = resolved.paramString("username");
		String password = resolved.paramString("password");
		final String service =  resolved.paramString("service");
		final boolean debug = resolved.paramBoolean("debug", ()->false);
		Flowable<NavajoStreamEvent> flow = client.callWithBodyToStream(server, e->
		e.header("X-Navajo-Username", username)
		 .header("X-Navajo-Password", password)
		 .header("X-Navajo-Service", service)
		 .method(HttpMethod.POST)
		, Flowable.<NavajoStreamEvent>empty()
				.compose(StreamDocument.inNavajo(service, Optional.of(username), Optional.of(password)))
				.lift(StreamDocument.serialize())
		, "text/xml")
		.doOnNext(e->System.err.println(new String(e)))
		.lift(XML.parseFlowable(10))
		.concatMap(e->e)
		.lift(StreamDocument.parse())
		.concatMap(e->e)
		.filter(e->e.type()!=NavajoStreamEvent.NavajoEventTypes.NAVAJO_STARTED && e.type()!=NavajoStreamEvent.NavajoEventTypes.NAVAJO_DONE)
		.doOnNext(e->System.err.println("ITEMMMM: "+e));

	
		Flowable<DataItem> fw =  Flowable.just(DataItem.ofEventStream(flow));
		return fw;
	}

	@Override
	public boolean streamInput() {
		return false;
	}

	@Override
	public Type sourceType() {
		return metadata.sourceType();
	}

}
