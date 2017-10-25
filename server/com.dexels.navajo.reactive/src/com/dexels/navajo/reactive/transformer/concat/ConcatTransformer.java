package com.dexels.navajo.reactive.transformer.concat;

import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.DataItem.Type;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.reactive.api.ReactiveTransformer;

import io.reactivex.FlowableTransformer;

public class ConcatTransformer implements ReactiveTransformer {

	public ConcatTransformer() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public FlowableTransformer<DataItem, DataItem> execute(StreamScriptContext context) {
		return null;
	}

	@Override
	public Type inType() {
		return Type.EVENT;
	}

	@Override
	public Type outType() {
		return Type.EVENT;
	}

}
