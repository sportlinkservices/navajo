/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.document.navascript.tags;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackReader;

import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Navascript;
import com.dexels.navajo.document.base.BaseExpressionTagImpl;

public class ExpressionTag extends BaseExpressionTagImpl implements NS3Compatible {

	public ExpressionTag(Navajo n, String condition, String value) {
		super(n, condition, value);
	}

	@Override
	public void formatNS3(int indent, OutputStream w) throws IOException {
		StringBuffer sb = new StringBuffer();
		if ( getCondition() != null && !"".equals(getCondition()) ) {
			sb.append(NS3Constants.CONDITION_IF + getCondition() + NS3Constants.CONDITION_THEN);
		} 
		sb.append(getValue());
		w.write((NS3Utils.generateIndent(indent) + sb.toString()).getBytes());
	}

}
