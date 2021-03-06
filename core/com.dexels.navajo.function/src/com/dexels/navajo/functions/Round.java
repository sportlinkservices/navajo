/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;


import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;


/**
 * Title:        Navajo
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Dexels
 * @author Arjen Schoneveld en Martin Bergman
 * @version $Id$
 *
 * $Id$
 *
 $ $Log$
 $ Revision 1.2  2011/03/09 09:31:43  frank
 $ Warning cleanup
 $
 $ Revision 1.1  2005/05/13 13:02:50  matthijs
 $ init version
 $
 $ Revision 1.4  2004/01/27 13:35:59  arjen
 $ Added some final qualifiers and fixed isAsyncEnabled() bug in
 $ TslCompiler (NOTE: remove previous compiled versions and sources of scripts
 $ that use Asynchronous mappable objects.
 $
 $ Revision 1.3  2002/11/06 09:33:47  arjen
 $ Used Jacobe code beautifier over all source files.
 $ Added log4j support.
 $
 $ Revision 1.2  2002/09/18 14:22:57  arjen
 $ *** empty log message ***
 $
 $ Revision 1.1.1.1  2002/06/05 10:12:27  arjen
 $ Navajo
 $
 $ Revision 1.2  2002/03/11 16:20:43  arjen
 $ *** empty log message ***
 $
 */

public final class Round extends FunctionInterface {

    public Round() {}

    @Override
	public String remarks() {
        return "With this function a floating point value can be rounded to a given number of digits. Round(2.372, 2) = 2.37";
    }

    @Override
	public String usage() {
        return "Round(float, integer).";
    }

	@Override
	public final Object evaluate() throws com.dexels.navajo.expression.api.TMLExpressionException {
		Object a = this.getOperands().get( 0 );
		Object b = this.getOperands().get( 1 );

		try {
			Double d = ( Double ) a;
			Integer i = ( Integer ) b;
			double dd = d.doubleValue();
			int digits = i.intValue();

			dd = (int) Math.signum(dd) * ( (int) ( 0.5 + Math.abs(dd) * Math.pow( 10.0, digits ) ) ) / Math.pow( 10.0, digits );

			return Double.valueOf( dd );
		} catch ( Exception e ) {
			throw new TMLExpressionException( this, "Illegal type specified in Round() function: " + e.getMessage() );
		}
	}

	public static void main(String [] args) throws Exception {
		Round r = new Round();

		r.reset();
		r.insertFloatOperand( Double.valueOf( -100.3 ) );
		r.insertIntegerOperand( Integer.valueOf( 0 ) );

		System.err.println("Input " + r.getOperand( 0 ) + ", rounded to " + r.getOperand( 1 ) + " digits: " + r.evaluate() );
		
				
	}
}
