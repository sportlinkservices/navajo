package com.dexels.navajo.document.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dexels.navajo.document.ExpressionTag;
import com.dexels.navajo.document.Navajo;

public class BaseExpressionTagImpl extends BaseNode implements ExpressionTag {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8407258788821157361L;
	
	String condition;
	String value;
	
	public BaseExpressionTagImpl(Navajo n, String condition, String value) {
		super(n);
		this.condition = condition;
		this.value = value;
	}
	
	@Override
	public Map<String, String> getAttributes() {
		Map<String,String> attr = new HashMap<>();
		if ( condition != null  && !"".equals(condition)) {
			attr.put("condition", condition);
		}
		if ( value != null  && !"".equals(value)) {
			attr.put("value", value);
		}
		return attr;
	}

	@Override
	public List<? extends BaseNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getTagName() {
		return "expression";
	}

	@Override
	public int compareTo(ExpressionTag o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String s) {
		this.value = s;
	}

	@Override
	public String getCondition() {
		return condition;
	}

	@Override
	public void setCondition(String s) {
		this.condition = s;
	}

	@Override
	public Object getRef() {
		// TODO Auto-generated method stub
		return null;
	}

}
