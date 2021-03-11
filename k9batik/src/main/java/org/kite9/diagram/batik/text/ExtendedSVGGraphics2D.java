package org.kite9.diagram.batik.text;

import org.apache.batik.svggen.SVGGraphics2D;
import org.w3c.dom.Element;

import java.util.Collections;


/**
 * Allows us to build an {@link SVGGraphics2D} at any random place in the dom tree
 * 
 * @author robmoffat
 *
 */
public class ExtendedSVGGraphics2D extends SVGGraphics2D  {

	public ExtendedSVGGraphics2D(ExtendedSVGGeneratorContext ctx, Element currentSubgroup) {
		super(ctx, false);
		getDOMTreeManager().setTopLevelGroup(currentSubgroup);
		clearUnsupportedAttributes();
	}
	
	private void clearUnsupportedAttributes() {
		// since we are converting from svg to svg, there should be no unsupported attributes
		setUnsupportedAttributes(Collections.emptySet());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

}
