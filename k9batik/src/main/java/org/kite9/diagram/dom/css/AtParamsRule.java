package org.kite9.diagram.dom.css;

import java.util.List;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.Rule;

public class AtParamsRule implements Rule {
	
	private String name;


	private Object value;

	public AtParamsRule(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public AtParamsRule(String name, List<String> additionalValues) {
		this.name = name;
		this.value = additionalValues;
	}

	@Override
	public short getType() {
		return 20;
	}

	@Override
	public String toString(CSSEngine eng) {
		return "@params { "+name+": "
			+value+"; }";
	}


	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
