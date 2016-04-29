package org.kite9.diagram.visualization.display.style.shapes;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.visualization.display.style.DirectionalValues;

/**
 * Reserves fixed sizes for the padding in each direction. Perfect for contexts.
 * @author robmoffat
 *
 */
public abstract class AbstractReservedFlexibleShape extends AbstractFlexibleShape {

	protected DirectionalValues reserved;

	public AbstractReservedFlexibleShape(double marginX, double marginY, DirectionalValues reserved) {
		super(marginX, marginY);
		this.reserved = reserved;
		this.context = true;
	}

	@Override
	protected DirectionalValues getBorderSizesInner(Dimension2D padded) {
		return reserved;
	}
}