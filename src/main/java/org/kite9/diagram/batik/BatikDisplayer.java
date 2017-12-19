package org.kite9.diagram.batik;

import org.kite9.diagram.model.AlignedRectangular;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
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
		if (element instanceof AlignedRectangular) {
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
		if (element instanceof AlignedRectangular) {
			return ((SizedRectangular) element).getPadding(d);
		} else if (element instanceof Connection) {
			return ((SizedRectangular) element).getPadding(d);
		} else {
			return 0;
		}
	}
	
	@Override
	public double getMargin(DiagramElement element, Direction d) {
		if (element instanceof AlignedRectangular) {
			return ((SizedRectangular) element).getMargin(d);
		} else if (element instanceof Connection) {
			return ((Connection) element).getMargin(d);
		} else {
			return 0;
		}
	}
	
	protected double getLinkGutter(Connected along, Terminator a, Direction aSide, Terminator b, Direction bSide) {
		double length = along.getLinkGutter();
		double aPadding = (a != null) ? a.getPadding(aSide) : 0;
		double bPadding = (b != null) ? b.getPadding(bSide) : 0;
		double aMargin = (a != null) ? a.getMargin(aSide) : 0;
		double bMargin = (b != null) ? b.getMargin(bSide) : 0;

		double terminatorSize = aPadding + bPadding;
		double margin = Math.max(aMargin, bMargin);
		length = Math.max(length, terminatorSize + margin);
		return length;
	}

	@Override
	public double getLinkMinimumLength(Connection element, boolean starting, boolean ending) {
		double length = 0;
		if (starting || ending) {
			length = element.getMinimumLength();
		}

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
