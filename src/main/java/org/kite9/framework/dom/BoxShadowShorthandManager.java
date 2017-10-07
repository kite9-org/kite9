package org.kite9.framework.dom;

import java.util.ArrayList;
import java.util.List;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.parser.CSSLexicalUnit;
import org.kite9.diagram.model.style.BoxShadowType;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class BoxShadowShorthandManager implements ShorthandManager {

	public String getPropertyName() {
		return "box-shadow";
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public void setValues(CSSEngine eng, PropertyHandler ph, LexicalUnit xo, boolean imp) throws DOMException {
		List<LexicalUnit> lengths = getLengths(xo);
		boolean inset = getInsetKeyword(xo);
		
		if (lengths.size() >= 2) {
			ph.property(CSSConstants.BOX_SHADOW_TYPE_PROPERTY, CSSLexicalUnit.createString(LexicalUnit.SAC_STRING_VALUE, 
					(inset ? BoxShadowType.INSET.toString() : BoxShadowType.OUTER.toString()), null), false);
			
			ph.property(CSSConstants.BOX_SHADOW_X_OFFSET_PROPERTY, lengths.get(0), false);
			ph.property(CSSConstants.BOX_SHADOW_Y_OFFSET_PROPERTY, lengths.get(1), false);
			if (lengths.size() >= 3) {
				ph.property(CSSConstants.BOX_SHADOW_BLUR_PROPERTY, lengths.get(1), false);
				
				if (lengths.size() >= 4) {
					ph.property(CSSConstants.BOX_SHADOW_SPREAD_PROPERTY, lengths.get(1), false);
					
				}
			}
		}
		
		LexicalUnit colour = getColour(xo);

		if (colour != null) {
			ph.property(CSSConstants.BOX_SHADOW_COLOR_PROPERTY, colour, false);
		}
		
	}

	private LexicalUnit getColour(LexicalUnit xo) {
		while (xo != null) {
			switch (xo.getLexicalUnitType()) {
			case LexicalUnit.SAC_RGBCOLOR:
			case LexicalUnit.SAC_URI:
				if (!isInset(xo)) {
					return xo;
				}
			}

			xo = xo.getNextLexicalUnit();
		}

		return null;
	}

	private boolean getInsetKeyword(LexicalUnit xo) {
		while (xo != null) {
			if (isInset(xo)) {
				return true;
			}
			
			xo = xo.getNextLexicalUnit();
		}
		
		return false;
	}

	protected boolean isInset(LexicalUnit xo) {
		return "inset".equals(xo.getStringValue().toLowerCase().trim());
	}

	private List<LexicalUnit> getLengths(LexicalUnit xo) {
		List<LexicalUnit> out = new ArrayList<>();
		while (xo != null) {
			switch (xo.getLexicalUnitType()) {
			case LexicalUnit.SAC_EM:
	        case LexicalUnit.SAC_EX:
	        case LexicalUnit.SAC_PIXEL:
	        case LexicalUnit.SAC_CENTIMETER:
	        case LexicalUnit.SAC_MILLIMETER:
	        case LexicalUnit.SAC_INCH:
	        case LexicalUnit.SAC_POINT:
	        case LexicalUnit.SAC_PICA:
	        case LexicalUnit.SAC_INTEGER:
	        case LexicalUnit.SAC_REAL:
	        case LexicalUnit.SAC_PERCENTAGE:
	        	out.add(xo);
			}
			
			xo = xo.getNextLexicalUnit();
		}
		
		return out;
	}

}
