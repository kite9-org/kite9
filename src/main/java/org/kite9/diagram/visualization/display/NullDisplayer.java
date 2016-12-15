package org.kite9.diagram.visualization.display;

import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.adl.Terminator;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RenderingInformation;

public class NullDisplayer implements CompleteDisplayer {

	public void draw(DiagramElement element, RenderingInformation ri) {
	}

	public CostedDimension size(DiagramElement element, Dimension2D within) {
		return CostedDimension.NOT_DISPLAYABLE;
	}

	public boolean isVisibleElement(DiagramElement element) {
		return false;
	}

	public void finish() {
	}

	public void prepareCanvas(Dimension2D size) {
	}

	public boolean canDisplay(DiagramElement element) {
		return false;
	}

	public double getMinimumDistanceBetween(DiagramElement a, Direction aSide, DiagramElement b, Direction bSide, Direction d) {
		return 0;
	}

	@Override
	public boolean isOutputting() {
		return true;
	}

	@Override
	public void setOutputting(boolean outputting) {
	}

	@Override
	public double getLinkMargin(DiagramElement element, Direction d) {
		return 0;
	}

	@Override
	public double getPadding(DiagramElement element, Direction d) {
		return 0;
	}

	@Override
	public double getTerminatorLength(Terminator terminator) {
		return 0;
	}

	@Override
	public double getTerminatorReserved(Terminator terminator, Connection c) {
		return 0;
	}

	@Override
	public boolean requiresDimension(DiagramElement de) {
		return false;
	}

	@Override
	public double getLinkGutter(DiagramElement element, Direction d) {
		return 0;
	}

}
