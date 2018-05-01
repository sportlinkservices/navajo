/* Generated By:JJTree&JavaCC: Do not edit this line. ASTNENode.java */

package com.dexels.navajo.parser.compiled;

import java.util.List;
import java.util.Optional;

import com.dexels.navajo.document.Property;
import com.dexels.navajo.parser.Utils;
import com.dexels.navajo.parser.compiled.api.ContextExpression;

public final class ASTNENode extends SimpleNode {
    public ASTNENode(int id) {
        super(id);
    }
	@Override
	public ContextExpression interpretToLambda(List<String> problems, String expression) {
		return lazyBiFunction(problems,expression, (a,b)->interpret(a, b,expression),equalOrEmptyTypes(),(a,b)->Optional.of(Property.BOOLEAN_PROPERTY));
	}
	
	public final Object interpret(Object a, Object b, String expression) {

        return Boolean.valueOf(!Utils.equals(a, b,expression));

    }
}
