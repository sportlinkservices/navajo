/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.reactive.kafka;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.dexels.kafka.api.OffsetQuery;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveSource;
import com.dexels.navajo.reactive.api.ReactiveSourceFactory;
import com.dexels.navajo.reactive.api.SourceMetadata;
import com.dexels.pubsub.rx2.api.TopicSubscriber;


public class SubscriberReactiveSourceFactory implements ReactiveSourceFactory, SourceMetadata {

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
	public ReactiveSource build(ReactiveParameters parameters) {
		return new SubscriberReactiveSource(this, topicSubscriber,Optional.ofNullable(this.offsetQuery), parameters,subscriberSettings);
	}


	@Override
	public Type sourceType() {
		return Type.DATA;
	}
	

	@Override
	public Optional<List<String>> allowedParameters() {
		return Optional.of(Arrays.asList(new String[] {"topic","from","to"}));
	}



	@Override
	public Optional<List<String>> requiredParameters() {
		return Optional.of(Arrays.asList(new String[] {"topic"}));
	}



	@Override
	public Optional<Map<String, String>> parameterTypes() {
		Map<String,String> types = new HashMap<>();
		types.put("topic", "string");
		types.put("group", "string");
		types.put("from", "string");
		types.put("to", "string");
		return Optional.of(Collections.unmodifiableMap(types));
	}



}
