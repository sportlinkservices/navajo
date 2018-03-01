package com.dexels.navajo.reactive.kafka;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dexels.kafka.api.OffsetQuery;
import com.dexels.navajo.document.nanoimpl.XMLElement;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.reactive.api.ReactiveMerger;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveSource;
import com.dexels.navajo.reactive.api.ReactiveSourceFactory;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.pubsub.rx2.api.TopicSubscriber;

import io.reactivex.functions.Function;

public class SubscriberReactiveSourceFactory implements ReactiveSourceFactory {

	public SubscriberReactiveSourceFactory() {}


	private TopicSubscriber topicSubscriber;
	private OffsetQuery offsetQuery;
	private Map<String, Object> subscriberSettings;
	

    public void setTopicSubscriber(TopicSubscriber topicSubscriber, Map<String,Object> settings) {
        this.topicSubscriber = topicSubscriber;
        this.subscriberSettings = settings;
    }

    public void clearTopicSubscriber(TopicSubscriber topicSubscriber) {
        this.topicSubscriber = null;
    }

    public void setOffsetQuery(OffsetQuery offsetQuery) {
        this.offsetQuery = offsetQuery;
    }

    public void clearOffsetQuery(OffsetQuery offsetQuery) {
        this.offsetQuery = null;
    }

    
	@Override
	public ReactiveSource build(String relativePath, String type, Optional<XMLElement> x, ReactiveParameters params,
			List<ReactiveTransformer> transformers, Type finalType, Function<String, ReactiveMerger> reducerSupplier) {
		return new SubscriberReactiveSource(topicSubscriber,Optional.ofNullable(this.offsetQuery), params,relativePath,x,finalType,transformers,reducerSupplier,subscriberSettings);
	}

	@Override
	public Type sourceType() {
		return Type.DATA;
	}

}
