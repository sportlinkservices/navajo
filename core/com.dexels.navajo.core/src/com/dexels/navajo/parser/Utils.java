/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.parser;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      Dexels BV
 * @author Arjen Schoneveld
 * @version $Id$
 *
 * DISCLAIMER
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL DEXELS BV OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 */
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.dexels.navajo.document.Property;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.document.types.Binary;
import com.dexels.navajo.document.types.ClockTime;
import com.dexels.navajo.document.types.DatePattern;
import com.dexels.navajo.document.types.Money;
import com.dexels.navajo.document.types.NavajoType;
import com.dexels.navajo.document.types.Percentage;
import com.dexels.navajo.document.types.StopwatchTime;
import com.dexels.navajo.expression.api.TMLExpressionException;

public final class Utils extends Exception {

	private static final long serialVersionUID = -5520295170789410974L;

	private Utils() {
	}

	private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

	private static final boolean compare(int a, int b, String c) {

		if (c.equals(">"))
			return (a > b);
		else if (c.equals("<"))
			return (a < b);
		else if (c.equals("=="))
			return (a == b);
		else if (c.equals("!="))
			return (a != b);
		else if (c.equals(">="))
			return (a >= b);
		else if (c.equals("<="))
			return (a <= b);
		else
			return false;
	}

	private static final boolean compare(Date a, Date b, String c) {
		if (c.equals(">"))
			return (a.after(b));
		else if (c.equals("<"))
			return (a.before(b));
		else if (c.equals("=="))
			return (a.equals(b));
		else if (c.equals("!="))
			return (!a.equals(b));
		else if (c.equals(">="))
			return (a.after(b) || a.equals(b));
		else if (c.equals("<="))
			return (a.before(b) || a.equals(b));
		else
			return false;
	}

	public static final <T extends Comparable<T>> boolean compare(T a, T b, String c) {

		int compareResult = a.compareTo(b);
		
		if (c.equals(">"))
			return compareResult > 0;
		else if (c.equals("<"))
			return compareResult < 0;
		else if (c.equals("=="))
			return compareResult == 0;
		else if (c.equals("!="))
			return compareResult != 0;
		else if (c.equals(">="))
			return compareResult >= 0;
		else if (c.equals("<="))
			return compareResult <= 0;
		else
			return false;
	}


	public static final boolean compareDates(Object a, Object b, String compareChar) {
		if (b instanceof Integer) {
			int offset = ((Integer) b).intValue();
			Calendar cal = Calendar.getInstance();
			Calendar cal2 = Calendar.getInstance();

			Date today = new Date();

			cal.setTime(today);
			cal.add(Calendar.YEAR, -offset);
			cal2.setTime((Date) a);

			today = cal.getTime();
			if (compareChar.equals("==")) {
				return (compare(cal.get(Calendar.YEAR), cal2.get(Calendar.YEAR), compareChar));
			} else {
				return (compare(today, (Date) a, compareChar));
			}
		} else if (b instanceof Date) {
			return (compare((Date) a, (Date) b, compareChar));
		} else if (b instanceof ClockTime) {
			return (compare(((ClockTime) a).dateValue(), ((ClockTime) b).dateValue(), compareChar));
		} else if (b == null) {
			if (compareChar.equals("==")) {
				return a == null;
			} else {
				return a != null;
			}
		} else {
			throw new TMLExpressionException("Invalid date comparison (a =" + a + ", b = " + b + ")");
		}
	}

	public static final double getDoubleValue(Object o) {
		if (o instanceof Integer)
			return ((Integer) o).intValue();
		else if (o instanceof Double)
			return ((Double) o).doubleValue();
		else if (o instanceof Money)
			return ((Money) o).doubleValue();
		else if (o instanceof Percentage)
			return ((Percentage) o).doubleValue();

		else if (o == null) {
			return 0d;
		}
		throw new TMLExpressionException("Invalid type: " + o.getClass().getName());
	}

	private static final String getStringValue(Object o) {
		if (o instanceof Integer)
			return (((Integer) o).intValue() + "");
		else if (o instanceof Double)
			return (((Double) o).doubleValue() + "");
		else if (o instanceof String)
			return (String) o;
		else if (o instanceof Boolean)
			return o + "";
		else if (o instanceof java.util.Date)
			return new SimpleDateFormat(Property.DATE_FORMAT1).format(o);
		else if (o instanceof Money)
			return ((Money) o).doubleValue() + "";
		else if (o instanceof Percentage)
			return ((Percentage) o).doubleValue() + "";
		else if (o instanceof ClockTime)
			return ((ClockTime) o).toString();
		else if (o instanceof Selection)
			return ((Selection) o).getValue();
		else if (o != null) {
			return o.toString();
		} else {
			throw new TMLExpressionException("Unknown type, can't determine type of null value.");
		}
	}

	/**
	 * Generic method to subtract two objects.
	 *
	 * @param a
	 * @param b
	 * @return
	 * @throws TMLExpressionException
	 */
	public static final Object subtract(Object a, Object b) {
		if ((a instanceof Integer) && (b instanceof Integer))
			return Integer.valueOf(((Integer) a).intValue() - ((Integer) b).intValue());
		else if ((a instanceof String) || (b instanceof String)) {
			throw new TMLExpressionException("Subtraction not defined for Strings");
		} else if (a instanceof Double && b instanceof Integer) {
			return Double.valueOf(((Double) a).doubleValue() - ((Integer) b).intValue());
		} else if (a instanceof Integer && b instanceof Double) {
			return Double.valueOf(((Integer) a).intValue() - ((Double) b).doubleValue());
		} else if (a instanceof Double && b instanceof Double) {
			return Double.valueOf(((Double) a).doubleValue() - ((Double) b).doubleValue());
		} else if ((a instanceof Money || b instanceof Money)) {
			if (!(a instanceof Money || a instanceof Integer || a instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + a.getClass());
			if (!(b instanceof Money || b instanceof Integer || b instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + b.getClass());
			Money arg1 = (a instanceof Money ? (Money) a : new Money(a));
			Money arg2 = (b instanceof Money ? (Money) b : new Money(b));
			return new Money(arg1.doubleValue() - arg2.doubleValue());
		}

		else if ((a instanceof Percentage || b instanceof Percentage)) {
			if (!(a instanceof Percentage || a instanceof Integer || a instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + a.getClass());
			if (!(b instanceof Percentage || b instanceof Integer || b instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + b.getClass());
			Percentage arg1 = (a instanceof Percentage ? (Percentage) a : new Percentage(a));
			Percentage arg2 = (b instanceof Percentage ? (Percentage) b : new Percentage(b));
			return new Percentage(arg1.doubleValue() - arg2.doubleValue());
		}

		if (a instanceof Date && b instanceof Date) {
			// Correct dates for daylight savings time.
			Calendar ca = Calendar.getInstance();
			ca.setTime((Date) a);
			ca.add(Calendar.MILLISECOND, ca.get(Calendar.DST_OFFSET));

			Calendar cb = Calendar.getInstance();
			cb.setTime((Date) b);
			cb.add(Calendar.MILLISECOND, cb.get(Calendar.DST_OFFSET));

			return Integer.valueOf((int) ((ca.getTimeInMillis() - cb.getTimeInMillis()) / (double) MILLIS_IN_DAY));
		}

		if ((a instanceof DatePattern || a instanceof Date) && (b instanceof DatePattern || b instanceof Date)) {
			DatePattern dp1 = null;
			DatePattern dp2 = null;

			if (a instanceof Date)
				dp1 = DatePattern.parseDatePattern((Date) a);
			else
				dp1 = (DatePattern) a;
			if (b instanceof Date)
				dp2 = DatePattern.parseDatePattern((Date) b);
			else
				dp2 = (DatePattern) b;
			dp1.subtract(dp2);
			return dp1.getDate();
		}

		if ((a instanceof ClockTime || a instanceof Date || a instanceof StopwatchTime)
				&& (b instanceof ClockTime || b instanceof Date || b instanceof StopwatchTime)) {
			long myMillis = (a instanceof ClockTime ? ((ClockTime) a).dateValue().getTime()
					: (a instanceof Date ? ((Date) a).getTime() : ((StopwatchTime) a).getMillis()));
			long otherMillis = (b instanceof ClockTime ? ((ClockTime) b).dateValue().getTime()
					: (b instanceof Date ? ((Date) b).getTime() : ((StopwatchTime) b).getMillis()));
			return new StopwatchTime((int) (myMillis - otherMillis));
		}

		if (a == null || b == null) {
			return null;
		} else {
			throw new TMLExpressionException("Unknown  for subtract");
		}
	}

	/**
	 * Generic method to add two objects.
	 *
	 * @param a
	 * @param b
	 * @return
	 * @throws TMLExpressionException
	 */
	public static final Object add(Object a, Object b, String expression) {
		if ((a == null) && (b == null))
			return null;
		else if (a == null)
			return b;
		else if (b == null)
			return a;
		else if ((a instanceof Integer) && (b instanceof Integer))
			return Integer.valueOf(((Integer) a).intValue() + ((Integer) b).intValue());
		else if ((a instanceof String) || (b instanceof String)) {
			String sA = Utils.getStringValue(a);
			String sB = Utils.getStringValue(b);

			return sA + sB;
		} else if (a instanceof Double && b instanceof Integer) {
			return Double.valueOf(((Double) a).doubleValue() + ((Integer) b).intValue());
		} else if (a instanceof Integer && b instanceof Double) {
			return Double.valueOf(((Integer) a).intValue() + ((Double) b).doubleValue());
		} else if (a instanceof Double && b instanceof Double) {
			return Double.valueOf(((Double) a).doubleValue() + ((Double) b).doubleValue());
		} else if ((a instanceof DatePattern || a instanceof Date) && (b instanceof DatePattern || b instanceof Date)) {
			DatePattern dp1 = null;
			DatePattern dp2 = null;

			if (a instanceof Date)
				dp1 = DatePattern.parseDatePattern((Date) a);
			else
				dp1 = (DatePattern) a;
			if (b instanceof Date)
				dp2 = DatePattern.parseDatePattern((Date) b);
			else
				dp2 = (DatePattern) b;
			dp1.add(dp2);
			return dp1.getDate();
		} else if ((a instanceof Money || b instanceof Money)) {
			if (!(a instanceof Money || a instanceof Integer || a instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + a.getClass()+" expression: "+expression);
			if (!(b instanceof Money || b instanceof Integer || b instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + b.getClass()+" expression: "+expression);
			Money arg1 = (a instanceof Money ? (Money) a : new Money(a));
			Money arg2 = (b instanceof Money ? (Money) b : new Money(b));
			return new Money(arg1.doubleValue() + arg2.doubleValue());
		} else if ((a instanceof Percentage || b instanceof Percentage)) {
			if (!(a instanceof Percentage || a instanceof Integer || a instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + a.getClass()+" expression: "+expression);
			if (!(b instanceof Percentage || b instanceof Integer || b instanceof Double))
				throw new TMLExpressionException("Invalid argument for operation: " + b.getClass()+" expression: "+expression);
			Percentage arg1 = (a instanceof Percentage ? (Percentage) a : new Percentage(a));
			Percentage arg2 = (b instanceof Percentage ? (Percentage) b : new Percentage(b));
			return new Percentage(arg1.doubleValue() + arg2.doubleValue());
		} else if ((a instanceof ClockTime && b instanceof DatePattern)) {
			DatePattern dp1 = DatePattern.parseDatePattern(((ClockTime) a).dateValue());
			DatePattern dp2 = (DatePattern) b;
			dp1.add(dp2);
			return new ClockTime(dp1.getDate());
		} else if ((b instanceof ClockTime && a instanceof DatePattern)) {
			DatePattern dp1 = DatePattern.parseDatePattern(((ClockTime) b).dateValue());
			DatePattern dp2 = (DatePattern) a;
			dp1.add(dp2);
			return new ClockTime(dp1.getDate());
		} else if ((a instanceof ClockTime && b instanceof ClockTime)) {
			DatePattern dp1 = DatePattern.parseDatePattern(((ClockTime) a).dateValue());
			DatePattern dp2 = DatePattern.parseDatePattern(((ClockTime) b).dateValue());
			dp1.add(dp2);
			return new ClockTime(dp1.getDate());
		} else if ((a instanceof Boolean && b instanceof Boolean)) {
			Boolean ba = (Boolean) a;
			Boolean bb = (Boolean) b;
			return Integer.valueOf((ba.booleanValue() ? 1 : 0) + (bb.booleanValue() ? 1 : 0));
		} else {
			throw new TMLExpressionException("Addition: Unknown type. "+" expression: "+expression);
		}
	}

	/**
	 * Fix money==null issue
	 * 
	 * @param a
	 * @return
	 */
	private static Object getActualValue(Object a) {
		if (a == null) {
			return null;
		} else {
			if (a instanceof NavajoType) {
				NavajoType n = (NavajoType) a;
				if (n.isEmpty()) {
					return null;
				} else {
					return a;
				}
			}
		}
		return a;
	}

	private static final boolean isEqual(Object aval, Object bval) throws TMLExpressionException {
		Object a = getActualValue(aval);
		Object b = getActualValue(bval);
		if ((a == null) && (b == null))
			return true;
		else if ((a == null) || (b == null))
			return false;
		else if (a instanceof Integer && b instanceof Integer)
			return (((Integer) a).intValue() == ((Integer) b).intValue());
		else if (a instanceof Integer && b instanceof Double)
			return (((Integer) a).intValue() == ((Double) b).doubleValue());
		else if (a instanceof Double && b instanceof Integer)
			return (((Integer) b).intValue() == ((Double) a).doubleValue());
		else if (a instanceof Double && b instanceof Double)
			return (((Double) b).doubleValue() == ((Double) a).doubleValue());
		else if (a instanceof Boolean && b instanceof Boolean)
			return (((Boolean) a).booleanValue() == ((Boolean) b).booleanValue());
		else if (a instanceof String || b instanceof String) {
			String sA = Utils.getStringValue(a);
			String sB = Utils.getStringValue(b);
			return (sA.equals(sB));
		} else if (a instanceof Date) {
			return Utils.compareDates(a, b, "==");
		} else if (a instanceof Money && b instanceof Money) {
			return (((Money) a).doubleValue() == ((Money) b).doubleValue());
		} else if (a instanceof Percentage && b instanceof Percentage) {
			return (((Percentage) a).doubleValue() == ((Percentage) b).doubleValue());
		} else if (a instanceof ClockTime && b instanceof ClockTime) {
			return Utils.compareDates(a, b, "==");
		} else if (a instanceof Binary && b instanceof Binary) {
			return ((Binary) a).isEqual((Binary) b);
		} else
			/**
			 * CHANGED BY FRANK: WANTED TO COMPARE IF TWO OBJECTS ARE IDENTICAL:
			 */
			return a == b;
	}

	/**
	 * Generic method to determine whether to object are equal.
	 *
	 * @param a
	 * @param b
	 * @return
	 * @throws TMLExpressionException
	 */
	@SuppressWarnings("rawtypes")
	public static final boolean equals(Object a, Object b, String expression) throws TMLExpressionException {
		if (a instanceof List) {
			boolean result = true;
			List list = (List) a;

            if (list.size() == 0) {
                return isEqual(a, b);
            }

			for (int i = 0; i < list.size(); i++) {
				boolean dum = isEqual(list.get(i), b);
				if (!(dum))
					return false;
				result = result && dum;
			}
			return result;
		} else {
			return isEqual(a, b);
		}

	}
}
