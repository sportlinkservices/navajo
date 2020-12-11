/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.navascript.tags;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.base.BaseMessageTagImpl;

public class MessageTag extends BaseMessageTagImpl {

	private Navajo myScript;

	public MessageTag(Navajo n, String name, String type) {
		super(n, name);
		if ( type != null ) {
			super.setType(type);
		}
		myScript = n;
	}

	// add <param>
	public ParamTag addParam(String condition, String value) {
		ParamTag pt = new ParamTag(myScript, condition, value);
		super.addParam(pt);
		return pt;
	}
	
	public FieldTag addField(MapTag parent, String condition, String name) {
		FieldTag pt = new FieldTag(parent, condition, name);
		super.addField(pt);
		return pt;
	}

	public PropertyTag addProperty(String condition, String name, String type, String value, int length, String description, String dir) {
		PropertyTag pt = new PropertyTag(myScript, name, type, value, length, description, dir);
		super.addProperty(pt);
		return pt;
	}

	public PropertyTag addProperty(String condition, String name, String type, String value) {
		PropertyTag pt = new PropertyTag(myScript, name, type, value, 0, "", "out");
		super.addProperty(pt);
		return pt;
	}

	// add <property>
	public PropertyTag addProperty(String condition, String name, String type) {
		PropertyTag pt = new PropertyTag(myScript, name, type, null, 0, "", "out");
		super.addProperty(pt);
		return pt;
	}

	// add <map>
	public MapTag addMap(String filter, String field, MapTag refParent, boolean oldStyleMap) {
		MapTag m = new MapTag(myScript, field, filter, refParent ,oldStyleMap);
		super.addMap(m);
		return m;
	}

	// add <message>
	public MessageTag addMessage(String name, String type) {
		MessageTag m = new MessageTag(myScript, name, type);
		super.addMessage(m);
		return m;
	}

	protected Navajo getNavajo() {
		return myScript;
	}

}
