/* Generated By:JJTree: Do not edit this line. ASTPipeline.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=true,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.dexels.navajo.parser.compiled;

import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.navajo.expression.api.ContextExpression;
import com.dexels.navajo.expression.api.FunctionClassification;

public class ASTPipeline extends SimpleNode {
	
	private static final Logger logger = LoggerFactory.getLogger(ASTPipeline.class);

  public ASTPipeline(int id) {
    super(id);
  }

  public ASTPipeline(CompiledParser p, int id) {
    super(id);
  }

@Override
public ContextExpression interpretToLambda(List<String> problems, String originalExpression,
		Function<String, FunctionClassification> functionClassifier) {
	return null;
}

@Override
public void jjtClose() {
	super.jjtClose();
	logger.info(">->Children: "+jjtGetNumChildren()+" > "+this);
	for (int i = 0; i < jjtGetNumChildren(); i++) {
		SimpleNode sn = (SimpleNode) jjtGetChild(i);
		logger.info("    Child: --> "+sn);
	}
}


}
/* JavaCC - OriginalChecksum=9d58eb4d5476d874f173a994f340ea89 (do not edit this line) */
