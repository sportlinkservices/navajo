/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.stream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.stream.api.Method;
import com.dexels.navajo.document.stream.api.Msg;
import com.dexels.navajo.document.stream.api.NavajoHead;
import com.dexels.navajo.document.stream.api.Prop;
import com.dexels.navajo.document.stream.events.NavajoStreamEvent;

public class NavajoStreamSerializer {

	private volatile int tagDepth = 0;
	private Stack<String> messageNameStack = new Stack<>();
	private Stack<Boolean> messageIgnoreStack = new Stack<>();
//	private final Map<String,AtomicInteger> arrayCounter = new ConcurrentHashMap<>();
	private final Stack<AtomicInteger> counterStack = new Stack<>();
	private final static Logger logger = LoggerFactory.getLogger(NavajoStreamSerializer.class);
	public static final int INDENT = 3;

	public NavajoStreamSerializer() {
	}
	
	public byte[] serialize(final NavajoStreamEvent event) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(baos);
//		StringWriter w = new StringWriter();
		processNavajoEvent(event, writer);
		try {
			writer.close();
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
		byte[] byteArray = baos.toByteArray();
		return byteArray;
	}
	@SuppressWarnings("unchecked")
	private void processNavajoEvent(NavajoStreamEvent event,Writer w) {
		try {
			String name = event.path();
			switch (event.type()) {
				case MESSAGE_STARTED:
					messageIgnoreStack.push("ignore".equals(event.attribute("mode")));
					if(!messageIgnoreStack.contains(true)) {
						printStartTag(w, INDENT * (tagDepth+1),true,"message",createMessageAttributes("name=\""+name+"\"",event.attributes()));
					} else {
						logger.info("Message IGNORE!");
					}
					tagDepth++;
					messageNameStack.push(name);
					break;
				case MESSAGE_DEFINITION_STARTED:
					messageIgnoreStack.push("ignore".equals(event.attribute("mode")));
					if(!messageIgnoreStack.contains(true)) {
						printStartTag(w, INDENT * (tagDepth+1),true,"message",createMessageAttributes("name=\""+name+"\" type=\"definition\"",event.attributes()));
					}
					tagDepth++;
					messageNameStack.push(name+"@definition");
					break;
				case ARRAY_ELEMENT_STARTED:
					messageIgnoreStack.push("ignore".equals(event.attribute("mode")));
					String arrayName = messageNameStack.peek();
					String pth = currentPath();
					AtomicInteger index = counterStack.peek();
	
					if(index==null) {
						logger.error("no current index for path: "+pth);
					}
					if(!"ignore".equals(event.attribute("mode"))) {
						printStartTag(w, INDENT * (tagDepth+1),true,"message",createMessageAttributes("name=\""+arrayName+"\" index=\""+index+"\" type=\"array_element\"",event.attributes()));
						tagDepth++;
					}
					int ind = index.getAndIncrement();
					messageNameStack.push("@"+ind);
					break;
				case MESSAGE:
				case MESSAGE_DEFINITION:
				case ARRAY_ELEMENT:
					messageNameStack.pop();
					Msg msgBody = event.message();
					List<Prop> properties = msgBody.properties();
					if(!messageIgnoreStack.contains(true)) {
						for (Prop prop : properties) {
							prop.write(w,INDENT * (tagDepth+1));
						}
						printEndTag(w, INDENT * tagDepth, "message");
					}
					messageIgnoreStack.pop();
					tagDepth--;
					break;
				case ARRAY_STARTED:
					messageIgnoreStack.push("ignore".equals(event.attribute("mode")));
					if(!messageIgnoreStack.contains(true)) {
						printStartTag(w, INDENT * (tagDepth+1),true,"message","name=\""+name+"\" type=\"array\"");
					}
					messageNameStack.push(name);
					tagDepth++;
					counterStack.push(new AtomicInteger());
//					arrayCounter.put(currentPath(), new AtomicInteger(0));
					break;
				case ARRAY_DONE:
					if(!messageIgnoreStack.contains(true)) {
						printEndTag(w, INDENT*tagDepth, "message");
					}
//					printCloseTag(w, INDENT*tagStack.size());
//					arrayCounter.remove(currentPath());
					tagDepth--;
					counterStack.pop();
					messageIgnoreStack.pop();
					messageNameStack.pop();
					break;
				case NAVAJO_STARTED:
					w.write("<tml>\n");
					NavajoHead head = (NavajoHead) event.body();
//					head.printElement(w,INDENT);
					head.print(w, INDENT);
//					Header h = NavajoFactory.getInstance().createHeader(null, head.name(), head.username(), head.password(), -1);
//					h.printElement(w, INDENT);
					break;				
				case NAVAJO_DONE:

					List<Method> methods = (List<Method>) event.body();
					if(methods.size()>0) {
						printStartTag(w, INDENT * (tagDepth+1),true,"methods","");
						methods.forEach(e->{
							try {
								e.write(w, INDENT * (tagDepth+2));
							} catch (IOException e1) {
								logger.error("Error: ", e1);
							}
						});
						printEndTag(w, INDENT*(tagDepth+1), "methods");
						
					}
					w.write("</tml>\n");
					break;
				case BINARY_STARTED:
					int length = (Integer)event.attribute("length");
					String description = (String)event.attribute("description");
					String direction = (String)event.attribute("direction");
					String subtype = (String)event.attribute("subtype");
					StringBuilder sb = new StringBuilder("name=\""+event.path()+"\" type=\"binary\" length=\""+length+"\"");
					if(description!=null) {
						sb.append(" description=\""+description+"\"");
					}
					if(direction!=null) {
						sb.append(" direction=\""+direction+"\"");
					}
					if(subtype!=null) {
						sb.append(" subtype=\""+subtype+"\"");
					}
					printStartTag(w, INDENT * (tagDepth+1),true,"property",sb.toString());
					tagDepth++;
					break;
				case BINARY_CONTENT:
					String body = (String)event.body();
					w.write(body);
					tagDepth++;
					break;
				case BINARY_DONE:
					tagDepth--;
					printEndTag(w, INDENT*tagDepth, "property");
					break;

				default:
					break;
			}
		} catch (IOException e) {
			logger.error("Error: ", e);
		}
	}

	private String createMessageAttributes(String initial, Map<String, Object> attributes) {
		StringBuilder sb = new StringBuilder(initial);
		String mode = (String) attributes.get(Message.MSG_MODE);
		String etag = (String) attributes.get(Message.MSG_ETAG);
		if(mode!=null) {
			sb.append(" mode=\""+mode+"\"");
		}
		if(etag!=null) {
			sb.append(" etag=\""+etag+"\"");
		}
		return sb.toString();
	}

	private String currentPath() {
		int i = 0;
		StringWriter sw = new StringWriter();
		for (String element : messageNameStack) {
			sw.write(element);
			if(i!=messageNameStack.size()-1) {
				sw.write("/");
			}
			i++;
		}
		return sw.toString();
	}
	
	private void printStartTag(final Writer sw, int indent,boolean forceDualTags,String tag,  String attributes) throws IOException {
		 for (int a = 0; a < indent; a++) {
			 sw.write(" ");
		 }
		 sw.write("<");
		 sw.write(tag);
		 if(!"".equals(attributes)) {
			 sw.write(" ");
		 }
//		 for (String attribute : attributes) {
			sw.write(attributes);
//		}
		sw.write(">\n");
	}
	
	private void printEndTag(final Writer sw, int indent,String tag) throws IOException {
		 for (int a = 0; a < indent; a++) {
			 sw.write(" ");
		 }
		 sw.write("</");
		 sw.write(tag);
		 sw.write(">\n");
	}
}
