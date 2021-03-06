/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
/* Generated By:JJTree&JavaCC: Do not edit this line. CompiledParserConstants.java */
package com.dexels.navajo.parser.compiled;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface CompiledParserConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int SINGLE_LINE_COMMENT = 6;
  /** RegularExpression Id. */
  int AND = 7;
  /** RegularExpression Id. */
  int OR = 8;
  /** RegularExpression Id. */
  int NOT = 9;
  /** RegularExpression Id. */
  int EQUALS = 10;
  /** RegularExpression Id. */
  int NOT_EQUALS = 11;
  /** RegularExpression Id. */
  int TRUE = 12;
  /** RegularExpression Id. */
  int FALSE = 13;
  /** RegularExpression Id. */
  int TODAY = 14;
  /** RegularExpression Id. */
  int NAME_OPTION = 15;
  /** RegularExpression Id. */
  int VALUE_OPTION = 16;
  /** RegularExpression Id. */
  int BEGIN_LIST = 17;
  /** RegularExpression Id. */
  int END_LIST = 18;
  /** RegularExpression Id. */
  int LIST_SEPARATOR = 19;
  /** RegularExpression Id. */
  int ADD = 20;
  /** RegularExpression Id. */
  int MUL = 21;
  /** RegularExpression Id. */
  int MIN = 22;
  /** RegularExpression Id. */
  int DIV = 23;
  /** RegularExpression Id. */
  int MOD = 24;
  /** RegularExpression Id. */
  int NULL = 25;
  /** RegularExpression Id. */
  int PIPE = 26;
  /** RegularExpression Id. */
  int HEADER = 27;
  /** RegularExpression Id. */
  int PIPESTART = 28;
  /** RegularExpression Id. */
  int SARTRE = 29;
  /** RegularExpression Id. */
  int INTEGER_LITERAL = 30;
  /** RegularExpression Id. */
  int FLOAT_LITERAL = 31;
  /** RegularExpression Id. */
  int TML_IDENTIFIER = 32;
  /** RegularExpression Id. */
  int EXISTS_TML_IDENTIFIER = 33;
  /** RegularExpression Id. */
  int REGULAREXPRESSION = 34;
  /** RegularExpression Id. */
  int PARENT_MSG = 35;
  /** RegularExpression Id. */
  int REGULAR1 = 36;
  /** RegularExpression Id. */
  int REGULAR2 = 37;
  /** RegularExpression Id. */
  int IDENTIFIER = 38;
  /** RegularExpression Id. */
  int LETTER = 39;
  /** RegularExpression Id. */
  int DIGIT = 40;
  /** RegularExpression Id. */
  int NORMAL_IDENTIFIER = 41;
  /** RegularExpression Id. */
  int STRING_LITERAL = 42;
  /** RegularExpression Id. */
  int EXPRESSION_LITERAL = 43;
  /** RegularExpression Id. */
  int TIPI_IDENTIFIER = 44;

  /** Lexical state. */
  int DEFAULT = 0;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\r\"",
    "\"\\n\"",
    "\"\\f\"",
    "<SINGLE_LINE_COMMENT>",
    "\"AND\"",
    "\"OR\"",
    "\"!\"",
    "\"==\"",
    "\"!=\"",
    "\"true\"",
    "\"false\"",
    "\"TODAY\"",
    "\":name\"",
    "\":value\"",
    "\"{\"",
    "\"}\"",
    "\",\"",
    "\"+\"",
    "\"*\"",
    "\"-\"",
    "\"/\"",
    "\"%\"",
    "\"null\"",
    "\"->\"",
    "\"=>\"",
    "\"|>\"",
    "<SARTRE>",
    "<INTEGER_LITERAL>",
    "<FLOAT_LITERAL>",
    "<TML_IDENTIFIER>",
    "<EXISTS_TML_IDENTIFIER>",
    "<REGULAREXPRESSION>",
    "\"../\"",
    "<REGULAR1>",
    "<REGULAR2>",
    "<IDENTIFIER>",
    "<LETTER>",
    "<DIGIT>",
    "<NORMAL_IDENTIFIER>",
    "<STRING_LITERAL>",
    "<EXPRESSION_LITERAL>",
    "<TIPI_IDENTIFIER>",
    "\"(\"",
    "\")\"",
    "\"<\"",
    "\">\"",
    "\"<=\"",
    "\">=\"",
    "\"#\"",
    "\"$\"",
    "\"]\"",
    "\".\"",
    "\"=\"",
  };

}
