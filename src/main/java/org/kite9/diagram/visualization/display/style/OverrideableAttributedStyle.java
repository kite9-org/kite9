package org.kite9.diagram.visualization.display.style;

import java.util.HashMap;
import java.util.Map;

public class OverrideableAttributedStyle implements Cloneable {

	Map<String, String> attr;
	
	public Map<String, String> getElements() {
		return attr;
	}

	public OverrideableAttributedStyle() {
		this.attr = new HashMap<String, String>();
	}
		
	public OverrideableAttributedStyle(Map<String, String> elements) {
		super();
		this.attr = new HashMap<String, String>();
		if (elements != null) {
			this.attr.putAll(elements);
		}
	}

	protected void set(String key, String value) {
		attr.put(key, value);
	}

	public Object get(String key) {
		return attr.get(key);
	}
	
	public static Object checkOverride(Object initial, Object newValue) {
		if (newValue != null) {
			return newValue;
		} else {
			return initial;
		}
	}
	
	
	
}
