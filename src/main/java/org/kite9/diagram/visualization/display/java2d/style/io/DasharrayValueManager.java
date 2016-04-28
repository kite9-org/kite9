package org.kite9.diagram.visualization.display.java2d.style.io;

import java.util.HashMap;
import java.util.Map;

import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.CSSStylableElement;
import org.apache.batik.css.engine.StyleMap;
import org.apache.batik.css.engine.value.FloatValue;
import org.apache.batik.css.engine.value.ListValue;
import org.apache.batik.css.engine.value.StringValue;
import org.apache.batik.css.engine.value.Value;
import org.apache.batik.css.engine.value.svg.StrokeDasharrayManager;
import org.apache.batik.util.CSSConstants;
import org.w3c.css.sac.LexicalUnit;
import org.w3c.dom.DOMException;
import org.w3c.dom.css.CSSPrimitiveValue;

/**
 * Handles conversion of raphael gradient paints to and from css.
 * @author robmoffat
 *
 */
public class DasharrayValueManager extends StrokeDasharrayManager {

	 public static final Map<String, int[]> DASH_PATTERNS = new HashMap<String, int[]>();
	 
	 static void addDashPattern(String key, int... value) {
		 DASH_PATTERNS.put(key, value);
	 }
	 
	 static {
		 addDashPattern( "-", 3, 1);
		 addDashPattern( ".", 1, 1);
		 addDashPattern( "-.", 3, 1, 1, 1);
		 addDashPattern( "-..", 3, 1,1, 1,1);
		 addDashPattern( ". ", 1, 3);
		 addDashPattern( "- ", 4, 3);
		 addDashPattern( "--", 8, 3); 
		 addDashPattern( "- .", 4, 3, 1, 3); 
		 addDashPattern( "--.", 8, 3, 1, 3); 
		 addDashPattern( "--..", 8,3, 1,3,1,3);	
	};
	
	public DasharrayValueManager() {
		super();
	}

	@Override
	public Value createValue(LexicalUnit lu, CSSEngine engine) throws DOMException {
		if (lu.getLexicalUnitType() == LexicalUnit.SAC_STRING_VALUE) {
			StringValue sv = new StringValue(CSSPrimitiveValue.CSS_STRING, lu.getStringValue());
			return sv;
		} else {
			return super.createValue(lu, engine);
		}
	}
	
	
	
	@Override
	public Value computeValue(CSSStylableElement elt, String pseudo, CSSEngine engine, int idx, StyleMap sm, Value value) {
		if (value instanceof StringValue) {
			// this works in the same way as raphael
			int idx2 = engine.getPropertyIndex(CSSConstants.CSS_STROKE_WIDTH_PROPERTY);
			Value width = engine.getComputedStyle(elt, pseudo, idx2);
			
			float butt = width.getFloatValue();
			int idx3 =  engine.getPropertyIndex(CSSConstants.CSS_STROKE_LINECAP_PROPERTY);
			Value linecap =  engine.getComputedStyle(elt, pseudo, idx3);
			
			if (linecap.getStringValue() == CSSConstants.CSS_BUTT_VALUE) {
				butt = 0;
			}
			
			int[] pattern = DASH_PATTERNS.get(value.getStringValue());
			if (pattern == null) {
				return new FloatValue(CSSPrimitiveValue.CSS_PRIMITIVE_VALUE, 1);
			}
			ListValue out = new ListValue(' ');
			for (int i = 0; i < pattern.length; i++) {
				out.append(new FloatValue(CSSPrimitiveValue.CSS_PX, getFloatForPattern(pattern, i, width.getFloatValue(), butt)));
			}
		
			return super.computeValue(elt, pseudo, engine, idx, sm, out);
		}
		return super.computeValue(elt, pseudo, engine, idx, sm, value);
	}

	private float getFloatForPattern(int[] pattern, int i, float width, float butt) {
		float amt =  pattern[i] * width + ((i % 2) == 1 ? 1 : -1) * butt;
		return amt;
	}
}
