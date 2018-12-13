/* Generated By:JJTree&JavaCC: Do not edit this line. ASTEQNode.java */
package com.dexels.navajo.parser.compiled;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.FunctionClassification;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.parser.Utils;

public final class ASTEQNode extends SimpleNode {
    public ASTEQNode(int id) {
        super(id);
    }

	public final Operand interpret( Operand a, Operand b, String expression) throws TMLExpressionException {
        return Operand.ofBoolean(Utils.equals(a.value, b.value,expression));
    }
	@Override
	public ContextExpression interpretToLambda(List<String> problems, String expression, Function<String, FunctionClassification> functionClassifier) {
		return lazyBiFunction(problems,expression,(a,b)->interpret(a, b, expression),equalOrEmptyTypes(),(a,b)->Optional.of(Property.BOOLEAN_PROPERTY),functionClassifier);
	}
    
}
