package org.kite9.diagram.batik.model;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.painter.SVGContainerRectangularPainter;
import org.kite9.diagram.batik.painter.SVGLeafPainter;
import org.kite9.diagram.batik.text.TextLeafPainter;
import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.common.elements.factory.TemporaryConnected;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.elements.StyledKite9XMLElement;
import org.kite9.diagram.dom.elements.XMLDiagramElementFactory;
import org.kite9.diagram.dom.managers.EnumValue;
import org.kite9.diagram.dom.model.AbstractDOMDiagramElement;
import org.kite9.diagram.dom.painter.DirectSVGGroupPainter;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.ContentTransform;
import org.kite9.diagram.model.style.DiagramElementType;
import org.kite9.diagram.model.style.RectangularElementUsage;

public class DiagramElementFactoryImpl implements XMLDiagramElementFactory {

	private Kite9BridgeContext context;
		
	public DiagramElementFactoryImpl() {
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

	private AbstractDOMDiagramElement instantiateDiagramElement(DiagramElement parent, StyledKite9XMLElement el, DiagramElementType lt, RectangularElementUsage usage) {
		if ((parent == null) && (lt != DiagramElementType.DIAGRAM)) {
			// all elements apart from diagram must have a parent
			return null;
		}
		
		switch (lt) {
		case DIAGRAM:
			if (parent != null) {
				throw new Kite9XMLProcessingException("Can't nest type 'diagram' @ "+el.getID(), el);
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
				// need to extend slideables to handle this
				// return new DecalContainerImpl(el, parent, context, new SVGContainerRectangularPainter(el, context), ContentTransform.POSITION);
				throw new Kite9XMLProcessingException("Decal containers not supported yet: @"+el.getID(), el);
			}
		case TEXT:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new TextLeafPainter(el, context), ContentTransform.CROP);
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new TextLeafPainter(el, context), ContentTransform.RESCALE);
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new TextLeafPainter(el, context), ContentTransform.CROP);
			} 
		case SVG:
			switch (usage) {
			case LABEL:
				return new LabelLeafImpl(el, parent, context, new SVGLeafPainter(el, context), ContentTransform.CROP);
			case DECAL:
				return new DecalLeafImpl(el, parent, context, new SVGLeafPainter(el, context), ContentTransform.POSITION);
			case REGULAR:
			default:
				return new ConnectedLeafImpl(el, parent, context, new SVGLeafPainter(el, context), ContentTransform.CROP);
			} 
		case LINK:
			return new ConnectionImpl(el, parent, context, new DirectSVGGroupPainter(el), ContentTransform.POSITION);
		case LINK_END:
			return new TerminatorImpl(el, parent, context, new DirectSVGGroupPainter(el), ContentTransform.POSITION);
		case NONE:
			return null;
		case UNSPECIFIED:
		default:
			throw new Kite9XMLProcessingException("Don't know how to process element: "+el+"("+el.getTagName()+") with type "+lt+" and usage "+usage+" and parent "+parent, el);	
		}
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
	public void setBridgeContext(Kite9BridgeContext c) {
		this.context = c;
	}

	@Override
	public TemporaryConnected createTemporaryConnected(DiagramElement parent, String idSuffix) {
		return new TemporaryConnectedImpl(parent, idSuffix);
	}
}
