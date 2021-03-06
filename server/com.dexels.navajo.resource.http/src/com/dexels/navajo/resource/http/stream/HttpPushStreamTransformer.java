/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.resource.http.stream;

import java.util.Optional;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.immutable.factory.ImmutableFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.reactive.api.ReactiveParameters;
import com.dexels.navajo.reactive.api.ReactiveResolvedParameters;
import com.dexels.navajo.reactive.api.ReactiveTransformer;
import com.dexels.navajo.reactive.api.TransformerMetadata;
import com.dexels.navajo.resource.http.HttpResourceFactory;

import io.reactivex.Flowable;
import io.reactivex.FlowableTransformer;

public class HttpPushStreamTransformer implements ReactiveTransformer {


	private final TransformerMetadata metadata;
	private final ReactiveParameters parameters;

	public HttpPushStreamTransformer(TransformerMetadata metadata, ReactiveParameters parameters) {
		this.parameters = parameters;
		this.metadata = metadata;
	}


	@Override
	public FlowableTransformer<DataItem, DataItem> execute(StreamScriptContext context, Optional<ImmutableMessage> current,ImmutableMessage param) {
		ReactiveResolvedParameters resolved = parameters.resolve(context,current,param,metadata);
		String name = resolved.paramString("name");
		String id = resolved.paramString("id");
		String bucket = resolved.paramString("bucket");
		String type = resolved.optionalString("type").orElse("application/octetstream");
		return flow->{
			Flowable<byte[]> in = flow.map(f->f.data());
			return HttpResourceFactory.getInstance()
					.getHttpResource(name)
					.put(context.getTenant(), bucket, id,type, in)
					.map(status->ImmutableFactory.empty().with("code", status, Property.INTEGER_PROPERTY)).map(DataItem::of)
					.toFlowable();
		};
	}

	@Override
	public TransformerMetadata metadata() {
		return metadata;
	}
	
	@Override
	public ReactiveParameters parameters() {
		return parameters;
	}


}
