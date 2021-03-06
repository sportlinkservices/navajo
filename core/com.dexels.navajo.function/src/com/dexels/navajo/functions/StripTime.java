/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;

import java.text.ParseException;
import java.util.Calendar;

import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;

public class StripTime extends FunctionInterface {
	public StripTime() {}

	@Override
	public String remarks() {
		return "Strips the time from a date or string object";
	}

	@Override
	public final Object evaluate() throws com.dexels.navajo.expression.api.TMLExpressionException {
		Object o = getOperand(0);
		java.util.Date date = null;
		if (o instanceof java.util.Date) {
			date = (java.util.Date) o;
		} else if (o instanceof String) {
		    String format = (String)this.getOperands().get(1);
		    java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat(format);
		    try {
		      date = formatter.parse((String)o);
		    } catch (ParseException ex) {
		      throw new TMLExpressionException(this, "Invalid date format: " + (String)o + " for given date pattern: " + format);
		    }
		} else {
			throw new TMLExpressionException("Invalid date: " + o);
		}
		
		if (date != null) {
			Calendar cal = Calendar.getInstance();  
			cal.setTime(date);  

			// Set time fields to zero  
			cal.set(Calendar.HOUR_OF_DAY, 0);  
			cal.set(Calendar.MINUTE, 0);  
			cal.set(Calendar.SECOND, 0);  
			cal.set(Calendar.MILLISECOND, 0);  
			date = cal.getTime();
		}
		return date;
	}

	@Override
	public String usage() {
		return "StripTime(Date/String): java.sql.Date";
	}

    public static void main(String args[]) throws Exception {
    	StripTime st = new StripTime();
    	st.reset();
    	java.util.Date input = new java.util.Date();
    	java.util.Date output = null;
//    	st.insertOperand(input);
    	st.insertStringOperand("01-07-2012 12:00:10");
    	st.insertStringOperand("dd-MM-yyyy hh:mm:ss");
    	output = (java.util.Date)st.evaluate();
    	System.out.println("Old date : " + input + " - New date : " + output);
    }
}
