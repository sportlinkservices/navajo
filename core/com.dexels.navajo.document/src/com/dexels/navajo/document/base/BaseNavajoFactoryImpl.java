/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.base;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import com.dexels.navajo.document.ExpressionTag;
import com.dexels.navajo.document.FieldTag;
import com.dexels.navajo.document.Header;
import com.dexels.navajo.document.MapTag;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Method;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Operation;
import com.dexels.navajo.document.ParamTag;
import com.dexels.navajo.document.Point;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.document.json.JSONTML;
import com.dexels.navajo.document.json.JSONTMLFactory;
import com.dexels.navajo.document.saximpl.SaxHandler;
import com.dexels.navajo.document.saximpl.qdxml.QDParser;

/**
 * <p>
 * Title:
 * </p>
 * <p>
 * Description:
 * </p>
 * <p>
 * Copyright: Copyright (c) 2003
 * </p>
 * <p>
 * Company:
 * </p>
 * 
 * @author not attributable
 * @version 1.0
 */

public class BaseNavajoFactoryImpl extends NavajoFactory implements Serializable {

	private static final long serialVersionUID = -4927821415020342916L;

	public BaseNavajoFactoryImpl() {
	}

	public Message create(Navajo n, String name) {
		return new BaseMessageImpl(n, name);
	}

	@Override
	public Property createProperty(Navajo n, String name, String type, String value, int i, String desc,
			String direction) {
		return new BasePropertyImpl(n, name, type, value, i, desc, direction);
	}

	@Override
	public Property createProperty(Navajo n, String name, String type, String value, int i, String desc,
			String direction, String subtype) {
		return new BasePropertyImpl(n, name, type, value, i, desc, direction, subtype);
	}

	@Override
	public Property createProperty(Navajo n, String name, String cardinality, String desc, String direction) {
		return new BasePropertyImpl(n, name, cardinality, desc, direction);
	}

	public Property createProperty(Navajo n, String name) {
		return new BasePropertyImpl(n, name);
	}

	@Override
	public Message createMessage(Navajo n, String name) {
		return new BaseMessageImpl(n, name);
	}

	public Message createArrayMessage(Navajo n, String name) {
		Message m = new BaseMessageImpl(n, name);
		m.setType(Message.MSG_TYPE_ARRAY);
		return m;
	}

	public Message createArrayElementMessage(Navajo n, String name) {
		Message m = new BaseMessageImpl(n, name);
		m.setType(Message.MSG_TYPE_ARRAY_ELEMENT);
		return m;
	}

	public Selection createSelection(BaseNavajoImpl n, String name, String value, boolean isSelected) {
		return new BaseSelectionImpl(n, name, value, isSelected);
	}

	public Selection createSelection(BaseNavajoImpl n, String name, String value, int isSelected) {
		return new BaseSelectionImpl(n, name, value, (isSelected > 0));
	}

	public Selection createSelection(BaseNavajoImpl n) {
		return new BaseSelectionImpl(n);
	}

	public Header createHeader(BaseNavajoImpl n, String username, String password, String service) {
		return new BaseHeaderImpl(n, username, password, service);
	}

	public Header createHeader(BaseNavajoImpl n) {
		return new BaseHeaderImpl(n);
	}

	@Override
	public Point createPoint(Property p) {
		throw new java.lang.UnsupportedOperationException("Method createPoint() not yet implemented.");
	}

	@Override
	public Header createHeader(Navajo n, String rpcName, String rpcUser, String rpcPassword, long expirationInterval) {
		BaseHeaderImpl hi = new BaseHeaderImpl(n, rpcUser, rpcPassword, rpcName);
		hi.setExpiration(expirationInterval);
		return hi;
	}

	@Override
	public NavajoException createNavajoException(Throwable e) {
		return new NavajoExceptionImpl(e);
	}

	@Override
	public Selection createDummySelection() {
		return new BaseSelectionImpl(null, Selection.DUMMY_SELECTION, Selection.DUMMY_ELEMENT, true);
	}

	@Override
	public Message createMessage(Navajo tb, String name, String type) {
		BaseMessageImpl mi = new BaseMessageImpl(tb, name);
		mi.setType(type);
		return mi;
	}

	@Override
	public Navajo createNavajo() {
		return new BaseNavajoImpl(this);
	}

	@Override
	public Selection createSelection(Navajo tb, String name, String value, boolean selected) {
		return new BaseSelectionImpl(tb, name, value, selected);
	}

	@Override
	public Selection createSelection(Navajo tb, String name, String value, int selected) {
		return new BaseSelectionImpl(tb, name, value, (selected > 0));
	}

	@Override
	public Selection createSelection(Navajo tb, String name, String value, Object selected) {

		BaseSelectionImpl si = null;

		if (selected == null) {
			si = new BaseSelectionImpl(tb, name, value, false);
		} else if (selected instanceof Boolean) {
			si = new BaseSelectionImpl(tb, name, value, (Boolean) selected);
		} else {
			si = new BaseSelectionImpl(tb, name, value, Integer.parseInt(selected + "") > 0);
		}

		return si;
	}

	@Override
	public Method createMethod(Navajo tb, String name, String server) {
		BaseMethodImpl mi = new BaseMethodImpl(tb, name);
		mi.setServer(server);
		return mi;
	}

	@Override
	public NavajoException createNavajoException(String message) {
		return new NavajoExceptionImpl(message);
	}

	@Override
	public NavajoException createNavajoException(String message, Throwable t) {
		return new NavajoExceptionImpl(message, t);
	}

	@Override
	public  Navajo createNavaScript(java.io.InputStream stream) {
		throw new java.lang.UnsupportedOperationException("Method createNavaScript() not yet implemented.");
	}

	@Override
	public Navajo createNavaScript(Object representation) {
		throw new java.lang.UnsupportedOperationException("Method createNavaScript() not yet implemented.");
	}

	@Override
	public Navajo createNavaScript() {
		throw new java.lang.UnsupportedOperationException("Method createNavaScript() not yet implemented.");
	}

	@Override
	public ExpressionTag createExpression(Navajo tb, String condition, String value) {
		throw new java.lang.UnsupportedOperationException("Method createExpression() not yet implemented.");
	}

	@Override
	public FieldTag createField(Navajo tb, String condition, String name) {
		throw new java.lang.UnsupportedOperationException("Method createExpression() not yet implemented.");
	}

	@Override
	public MapTag createMapObject(Navajo tb, String object, String condition) {
		throw new java.lang.UnsupportedOperationException("Method createMapObject() not yet implemented.");
	}

	@Override
	public MapTag createMapRef(Navajo tb, String ref, String condition, String filter) {
		throw new java.lang.UnsupportedOperationException("Method createMapRef() not yet implemented.");
	}

	@Override
	public ParamTag createParam(Navajo tb, String condition, String name) {
		throw new java.lang.UnsupportedOperationException("Method createParam() not yet implemented.");
	}

	@Override
	public Navajo createNavajo(InputStream stream) {
		try {
			SaxHandler sax = new SaxHandler();
			try(Reader isr = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
				QDParser.parse(sax, isr);
			}
			return sax.getNavajo();
		} catch (Exception e) {
			throw NavajoFactory.getInstance().createNavajoException(e);
		}
	}

	@Override
	public Navajo createNavajo(Reader r) {
		try {
			SaxHandler sax = new SaxHandler();
			QDParser.parse(sax, r);
			return sax.getNavajo();
		} catch (Exception e) {
			throw NavajoFactory.getInstance().createNavajoException(e);
		}
	}

	@Override
	public Navajo createNavajoJSON(Reader r) {
		try {
			JSONTML jsoParser = JSONTMLFactory.getInstance();
			return jsoParser.parse(r);
		} catch (Exception e) {
			throw NavajoFactory.getInstance().createNavajoException(e);
		}
	}

	@Override
	public Navajo createNavajo(Object representation) {
		return null;
	}

	@Override
	public Message createMessage(Object representation) {
		return null;
	}

	@Override
	public Property createProperty(Object representation) {
		return null;
	}

	public Operation createOperation(Navajo n, String method, String service, String entityName, Message extra) {
		return createOperation(n, method, service, null, entityName, extra);
	}

	@Override
	public Operation createOperation(Navajo n, String method, String service, String validationService,
			String entityName, Message extra) {
		BaseOperationImpl oi = new BaseOperationImpl(n);
		oi.setMethod(method);
		oi.setService(service);
		oi.setValidationService(validationService);
		oi.setEntityName(entityName);
		oi.setExtraMessage(extra);
		return oi;
	}

}
