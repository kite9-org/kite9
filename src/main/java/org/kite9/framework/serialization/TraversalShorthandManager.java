package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.parser.CSSLexicalUnit;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class TraversalShorthandManager implements ShorthandManager {

	public String getPropertyName() {
		return "traversal";
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public void setValues(CSSEngine eng, PropertyHandler ph, LexicalUnit first, boolean imp) throws DOMException {
		int count = countLexicalUnits(first);
		
		
		if (count == 1) {
			String val = first.getStringValue();
			CSSLexicalUnit unit = CSSLexicalUnit.createString(CSSLexicalUnit.SAC_IDENT, val, null);
			ph.property(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY, unit, false);
			ph.property(CSSConstants.TRAVERSAL_LEFT_PROPERTY, unit, false);
			ph.property(CSSConstants.TRAVERSAL_RIGHT_PROPERTY, unit, false);
			ph.property(CSSConstants.TRAVERSAL_TOP_PROPERTY, unit, false);
			
		} else if (count == 4) {
			LexicalUnit second = first.getNextLexicalUnit();
			LexicalUnit third = second.getNextLexicalUnit();
			LexicalUnit fourth = third.getNextLexicalUnit();
			
			CSSLexicalUnit top = CSSLexicalUnit.createString(CSSLexicalUnit.SAC_IDENT, first.getStringValue(), null);
			CSSLexicalUnit right = CSSLexicalUnit.createString(CSSLexicalUnit.SAC_IDENT, second.getStringValue(), null);
			CSSLexicalUnit bottom = CSSLexicalUnit.createString(CSSLexicalUnit.SAC_IDENT, third.getStringValue(), null);
			CSSLexicalUnit left = CSSLexicalUnit.createString(CSSLexicalUnit.SAC_IDENT, fourth.getStringValue(), null);
			
			ph.property(CSSConstants.TRAVERSAL_BOTTOM_PROPERTY, bottom, false);
			ph.property(CSSConstants.TRAVERSAL_LEFT_PROPERTY, left, false);
			ph.property(CSSConstants.TRAVERSAL_RIGHT_PROPERTY, right, false);
			ph.property(CSSConstants.TRAVERSAL_TOP_PROPERTY, top, false);
		}
		
		
	}

	private int countLexicalUnits(LexicalUnit xo) {
		if (xo == null) {
			return 0;
		} else {
			return countLexicalUnits(xo.getNextLexicalUnit()) + 1;
		}
	}

}
