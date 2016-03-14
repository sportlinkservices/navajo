package com.dexels.navajo.listeners.stream.impl;

import com.dexels.navajo.document.stream.api.NavajoHead;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;

import rx.Observable.Operator;
import rx.Subscriber;

public class ObservableAuthenticationHandler implements Operator<NavajoStreamEvent,NavajoStreamEvent> {
	

	private String authorizationObject = null;
	private String scriptName;

	private NavajoStreamEvent authorize(NavajoStreamEvent streamEvent) {
		if(streamEvent.type()==NavajoStreamEvent.NavajoEventTypes.NAVAJO_STARTED) {
			
			NavajoHead header = (NavajoHead) streamEvent.body();
			this.authorizationObject  = header.username()+"|"+header.password();
			this.scriptName = header.name();
		}
		return streamEvent.withAttribute("Auth",authorizationObject).withAttribute("Script", scriptName);
	}

	@Override
	public Subscriber<? super NavajoStreamEvent> call(Subscriber<? super NavajoStreamEvent> subscriber) {
		return new Subscriber<NavajoStreamEvent>(subscriber) {

			@Override
			public void onCompleted() {
				subscriber.onCompleted();
			}

			@Override
			public void onError(Throwable t) {
				subscriber.onError(t);
			}

			@Override
			public void onNext(NavajoStreamEvent streamEvent) {
				NavajoStreamEvent authorize = authorize(streamEvent);
				Object authAttribute = authorize.attribute("Auth");
				if("username|pw".equals( authAttribute)) {
					subscriber.onNext(authorize);
				} else {
					subscriber.onError(new Exception("Auth error!"));
				}
			}
		};
	}

	public String getScriptName() {
		return scriptName;
	}

}
