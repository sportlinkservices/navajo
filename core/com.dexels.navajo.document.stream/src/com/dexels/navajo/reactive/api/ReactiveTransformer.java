/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.reactive.api;

import java.util.Optional;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.api.StreamScriptContext;

import io.reactivex.FlowableTransformer;

public interface ReactiveTransformer {

	public FlowableTransformer<DataItem,DataItem> execute(StreamScriptContext context,Optional<ImmutableMessage> current, ImmutableMessage param);
	public TransformerMetadata metadata();
	public ReactiveParameters parameters();
	default public Optional<String> mimeType() {
		return Optional.empty();
	}
}
