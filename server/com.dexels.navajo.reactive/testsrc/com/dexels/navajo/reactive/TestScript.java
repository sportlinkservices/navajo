package com.dexels.navajo.reactive;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;

import com.dexels.navajo.adapters.stream.SQL;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.stream.StreamDocument;
import com.dexels.navajo.document.stream.api.ReactiveScriptRunner;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;
import com.dexels.navajo.parser.Expression;
import com.dexels.navajo.reactive.source.single.SingleSourceFactory;
import com.dexels.navajo.reactive.source.sql.SQLReactiveSourceFactory;
import com.dexels.navajo.reactive.stored.InputStreamSourceFactory;
import com.dexels.navajo.reactive.transformer.call.CallTransformerFactory;
import com.dexels.navajo.reactive.transformer.csv.CSVTransformerFactory;
import com.dexels.navajo.reactive.transformer.filestore.FileStoreTransformerFactory;
import com.dexels.navajo.reactive.transformer.mergesingle.MergeSingleTransformerFactory;
import com.dexels.navajo.reactive.transformer.single.SingleMessageTransformerFactory;
import com.dexels.navajo.reactive.transformer.stream.StreamMessageTransformerFactory;
import com.dexels.replication.factory.ReplicationFactory;
import com.dexels.replication.impl.json.JSONReplicationMessageParserImpl;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;

public class TestScript {

	private ReactiveScriptParser reactiveScriptParser;
//	private ReplicationMessage person1;
//	private ReplicationMessage person2;

	@Before
	public void setup() {
		ReplicationFactory.setInstance(new JSONReplicationMessageParserImpl());
		Expression.compileExpressions = true;
		reactiveScriptParser = new ReactiveScriptParser();
		reactiveScriptParser.addReactiveSourceFactory(new SQLReactiveSourceFactory(),"sql");
		reactiveScriptParser.addReactiveSourceFactory(new SingleSourceFactory(),"single");
		reactiveScriptParser.addReactiveSourceFactory(new InputStreamSourceFactory(),"inputstream");
		reactiveScriptParser.addReactiveTransformerFactory(new CSVTransformerFactory(),"csv");
		reactiveScriptParser.addReactiveTransformerFactory(new FileStoreTransformerFactory(),"filestore");
		reactiveScriptParser.addReactiveTransformerFactory(new MergeSingleTransformerFactory(),"mergeSingle");
		reactiveScriptParser.addReactiveTransformerFactory(new CallTransformerFactory(),"call");
		reactiveScriptParser.addReactiveTransformerFactory(new StreamMessageTransformerFactory(),"stream");
		reactiveScriptParser.addReactiveTransformerFactory(new SingleMessageTransformerFactory(),"single");
	}

	public StreamScriptContext createContext(String serviceName, Optional<ReactiveScriptRunner> runner) {
		Navajo input = NavajoFactory.getInstance().createNavajo();
		return createContext(serviceName, input,runner);
	}
	public StreamScriptContext createContext(String serviceName,Navajo input, Optional<ReactiveScriptRunner> runner) {
		Flowable<NavajoStreamEvent> inStream = Observable.just(input).lift(StreamDocument.domStream()).toFlowable(BackpressureStrategy.BUFFER);
		StreamScriptContext context = new StreamScriptContext("tenant", serviceName, Optional.of("username"), Optional.of("password"), Collections.emptyMap(), Optional.of(inStream),runner);
		return context;
	}
	
	@Test
	public void testSQL() {
		SQL.query("dummy", "KNVB", "select * from organization where rownum < 500")
			.flatMap(msg->StreamDocument.replicationMessageToStreamEvents("Organization", msg, true))
			.compose(StreamDocument.inArray("Organization"))
			.compose(StreamDocument.inNavajo("ProcessGetOrg", Optional.empty(), Optional.empty()))
			.lift(StreamDocument.serialize())
		
		.blockingForEach(e->System.err.print(new String(e)));
	}
	@Test
	public void testSimpleScript() throws IOException {
		try( InputStream in = TestScript.class.getClassLoader().getResourceAsStream("simplereactive.xml")) {
			StreamScriptContext myContext = createContext("SimpleReactiveSql",Optional.empty());
			reactiveScriptParser.parse(myContext.service, in, "serviceName").execute(myContext)
				.map(di->di.event())
				.lift(StreamDocument.serialize())
				.blockingForEach(e->System.err.print(new String(e)));
		}
	}
	
	@Test 
	public void testScript() throws IOException {
		try( InputStream in = TestScript.class.getClassLoader().getResourceAsStream("reactive.xml")) {
			StreamScriptContext myContext = createContext("AdvancedReactiveSql",Optional.empty());
			reactiveScriptParser.parse(myContext.service, in,"serviceName").execute(myContext)
				.map(di->di.event())
				.lift(StreamDocument.serialize())
				.blockingForEach(e->System.err.print(new String(e)));
		}
	}
	

	@Test
	public void testSingle() throws UnsupportedEncodingException, IOException {
		try( InputStream in = TestScript.class.getClassLoader().getResourceAsStream("single.xml")) {
			StreamScriptContext myContext = createContext("Single",Optional.empty());
			reactiveScriptParser.parse(myContext.service, in,"serviceName")
				.execute(myContext)
				.map(di->di.event())
				.lift(StreamDocument.serialize())
				.blockingForEach(e->System.err.print(new String(e)));
		}
	}
	


}


