package com.dexels.navajo.functions.util;

import navajo.ExtensionDefinition;

import com.dexels.navajo.parser.FunctionInterface;

public final class FunctionDefinition {

	private final String object;
	private final String description;
	private final String [][] inputParams;
	private final String [] resultParam;
	private final Class <? extends FunctionInterface> myClass;
	
	public FunctionDefinition(final String object, final String description, final String inputParams, final String resultParam, ExtensionDefinition fd) throws ClassNotFoundException {
		this.object = object;
		this.description = description;
		if ( inputParams != null && !inputParams.equals("") ) {
			String [] params = inputParams.split(",");
			this.inputParams = new String[params.length][];
			for (int i = 0; i < params.length; i++) {
				this.inputParams[i] = params[i].split("\\|");
			}
		} else {
			this.inputParams =  null;
		}
		if ( resultParam != null && !resultParam.equals("") ) {
			this.resultParam = resultParam.split("\\|");
		} else {
			this.resultParam = null;
		}
		ClassLoader cl = null;
		if(fd!=null) {
			cl = fd.getClass().getClassLoader();
		} else {
			cl = this.getClass().getClassLoader();
		}
		myClass = (Class<? extends FunctionInterface>) Class.forName(object,true,cl);
	}

	
	public final Class<? extends FunctionInterface> getFunctionClass() {
		return myClass;
	}
	
	public final String getObject() {
		return object;
	}

	public final String getDescription() {
		return description;
	}

	public final String [][] getInputParams() {
		return inputParams;
	}

	public final String [] getResultParam() {
		return resultParam;
	}

	public static void main(String [] args) throws Exception {
		
	}
}
