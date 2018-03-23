/* Generated By:JJTree&JavaCC: Do not edit this line. ASTDivNode.java */
package com.dexels.navajo.parser.internal;

import com.dexels.navajo.parser.TMLExpressionException;
import com.dexels.navajo.parser.Utils;

public final class ASTDivNode extends SimpleNode {
	public ASTDivNode(int id) {
		super(id);
		// System.out.println("in ASTDivNode()");
	}

	@Override
	public final Object interpret() throws TMLExpressionException {

		// System.out.println("in interpret() ASTDivNode");
		Object a = this.jjtGetChild(0).interpret();
		Object b = this.jjtGetChild(1).interpret();

        if (a == null || b == null) {
            throw new TMLExpressionException("Illegal arguement for div. Cannot div " + a + " / " + b + ". No null values are allowed.");
        }

		if (a instanceof String || b instanceof String)
			throw new TMLExpressionException("Division not defined for strings");
		if (a instanceof Integer && b instanceof Integer)
			return new Integer(((Integer) a).intValue() / ((Integer) b).intValue());
		double dA = Utils.getDoubleValue(a);
		double dB = Utils.getDoubleValue(b);

		return new Double(dA / dB);
	}
}
