package com.dexels.navajo.rhino;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

import com.dexels.navajo.document.ExpressionEvaluator;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Method;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.mapping.MappingUtils;
import com.dexels.navajo.parser.DefaultExpressionEvaluator;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.script.api.MappingException;

public class StackScriptEnvironment extends ScriptEnvironment {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Map<String, Navajo> myNavajoMap = new HashMap<String, Navajo>();
	private final Map<Navajo, String> myInverseNavajoMap = new HashMap<Navajo, String>();
	private final Stack<Object> myElementStack = new Stack<Object>();
	private final Stack<MappableTreeNode> treeNodeStack = new Stack<MappableTreeNode>();
	private final Map<String, Boolean> isArrayRef = new HashMap<String,Boolean>();
	
	private Message currentParamMessage = null;

	public StackScriptEnvironment() {
		if (NavajoFactory.getInstance().getExpressionEvaluator() == null) {
			NavajoFactory.getInstance().setExpressionEvaluator(
					new DefaultExpressionEvaluator());
		}
	}

	public StackScriptEnvironment(StackScriptEnvironment original) {
		myNavajoMap.putAll(original.myNavajoMap);
		myInverseNavajoMap.putAll(original.myInverseNavajoMap);
		myElementStack.addAll(original.myElementStack);
		treeNodeStack.addAll(original.treeNodeStack);
		currentParamMessage = original.currentParamMessage;
	}

	public void blockDebug() {
	}

	@Override
	public Object navajoEvaluate(String expression) throws NavajoException {
		Operand o = null;
		Navajo inDoc = getAccess().getInDoc();
		Message top = null;
		if (!inputMessageStack.isEmpty()) {
			top = inputMessageStack.peek();
		}
		ExpressionEvaluator expressionEvaluator = NavajoFactory.getInstance()
				.getExpressionEvaluator();
		o = expressionEvaluator.evaluate(expression, inDoc,
				getCurrentTreeNode(), top, getTopParamStackMessage(),null,null,null,Optional.empty(),Optional.empty());
		if (o == null) {
			return null;
		}
		return o.value;
	}

	private Message getTopParamStackMessage() throws NavajoException {
		if (paramMessageStack.isEmpty()) {
			return getTopParamMessage();
		}
		return paramMessageStack.peek();
	}

	private Message getTopParamMessage() throws NavajoException {
		for (int i = myElementStack.size() - 1; i >= 0; i--) {
			Object o = myElementStack.get(i);
			if (o instanceof Message) {
				if (isParamMessage((Message) o)) {
					return (Message) o;
				}

			}
		}
		return getParamMessage();
	}

	private boolean isParamMessage(Message m) {
		Message c = m;
		Message paramRoot = getAccess().getInDoc().getMessage(
				Message.MSG_PARAMETERS_BLOCK);
		do {
			c = c.getParentMessage();
			if (c == paramRoot) {
				return true;
			}
		} while (c != null);
		return false;
	}

	public void addField(String fieldName, Object value)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		MappableTreeNode treeTop = treeNodeStack.peek();
		if (treeTop == null) {
			log("Can not set field, no map detected");
			return;
		}
		Object map = treeTop.getMyMap();
		String fieldSetter = "set" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
		Class<? extends Object> mapClass = map.getClass();

		Class<?> t = mapClass;

		int emergencyCounter = 0;
		while (t != null && !t.equals(Object.class) && emergencyCounter < 10) {
			boolean found = setValueForClass(value, map, fieldSetter, t);
			if (found) {
				return;
			}

			t = mapClass.getSuperclass();
			emergencyCounter++;
		}

		log("WARNING SETTER FOR FIELD: " + fieldName + " failed");
	}

	private boolean setValueForClass(Object value, Object map,
			String fieldSetter, Class<? extends Object> mapClass)
			throws IllegalAccessException, InvocationTargetException {
		java.lang.reflect.Method[] methods = mapClass.getMethods();
		for (java.lang.reflect.Method method : methods) {
			if (method.getName().equals(fieldSetter)) {
				// log("found qualified setter (based on name");
				Class<?>[] params = method.getParameterTypes();
				if (params.length == 1) {
					// log("Single parameter. Looking good.");
					// method.invoke(map, value);
					Class<?> prm = params[0];
					if (value == null) {
						// no further detective work possible
						method.invoke(map, value);
						return true;
					} else {

						//boolean a = prm.isAssignableFrom(value.getClass());

						Object v;
						if (prm.equals(Float.class)) {
//							System.err.println("Float conversion performed");
							v = Float.valueOf(value.toString());
						} else if (prm.equals(float.class)) {
							v = Float.valueOf(value.toString()).floatValue();
						} else {
							v = value;
						}
						method.invoke(map, v);
						return true;
					}
				} else {
					log("Ignoring multiparam setter");
				}
			}
		}
		return false;
	}

	public void pushMapReference(String fieldName)
			throws IllegalArgumentException, IllegalAccessException,
			InvocationTargetException {
		MappableTreeNode treeTop = treeNodeStack.peek();
		if (treeTop == null) {
			log("Can not set field, no map detected");
			return;
		}
		// Object map = treeTop.getMyMap();

		Object map = createMapRef(fieldName);
		pushMappableTreeNode(map);
	}

	public final boolean isArrayMapRef(String fieldName)
			throws 
			NoSuchMethodException {
		Object map = getCurrentTreeNode().getMyMap();
		String key = map.getClass().getName()+"."+fieldName;
		if ( !isArrayRef.containsKey(key)) {
			try {
				boolean result = MappingUtils.isArrayAttribute(map.getClass(), fieldName);
				isArrayRef.put(key, result);
				return result;
			} catch (Exception e) {
				throw new NoSuchMethodException(fieldName + " in object: " + map);
			}
		} else {
			return isArrayRef.get(key).booleanValue();
		}
		
	}
	
	public void debugTreeNodeStack(String message) {
		log("DEBUG TREE: " + message);
		if (treeNodeStack.isEmpty()) {
			log(">>> EMPTY");
		} else {
			for (MappableTreeNode m : treeNodeStack) {
				logger.info("M: " + m.getMapName());
			}
		}
	}

	@SuppressWarnings("rawtypes")
	public Object createMapRefObjects(String fieldName, int count) 
			throws IllegalAccessException, NoSuchMethodException, SecurityException, NoSuchFieldException, InstantiationException {
		Object map = getCurrentTreeNode().getMyMap();
		Class fieldType = null; 
		if (isArrayMapRef(fieldName)) {
			fieldType = map.getClass().getField(fieldName).getType().getComponentType();
			logger.debug("IN CREATEMAPREFSOBJECT (ARRAY): " + fieldType + ", COUNT=" + count);
			Object fieldArrayObject = Array.newInstance(fieldType, count);
			for ( int i = 0; i < count; i++) {
				Object fieldObject = fieldType.newInstance();
				Array.set(fieldArrayObject, i, fieldObject);
			}
			return fieldArrayObject;
		} else {
			fieldType = map.getClass().getField(fieldName).getType();
			Object fieldObject = fieldType.newInstance();
			logger.info("IN CREATEMAPREFSOBJECT: " + fieldType + ", COUNT="+count);
			return fieldObject;
			
		}
		
	}
	
	// includes a stack push!
	public Object createMapRef(String fieldName) throws IllegalAccessException,
			InvocationTargetException {
		// It's also possible that there is no mapRef yet, and we need to create one!
		String fieldGetter = "get" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1);
		Object map = getCurrentTreeNode().getMyMap();
		java.lang.reflect.Method[] methods = map.getClass().getMethods();
		for (java.lang.reflect.Method method : methods) {
			if (method.getName().equals(fieldGetter)) {
//				int paramCount = method.getParameterTypes().length;
				Object mapref = method.invoke(map, new Object[] {});
				boolean isArray = method.getReturnType().isArray();
				if(mapref==null) {
					// so: no 
				} else {
					if (isArray ) {
						 log("Element# "+ ((Object[])mapref).length+" for getter: "+fieldGetter+" map: "+map);
					}
					//pushMappableTreeNode(mapref, isArray);
				}
				return mapref;
			}
		}
		log("No getter found?!");
		return null;
	}

	public void popMapReference() {
		popMappableTreeNode();
	}

	@Override
	public void setAccess(Access access) {
		super.setAccess(access);
		myElementStack.push(access.getOutputDoc());

	}

	public void pushMappableTreeNode(Object o) {
		pushMappableTreeNode(o, false);
	}

	public Object pushMappableTreeNode(Object o, boolean isArray) {
		// log("pushMappable>>> "+o);
		MappableTreeNode top = null;
		if (!treeNodeStack.isEmpty()) {
			top = treeNodeStack.peek();
		}
		MappableTreeNode mtn = new MappableTreeNode(getAccess(), top, o,
				isArray);
		treeNodeStack.push(mtn);
		return mtn.getMyMap();
	}

	public Message getParamMessage(String name) {
		Navajo n = super.getAccess().getOutputDoc();
		if (name.startsWith("/@")) {
			name = name.substring(2);
		}
		Message params = n.getMessage(Message.MSG_PARAMETERS_BLOCK);
		if (params == null) {
			return null;
		}
		return params.getMessage(name);
	}

	public MappableTreeNode getCurrentTreeNode() {
		if (!treeNodeStack.isEmpty()) {
			return treeNodeStack.peek();
		}
		return null;
	}

	public MappableTreeNode popMappableTreeNode() {
		return treeNodeStack.pop();
	}

	public void reset() {
		myNavajoMap.clear();
		myInverseNavajoMap.clear();
		myElementStack.clear();
	}

	public Map<String, Navajo> getNavajos() {
		return myNavajoMap;
	}

	public String getServiceName(Navajo n) {
		return myInverseNavajoMap.get(n);
	}

	@Override
	public void callFinished(String service, Navajo n) {
		myNavajoMap.put(service, n);
		myInverseNavajoMap.put(n, service);
		myElementStack.push(n);

	}

	public boolean hasNavajo(String name) {
		return myNavajoMap.containsKey(name);
	}

	public int getStackSize() {
		return myElementStack.size();
	}

	public void dumpTopElement() {
		Object o = myElementStack.peek();
		if (o instanceof Navajo) {
			Navajo n = (Navajo) o;
			logger.info("Navajo on top:");
			n.write(System.err);
		} else if (o instanceof Message) {
			logger.info("Message on top: "
					+ ((Message) o).getFullMessageName());
		} else if (o instanceof Property) {
			logger.info("Property on top: "
						+ ((Property) o).getFullPropertyName());

		} else {
			if (o != null) {
				logger.info("Other object:" + o.getClass());
			} else {
				logger.info("Null object on stack!");
			}
		}

	}

	public Navajo getNavajo(String name) {

		Navajo navajo = myNavajoMap.get(name);
		// if (navajo == null) {
		// throw new IllegalStateException( "Unknown service: " +
		// name+" known services: "+myNavajoMap.keySet());
		// }
		return navajo;
	}

	public Property getProperty(String service, String path) {
		Navajo n = getNavajo(service);
		Property p = n.getProperty(path);
		if (p == null) {
			throw new IllegalStateException("Unknown property: " + path
					+ " in service " + service);
		}
		return p;
	}

	public Object getPropertyValue(String service, String path) {
		Property p = getProperty(service, path);
		return p.getTypedValue();
	}

	public String getNavajoName() {
		Navajo n = getNavajo();
		if (n == null) {
			return null;
		}
		return myInverseNavajoMap.get(n);
	}

	public Navajo getNavajo() {
		if (myElementStack.isEmpty()) {
			throw new IllegalStateException(
					"No default navajo found. Either supply a name explicitly, or make sure you are within a 'call' tag");
		}
		return (Navajo) getTopmostElement(Navajo.class);

	}

	public void popNavajo() {

		myElementStack.pop();

	}

	public void popElement() {
	
		myElementStack.pop();

	}
	public void pushNavajo(Navajo m) {

		myElementStack.push(m);
	}

	public Message getMessage() {
		if (myElementStack.isEmpty()) {
			logger.error("Empty stack!");
			return null;
		}
		return (Message) getTopmostElement(Message.class);
	}

	public Object peek() {
		if (myElementStack.isEmpty()) {
			throw new IllegalStateException(
					"No default myMessageStack found. Either supply a name explicitly, or make sure you are within a 'message' tag");
		}
		return myElementStack.peek();
	}

	public Property getProperty() {
		if (myElementStack.isEmpty()) {
			throw new IllegalStateException(
					"No default myMessageStack found. Either supply a name explicitly, or make sure you are within a 'message' tag");
		}
		return (Property) getTopmostElement(Property.class);
	}

	private Object getTopmostElement(Class<?>[] cls) {
		for (int i = myElementStack.size() - 1; i >= 0; i--) {
			Object e = myElementStack.get(i);
			for (Class<?> clz : cls) {
				if (clz.isAssignableFrom(e.getClass())) {
					return e;
				}
			}
		}
		return null;
	}

	private Object getTopmostElement(@SuppressWarnings("rawtypes") Class cls) {
		return getTopmostElement(new Class[] { cls });
	}

	public void popMessage() {
		myElementStack.pop();
		getAccess().setCurrentOutMessage(getMessage());
	}
	
	// no stack activity
	public Message getInputMessage(String path) throws NavajoException {
		String path2 = path.replaceAll("@", Message.MSG_PARAMETERS_BLOCK + "/");
		Message result = getAccess().getInDoc().getMessage(path2);
		if (result == null) {
			logger.error("Can't find message: " + path2);
		}
		return result;
	}

	public void pushMessage(Message m) {
		getAccess().setCurrentOutMessage(m);
		if (m != null) {
			pushElement(m);
		}
	}

	public void pushProperty(Property p) {
		if (p != null) {
			pushElement(p);
		}
	}
	
	public void pushElement(Object item) {
		myElementStack.push(item);
	}



	public void debug() {
	}

	public String getMessagePath() {
		Message m = getMessage();
		if (m == null) {
			return null;
		}
		return createMessagePath(m);
	}

	public String getPropertyPath() {
		Property p = getProperty();
		if (p == null) {
			return null;
		}
		return createPropertyPath(p);
	}

	private String createMessagePath(Message m) {
		String navajoName = getServiceName(m.getRootDoc());
		String msg = m.getFullMessageName();
		return navajoName + "|" + msg;
	}

	private String createPropertyPath(Property p) {
		String navajoName = getServiceName(p.getRootDoc());
		String prop;
		prop = p.getFullPropertyName();
		return navajoName + ":" + prop;
	}

	public Property parsePropertyPath(String path) {
		if (path.indexOf(":") == -1) {
			Navajo n = getNavajo();
			if (n != null) {
				return n.getProperty(path);
			}

		} else {
			String[] elts = path.split(":");
			Navajo n = getNavajo(elts[0]);
			return n.getProperty(elts[1]);
		}
		return null;
	}

	public Map<String, Property> getPropertyElement() {
		return new PropertyAccessMap(this);
	}

	public void resolvePost(String name, String value) {
		if (name.indexOf(":") == -1) {
			return;
		}
		String[] keyVal = name.split(":");
		String navajo = keyVal[0];
		String path = keyVal[1];
		Navajo n = getNavajo(navajo);
		Property p = n.getProperty(path);
		p.setValue(value);
	}

	public String dumpStack() {
		StringBuffer sb = new StringBuffer();
		if (myElementStack.isEmpty()) {
			sb.append("Empty element stack!");
		} else {
			sb.append("Stacksize: " + myElementStack.size());
		}
		for (Object a : myElementStack) {
			sb.append("Current object: " + a.getClass() + "\n");
		}
		return sb.toString();
	}

	public void popProperty() {
		myElementStack.pop();

	}

	public void setValue(String path, Object value) {
		Object oo = getTopmostElement(new Class[] { Message.class, Navajo.class });
		if (Message.class.isAssignableFrom(oo.getClass())) {
			Property p = ((Message) oo).getProperty(path);
			p.setAnyValue(value);
		}
		if (Navajo.class.isAssignableFrom(oo.getClass())) {
			Property p = ((Navajo) oo).getProperty(path);
			p.setAnyValue(value);
		}
		logger.info("Odd stack problem");
	}

	public void setValue(Object value) {
		getProperty().setAnyValue(value);
	}

	public Message addMessage(String name,Map<String, String> attributes) throws NavajoException, MappingException {
		Object oo = getTopmostElement(new Class[] { Message.class, Navajo.class });
		if (Message.class.isAssignableFrom(oo.getClass())) {
			Message parent = (Message) oo;
			Message[] mm = MappingUtils.addMessage(parent.getRootDoc(), parent, name, "", 1, attributes.get(Message.MSG_TYPE), attributes.get(Message.MSG_MODE), attributes.get(Message.MSG_ORDERBY));
			if(mm.length==0) {
				throw new MappingException("I've just created a message, but it isn't there.");
			}
			pushMessage(mm[0]);
			return mm[0];
		}
		if (Navajo.class.isAssignableFrom(oo.getClass())) {
			Navajo n = (Navajo)oo;
			Message[] mm = MappingUtils.addMessage(n, null, name, "", 1, attributes.get(Message.MSG_TYPE), attributes.get(Message.MSG_MODE), attributes.get(Message.MSG_ORDERBY));
			if(mm.length==0) {
				throw new MappingException("I've just created a message, but it isn't there.");
			}
			pushMessage(mm[0]);
			return mm[0];
		}

		return null;
	}

	public Message addArrayMessage(String name,Map<String, String> attributes) throws NavajoException, MappingException {
		
		Map<String,String> cloneAttributes = new HashMap<String,String>(attributes);
		cloneAttributes.put(Message.MSG_TYPE, Message.MSG_TYPE_ARRAY);
		return addMessage(name, cloneAttributes);

	}

	public Message addParamArrayMessage(String name) throws NavajoException {
		Navajo out = getAccess().getOutputDoc();
		Message params = getTopParamMessage();
		Message result = NavajoFactory.getInstance().createMessage(out, name,
				Message.MSG_TYPE_ARRAY);
		params.addMessage(result);

		result.write(System.err);
		currentParamMessage = result;
		pushMessage(result);
		return result;
	}

	public Message addElement() throws NavajoException {
		Message e = super.addElement(getMessage());
		pushElement(e);
		return e;
	}

	public Property addProperty(String name, Object value, Map<String,String> attributes)
			throws NavajoException, MappingException {
		if (getMessage() == null) {
			log("No message, can not add property!");
			log(dumpStack());
		}
		Property p = super.addProperty(getMessage(), name, value,attributes);
		// pushProperty(p);
		return p;
	}

	public Property addParam(String name, Object value,
			Map<String, String> attributes) throws NavajoException {
//		int length = 0;
//		String str = attributes.get("length");
//		if (str != null) {
//			length = Integer.valueOf(str);
//		}

		String type = attributes.get("type");
		if (type == null) {
			type = Property.STRING_PROPERTY;
		}
		
		if(name.startsWith("/")) {
			Message param = getTopParamMessage();
			name = name.substring(1);
			Property pp = getParam(name, param);
			if (pp == null) {
				pp = createProperty(name, value, attributes, getAccess()
						.getInDoc());
				param.addProperty(pp);
			} else {
				pp.setAnyValue(value);
			}
			return pp;
			
		}
		try {
			Message param = getTopParamStackMessage();
			Property pp = getParam(name, param);
			if (pp == null) {
				pp = createProperty(name, value, attributes, getAccess()
						.getInDoc());
				param.addProperty(pp);
			} else {
				pp.setAnyValue(value);
			}
			return pp;
		} catch (NumberFormatException e) {
			logger.error("Error: ", e);
		}
		// return p;
		return null;
	}

	private Property getParam(String name, Message param)
			throws NavajoException {
		if (param == null) {
			param = getParamMessage();
		}
		Property pp = param.getProperty(name);

		return pp;
	}

	private Message getParamMessage() throws NavajoException {
		Message par = currentParamMessage;
		// if(par!=null) {
		// return par;
		// }
		par = getAccess().getInDoc().getMessage(Message.MSG_PARAMETERS_BLOCK);
		if (par == null) {
			par = NavajoFactory.getInstance().createMessage(
					getAccess().getInDoc(), Message.MSG_PARAMETERS_BLOCK,
					Message.MSG_TYPE_SIMPLE);
			getAccess().getInDoc().addMessage(par);
		}
		return par;
	}

	public Selection addSelection(String name, String value, Object selected)
			throws NavajoException {
		return super.addSelection(getProperty(), name, value, selected);
	}
	public Selection addSelectionToProperty(Property p, String name, Object value,
			Object selected) throws NavajoException {
		return super.addSelection(p, name, value, selected);
	}
	public Method addMethod(String name) throws NavajoException {
		// return super.addProperty(getMessage(), name, value);
		Method m = NavajoFactory.getInstance().createMethod(
				getAccess().getOutputDoc(), name, null);
		getAccess().getOutputDoc().addMethod(m);
		return m;
	}

	@Override
	public ScriptEnvironment createEnvironment() {
		return new StackScriptEnvironment(this);
	}

}
