/* Generated By:JJTree&JavaCC: Do not edit this line. ASTMulNode.java */
package com.dexels.navajo.parser.compiled;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.document.types.Percentage;
import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.FunctionClassification;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.parser.Utils;


public final class ASTMulNode extends SimpleNode {

    public ASTMulNode(int id) {
        super(id);
    }
	@Override
	public ContextExpression interpretToLambda(List<String> problems, String expression, Function<String, FunctionClassification> functionClassifier) {
		// TODO We can do *some* type restriction, just not much.
		return lazyBiFunction(problems,expression, (a,b)->interpret(a, b),(a,b)->true,(a,b)->Optional.empty(),functionClassifier);
	}
	
	public  Operand interpret(Operand ao, Operand bo) {
		Object a = ao.value;
		Object b = bo.value;

        if ((a instanceof Integer) && (b instanceof Integer))
            return  Operand.ofInteger(((Integer) a).intValue() * ((Integer) b).intValue());
        else if ((a instanceof String) || (b instanceof String))
            throw new TMLExpressionException("Multiplication not defined for String values");
        else if (a instanceof Double && b instanceof Integer)
            return Operand.ofFloat(((Double) a).doubleValue() * ((Integer) b).intValue());
        else if (a instanceof Integer && b instanceof Double)
            return Operand.ofFloat(((Double) b).doubleValue() * ((Integer) a).intValue());
        else if (a instanceof Double && b instanceof Double)
            return Operand.ofFloat(((Double) b).doubleValue() * ((Double) a).doubleValue());
        else if (a instanceof Money || b instanceof Money)
            return Operand.ofMoney(new Money(Utils.getDoubleValue(a) * Utils.getDoubleValue(b)));
          else if (a instanceof Percentage || b instanceof Percentage)
              return Operand.ofMoney(new Money(Utils.getDoubleValue(a) * Utils.getDoubleValue(b)));
        else
//            throw new TMLExpressionException("Unknown type");
        	return null;
    }

}
