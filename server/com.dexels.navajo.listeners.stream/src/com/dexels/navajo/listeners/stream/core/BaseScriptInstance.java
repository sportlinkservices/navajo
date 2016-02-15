package com.dexels.navajo.listeners.stream.core;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.stream.events.Events;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public abstract class BaseScriptInstance {

	private final Map<String,Set<Action1<Message>>> messageRegistrations = new HashMap<>();
	private final Map<String,Set<Action1<Message>>> elementRegistrations = new HashMap<>();

	Navajo outputAssembly = NavajoFactory.getInstance().createNavajo();

//	public void run(Observable<NavajoStreamEvent> input, Subscriber<NavajoStreamEvent> output) {
//		output.onNext(navajoStarted());
//	}
//	
	
	public Observable<NavajoStreamEvent> init() {
		return Observable.<NavajoStreamEvent>empty();
	};
	public Observable<NavajoStreamEvent> complete() {
		return Observable.<NavajoStreamEvent>empty();
	};
	
	public Observable<NavajoStreamEvent> runInternal(NavajoStreamEvent streamEvent) {
		switch (streamEvent.type()) {
		case MESSAGE:
			Set<Action1<Message>> regs = messageRegistrations.get(streamEvent.path());
			if(regs!=null) {
				regs.forEach(e->e.call((Message)streamEvent.body()));
			}
			break;
		case ARRAY_ELEMENT:
			Set<Action1<Message>> arrayRegs = elementRegistrations.get(streamEvent.path());
			if(arrayRegs!=null) {
				arrayRegs.forEach(e->e.call((Message)streamEvent.body()));
			}
			break;
		case NAVAJO_DONE:
			return complete().concatWith(Observable.<NavajoStreamEvent>just(streamEvent));
//			output.onNext(streamEvent);
		default:
			break;
		}
		return Observable.<NavajoStreamEvent>empty();
	}

	public Observable<NavajoStreamEvent> run(final NavajoStreamEvent streamEvents) {
		return runInternal(streamEvents);
	}
	
	public void registerArrayStream(String path,Action1<Message> onElement, Action0 done) {
		
	}

	public void onMessage(String path, Action1<Message> cb) {
		Set<Action1<Message>> callbacks = messageRegistrations.get(path);
		if(callbacks==null) {
			callbacks = new HashSet<>();
			messageRegistrations.put(path,callbacks);
		}
		callbacks.add(cb);
	}
	
	public void onElement(String path, Action1<Message> cb) {
		Set<Action1<Message>> callbacks = messageRegistrations.get(path);
		if(callbacks==null) {
			callbacks = new HashSet<>();
			elementRegistrations.put(path,callbacks);
		}
		callbacks.add(cb);
	}


	protected Observable<NavajoStreamEvent> array(String name, Func0<Observable<NavajoStreamEvent>> func) {
		NavajoStreamEvent startEvent = Events.arrayStarted(name );
		NavajoStreamEvent arrayDone = Events.arrayDone(name );
		return func.call().startWith(Observable.just(startEvent)).concatWith(Observable.just(arrayDone));
	}
	
//	protected Observable<NavajoStreamEvent> element(String name,Func1<Msg,Observable<NavajoStreamEvent>> action) {
//		Msg.
//		Observable<NavajoStreamEvent> elements = action.call(element);
////		return Observable.<NavajoStreamEvent>just(EventFactory.arrayElementStarted(element, name), EventFactory.arrayElement(element, name));
//		return elements.startWith(Events.arrayElementStarted(element, name)).concatWith(Observable.just( Events.arrayElement(element, name)));
////		return  Observable.just(EventFactory.arrayElementStarted(element, name), EventFactory.arrayElement(element, name)); //EventFactory.arrayElement(element, "Company");
//
//	}

	protected Observable<NavajoStreamEvent> callInit(String name) {
		return Observable.<NavajoStreamEvent>create(subscriber-> {
			
		}).subscribeOn(Schedulers.io());
	}
	
	protected Message createMessage(String name) {
		return NavajoFactory.getInstance().createMessage(outputAssembly, name);
	}
	
	protected Property createProperty(String name,Object value) {
		Property prop = NavajoFactory.getInstance().createProperty(outputAssembly, name, Property.STRING_PROPERTY, "", -1, "", Property.DIR_OUT);
		prop.setAnyValue(value);
		return prop;
	}
	// TODO Name should be removed at some point
//	protected Observable<NavajoStreamEvent> emitElement(Message element, String name) {
//		return Observable.<NavajoStreamEvent>just(Events.arrayElementStarted(name ), Events.arrayElement(element,name ));
//	}
	
	
	protected Message createElement() {
		return NavajoFactory.getInstance().createMessage(null, "DefaultName", Message.MSG_TYPE_ARRAY_ELEMENT);
	}
	
}
