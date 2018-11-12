/* Generated By:JJTree: Do not edit this line. ASTReactivePipe.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package com.dexels.navajo.parser.compiled;

import java.util.List;

import com.dexels.navajo.parser.compiled.api.ContextExpression;
import com.dexels.navajo.parser.compiled.api.ParseMode;

public
class ASTReactivePipe extends SimpleNode {
  public ASTReactivePipe(int id) {
    super(id);
  }

@Override
public ContextExpression interpretToLambda(List<String> problems, String originalExpression, ParseMode mode) {
	int count = jjtGetNumChildren();
	if(count==0) {
		throw new RuntimeException("No reactive children found. Weird, should not happen, should handle this better");
	}
	jjtGetChild(0).interpretToLambda(problems, originalExpression,ParseMode.REACTIVE_SOURCE);
	for (int i = 1; i < count; i++) {
		jjtGetChild(i).interpretToLambda(problems, originalExpression,ParseMode.REACTIVE_TRANSFORMER);
	}
	return null;
}

}
/* JavaCC - OriginalChecksum=dd1db8c7a34ea094a180c8dc73739db3 (do not edit this line) */
