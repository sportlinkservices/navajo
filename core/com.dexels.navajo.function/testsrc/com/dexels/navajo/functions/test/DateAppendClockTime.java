package com.dexels.navajo.functions.test;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

import com.dexels.navajo.document.types.ClockTime;

/**
 * Code for the test suite <b>DateAppendClockTime</b> located at
 * <i>/NavajoFunctions/testsrc/DateAppendClockTime.testsuite</i>.
 */
public class DateAppendClockTime {
	/**
	 * Constructor for DateAdd.
	 * 
	 * @param name
	 */

	com.dexels.navajo.functions.DateAppendClockTime da = new com.dexels.navajo.functions.DateAppendClockTime();

	@Test
	public void testValidArguements() throws Exception {
		Date date = new Date();
		ClockTime cTime = new ClockTime("11:28");

		System.out.println(" ------ Running Valid Parameters case ------ ");
		System.out.println("Sending Date :: " + date);
		System.out.println("Sending ClockTime :: " + cTime);

		da.reset();
		da.insertOperand(date);
		da.insertOperand(cTime);
		System.out.println(da.evaluate());

		Object o = da.evaluate();
		assertEquals(o.getClass(), java.util.Date.class);
	}

	@Test(expected = com.dexels.navajo.parser.TMLExpressionException.class)
	public void testInvalidNumArguements() throws Exception {
		Date date = new Date();
		ClockTime cTime = new ClockTime("11:28");

		System.out.println(" ------ Running Valid Parameters case ------ ");
		System.out.println("Sending Date :: " + date);
		System.out.println("Sending ClockTime :: " + cTime);

		da.reset();
		System.out.println(da.evaluate());

		Object o = da.evaluate();
	}

	@Test(expected = com.dexels.navajo.parser.TMLExpressionException.class)
	public void testInvalidTypeArguements() throws Exception {
		Date date = new Date();
		ClockTime cTime = new ClockTime("11:28");

		System.out.println(" ------ Running Valid Parameters case ------ ");
		System.out.println("Sending Date :: " + date);
		System.out.println("Sending ClockTime :: " + cTime);

		da.reset();
		da.insertOperand(date);
		da.insertOperand("11:28");
		System.out.println(da.evaluate());

		Object o = da.evaluate();
	}

}
