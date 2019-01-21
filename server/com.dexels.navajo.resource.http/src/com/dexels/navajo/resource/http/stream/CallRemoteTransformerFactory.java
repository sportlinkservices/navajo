package com.dexels.navajo.resource.http.stream;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.dexels.navajo.client.stream.jetty.JettyClient;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.document.stream.ReactiveParseProblem;
import com.dexels.navajo.reactive.api.ReactiveBuildContext;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.ReactiveTransformerFactory;

public class CallRemoteTransformerFactory implements ReactiveTransformerFactory {

	private JettyClient client;

	public CallRemoteTransformerFactory() throws Exception {
		this.client = new JettyClient();

	}

	@Override
	public ReactiveTransformer build(List<ReactiveParseProblem> problems,
			ReactiveParameters parameters) {

		return new CallRemoteTransformer(this,client,parameters);
	}
	@Override
	public Optional<List<String>> allowedParameters() {
		return Optional.of(Arrays.asList(new String[]{"service","debug","server","username","password"}));
	}

	@Override
	public Optional<List<String>> requiredParameters() {
		return Optional.of(Arrays.asList(new String[]{"service","server","username","password"}));
	}

	@Override
	public Optional<Map<String, String>> parameterTypes() {
		Map<String,String> r = new HashMap<String, String>();
		r.put("service", Property.STRING_PROPERTY);
		r.put("debug", Property.BOOLEAN_PROPERTY);
		r.put("server", Property.STRING_PROPERTY);
		r.put("username", Property.STRING_PROPERTY);
		r.put("password", Property.STRING_PROPERTY);
		return Optional.of(r);
	}
	
	@Override
	public Set<Type> inType() {
		return new HashSet<>(Arrays.asList(new Type[] {Type.EVENTSTREAM})) ;
	}

	@Override
	public Type outType() {
		return Type.EVENTSTREAM;
	}

	@Override
	public String name() {
		return "call";
	}

	
}
