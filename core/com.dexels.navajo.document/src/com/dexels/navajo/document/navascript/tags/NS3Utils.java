package com.dexels.navajo.document.navascript.tags;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dexels.navajo.document.base.BaseNode;

public class NS3Utils {

	enum ParseState {
		START, START_PARAMETERS, START_KEY, FOUND_KEY, FOUND_VALUE, IN_STRING_CONSTANT
	}

	public static String generateIndent(int indent) {
		StringBuffer sb = new StringBuffer();
		for ( int i = 0; i < indent; i++ ) {
			sb.append(NS3Constants.INDENT_STEP);
		}
		return sb.toString();
	}

	public static void writeConditionalExpressions(int indent, OutputStream w, List<? extends BaseNode> expressions) throws IOException {
		if ( expressions.size() == 1 ) {
			ExpressionTag et = (ExpressionTag) expressions.get(0);
			et.formatNS3(0, w);
		} else {
			w.write("\n".getBytes());
			int index = 0;
			for ( BaseNode e : expressions ) {
				ExpressionTag et = (ExpressionTag) e;
				index++;
				if ( index == expressions.size()) {
					w.write(NS3Utils.generateIndent(indent+1).getBytes());
					w.write((NS3Constants.CONDITION_ELSE + " ").getBytes());
					et.formatNS3(0, w);
				} else {
					et.formatNS3(indent+1, w);
					if ( index < expressions.size() ) {
						w.write("\n".getBytes());
					}
				}
			}
		}
	}

	public static String readUntil(PushbackReader r, char stop) throws IOException {
		StringBuffer sb = new StringBuffer();
		char c;
		while ( ( c = (char) r.read()) != stop) {
			sb.append(c);
		}
		return sb.toString();
	}
	
	public static String readUntil(PushbackReader r, String stop) throws IOException {
		StringBuffer sb = new StringBuffer();
		String token = null;
		boolean found = false;
		while ( !found ) {
			token = readUntil(r, ' ');
			if ( token == null ) {
				break;
			}
			System.err.println("token: " + token);
			if ( token.equals(stop)) {
				System.err.println("Found token: " + stop);
				found = true;
			} else {
				sb.append(token + " ");
			}
		}
		return sb.toString();
	}

	/**
	 * condition=?[/Transaction/Operation] AND [/Transaction/Operation] == 'DELETE' AND [/@SourceTable] == 'SYSTEMUSER name='/LegacyUser/_id' value=$../property('/LegacyUser/_id') ;
	 */
	public static Map<String,String> parseParameters(String raw) throws IOException {
		Map<String,String> keyValues = new HashMap<>();

		Reader r = new StringReader(raw);
		char c;
		StringBuffer sb = new StringBuffer();
		ParseState state = ParseState.START;
		String key = null;
		String value = null;
		int size = raw.length();
		int index = 0;
		int bracketCount=0;
		while ( index < size ) {
			c = (char) r.read();
			index++;
			if ( state == ParseState.START_KEY && c == ')') {
				// END.
				keyValues.put(key, value);
			} else if ( state == ParseState.START && c == '(') {
				state = ParseState.START_KEY;
			} else if ( state == ParseState.FOUND_VALUE && c == ',') {
				state = ParseState.START_KEY;
			} else if ( state == ParseState.START_KEY && c != '=' ) {
				sb.append(c);
			} else if ( state == ParseState.START_KEY ){
				// Found a key.
				key = sb.toString();
				state = ParseState.FOUND_KEY;
				sb = new StringBuffer();
			} else if ( state == ParseState.FOUND_KEY || state == ParseState.IN_STRING_CONSTANT ) {
				if ( c == '(' ) {
					bracketCount++;
				}
				if ( c == ')' ) {
					bracketCount--;
					if ( bracketCount < 0 ) { // Could be end of parameters bracket.
						// END.
						value = sb.toString();
						keyValues.put(key, value);
						state = null;
					}
				}
				if ( state == ParseState.IN_STRING_CONSTANT && c != '\'') {
					sb.append(c);
				} else if ( state == ParseState.FOUND_KEY && c == '\'') {
					state = ParseState.IN_STRING_CONSTANT;
					sb.append(c);
				} else if ( state == ParseState.IN_STRING_CONSTANT && c == '\'') {
					state = ParseState.FOUND_KEY;
					sb.append(c);
				} else if ( c == ',') {
					state = ParseState.START_KEY;
					value = sb.toString();
					keyValues.put(key, value);
					sb = new StringBuffer();
					key = null;
					value = null;
				} else {
					sb.append(c);
				}
			} 
		}

		return keyValues;
	}

	public static void main(String [] args) throws Exception {

		String test = "break (name=Κibbeling('/MatchEvents/MatchId' + 'aap'),value=[/CountMatchEvents/MatchId] + (3/2)) ;";
		PushbackReader r = new PushbackReader(new StringReader(test), 100);

		String token = readUntil(r, ' ');
		System.err.println("token = " + token);

		String a = readUntil(r, ';');

		System.err.println("a = " + a);

		Map<String,String> kvPairs = parseParameters(a);

		System.err.println("=======================================");
		
		for ( String k : kvPairs.keySet() ) {
			System.err.println(k + ":" + kvPairs.get(k));
		}

		String find = "if 3 - 4 == 2 then 'Aap'";
		r = new PushbackReader(new StringReader(find), 100);
		
		System.err.println(readUntil(r, "then"));
	}
}
