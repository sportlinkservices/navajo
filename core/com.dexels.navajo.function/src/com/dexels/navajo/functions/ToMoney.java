/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;

import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.parser.Expression;


/**
 * <p>Title: Navajo Product Project</p>
 * <p>Description: This is the official source for the Navajo server</p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Dexels BV</p>
 * @author Arjen Schoneveld
 * @version 1.0
 */

public final class ToMoney extends FunctionInterface {
  public ToMoney() {
  }

  @Override
public String remarks() {
    return "Cast a string/double/integer to a money object";
  }
  @Override
	public boolean isPure() {
  		return false;
  }

  @Override
public final Object evaluate() throws com.dexels.navajo.expression.api.TMLExpressionException {
    Object o = getOperand(0);
   if (o == null) {
     return new Money( (Money)null);
   }
   else {
     return new Money(o);
   }
  }

  @Override
public String usage() {
    return "ToMoney(String/Integer/Double): Money";
  }

  public static void main(String [] args) throws Exception {

    java.util.Locale.setDefault(new java.util.Locale("nl", "NL"));
    // Tests.
   ToMoney tm = new ToMoney();
    tm.reset();
    tm.insertFloatOperand(Double.valueOf(1024.4990));
    System.out.println("result = " + ((Money) tm.evaluate()).formattedString());

    // Using expressions.
    String expression = "ToMoney(1024.50) + 500 - 5.7 + ToMoney(1)/2";
    Operand o = Expression.evaluate(expression, null);
    System.out.println("o = " + o.value);
    System.out.println("type = " + o.type);

    System.err.println("ToMoney('') = " + Expression.evaluate("ToMoney('')", null).value);

  }

}
