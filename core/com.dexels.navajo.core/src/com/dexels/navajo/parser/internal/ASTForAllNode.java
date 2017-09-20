/* Generated By:JJTree&JavaCC: Do not edit this line. ASTForAllNode.java */

package com.dexels.navajo.parser.internal;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.NavajoException;
import com.dexels.navajo.parser.Condition;
import com.dexels.navajo.parser.TMLExpressionException;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.script.api.SystemException;

public final class ASTForAllNode extends SimpleNode {

	String functionName;
	Navajo doc;
	Message parentMsg;
	MappableTreeNode mapObject;
	private Access access;

	private final static Logger logger = LoggerFactory.getLogger(ASTForAllNode.class);

	public ASTForAllNode(int id) {
		super(id);
	}

	public Access getAccess() {
		return access;
	}

	public void setAccess(Access access) {
		this.access = access;
	}

	/**
	 * FORALL(<EXPRESSION>, `[$x] <EXPRESSION>`) E.G.
	 * FORALL([/ClubMembership/ClubMemberships/ClubIdentifier],
	 * `CheckRelatieCode([$x])`)
	 * 
	 * @return
	 * @throws TMLExpressionException
	 */
	@Override
	public final Object interpret() throws TMLExpressionException {

		boolean matchAll = true;

		if (functionName.equals("FORALL"))
			matchAll = true;
		else
			matchAll = false;

		Object a = jjtGetChild(0).interpret();

		String msgList = (String) a;

		Object b = jjtGetChild(1).interpret();

		try {
			List<Message> list = null;

			if (parentMsg == null) {
				list = doc.getMessages(msgList);
			} else {
				list = parentMsg.getMessages(msgList);
			}

			for (int i = 0; i < list.size(); i++) {
				Object o = list.get(i);

				parentMsg = (Message) o;

				// ignore definition messages in the evaluation
				if (parentMsg.getType().equals(Message.MSG_TYPE_DEFINITION))
					continue;

				String expr = (String) b;

				boolean result = Condition.evaluate(expr, doc, mapObject, parentMsg, access);

				if ((!(result)) && matchAll)
					return Boolean.FALSE;
				if ((result) && !matchAll)
					return Boolean.TRUE;
			}

		} catch (SystemException se) {
			logger.error("Error: ", se);
			throw new TMLExpressionException("Invalid expression in FORALL construct: \n" + se.getMessage());
		} catch (NavajoException ne) {
			logger.error("Error: ", ne);
			throw new TMLExpressionException("Invalid expression in FORALL construct: \n" + ne.getMessage());
		}

		if (matchAll)
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}
}
