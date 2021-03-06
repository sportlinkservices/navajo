/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;

import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.functions.security.Security;

public class DecryptString extends FunctionInterface {

	@Override
	public String remarks() {
		return "Encrypts a message using a 128 bit key";
	}

	@Override
	public Object evaluate() throws TMLExpressionException {
		
		String result = null;
		
		String key = (String) getOperand(0);
		String message = (String) getOperand(1);
		try {
			Security s = new Security(key);
			result = s.decrypt(message);
		} catch (Exception e) {
			throw new TMLExpressionException(e.getMessage(),e);
		}
		
		return result;
	}

	static String decrypt(String key, String message) throws TMLExpressionException {
		DecryptString es = new DecryptString();
		es.reset();
		es.insertStringOperand(key);
		es.insertStringOperand(message);
		String result = (String) es.evaluate();
		return result;
	}
	
	public static void main(String [] args) throws Exception {
		
//		String s = "6jyZodUTXHmq5vR36F3Sf8AQw5Eil4Hubn0sEdAas4nOV4Fkh9vtuSJGQZqsEJDpIV+XnvNoaL7EPjhzy/AlLn9ZLmyFTxbLcC79a/quGHo=";
		String s = "5CO5fUgfoGOQMus628uG/Ip5jzSrLGVKAdP4rElwJm7/BijV4ph76b1K/f68yb1yaIymuih4ane9H/qdmkVUnU0Q+Ar5gZSuRCtwCKPcs6E=";
		DecryptString e = new DecryptString();
		e.reset();
		e.insertStringOperand("aap123");
		e.insertStringOperand(s);
		decrypt("aap123", s);
		String result = (String) e.evaluate();
		System.err.println("result: " + result);
	}
}
