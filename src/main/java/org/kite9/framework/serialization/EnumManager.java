package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.SVGTypes;

/**
 * Used for creating a CSS property that reflects an enum in Java.
 */
public class EnumManager extends IdentifierManager {

    private final StringMap values = new StringMap();
    private final String propertyName;
    private final Value defaultValue;
    
    public EnumManager(String propertyName, Class<?> e, Enum<?> defaultValue) {
    	if (!e.isEnum()) {
    		throw new UnsupportedOperationException("Can only init with an enum class");
    	}
    	
    	this.propertyName = propertyName;
    	this.defaultValue = constantValueFor(defaultValue);
    	for (Object o : e.getEnumConstants()) {
			values.put(cssValueFor(o.toString()), constantValueFor((Enum<?>) o));
		}
    }
    
    public static final String cssValueFor(String identifier) {
    	return identifier.toLowerCase().replace("_", "-").intern();
    }
    
    public static final EnumValue constantValueFor(Enum<?> e) {
    	return new EnumValue(e);
    }
 
    public boolean isInheritedProperty() {
        return false;
    }

    public boolean isAnimatableProperty() {
        return false;
    }

    public boolean isAdditiveProperty() {
        return false;
    }

    public int getPropertyType() {
        return SVGTypes.TYPE_IDENT;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public Value getDefaultValue() {
        return defaultValue;
    }

    public StringMap getIdentifiers() {
        return values;
    }

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		return value;
	}
    
    
}
