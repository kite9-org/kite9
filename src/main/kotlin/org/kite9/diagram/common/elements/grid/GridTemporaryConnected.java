package org.kite9.diagram.common.elements.grid;

import java.util.Collections;
import java.util.List;

import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.dom.managers.IntegerRangeValue;
import org.kite9.diagram.dom.model.HasSVGRepresentation;
import org.kite9.diagram.dom.painter.SVGRectPainter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.CostedDimension;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.diagram.model.style.ConnectionAlignment;
import org.kite9.diagram.model.style.ConnectionsSeparation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.GridContainerPosition;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A placeholder for spaces in a grid layout which are unoccupied.
 * 
 * @author robmoffat
 *
 */
public class GridTemporaryConnected extends AbstractTemporaryConnected implements Container, HasSVGRepresentation, SizedRectangular {

	private GridContainerPosition gcp;
	
	public GridTemporaryConnected(DiagramElement parent, int x, int y) {
		super(parent);
		this.id = parent.getID()+"-g-"+x+"-"+y;
		this.gcp = new GridContainerPosition(new IntegerRangeValue(x, x), new IntegerRangeValue(y, y));
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}
	
	public String toString() {
		return "[grid-temporary: "+id+"]";
	}

	private RectangleRenderingInformation rri = new RectangleRenderingInformationImpl(null, null, null, false);
	
	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		return rri;
	}

	@Override
	public ContainerPosition getContainerPosition() {
		return gcp;
	}
	
	@Override
	public DiagramElementSizing getSizing(boolean horiz) {
		return null;	// no preference
	}

	@Override
	public ConnectionsSeparation getConnectionsSeparationApproach() {
		return ConnectionsSeparation.SEPARATE;   // irrelevant, won't have connections
	}

	@Override
	public List<DiagramElement> getContents() {
		return Collections.emptyList();
	}

	@Override
	public Layout getLayout() {
		return null;
	}

	@Override
	public BorderTraversal getTraversalRule(Direction d) {
		return BorderTraversal.ALWAYS;
	}

	@Override
	public int getGridColumns() {
		return 1;
	}

	@Override
	public int getGridRows() {
		return 1;
	}

	@Override
	public double getLinkGutter() {
		return 0;
	}

	@Override
	public double getLinkInset() {
		return 0;
	}

	@Override
	public ConnectionAlignment getConnectionAlignment(Direction side) {
		return ConnectionAlignment.NONE;
	}

	@Override
	public Element output(Document d, XMLProcessor p) {
		SVGRectPainter rectPainter = new SVGRectPainter("grid-temporary");
		rectPainter.setDiagramElement(this);
		return rectPainter.output(d, p);
	}

	@Override
	public double getMargin(Direction d) {
		return 0;
	}

	@Override
	public double getPadding(Direction d) {
		return 0;
	}

	@Override
	public CostedDimension getSize(Dimension2D within) {
		return CostedDimension.ZERO;
	}

	@Override
	public Dimension2D getMinimumSize() {
		return CostedDimension.ZERO;
	}

}
