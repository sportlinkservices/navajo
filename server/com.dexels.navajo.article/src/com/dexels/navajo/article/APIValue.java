/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.article;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.POJONode;

public class APIValue {
	
	private static final String TYPE_SELECTION = "selection";
	private static final String TYPE_DATETIME = "datetime";
	private static final String TYPE_INTEGER = "integer";
	//private static final String TYPE_BOOLEAN = "boolean";
	
	public static void setValueOnNodeForType(ObjectNode node, String id, String type, Property property, ArticleRuntime runtime) throws APIException {
		if (property == null || property.getTypedValue() == null) {
			node.putNull(id);
		} else {
			if (TYPE_SELECTION.equals(type)) {
				ArrayNode options = runtime.getObjectMapper().createArrayNode();
				
				if (property.getAllSelections() != null && property.getAllSelections().size() > 0) {
					for (Selection s : property.getAllSelections()) {
						ObjectNode option = runtime.getObjectMapper().createObjectNode();
						
						option.put("selected", s.isSelected());
						option.put("value", s.getValue());
						option.put("name", s.getName());
						
						options.add(option);	
					}
				}
				
				node.set(id, options);
			} else if (TYPE_DATETIME.equals(type)) {
				validatedType(property, Property.DATE_PROPERTY, id);
				
				DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				String ISO8601Date = df.format(((Date) property.getTypedValue()));
				node.put(id, ISO8601Date);
			} else if (TYPE_INTEGER.equals(type)) {
				//Null number are sometimes represented as empty strings.				
				if (property.getTypedValue() instanceof String && ((String)property.getTypedValue()).trim().length() == 0) {
					node.putNull(id);
				} else {	
					Integer value = -1;
					try {
						value = Integer.parseInt(property.getValue());
					} catch (NumberFormatException e) {
						throw new APIException("Id " + id + " says it is an integer but cannot be parsed to an int", null, APIErrorCode.InternalError);
					}
					
					node.put(id, value);
				}
			} else {
				if (property.getType().equals(Property.SELECTION_PROPERTY)) {
					if (property.getSelected() == null || property.getSelected().getName() == Selection.DUMMY_SELECTION) {
						node.putNull(id);
					} else {
						node.put(id, property.getSelected().getName());
					}
				} else if (property.getType().equals(Property.BINARY_PROPERTY)) {
				    // Put the binary object in - TmlBinarySerializer will take care of serializing it
				    node.set(id, new POJONode(property.getTypedValue()));
				    
				} else {
					//Default
					node.put(id, property.getValue());
				}
			}
		}
	}
	
	public static void validatedType(Property property, String expectedType, String id) throws APIException {
		if (!expectedType.equals(property.getType())) {
			throw new APIException("Expected type: " + expectedType + " but is: " + property.getType() + " for id: " + id + ", value: " + property.getValue(), null, APIErrorCode.InternalError);
		}
	}
}
