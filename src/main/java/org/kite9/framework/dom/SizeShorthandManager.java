package org.kite9.framework.dom;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;

public class SizeShorthandManager implements ShorthandManager {

	private String xProp, yProp, sizeProp;
	
	public SizeShorthandManager(String xProp, String yProp, String sizeProp) {
		super();
		this.xProp = xProp;
		this.yProp = yProp;
		this.sizeProp = sizeProp;
	}

	public String getPropertyName() {
		return sizeProp;
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
			ph.property(xProp, first, false);
			ph.property(yProp, first.getNextLexicalUnit(), false);
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
