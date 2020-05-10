package org.kite9.diagram.dom.managers;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.kite9.diagram.dom.css.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class SizingShorthandManager implements ShorthandManager {	

	public String getPropertyName() {
		return CSSConstants.ELEMENT_SIZING_PROPERTY;
	}

	public boolean isAnimatableProperty() {
		return false;
	}

	public boolean isAdditiveProperty() {
		return false;
	}

	public void setValues(CSSEngine eng, PropertyHandler ph, LexicalUnit first, boolean imp) throws DOMException {
		ph.property(CSSConstants.ELEMENT_HORIZONTAL_SIZING_PROPERTY, first, false);
		ph.property(CSSConstants.ELEMENT_VERTICAL_SIZING_PROPERTY, first, false);
	}

}
