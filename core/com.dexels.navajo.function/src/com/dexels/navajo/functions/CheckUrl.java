/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.functions;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.expression.api.FunctionInterface;
import com.dexels.navajo.expression.api.TMLExpressionException;

public final class CheckUrl extends FunctionInterface {

	
	private final static Logger logger = LoggerFactory
			.getLogger(CheckUrl.class);
	
	@Override
	public String remarks() {
		return "This function will check whether it returns a valid stream. It will throw an exception when the the URL is malformed, it returns false when the string is null";
	}

	@Override
	public String usage() {
		
		return "Used to check if an URL is responding.";
	}

	@Override
	public final Object evaluate() throws TMLExpressionException {
	      // input (ArrayList, Object).
        if (this.getOperands().size() != 1)
            throw new TMLExpressionException("CheckUrl(String) expected");
        Object a = this.getOperands().get(0);
        if (a==null) {
			return Boolean.FALSE;
		}
        if (!(a instanceof String))
            throw new TMLExpressionException("CheckUrl(String) expected");

        URL u;
		try {
			u = new URL((String)a);
		} catch (MalformedURLException e) {
			throw new TMLExpressionException("CheckUrl: bad url: "+a);
		}

        return Boolean.valueOf(check(u));
	}
	
    public boolean check(URL u) {
        InputStream os = null;
        try {
            os = u.openConnection().getInputStream();
           return true;
        } catch (IOException e) {
           return false;
        } finally {
            if (os!=null) {
               try {
                   os.close();
               } catch (IOException e) {
            	   logger.info("Closing problem: ",e);
               }
           }
        }
    }

}
