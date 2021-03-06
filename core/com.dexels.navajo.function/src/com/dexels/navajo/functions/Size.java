/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;


import java.util.List;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoFactory;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.parser.Expression;


/**
 * Title:        Navajo
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Dexels
 * @author Arjen Schoneveld en Martin Bergman
 * @version $Id$
 */

public final class Size extends FunctionInterface {

    public Size() {}

    @Override
	@SuppressWarnings("rawtypes")
	public final Object evaluate() throws com.dexels.navajo.expression.api.TMLExpressionException {
    	if(this.getOperands().size()==0) {
    		return Integer.valueOf(0);
    	}
    	if(this.getOperands().size()==1 && this.getOperands().get(0)==null) {
    		return Integer.valueOf(0);
    	}

    	Object arg = this.getOperands().get(0);

        //System.out.println("IN SIZE(), ARG = " + arg);
        if (arg == null) {
            throw new TMLExpressionException("Argument expected for Size() function.");
        }
        else if (arg instanceof java.lang.String) {
            return Integer.valueOf(((String) arg).length());
        }
        else if (arg instanceof Binary) {
        	return Integer.valueOf( (int) ((Binary) arg).getLength());
        }
        else if (arg instanceof Message) {
        	return Integer.valueOf( ((Message) arg).getArraySize());
        }
        else if (arg instanceof Object[]) {
          	return Integer.valueOf( ((Object[]) arg).length);
          }

        else if (!(arg instanceof List)) {
            throw new TMLExpressionException("Expected list argument for size() function.");
        }

        List list = (List) arg;

        return Integer.valueOf(list.size());
    }

    @Override
	public String usage() {
        return "Size(list | arraymessage | array)";
    }

    @Override
	public String remarks() {
        return "This function return the size of a list argument, the length of an array, or the size of an array message.";
    }

    public static void main(String [] args) throws Exception {
      Navajo n = NavajoFactory.getInstance().createNavajo();
      Message m = NavajoFactory.getInstance().createMessage(n, "Aap");
      n.addMessage(m);
      Property p = NavajoFactory.getInstance().createProperty(n, "Selection", "+", "", "out");
      m.addProperty(p);
      Selection s = NavajoFactory.getInstance().createSelection(n, "Aap", "0", false);
      p.addSelection(s);
      n.write(System.err);
      Operand o = Expression.evaluate("GetPropertyType('/Aap/Selection') == 'selection' AND Size([Aap/Selection]) == 0", n);
      System.err.println("o " + o.value + ", type: " + o.type);
    }
}
