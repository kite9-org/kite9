package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.AbstractValueManager;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.URIValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.ValueConstants;
import org.apache.batik.util.SVGTypes;
import org.kite9.diagram.dom.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

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
		return SVGTypes.TYPE_URI_LIST;
	}

	@Override
	public Value getDefaultValue() {
		return ValueConstants.NONE_VALUE;
	}

	/**
	 * Will return either a template uri value, or a uri value followed by a list of string 
	 * parameters.
	 */
	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		Value template = null;
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_URI) {
			String uri = resolveURI(engine.getCSSBaseURI(), lu.getStringValue());
			template = new URIValue(lu.getStringValue(), uri);
		} else {
			throw createMalformedLexicalUnitDOMException();
		}
		
		lu = lu.getNextLexicalUnit();
		
		if (lu == null) {
			return template;
		} else {
			ListValue list = new ListValue();
			list.append(template);
			while (lu != null) {
				if (!(lu.getLexicalUnitType() == LexicalUnit.SAC_OPERATOR_COMMA)) {
					throw createMalformedLexicalUnitDOMException();
				}
				
				lu = lu.getNextLexicalUnit();
				
				switch (lu.getLexicalUnitType()) {
				case LexicalUnit.SAC_URI:
					String uri = resolveURI(engine.getCSSBaseURI(), lu.getStringValue());
					list.append(new URIValue(lu.getStringValue(), uri));
					break;
				case LexicalUnit.SAC_STRING_VALUE:
				case LexicalUnit.SAC_IDENT:
				case LexicalUnit.SAC_INTEGER:
				case LexicalUnit.SAC_REAL:
					String str = lu.getStringValue();
					list.append(new StringValue(CSSPrimitiveValue.CSS_STRING, str));
					break;
				default:
					throw createMalformedLexicalUnitDOMException();
				}
				
				lu = lu.getNextLexicalUnit();
			}
			
			return list;
		}
	}

	@Override
	public String getPropertyName() {
		return CSSConstants.TEMPLATE;
	}

}
