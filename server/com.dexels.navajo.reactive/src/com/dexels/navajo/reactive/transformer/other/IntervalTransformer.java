package com.dexels.navajo.reactive.transformer.other;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.dexels.immutable.factory.ImmutableFactory;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveResolvedParameters;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.TransformerMetadata;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;

public class IntervalTransformer implements ReactiveTransformer {

    private final ReactiveParameters parameters;
    private final TransformerMetadata metadata;

    public IntervalTransformer(TransformerMetadata metadata, ReactiveParameters parameters) {
        this.parameters = parameters;
        this.metadata = metadata;
    }

    @Override
    public FlowableTransformer<DataItem, DataItem> execute(StreamScriptContext context) {
        ReactiveResolvedParameters parms = parameters.resolveNamed(context, Optional.empty(), ImmutableFactory.empty(), metadata, Optional.empty(), "");
        int delay = parms.paramInteger("delay");
        boolean debug = parms.optionalBoolean("debug").orElse(false);
        return e-> Flowable.interval(delay,  TimeUnit.MILLISECONDS)
                .doOnNext(counter -> {
                    if (debug) System.err.println("Forwaring itemnr: " + counter + " after sleeping " + delay);
                })
                .flatMap(counter -> e.take(1));
    }

    @Override
    public TransformerMetadata metadata() {
        return metadata;
    }
}
