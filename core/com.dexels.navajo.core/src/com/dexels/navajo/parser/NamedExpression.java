/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.parser;

import java.util.Optional;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.TMLExpressionException;
import com.dexels.navajo.expression.api.TipiLink;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;

public class NamedExpression implements ContextExpression {
	public final String name;
	public final ContextExpression expression;

	public NamedExpression(String name, ContextExpression expression) {
		this.name = name;
		this.expression = expression;
	}

	@Override
	public Operand apply(Navajo doc, Message parentMsg, Message parentParamMsg, Selection parentSel,
			MappableTreeNode mapNode, TipiLink tipiLink, Access access, Optional<ImmutableMessage> immutableMessage,
			Optional<ImmutableMessage> paramMessage) throws TMLExpressionException {
		return expression.apply(doc,parentMsg,parentParamMsg,parentSel,mapNode,tipiLink,access,immutableMessage,paramMessage);
	}

	@Override
	public boolean isLiteral() {
		return expression.isLiteral();
	}

	@Override
	public Optional<String> returnType() {
		return expression.returnType();
	}

	@Override
	public String expression() {
		return expression.expression();
	}
}
