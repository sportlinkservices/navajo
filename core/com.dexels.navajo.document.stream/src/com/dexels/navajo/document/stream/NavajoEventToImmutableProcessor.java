/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.navajo.document.stream.api.Msg;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;

public class NavajoEventToImmutableProcessor {

	Stack<String> pathStack = new Stack<>();
	AtomicInteger arrayCounter = new AtomicInteger();
	List<ImmutableMessage> currentArray  = new ArrayList<>();
	Map<String,ImmutableMessage> submessages = new HashMap<>();
	Map<String,List<ImmutableMessage>> submessageLists = new HashMap<>();

	public void event(NavajoStreamEvent event) {
		switch(event.type()) {
			case ARRAY_STARTED:
				arrayCounter.set(0);
				currentArray.clear();
			case MESSAGE_STARTED:
			case MESSAGE_DEFINITION_STARTED:
				// TODO ignore definition messages?
				pathStack.push(event.path());
			case ARRAY_ELEMENT_STARTED:
				
				break;
			case MESSAGE:
			case ARRAY_ELEMENT:
				Msg m = (Msg)event.body();
				arrayCounter.incrementAndGet();
				m.toImmutableMessage();
				break;
				
			case NAVAJO_DONE:
			case NAVAJO_STARTED:
			case BINARY_STARTED:
			case BINARY_CONTENT:
			case BINARY_DONE:
			case ARRAY_DONE:
			case MESSAGE_DEFINITION:
				return;
				
			default:
				throw new UnsupportedOperationException("Unknown event found in NAVADOC: "+event.type());
		}
//	}
	}

}
