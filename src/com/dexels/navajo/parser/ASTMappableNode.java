/* Generated By:JJTree: Do not edit this line. ASTMappableNode.java */

package com.dexels.navajo.parser;


import java.util.*;
import com.dexels.navajo.mapping.Mappable;
import com.dexels.navajo.mapping.MappableTreeNode;
import com.dexels.navajo.util.Util;


public class ASTMappableNode extends SimpleNode {

    String val = "";
    MappableTreeNode mapObject;
    int args = 0;

    public ASTMappableNode(int id) {
        super(id);
    }

    public final Object interpret() throws TMLExpressionException {

        if (mapObject == null)
            throw new TMLExpressionException("No known mapobject");

        ArrayList objects = null;

        // Parameter array may contain parameters that are used when calling the get method.
        Object[] parameterArray = null;

        if (args > 0)
            objects = new ArrayList();

        for (int i = 0; i < args; i++) {
            Object a = (Object) jjtGetChild(i).interpret();
            objects.add(a);
        }

        if (objects != null) {
            parameterArray = new Object[objects.size()];
            parameterArray = (Object[]) objects.toArray(parameterArray);
        }

        try {
            Object oValue = com.dexels.navajo.mapping.XmlMapperInterpreter.getAttributeValue(mapObject, val, parameterArray);
            if (oValue == null)
                return null;
            else if (oValue instanceof Float) {
              return new Double(((Float) oValue).doubleValue());
            } else if (oValue instanceof Long) {
              return new Integer(((Long) oValue).intValue());
            } else
              return oValue;

        } catch (Exception me) {
            me.printStackTrace();
            throw new TMLExpressionException(me.getMessage());
        }
    }

    public static void main(String args[]) {
        int a = 10;

    }
}

