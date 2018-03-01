package com.dexels.navajo.document.stream;

import java.util.List;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.immutable.factory.ImmutableFactory;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;

public class DataItem {

	private final ImmutableMessage msg;
	private final byte[] data;
	private final NavajoStreamEvent streamEvent;
	private final List<ImmutableMessage> msgList;
	public final Type type;
	public enum Type {
		MESSAGE,
		SINGLEMESSAGE,
		EVENT,
		DATA,
		LIST,
		EMPTY
	}
	
	public DataItem() {
		this.msg = null;
		this.data = null;
		this.streamEvent = null;
		this.msgList = null;
		this.type = Type.EMPTY;
	}
	
	public DataItem(ImmutableMessage msg) {
		this.msg = msg;
		this.data = null;
		this.streamEvent = null;
		this.msgList = null;
		this.type = Type.MESSAGE;
	}
	
	public DataItem(byte[] data) {
		this.msg = null;
		this.data = data;
		this.streamEvent = null;
		this.msgList = null;
		this.type = Type.DATA;
	}

	public DataItem(NavajoStreamEvent event) {
		this.msg = null;
		this.data = null;
		this.streamEvent = event;
		this.msgList = null;
		this.type = Type.EVENT;
	}
	
	public DataItem(List<ImmutableMessage> msgList) {
		this.msg = null;
		this.data = null;
		this.streamEvent = null;
		this.msgList = msgList;
		this.type = Type.LIST;
	}
	

	public ImmutableMessage message() {
		if(this.msg==null) {
			throw new NullPointerException("DataItem without message, can't request message of dataitem of type: "+this.type);
		}
		return this.msg;
	}

	public NavajoStreamEvent event() {
		if(this.streamEvent==null) {
			NullPointerException nullPointerException = new NullPointerException("DataItem without streamEvent, can't request event of dataitem of type: "+this.type);
			nullPointerException.printStackTrace();
			throw nullPointerException;
		}

		return this.streamEvent;
	}
	
	public byte[] data() {
		if(this.data==null) {
			throw new NullPointerException("DataItem without data, can't request data of dataitem of type: "+this.type+" item: "+toString());
		}
		return data;
	}

	public List<ImmutableMessage> msgList() {
		if(this.msgList==null) {
			throw new NullPointerException("DataItem without msgList, can't request msgList of dataitem of type: "+this.type+" item: "+toString());
		}
		return msgList;
	}

	public static DataItem empty() {
		return new DataItem();
	}

	public static DataItem of(ImmutableMessage repl) {
		return new DataItem(repl);
	}
	public static DataItem of(byte[] data) {
		return new DataItem(data);
	}
	
	public static DataItem of(NavajoStreamEvent event) {
		return new DataItem(event);
	}

	public static DataItem of(List<ImmutableMessage> msgList) {
		return new DataItem(msgList);
	}
	
	public String toString() {
		if(streamEvent!=null) {
			return "event: "+streamEvent;
		} else if(data!=null) {
			return "data: "+new String(data);
		} else if(msgList!=null) {
			return "msglist: "+msgList;		
		} else {
			return msg.toFlatString(ImmutableFactory.getInstance());
		}
	}
}
