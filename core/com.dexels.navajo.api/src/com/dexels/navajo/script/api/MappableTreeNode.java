/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.script.api;

/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version 1.0
 */

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

class ArrayChildStatistics implements Serializable {
    /**
	 * 
	 */
    private static final long serialVersionUID = 576411679354811834L;

    public int elementCount;
    public int totalTime;
}

@SuppressWarnings("unchecked")
public final class MappableTreeNode implements Mappable, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 6880152616096374576L;

    public MappableTreeNode parent = null;
    public Mappable myMap = null;
    public transient Object myObject = null;
    public String name = "";
    public String ref = "";
    public String currentMethod = "";
    public long starttime;
    public long endtime = -1;
    public int totaltime;
    public int navajoLineNr = -1;

    // HashMap to cache method references.
    private transient Map<String, Method> methods;
    private int id = 0;
    private transient Map<String, ArrayChildStatistics> elementCount = null;
    private Access myAccess = null;
    private boolean arrayElement = false;

    private MapStatistics myStatistics;

    public MappableTreeNode(MappableTreeNode parent, Object o) {
        this(null, parent, o, false);
    }

    public MappableTreeNode(Access a, MappableTreeNode parent, Object o, boolean isArrayElement) {
        this.parent = parent;
        this.myObject = o;
        methods = new HashMap<>();
        starttime = System.currentTimeMillis();
        myAccess = a;
        arrayElement = isArrayElement;
        if (parent == null) {
            id = 0;
        } else {
            id = parent.getId() + 1;
            if (isArrayElement) {
                parent.incrementElementCount(o.getClass().getName());
            }
        }
        if (myAccess != null && !arrayElement && !hasArrayParent()) {
            myStatistics = a.createStatistics();
        }
        // Call setDebug automatically if object class implements Debugable
        // interface and full debug is set for webservice.
        if (a != null && a.logFullAccessLog() && Debugable.class.isInstance(o)) {
            ((Debugable) o).setDebug(true);
        }
    }

    public Object getMyMap() {
        return myObject;
    }

    public String getCurrentMethod() {
        return currentMethod;
    }

    private final boolean hasArrayParent() {
        if (getParent() == null) {
            return false;
        }
        if (getParent().arrayElement) {
            return true;
        }
        return getParent().hasArrayParent();
    }

    private final ArrayChildStatistics getArrayChildStatistics(String mapName) {
        if (elementCount == null) {
            return null;
        }
        return elementCount.get(mapName);
    }

    private final void incrementElementCount(String mapName) {
        if (elementCount == null) {
            elementCount = new HashMap<>();
        }
        ArrayChildStatistics c = elementCount.get(mapName);
        if (c == null) {
            c = new ArrayChildStatistics();
            c.elementCount = 1;
        } else {
            c.elementCount++;
        }
        elementCount.put(mapName, c);
    }

    public int getId() {
        return id;
    }

    public MappableTreeNode getParent() {
        return parent;
    }

    /**
     * end time is called when finished with map.
     *
     */
    public final void setEndtime() {
        endtime = System.currentTimeMillis();
        if (myAccess != null && !arrayElement && !hasArrayParent()) {
            if (myObject != null) {
                myAccess.updateStatistics(myStatistics, id, myObject.getClass().getSimpleName(), getTotaltime(), 0, false, navajoLineNr);
            }
            // Sum array children.
            if (elementCount != null) {
                int childId = id + 1;
                for (Iterator<String> iter = elementCount.keySet().iterator(); iter.hasNext();) {
                    String mapName = iter.next();
                    ArrayChildStatistics acs = elementCount.get(mapName);
                    MapStatistics childStatistics = myAccess.createStatistics();
                    myAccess.updateStatistics(childStatistics, childId, mapName, acs.totalTime, acs.elementCount, true, navajoLineNr);
                }
            }
        } else { // I am array child element.
            if (getParent() != null) {
                if (myObject != null) {
                    ArrayChildStatistics acs = getParent().getArrayChildStatistics(myObject.getClass().getName());
                    if (acs != null) {
                        acs.totalTime += getTotaltime();
                    }
                }
            }
        }
    }

    public int getTotaltime() {
        if (endtime == -1) {
            return (int) (System.currentTimeMillis() - starttime);
        } else {
            return (int) (endtime - starttime);
        }
    }

    public String getMapName() {
        if (myObject != null) {
            return myObject.getClass().getName();
        } else {
            return null;
        }
    }

    @SuppressWarnings("rawtypes")
    public final Method getMethodReference(String name, Object[] arguments) throws MappingException {

        StringBuilder key = new StringBuilder();
        key.append(name);

        // Determine method unique method key:a
        Class[] classArray = null;
        int argsLength = 0;
        if (arguments != null) {
            // Get method with arguments.
            classArray = new Class[arguments.length];
            argsLength = arguments.length;
            for (int i = 0; i < arguments.length; i++) {
                if (arguments[i] == null) {
                    classArray[i] = null;
                    key.append("null");

                } else {
                    classArray[i] = arguments[i].getClass();
                    key.append(arguments[i].getClass().getName());
                }
               
            }
        }

        Method m = methods.get(key.toString());

        if (m == null) {

        	StringBuilder methodNameBuffer = new StringBuilder();
            methodNameBuffer.append("get").append((name.charAt(0) + "").toUpperCase())
                    .append(name.substring(1, name.length()));

            String methodName = methodNameBuffer.toString();

            Class c = myObject.getClass();
            
            try {
                m = c.getMethod(methodName, classArray);
                methods.put(key.toString(), m);
            } catch (NoSuchMethodException nsme) {
                // Check if its a parameter mismatch but interface match
                
                for (Method method : c.getMethods()) {
                    if (!method.getName().equals(methodName)) {
                        continue;
                    }
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    if ( method.getParameterTypes().length !=  argsLength) {
                        continue;
                    }
                    boolean matches = true;
                    for (int i = 0; i < parameterTypes.length; i++) {
                        if (arguments[i] == null) {
                            // Null argument is allowed. isAssignableFrom would give NPE however
                            matches = true;
                            break;
                        } else if (!parameterTypes[i].isAssignableFrom(arguments[i].getClass())) {
                            matches = false;
                            break;
                        }
                        
                    }
                    if (matches) {
                        m = method;
                        methods.put(key.toString(), m);
                        break;
                    }
                }
                
                // If m is still null...
                if (m == null) {
                    throw new MappingException("Could not find method in Mappable object: " + methodName + " in object: "
                            + getMyMap());
                }
            }
        }
        return m;

    }

    @SuppressWarnings("rawtypes")
    public final Method setMethodReference(String name, Class[] parameters) throws MappingException {
        java.lang.reflect.Method m = methods.get(name + Arrays.hashCode(parameters));

        if (m == null) {
            String methodName = "set" + (name.charAt(0) + "").toUpperCase() + name.substring(1, name.length());

            Class c = this.myObject.getClass();

            java.lang.reflect.Method[] all = c.getMethods();
            for (int i = 0; i < all.length; i++) {
                if (all[i].getName().equals(methodName)) {
                    m = all[i];
                    Class[] inputParameters = m.getParameterTypes();
                    if (inputParameters.length == parameters.length) {
                        for (int j = 0; j < inputParameters.length; j++) {
                            Class myParm = parameters[j];
                            if (inputParameters[j].isAssignableFrom(myParm)) {
                                i = all.length + 1;
                                j = inputParameters.length + 1;
                                break;
                            }
                        }
                    }
                }
            }
            if (m == null) {
                throw new MappingException("Could not find method in Mappable object: " + methodName);
            }
            methods.put(name + Arrays.hashCode(parameters), m);
        }

        return m;
    }

    @Override
    public void load(Access access) throws MappableException, UserException {
    }

    @Override
    public void store() throws MappableException, UserException {
    }

    @Override
    public void kill() {
    }

    public int getNavajoLineNr() {
        return navajoLineNr;
    }

    /**
     *  Fallback method for when no Line attribute is found in the XML ELement (e.g. parsing a TSL script)
     */
    public void setNavajoLineNr() {
        
    }
    
    public void setNavajoLineNr(int navajoLineNr) {
        this.navajoLineNr = navajoLineNr;
    }
    
    

}
