/* Generated By:JJTree: Do not edit this line. ASTExpresionLiteralNode.java */

package com.dexels.navajo.parser;


public class ASTExpresionLiteralNode extends SimpleNode {

    String val;

    public ASTExpresionLiteralNode(int id) {
        super(id);
    }

    public final Object interpret() {

        // Strip quotes.
        return new String(val.substring(1, val.length() - 1));
    }
}
