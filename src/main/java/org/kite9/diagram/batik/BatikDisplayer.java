package org.kite9.diagram.batik;

import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.Terminator;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.visualization.display.AbstractCompleteDisplayer;
import org.kite9.framework.common.Kite9ProcessingException;

public class BatikDisplayer extends AbstractCompleteDisplayer {
	
	public BatikDisplayer(boolean buffer, int gridSize) {
		super(buffer, gridSize);
	}

	protected CostedDimension size(DiagramElement element, Dimension2D within) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getSize(within);
		}

		// not a CompactedRectangular
		return CostedDimension.ZERO;

	}

	/**
	 * Handle scaling before translation, otherwise everything goes whack.
	 */
	@Override
	public void draw(DiagramElement element, RenderingInformation ri){
		throw new Kite9ProcessingException("Unsupported operation");
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getPadding(d);
		} else if (element instanceof Connection) {
			return ((SizedRectangular) element).getPadding(d);
		} else {
			return 0;
		}
	}
	
	@Override
	public double getMargin(DiagramElement element, Direction d) {
		if (element instanceof SizedRectangular) {
			return ((SizedRectangular) element).getMargin(d);
		} else if (element instanceof Connection) {
			return ((Connection) element).getMargin(d);
		} else {
			return 0;
		}
	}
	
	protected double getLinkGutter(Connected along, Terminator a, Terminator b) {
		double length = along.getLinkGutter();
		length = Math.max(length, a.getMargin() + b.getMargin());
		return length;
	}

	@Override
	public double getLinkMinimumLength(Connection element, boolean starting, boolean ending) {
		double length = element.getMinimumLength();
		if (starting) {
			length += element.getFromDecoration().getReservedLength();
		} 
		
		if (ending) {
			length += element.getToDecoration().getReservedLength();
		}
		
		return length;
	}

	@Override
	public boolean requiresHopForVisibility(Connection a, Connection b) {
		return true;
	}

	@Override
	protected double getLinkInset(Connected element, Direction d) {
		return element.getLinkInset();
	}

}
