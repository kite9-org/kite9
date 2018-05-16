package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.SVGContainerRectangularPainter;
import org.kite9.diagram.batik.painter.SVGLeafRectangularPainter;
import org.kite9.diagram.batik.painter.TextRectangularPainter;
import org.kite9.diagram.dom.CSSConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9SVGElement;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.model.AbstractDOMDiagramElement;
import org.kite9.diagram.dom.model.DiagramElementFactory;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;
import org.kite9.framework.common.Kite9ProcessingException;

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
		if ((parent == null) && (lt != DiagramElementType.DIAGRAM)) {
			// all elements apart from diagram must have a parent
			return null;
		}
		
		switch (lt) {
		case DIAGRAM:
			if (parent != null) {
				throw new Kite9ProcessingException("Can't nest type 'diagram' @ "+el.getID());
			}
			return new DiagramImpl(el, context, new SVGContainerRectangularPainter(el, context), ContentTransform.POSITION);
		case CONTAINER:
			switch (usage) {
			case LABEL:
				return new LabelContainerImpl(el, parent, context, new SVGContainerRectangularPainter(el, context), ContentTransform.POSITION);
			case REGULAR:
				return new ConnectedContainerImpl(el, parent, context, new SVGContainerRectangularPainter(el, context), ContentTransform.POSITION);
			case DECAL:
			default:
				throw new Kite9ProcessingException("Decal containers not supported yet: @"+el.getID());
			}
		case TEXT:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new TextRectangularPainter(el, context), ContentTransform.CROP);
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new TextRectangularPainter(el, context), ContentTransform.RESCALE);
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new TextRectangularPainter(el, context), ContentTransform.CROP);
			} 
		case SVG:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new SVGLeafRectangularPainter(el, context), ContentTransform.CROP);
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new SVGLeafRectangularPainter(el, context), ContentTransform.RESCALE);
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new SVGLeafRectangularPainter(el, context), ContentTransform.CROP);
			}
		case LINK:
			return new ConnectionImpl(el, parent, context, new DirectSVGGroupPainter(el, context.getXMLProcessor()), ContentTransform.POSITION);
		case LINK_END:
			return new TerminatorImpl(el, parent, context, new DirectSVGGroupPainter(el, context.getXMLProcessor()), ContentTransform.POSITION);
		case NONE:
			return null;
		case UNSPECIFIED:
		default:
			throw new Kite9ProcessingException("Don't know how to process element: "+el+"("+el.getTagName()+") with type "+lt+" and usage "+usage+" and parent "+parent);	
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
