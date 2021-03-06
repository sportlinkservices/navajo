/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/* Generated By:JJTree&JavaCC: Do not edit this line. ASTLENode.java */
package com.dexels.navajo.parser.compiled;


import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.types.ClockTime;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.document.types.Percentage;
import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.FunctionClassification;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.parser.Utils;

@SuppressWarnings({"rawtypes"})

abstract class ComparisonNode extends SimpleNode {
    
	ComparisonNode(int id) {
        super(id);
    }
	@Override
	public ContextExpression interpretToLambda(List<String> problems, String expression, Function<String, FunctionClassification> functionClassifier, Function<String,Optional<Node>> mapResolver) {
		return lazyBiFunction(problems,expression,(a,b)->interpret(a, b,expression),(a,b)->true,(a,b)->Optional.of(Property.BOOLEAN_PROPERTY),functionClassifier,mapResolver);
	}
	
	protected abstract ComparisonOperator getComparisonOperator();
	
    private static final Boolean compare(ComparisonOperator compOp, Operand ao, Operand bo, String expression) {
    	Object a = ao.value;
    	Object b = bo.value;
        if (a == null || b == null) {
            throw new TMLExpressionException(
                    "Illegal arguement for " + compOp.getDescription() + ";. Cannot compare " + a + " " + compOp.getOperator() + " " + b + ". No null values are allowed. Expression: "+expression);
        }
        
        if (a instanceof Integer && b instanceof Integer)
        	return Utils.compare((Integer) a, (Integer) b, compOp.getOperator());
        else if (a instanceof Integer && b instanceof Double)
        	return Utils.compare(((Integer) a).doubleValue(), (Double) b, compOp.getOperator());
        else if (a instanceof Double && b instanceof Integer)
        	return Utils.compare((Double) a, ((Integer) b).doubleValue(), compOp.getOperator());
        else if (a instanceof Double && b instanceof Double)
        	return Utils.compare((Double) a, (Double) b, compOp.getOperator());
        else if (a instanceof Date)
            return Boolean.valueOf(Utils.compareDates(a, b, compOp.getOperator()));
        else if (a instanceof Money || b instanceof Money)
        	return Utils.compare(Utils.getDoubleValue(a), Utils.getDoubleValue(b), compOp.getOperator());
        else if (a instanceof Percentage || b instanceof Percentage)
        	return Utils.compare(Utils.getDoubleValue(a), Utils.getDoubleValue(b), compOp.getOperator());
        else if (a instanceof ClockTime && b instanceof ClockTime)
           return Boolean.valueOf(Utils.compareDates(a, b, compOp.getOperator()));
        else if (a instanceof String && b instanceof String)
        	return Utils.compare((String) a, (String) b, compOp.getOperator());
        else
            throw new TMLExpressionException("Illegal comparison for " + compOp.getDescription() + "; " + a.getClass().getName() + " " + b.getClass().getName());
    }

	private final Operand interpret(Operand a, Operand b, String expression)  {
        if (a instanceof List) { // Compare all elements in the list.
            List list = (List) a;
            for (int i = 0; i < list.size(); i++) {
                boolean dum = compare(getComparisonOperator(), Operand.ofDynamic(list.get(i)), b, expression).booleanValue();

                if (!(dum))
                    return Operand.FALSE;
            }
            return Operand.TRUE;
        } else {
            return Operand.ofBoolean(compare(getComparisonOperator(), a, b, expression ) );
        }
    }
	
	public enum ComparisonOperator {
		LE( "<=" ), LT( "<" ), GE( ">=" ), GT( ">" );
		
		private final String operator;
		
		ComparisonOperator(String op)
		{
			operator = op;
		}
		
		String getDescription() { return this.name().toLowerCase(); }
		String getOperator() { return operator; }
		
	}
}
