package com.dexels.navajo.resource.http.stream;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.document.stream.ReactiveParseProblem;
import com.dexels.navajo.reactive.api.ReactiveBuildContext;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.ReactiveTransformerFactory;
import com.dexels.navajo.reactive.api.TransformerMetadata;

public class HttpPushStreamTransformerFactory implements ReactiveTransformerFactory, TransformerMetadata {

	@Override
	public ReactiveTransformer build(Type parentType, String relativePath, List<ReactiveParseProblem> problems,ReactiveParameters parameters, Optional<XMLElement> xml,
			ReactiveBuildContext buildContext) {

		return new HttpPushTransformer(this,relativePath, problems, parameters, xml,buildContext.useGlobalInput);
	}

	@Override
	public Set<Type> inType() {
		return new HashSet<>(Arrays.asList(new Type[] {Type.DATA})) ;
	}

	@Override
	public Type outType() {
		return Type.SINGLEMESSAGE;
	}

	@Override
	public Optional<List<String>> allowedParameters() {
		return Optional.of(Arrays.asList(new String[]{"name","method","bucket","id"}));
	}

	@Override
	public Optional<List<String>> requiredParameters() {
		return Optional.of(Collections.emptyList());
	}

	@Override
	public Optional<Map<String, String>> parameterTypes() {
		return Optional.of(Collections.emptyMap());
	}
	

	@Override
	public String name() {
		return "httppush";
	}
}
