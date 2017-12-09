package org.kite9.diagram.model;

import org.kite9.diagram.model.style.HorizontalAlignment;
import org.kite9.diagram.model.style.VerticalAlignment;

public interface AlignedRectangular extends Rectangular {

	VerticalAlignment getVerticalAlignment();

	HorizontalAlignment getHorizontalAlignment();

}