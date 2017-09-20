/* Generated By:JJTree&JavaCC: Do not edit this line. ASTOrNode.java */
package com.dexels.navajo.parser.internal;

import com.dexels.navajo.parser.TMLExpressionException;

public final class ASTOrNode extends SimpleNode {
	public ASTOrNode(int id) {
		super(id);
	}

	@Override
	public final Object interpret() throws TMLExpressionException {
		// System.out.println("in ASTOrNode()");
		Boolean a = (Boolean) jjtGetChild(0).interpret();
		if (a == null) {
			a = Boolean.FALSE;
		}
		if (a.booleanValue())
			return Boolean.TRUE;

		// System.out.println("Got first argument");
		Boolean b = (Boolean) jjtGetChild(1).interpret();
		if (b == null) {
			b = Boolean.FALSE;
		}

		// System.out.println("Got second argument");

		return Boolean.valueOf(a.booleanValue() || b.booleanValue());
	}
}
