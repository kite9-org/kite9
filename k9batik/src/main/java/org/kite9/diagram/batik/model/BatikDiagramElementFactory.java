package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.BatikLeafPainter;
import org.kite9.diagram.batik.text.TextLeafPainter;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.elements.factory.TemporaryConnected;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.elements.XMLDiagramElementFactory;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.model.AbstractDiagramElementFactory;
import org.kite9.diagram.dom.model.TemporaryConnectedImpl;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.painter.SVGContainerRectangularPainter;
import org.kite9.diagram.dom.painter.SVGRectPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.w3c.dom.Element;

public class BatikDiagramElementFactory extends AbstractDiagramElementFactory<Kite9XMLElement> implements XMLDiagramElementFactory {

	public BatikDiagramElementFactory() {
		super();
	}

	/**
	 * Produces the diagram element for the underlying XML.
	 */
	public DiagramElement createDiagramElement(Kite9XMLElement in, DiagramElement parent) {
		if (in instanceof StyledKite9XMLElement) {
			StyledKite9XMLElement in2 = (StyledKite9XMLElement) in;
			DiagramElementType lt = getElementType(in2);
			RectangularElementUsage usage = getElementUsage(in2);
			DiagramElement out = instantiateDiagramElement(parent, in2, lt, usage);
			return out;
		}
		
		throw new Kite9XMLProcessingException("Don't know how to create diagram element from "+in+"#"+in.getID(), in);
		
	}

	@Override
	protected TextLeafPainter getTextPainter(Element el) {
		return new TextLeafPainter(el, (Kite9BridgeContext) getContext());
	}

	@Override
	protected BatikLeafPainter getLeafPainter(Element el) {
		return new BatikLeafPainter(el, getContext());
	}

	@Override
	protected SVGContainerRectangularPainter getContainerPainter(Element el) {
		return new SVGContainerRectangularPainter(el, getContext());
	}

	public static RectangularElementUsage getElementUsage(StyledKite9XMLElement in2) {
		EnumValue v = (EnumValue) in2.getCSSStyleProperty(CSSConstants.ELEMENT_USAGE_PROPERTY);
		RectangularElementUsage reu = (RectangularElementUsage) v.getTheValue();
		return reu;
	}

	public static DiagramElementType getElementType(StyledKite9XMLElement in2) {
		return in2.getType();
	}

	@Override
	public TemporaryConnected createTemporaryConnected(DiagramElement parent, String idSuffix) {
		return new TemporaryConnectedImpl(parent, idSuffix, new SVGRectPainter("grid-temporary"));
	}

	@Override
	protected Painter getDirectPainter(Element el) {
		return new DirectSVGGroupPainter(el);
	}
}
