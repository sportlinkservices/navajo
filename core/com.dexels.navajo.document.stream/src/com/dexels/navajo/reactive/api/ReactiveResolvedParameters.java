/*
This file is part of the Navajo Project. 
It is subject to the license terms in the COPYING file found in the top-level directory of this distribution and at https://www.gnu.org/licenses/agpl-3.0.txt. 
No part of the Navajo Project, including this file, may be copied, modified, propagated, or distributed except according to the terms contained in the COPYING file.
*/
package com.dexels.navajo.reactive.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dexels.immutable.api.ImmutableMessage;
import com.dexels.navajo.document.Navajo;
import com.dexels.navajo.document.Operand;
import com.dexels.navajo.document.stream.DataItem;
import com.dexels.navajo.document.stream.api.StreamScriptContext;
import com.dexels.navajo.expression.api.ContextExpression;

import io.reactivex.Flowable;

public class ReactiveResolvedParameters {

	Map<String,Operand> resolvedNamed = new HashMap<>();
	List<Operand> resolvedUnnamed = new ArrayList<>();
	Map<String,String> resolvedTypes = new HashMap<>();
	
	private static final Logger logger = LoggerFactory.getLogger(ReactiveResolvedParameters.class);
	private boolean allResolved = false;
	private final Optional<ImmutableMessage> currentMessage;
	private final ImmutableMessage paramMessage;
	private final Optional<Map<String,String>> expectedTypes;

	private final Map<String, ContextExpression> named;

	private final List<ContextExpression> unnamed;

	private final Optional<Flowable<DataItem>> inputFlowable;
	private Navajo resolvedInput;
	
	public ReactiveResolvedParameters(StreamScriptContext context, Map<String, ContextExpression> named, List<ContextExpression> unnamed,
			Optional<ImmutableMessage> currentMessage, ImmutableMessage paramMessage, ParameterValidator validator) {
		this.currentMessage = currentMessage;
		this.paramMessage = paramMessage;
		this.named = named;
		this.unnamed = unnamed;
		this.resolvedInput = context.resolvedNavajo();
		this.inputFlowable = context.inputFlowable();
		Optional<List<String>> allowed = validator.allowedParameters();
		Optional<List<String>> required = validator.requiredParameters();

		if(allowed.isPresent()) {
			List<String> al = allowed.get();
			named.entrySet().forEach(e->{
				if(!al.contains(e.getKey())) {
					throw new ReactiveParameterException("Parameter name: "+e.getKey()+" is not allowed for this entity. Allowed entities: "+al+"");
				}
			});
		}
		if(required.isPresent()) {
			List<String> req = required.get();
			for (String requiredParam : req) {
				if(!named.containsKey(requiredParam)) {
					throw new ReactiveParameterException("Missing parameter name: "+requiredParam+". Supplied params: "+named.keySet());
				}
			}
		}
		expectedTypes = validator.parameterTypes();
		resolveAllParams();
	}
	
	public List<Operand> unnamedParameters() {
		return this.resolvedUnnamed;
	}
	
	public Operand[] unnamedParametersArray() {
		return this.resolvedUnnamed.toArray(new Operand[] {});
	}
	
	public Map<String,Operand> namedParameters() {
		return this.resolvedNamed;
	}
	private Map<String,Object> resolveAllParams() {
		if(!allResolved) {
			resolveNamed();
			resolveUnnamed();
		}
		return Collections.unmodifiableMap(resolvedNamed);
	}

	private Optional<Operand> paramValue(String key) {
		if(resolvedNamed.containsKey(key)) {
			return Optional.ofNullable(resolvedNamed.get(key));
		}
		Optional<String> expectedType = expectedTypes.isPresent() ? Optional.ofNullable(expectedTypes.get().get(key)) : Optional.empty();
		ContextExpression function = named.get(key);
		if(function==null) {
			return Optional.empty();
		}
		return Optional.of(resolveParam(key,expectedType, function));
	}
	
	// Guarantees it's there, will fail otherwise
	public String paramString(String key) {
		Operand operand = resolvedNamed.get(key);
		if(operand==null) {
			logger.warn("Trouble retrieving key: {}, there is no such operand. Available: {}",key,resolvedNamed.keySet());
		}
		return operand.stringValue();
	}

	public Optional<String> optionalString(String key) {
		Operand res = resolvedNamed.get(key);
		if(res==null) {
			return Optional.empty(); 
		}
		return Optional.of(res.stringValue());
//		return typeCheckedOperand(paramValue(key), key, Optional.of(Property.STRING_PROPERTY)).map(e->(String)e);

	}
	public String paramString(String key,Callable<String> defaultValue) {
		try {
			return optionalString(key).orElse(defaultValue.call());
		} catch (Exception e) {
			throw new RuntimeException("Default value callable failed",e);
		}
	}

	public Object paramObject(String key,Callable<Object> defaultValue) {
		try {
			Operand res = resolvedNamed.get(key);
			if(res==null) {
				return defaultValue.call();
			}
			return res.value;
		} catch (Exception e) {
			throw new ReactiveParameterException("Default value failed",e);
		}
		
	}
	
	public int paramInteger(String key) {
		Optional<Operand> v = paramValue(key);
		if(!v.isPresent()) {
			throw new ReactiveParameterException("Missing value: "+key); 
		}
		return v.get().integerValue();
		
	}

	public Optional<Integer> optionalInteger(String key) {
		return paramValue(key).map(e->e.integerValue());
	}


	public int paramInteger(String key, Supplier<Integer> defaultValue) {
		return optionalInteger(key).orElseGet(()->defaultValue.get());
	}

	public Optional<Boolean> optionalBoolean(String key) {
		return paramValue(key).map(e->e.booleanValue());
	}
	
	public boolean paramBoolean(String key) {
		return optionalBoolean(key).orElseThrow(()->new ReactiveParameterException("Missing key: "+key+" no value found."));
	}

	public boolean paramBoolean(String key, Supplier<Boolean> defaultValue) {
		return optionalBoolean(key).orElseGet(()->defaultValue.get());
	}
	
	private void resolveUnnamed() {
		
		List<? extends Operand> resolved = unnamed.stream()
				.map(e->{
					Operand value = e.apply(this.resolvedInput, this.currentMessage, Optional.of(this.paramMessage));
					if(value==null) {
						throw new NullPointerException("Unnamed expression resolved to null");
					}

					return value;
				}).collect(Collectors.toList());
		this.allResolved=true;
		this.resolvedUnnamed.addAll(resolved);
	}
//	
	private void resolveNamed() {
		named.entrySet().forEach(e->{
			Optional<String> expectedType = expectedTypes.isPresent() ? Optional.ofNullable(expectedTypes.get().get(e.getKey())) : Optional.empty();
			ContextExpression value = e.getValue();
			if(value==null) {
				throw new NullPointerException("Named Expression with key: "+e.getKey()+" resolved to null");
			}
			resolveParam(e.getKey(),expectedType,value);
		});
		allResolved = true;
	}
	

	private Operand resolveParam(String key,Optional<String> expectedType, ContextExpression function) {
		Operand applied;
		try {
			// TODO move this to constructor or something
			Navajo in = this.resolvedInput!=null ? this.resolvedInput : inputFlowable.isPresent() ? null : resolvedInput;
			applied = function.apply(in, currentMessage,Optional.of(paramMessage));
			resolvedNamed.put(key, applied);
			if(expectedType.isPresent()) {
				resolvedTypes.put(key, expectedType.get());
			}
			return applied;
		} catch (Exception e1) {
			throw new ReactiveParameterException("Error applying param function for named param: "+key,e1);
		}
	}

}
