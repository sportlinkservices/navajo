/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/*
 * Created on Apr 25, 2005
 *
 */
package com.dexels.navajo.functions;

import java.util.List;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.document.types.Percentage;
import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.parser.Condition;
import com.dexels.navajo.parser.Expression;
import com.dexels.navajo.script.api.SystemException;

/**
 * @author arjen
 *
 */
public class SumExpressions extends FunctionInterface {

	/* (non-Javadoc)
	 * @see com.dexels.navajo.parser.FunctionInterface#remarks()
	 */
	@Override
	public String remarks() {
		return "Sum expressions in a given array message";
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.parser.FunctionInterface#usage()
	 */
	@Override
	public String usage() {
		 return "SumExpressions(<Array message name>,<expression>[,<filter>])";
	}

	/* (non-Javadoc)
	 * @see com.dexels.navajo.parser.FunctionInterface#evaluate()
	 */
	@Override
	public Object evaluate() throws TMLExpressionException {
		 if (getOperands().size() < 2) {
		      for (int i = 0; i < getOperands().size(); i++) {
		        Object o = getOperands().get(i);
		        System.err.println("Operand # " + i + " is: " + o.toString() + " - " +
		                           o.getClass());
		      }
		      throw new TMLExpressionException(this,
		                                       "Wrong number of arguments: " +
		                                       getOperands().size());
		    }
		    if (! (getOperand(0) instanceof String && getOperand(1) instanceof String)) {
		      throw new TMLExpressionException(this,
		                                       "Wrong argument types: " +
		                                       getOperand(0).getClass() + " and " +
		                                       getOperand(1).getClass());
		    }
		    String messageName = (String) getOperand(0);
		    String expression = (String) getOperand(1);
		    String filter = null;
		    if (getOperands().size() > 2) {
		      filter = (String) getOperand(2);
		    }
		    Message parent = getCurrentMessage();
		    Navajo doc = getNavajo();

		    try {
		      List<Message> arrayMsg = (parent != null ? parent.getMessages(messageName) :
		                            doc.getMessages(messageName));
		      if (arrayMsg == null) {
		        throw new TMLExpressionException(this,
		            "Empty or non existing array message: " + messageName);
		      }

		      String sumType = "int";
		      double sum = 0;
		      for (int i = 0; i < arrayMsg.size(); i++) {
		        Message m = arrayMsg.get(i);
		      
		        boolean evaluate = (filter != null ?
		                            Condition.evaluate(filter, doc, null, m,getAccess()) : true);
		        if (evaluate) {
		        	Operand o = Expression.evaluate(expression, m.getRootDoc(), null, m);
		        	if (o.value == null) {
		        		throw new TMLExpressionException(this, "Null value encountered");
		        	}
		        	if (o.value instanceof Integer) {
		        		sum += ((Integer) o.value).doubleValue();
		        	} else if (o.value instanceof Double) {
		        		sum += ((Double) o.value).doubleValue();
		        	} else {
		        		throw new TMLExpressionException(this, "Incompatible type while summing: " + o.value.getClass().getName());
		        	}
		        }
		      }
		      if (sumType.equals("int")) {
		        return Integer.valueOf( (int) sum);
		      }
		      else if (sumType.equals("money")) {
		        return new Money(sum);

		      }
		      else if (sumType.equals("percentage")) {
		        return new Percentage(sum);
		      }
		      else {
		        return Double.valueOf(sum);
		      }
		    }
		    catch (NavajoException ne) {
		      throw new TMLExpressionException(this, ne.getMessage());
		    }
		    catch (SystemException se) {
		      throw new TMLExpressionException(this, se.getMessage());
		    }
	}

}
