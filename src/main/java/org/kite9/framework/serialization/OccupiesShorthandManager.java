package org.kite9.framework.serialization;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.parser.CSSLexicalUnit;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class OccupiesShorthandManager implements ShorthandManager {

	public String getPropertyName() {
		return "occupies";
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public void setValues(CSSEngine eng, PropertyHandler ph, LexicalUnit first, boolean imp) throws DOMException {
		int count = countLexicalUnits(first);
		
		
		if (count == 2) {
			ph.property(CSSConstants.GRID_OCCUPIES_X_PROPERTY, CSSLexicalUnit.createInteger(first.getIntegerValue(), null), false);
			ph.property(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, CSSLexicalUnit.createInteger(first.getNextLexicalUnit().getIntegerValue(), null), false);
		} else if (count == 4) {
			LexicalUnit second = first.getNextLexicalUnit();
			LexicalUnit third = second.getNextLexicalUnit();
			LexicalUnit fourth = third.getNextLexicalUnit();
									
			LexicalUnit xrange = CSSLexicalUnit.createInteger(second.getIntegerValue(), CSSLexicalUnit.createInteger(first.getIntegerValue(), null));
			LexicalUnit yrange = CSSLexicalUnit.createInteger(fourth.getIntegerValue(), CSSLexicalUnit.createInteger(third.getIntegerValue(), null));
			
			ph.property(CSSConstants.GRID_OCCUPIES_X_PROPERTY, xrange.getPreviousLexicalUnit(), false);
			ph.property(CSSConstants.GRID_OCCUPIES_Y_PROPERTY, yrange.getPreviousLexicalUnit(), false);
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
