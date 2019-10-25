/* Generated By:JJTree: Do not edit this line. ASTTransformerNode.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.dexels.navajo.parser.compiled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.navajo.document.Message;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.Selection;
import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.FunctionClassification;
import com.dexels.navajo.expression.api.TipiLink;
import com.dexels.navajo.parser.RenameTransformerFunction;
import com.dexels.navajo.parser.TransformerFunction;
import com.dexels.navajo.reactive.api.Reactive;
import com.dexels.navajo.script.api.Access;
import com.dexels.navajo.script.api.MappableTreeNode;

class ASTTransformerNode extends SimpleNode {

	public int args = 0;
	public String transformerName;
	ASTTransformerNode(int id) {
		super(id);
	}

	@Override
	public ContextExpression interpretToLambda(List<String> problems, String originalExpression, Function<String, FunctionClassification> functionClassifier, Function<String,Optional<Node>> mapResolver) {
		List<ContextExpression> parameters = new ArrayList<>();
		for (int i = 0; i < jjtGetNumChildren(); i++) {
			Node node = jjtGetChild(i);
			parameters.add(node.interpretToLambda(problems, originalExpression,functionClassifier,mapResolver));
		}
		return new ContextExpression() {
		
			@Override
			public Optional<String> returnType() {
				return Optional.of("transformer");
			}
			
			@Override
			public boolean isLiteral() {
				return false;
			}
			
			@Override
			public String expression() {
				return originalExpression;
			}
			
			@Override
			public Operand apply(Navajo doc, Message parentMsg, Message parentParamMsg, Selection parentSel,
					MappableTreeNode mapNode, TipiLink tipiLink, Access access, Optional<ImmutableMessage> immutableMessage,
					Optional<ImmutableMessage> paramMessage) {
				List<Operand> params = parameters.stream().map(e->e.apply(doc, parentMsg, parentParamMsg, parentSel, mapNode, tipiLink, access, immutableMessage, paramMessage)).collect(Collectors.toList());
				System.err.println(">>> "+transformerName);
				TransformerFunction ff = new RenameTransformerFunction();
				return Operand.ofCustom(ff.create(params, problems),Reactive.ReactiveItemType.REACTIVE_TRANSFORMER.toString());
			}
		};
	}
}
/* JavaCC - OriginalChecksum=1d2eb60a4b53e6f73d69612ad626466b (do not edit this line) */
