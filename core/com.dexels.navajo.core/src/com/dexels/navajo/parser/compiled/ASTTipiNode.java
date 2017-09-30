/* Generated By:JJTree&JavaCC: Do not edit this line. ASTTipiNode.java */

package com.dexels.navajo.parser.compiled;

import java.util.Optional;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.parser.TMLExpressionException;
import com.dexels.navajo.parser.compiled.api.ContextExpression;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.tipilink.TipiLink;
import com.dexels.replication.api.ReplicationMessage;

public final class ASTTipiNode extends SimpleNode {

  String val = "";
//  TipiLink tipiLink;

  public ASTTipiNode(int id) {
    super(id);
  }

@Override
public ContextExpression interpretToLambda() {
	return new ContextExpression() {
		
		@Override
		public boolean isLiteral() {
			return false;
		}
		
		@Override
		public Object apply(Navajo doc, Message parentMsg, Message parentParamMsg, Selection parentSel,
				 MappableTreeNode mapNode, TipiLink tipiLink, Access access, Optional<ReplicationMessage> immutableMessage) throws TMLExpressionException {
		      try {
				return tipiLink.evaluateExpression(val);
			} catch (Exception e) {
				throw new TMLExpressionException("Error evaluating tipiLink: "+val, e);
			}
		}
	};
}

}
