/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.stream.api;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.stream.NavajoStreamSerializer;
import com.dexels.navajo.document.types.Binary;

public class Prop {
	private final String name;
	private final String value;
	private final String type;
	private final List<Select> selections = new ArrayList<>();
	private final int length;
	private final String description;
	private final Optional<Direction> direction;
	private final String subtype;
	private final Optional<String> cardinality;
	private final Binary binary;

	private final static Logger logger = LoggerFactory.getLogger(Prop.class);


	public enum Direction {
		IN,OUT
	}

	Prop(String name, String value, String type, List<Select> selections, Optional<String> cardinality) {
		this(name,value,type,selections,Optional.empty(),"",-1,"",cardinality,null);
	}

	public Prop copy() {
		return new Prop(name, value, type,selections,direction,description,length,subtype,cardinality,binary);
	}

	public Prop(String name, String value, String type) {
		this(name, value, type, Collections.emptyList(),Optional.empty());
	}

	public Prop(String name, String value, String type, List<Select> selections, Optional<Prop.Direction> direction,
			String description, int length, String subtype,Optional<String> cardinality, Binary binary) {
		this.name = name;
		this.type = type;
		this.value = value;
		this.selections.addAll(selections);
		this.direction = direction;
		this.description = description;
		this.length = length;
		this.subtype = subtype;
		this.cardinality = cardinality;
		this.binary = binary;
	}

	public static Prop create(String name) {
		return new Prop(name,null,null);
	}


	public static Prop create(Map<String,String> attributes, List<Select> selections) {
		String lngth = attributes.get("length");
		String cardinality = attributes.get("cardinality");
		int len = lngth==null || "".equals(lngth)?-1:Integer.parseInt(lngth);
		return create(attributes.get("name"),attributes.get("value"),attributes.get("type"),selections,parseDirection(attributes.get("direction")),attributes.get("description"),len,attributes.get("subtype"),Optional.ofNullable(cardinality),null);
	}

	public static Prop create(Map<String, String> attributes, Binary currentBinary) {
		int len =  (int) currentBinary.getLength(); // lngth==null || "".equals(lngth)?-1:Integer.parseInt(lngth);
		return create(attributes.get("name"),(String)null,attributes.get("type"),Collections.emptyList(),parseDirection(attributes.get("direction")),attributes.get("description"),len,attributes.get("subtype"),Optional.empty(),currentBinary);
	}

	private static Optional<Direction> parseDirection(String direction) {
		if(direction==null) {
			return Optional.empty();
		}
		return direction.equals("in")?Optional.of(Direction.IN):Optional.of(Direction.OUT);
	}

	public static Prop create(String name, String value) {
		return new Prop(name,value,null);
	}

	public static Prop create(String name, String value, String type) {
		return new Prop(name,value,type);
	}

	public static Prop create(String name, String value, String type,List<Select> selections, Optional<String> cardinality) {
		return new Prop(name,value,type,selections,Optional.empty(),"",-1,"",cardinality,null);
	}

	public static Prop create(String name, String value, String type,List<Select> selections, Optional<Prop.Direction> direction, String description, int length, String subtype, Optional<String> cardinality, Binary binary ) {
		return new Prop(name,value,type,selections,direction,description,length,subtype,cardinality,binary);
	}



	public Prop withSelections(List<Select> currentSelections) {
		return new Prop(name, value, type,currentSelections,cardinality);
	}

	public Prop withValue(String val) {
		return new Prop(name, val, type,selections,direction,description,length,subtype,cardinality,null);
	}

	public Prop withBinaryFromFile(String path) {
		File fpath = new File(path);
		if(fpath.exists()) {
			Binary b;
			try {
				b = new Binary(fpath);
			} catch (IOException e) {
				logger.error("Error loading binary: ", e);
				b = new Binary();
			}
			return new Prop(name, null, Property.BINARY_PROPERTY,selections,direction,description,length,subtype,cardinality,b);
		}
		return     new Prop(name, null, type,selections,direction,description,length,subtype,cardinality,new Binary());
	}

	public Prop withName(String newName) {
		return new Prop(newName, value, type,selections,direction,description,length,subtype,cardinality,binary);
	}

	public Prop emptyWithType(String type) {
		return new Prop(name,null,type);
	}

	public String name() {
		return name;
	}

	public String type() {
		return type;
	}

	public int length() {
		return this.length;
	}

	public String toString() {
		return name+":"+value;
	}

	public String description() {
		return this.description;
	}

	public Optional<String> direction() {
		if(!this.direction.isPresent()) {
			return Optional.empty();
		}
		switch(this.direction.get()) {
		case IN:
			return Optional.of("in");
		default:
			return Optional.of("out");
		}

	}

	public Object value() {
		return value;
	}

	public String valueAsString() {
		if(value==null) {
			return null;
		}
		if(value instanceof String) {
			return (String)value;
		}
		return value.toString();
	}

	public void write(Writer sw, int indent) throws IOException {
		 for (int a = 0; a < indent; a++) {
			 sw.write(" ");
		 }
		 sw.write("<property");
		 if(name!=null) {
			 sw.write(" name=\""+ StringEscapeUtils.escapeXml(name)+"\"");
		 }
		 if(type!=null) {
			 sw.write(" type=\""+type+"\"");
		 }
		 if(value!=null && !isBinary()) {
			 String value = valueAsString();
			 String escapedValue = StringEscapeUtils.escapeXml(value);
			sw.write(" value=\""+escapedValue+"\"");
		 }
		 if(direction!=null && direction.isPresent()) {
			 sw.write(" direction=\""+direction().get()+"\"");
		 }
		 if(description!=null && !"".equals(description)) {
			 sw.write(" description=\""+description+"\"");
		 }
		 if(cardinality!=null && cardinality.isPresent() && !"".equals(cardinality.get())) {
			 sw.write(" cardinality=\""+cardinality.get()+"\"");
		 }
		 if(length>0) {
			 sw.write(" length=\""+length+"\"");
		 }
		 if(!isBinary() && (selections==null || selections.isEmpty())) {
				sw.write("/>\n");
		 } else {
			 sw.write(">\n");
			 if (isBinary()) {
				Binary b = binary;
				b.writeBase64(sw);
			} else {
				for (Select select : selections) {
					writeSelection(sw, select,indent+NavajoStreamSerializer.INDENT);
				}
			}
			 for (int a = 0; a < indent; a++) {
				 sw.write(" ");
			 }
			sw.write("</property>\n");

		 }
	}

	private void writeSelection(Writer sw, Select select,int indent) throws IOException {
		 for (int a = 0; a < indent; a++) {
			 sw.write(" ");
		 }
		sw.write("<option name=\""+ StringEscapeUtils.escapeXml(select.name())+"\" value=\""+ StringEscapeUtils.escapeXml(select.value())+"\" selected=\""+(select.selected()?"1":"0")+"\"/>\n");
	}

	private boolean isBinary() {
		return binary!=null;
	}
	public void printStartTag(final Writer sw, int indent,boolean forceDualTags,String tag,  String[] attributes) throws IOException {
		 for (int a = 0; a < indent; a++) {
			 sw.write(" ");
		 }
		 sw.write("<");
		 sw.write(tag);
		 sw.write(" ");
		 for (String attribute : attributes) {
			sw.write(attribute);
		}
		sw.write(">");
	}

	public String subtype() {

		return this.subtype;
	}

	public void subtype(String sub) {
		// TODO, don't want to make it mutable
	}

	public String subtypes(String subTypeKey) {
		Map<String,String> subtypes = Collections.emptyMap();
		return subtypes.get(subTypeKey);
	}

	public void addSelect(Select s) {
		selections.add(s);

	}

	public Optional<String> cardinality() {
		return cardinality;
	}

	public List<Select> selections() {
		return selections;
	}




}
