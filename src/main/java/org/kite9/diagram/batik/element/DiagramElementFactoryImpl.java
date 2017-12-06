package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.DirectSVGPainter;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.bridge.SVGContainerRectangularPainter;
import org.kite9.diagram.batik.bridge.SVGLeafRectangularPainter;
import org.kite9.diagram.batik.bridge.TextRectangularPainter;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.dom.CSSConstants;
import org.kite9.framework.dom.EnumValue;
import org.kite9.framework.xml.Kite9XMLElement;
import org.kite9.framework.xml.StyledKite9SVGElement;

public class DiagramElementFactoryImpl implements DiagramElementFactory {

	private Kite9BridgeContext context;
		
	public DiagramElementFactoryImpl(Kite9BridgeContext context) {
		super();
		this.context = context;
	}

	/**
	 * Produces the diagram element for the underlying XML.
	 */
	public DiagramElement createDiagramElement(Kite9XMLElement in, DiagramElement parent) {
		Exception e = null;
		if (in instanceof StyledKite9SVGElement) {
			try {
				StyledKite9SVGElement in2 = (StyledKite9SVGElement) in;
				DiagramElementType lt = getElementType(in2);
				RectangularElementUsage usage = getElementUsage(in2);
				DiagramElement out = instantiateDiagramElement(parent, in2, lt, usage);
				return out;
			} catch (Exception e2) {
				e = e2;
			}
		}
		
		throw new Kite9ProcessingException("Don't know how to create diagram element from "+in, e);
		
	}

	private AbstractDOMDiagramElement instantiateDiagramElement(DiagramElement parent, StyledKite9SVGElement el, DiagramElementType lt, RectangularElementUsage usage) {
		switch (lt) {
		case DIAGRAM:
			if (parent != null) {
				throw new Kite9ProcessingException("Can't nest type 'diagram' @ "+el.getID());
			}
			return new DiagramImpl(el, context, new SVGContainerRectangularPainter());
		case CONTAINER:
			switch (usage) {
			case LABEL:
				return new LabelContainerImpl(el, parent, context, new SVGContainerRectangularPainter());
			case REGULAR:
				return new ConnectedContainerImpl(el, parent, context, new SVGContainerRectangularPainter());
			case DECAL:
			default:
				throw new Kite9ProcessingException("Decal containers not supported yet: @"+el.getID());
			}
		case TEXT:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new TextRectangularPainter(context));
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new TextRectangularPainter(context));
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new TextRectangularPainter(context));
			} 
		case SVG:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new SVGLeafRectangularPainter(context));
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new SVGLeafRectangularPainter(context));
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new SVGLeafRectangularPainter(context));
			}
		case LINK:
			return new ConnectionImpl(el, parent, context, new DirectSVGPainter<Connection>());
		case LINK_END:
			return new TerminatorImpl(el, parent, context, new SVGLeafRectangularPainter(context));
		case NONE:
			return null;
		case UNSPECIFIED:
		default:
			throw new Kite9ProcessingException("Don't know how to process element: "+el+"("+el.getTagName()+") with type "+lt+" and usage "+usage);	
		}
	}

	private static DiagramElementType getElementType(StyledKite9SVGElement in2) {
		EnumValue v = (EnumValue) in2.getCSSStyleProperty(CSSConstants.ELEMENT_TYPE_PROPERTY);
		DiagramElementType lt = (DiagramElementType) v.getTheValue();
		return lt;
	}
	
	private static RectangularElementUsage getElementUsage(StyledKite9SVGElement in2) {
		EnumValue v = (EnumValue) in2.getCSSStyleProperty(CSSConstants.ELEMENT_USAGE_PROPERTY);
		RectangularElementUsage reu = (RectangularElementUsage) v.getTheValue();
		return reu;
	}

}
