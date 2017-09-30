/* Generated By:JJTree&JavaCC: Do not edit this line. ASTFalseNode.java */
package com.dexels.navajo.parser.compiled;

import java.util.Optional;

import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.parser.compiled.api.ContextExpression;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;
import com.dexels.navajo.tipilink.TipiLink;
import com.dexels.replication.api.ReplicationMessage;

public final class ASTFalseNode extends SimpleNode {
    public ASTFalseNode(int id) {
        super(id);
    }

	@Override
	public ContextExpression interpretToLambda() {
		return new ContextExpression() {
			
			@Override
			public boolean isLiteral() {
				return true;
			}
			
			@Override
			public Object apply(Navajo doc, Message parentMsg, Message parentParamMsg, Selection parentSel,
					 MappableTreeNode mapNode, TipiLink tipiLink, Access access, Optional<ReplicationMessage> immutableMessage) {
		        return Boolean.valueOf(false);
			}
		};
	}

}
