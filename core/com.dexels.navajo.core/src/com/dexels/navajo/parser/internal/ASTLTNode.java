/* Generated By:JJTree&JavaCC: Do not edit this line. ASTLTNode.java */
package com.dexels.navajo.parser.internal;

import java.util.ArrayList;
import java.util.Date;

import com.dexels.navajo.document.types.ClockTime;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.document.types.Percentage;
import com.dexels.navajo.parser.TMLExpressionException;
import com.dexels.navajo.parser.Utils;

@SuppressWarnings({ "rawtypes" })

public final class ASTLTNode extends SimpleNode {
	public ASTLTNode(int id) {
		super(id);
	}

	public final static Boolean compare(Object a, Object b) throws TMLExpressionException {

        if (a == null || b == null) {
            throw new TMLExpressionException(
                    "Illegal arguement for lt;. Cannot compare " + a + " < " + b + ". No null values are allowed.");
        }

		if (a instanceof Integer && b instanceof Integer)
			return Boolean.valueOf(((Integer) a).intValue() < ((Integer) b).intValue());
		else if (a instanceof Integer && b instanceof Double)
			return Boolean.valueOf(((Integer) a).intValue() < ((Double) b).doubleValue());
		else if (a instanceof Double && b instanceof Integer)
			return Boolean.valueOf(((Double) a).intValue() < ((Integer) b).doubleValue());
		else if (a instanceof Double && b instanceof Double)
			return Boolean.valueOf(((Double) a).doubleValue() < ((Double) b).doubleValue());
		else if (a instanceof Date)
			return Boolean.valueOf(Utils.compareDates(a, b, "<"));
		else if (a instanceof Money || b instanceof Money)
			return Boolean.valueOf(Utils.getDoubleValue(a) < Utils.getDoubleValue(b));
		else if (a instanceof Percentage || b instanceof Percentage)
			return Boolean.valueOf(Utils.getDoubleValue(a) < Utils.getDoubleValue(b));
		else if (a instanceof ClockTime && b instanceof ClockTime)
			return Boolean.valueOf(Utils.compareDates(a, b, "<"));
		else
			throw new TMLExpressionException(
                    "Illegal comparison for lt; " + a.getClass().getName() + " " + b.getClass().getName());
	}

	@Override
	public final Object interpret() throws TMLExpressionException {
		// System.out.println("in ASTGENode()");
		Object a = jjtGetChild(0).interpret();
		// System.out.println("Got first argument");
		Object b = jjtGetChild(1).interpret();

		// System.out.println("Got second argument");

		if (a instanceof java.util.ArrayList) { // Compare all elements in the list.
			ArrayList list = (ArrayList) a;
			boolean result = true;

			for (int i = 0; i < list.size(); i++) {
				boolean dum = compare(list.get(i), b).booleanValue();

				if (!(dum))
					return Boolean.valueOf(false);
				result = result && dum;
			}
			return Boolean.valueOf(result);
		} else {
			return compare(a, b);
		}
	}
}
