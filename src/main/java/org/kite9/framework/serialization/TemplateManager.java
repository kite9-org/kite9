package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.SVGTypes;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

/**
 * A manager for determining the 'template' CSS element, which allows 
 * us to load in SVG from somewhere for our diagram.
 * 
 * @author robmoffat
 *
 */
public class TemplateManager extends AbstractValueManager {

	@Override
	public boolean isInheritedProperty() {
		return false;
	}

	@Override
	public boolean isAnimatableProperty() {
		return false;
	}

	@Override
	public boolean isAdditiveProperty() {
		return false;
	}

	@Override
	public int getPropertyType() {
		return SVGTypes.TYPE_URI;
	}

	@Override
	public Value getDefaultValue() {
		return ValueConstants.NONE_VALUE;
	}

	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
			String uri = resolveURI(engine.getCSSBaseURI(), lu.getStringValue());
			return new URIValue(lu.getStringValue(), uri);
		} else {
			throw createMalformedLexicalUnitDOMException();
		}

	}

	@Override
	public String getPropertyName() {
		return CSSConstants.TEMPLATE;
	}

}
