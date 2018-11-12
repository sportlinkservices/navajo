package com.dexels.navajo.reactive.transformer.other;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.document.stream.ReactiveParseProblem;
import com.dexels.navajo.reactive.api.ReactiveBuildContext;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.ReactiveTransformerFactory;
import com.dexels.navajo.reactive.api.TransformerMetadata;

public class IntervalTransformerFactory  implements ReactiveTransformerFactory, TransformerMetadata {

    @Override
    public ReactiveTransformer build(Type parentType, String relativePath, List<ReactiveParseProblem> problems, ReactiveParameters parameters, Optional<XMLElement> xml,
            ReactiveBuildContext buildContext) {

        return new IntervalTransformer(this,parameters,xml);
    }

    @Override
    public Set<Type> inType() {
        return new HashSet<>(Arrays.asList(new Type[] {DataItem.Type.MESSAGE}));
    }

    @Override
    public Type outType() {
        return DataItem.Type.MESSAGE;
    }

    @Override
    public Optional<List<String>> allowedParameters() {
        return Optional.of(Arrays.asList(new String[] {"delay", "debug"}));
    }

    @Override
    public Optional<List<String>> requiredParameters() {
        return Optional.of(Arrays.asList(new String[] {"delay"}));
    }

    @Override
    public Optional<Map<String, String>> parameterTypes() {
        Map<String, String> r = new HashMap<>();
        r.put("delay", Property.INTEGER_PROPERTY);
        r.put("debug", Property.BOOLEAN_PROPERTY);
        return Optional.of(Collections.unmodifiableMap(r));
    }


    @Override
    public String name() {
        return "interval";
    }


}
