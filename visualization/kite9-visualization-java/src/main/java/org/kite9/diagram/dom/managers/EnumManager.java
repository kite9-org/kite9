package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.IdentifierManager;
import org.apache.batik.css.engine.value.StringMap;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * Used for creating a CSS property that reflects an enum in Java.
 */
public class EnumManager extends IdentifierManager {

	private final StringMap values = new StringMap();
	private final String propertyName;
	private final Value defaultValue;
	private final boolean inherited;

	public EnumManager(String propertyName, Class<?> e, Enum<?> defaultValue, boolean inherited) {
		if (!e.isEnum()) {
			throw new UnsupportedOperationException("Can only init with an enum class");
		}

		this.propertyName = propertyName;
		this.defaultValue = constantValueFor(defaultValue);
		for (Object o : e.getEnumConstants()) {
			values.put(cssValueFor(o.toString()), constantValueFor((Enum<?>) o));
		}
		this.inherited = inherited;
	}

	public static final String cssValueFor(String identifier) {
		return identifier.toLowerCase().replace("_", "-").intern();
	}

	public static final EnumValue constantValueFor(Enum<?> e) {
		return new EnumValue(e);
	}

	public boolean isInheritedProperty() {
		return inherited;
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
	
	/**
	 * If the enum doesn't match something we know, just return the default.
	 */
	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		try {
			return super.createValue(lu, engine);
		} catch (DOMException de) {
			return defaultValue;
		}
	}

	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		return value;
	}

}
