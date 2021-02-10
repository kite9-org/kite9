package org.kite9.diagram.dom.model;

import org.kite9.diagram.common.elements.factory.AbstractTemporaryConnected;
import org.kite9.diagram.dom.model.HasSVGRepresentation;
import org.kite9.diagram.dom.painter.SVGRectPainter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.SizedRectangular;
import org.kite9.diagram.model.position.*;
import org.kite9.diagram.model.style.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.List;

/**
 * A placeholder for spaces in a grid layout which are unoccupied.
 * 
 * @author robmoffat
 *
 */
public class TemporaryConnectedImpl extends AbstractTemporaryConnected implements Container, HasSVGRepresentation, SizedRectangular {

	private ContainerPosition gcp;
	
	public TemporaryConnectedImpl(DiagramElement parent, String id) {
		super(parent.getID()+"-g-"+id, parent);
	}

	public String toString() {
		return "[grid-temporary: "+getID()+"]";
	}

	private RectangleRenderingInformation rri = new RectangleRenderingInformationImpl(null, null, false);
	
	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		return rri;
	}

	@Override
	public ContainerPosition getContainerPosition() {
		return gcp;
	}

	public void setContainerPosition(ContainerPosition cp) {
		this.gcp = cp;
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
		return ConnectionAlignment.Companion.getNONE();
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
	public CostedDimension2D getSize(Dimension2D within) {
		return CostedDimension2D.Companion.getZERO();
	}

	@Override
	public Dimension2D getMinimumSize() {
		return CostedDimension2D.Companion.getZERO();
	}

}
