package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.kite9.diagram.dom.css.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class GridSizeShorthandManager implements ShorthandManager {

	public String getPropertyName() {
		return CSSConstants.GRID_SIZE_PROPERTY;
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public void setValues(CSSEngine eng, PropertyHandler ph, LexicalUnit xo, boolean imp) throws DOMException {
		LexicalUnit yo = xo.getNextLexicalUnit();
		
		ph.property(CSSConstants.GRID_COLUMNS_PROPERTY, xo, false);
		ph.property(CSSConstants.GRID_ROWS_PROPERTY, yo, false);
	}

}
