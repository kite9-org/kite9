package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.style.DiagramElementFactory;
import org.kite9.diagram.style.DiagramElementSizing;
import org.kite9.diagram.style.DiagramElementType;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.kite9.diagram.xml.XMLElement;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.EnumValue;

public class DiagramElementFactoryImpl implements DiagramElementFactory {

	private Kite9BridgeContext context;
		
	public DiagramElementFactoryImpl(Kite9BridgeContext context) {
		super();
		this.context = context;
	}

	/**
	 * Produces the diagram element for the underlying XML.
	 */
	public DiagramElement createDiagramElement(XMLElement in, DiagramElement parent) {
		if (in instanceof StyledKite9SVGElement) {
			StyledKite9SVGElement in2 = (StyledKite9SVGElement) in;
			DiagramElementType lt = getElementType(in2);
			DiagramElementSizing sizing = getElementSizing(in2);
			DiagramElement out = instantiateDiagramElement(parent, in2, lt, sizing);
			context.handleTemplateElement(in, out);
			return out;
		} else {
			throw new Kite9ProcessingException("Don't know how to create diagram element from "+in);
		}
	}

	private DiagramElement instantiateDiagramElement(DiagramElement parent, StyledKite9SVGElement el, DiagramElementType lt, DiagramElementSizing sizing) {
		switch (lt) {
		case DIAGRAM:
			if (parent != null) {
				throw new Kite9ProcessingException("Can't nest type 'diagram' @ "+el.getID());
			}
			return new DiagramImpl(el, context);
		case LABEL:
			return new LabelImpl(el, parent, context);
		case DECAL:
			return new DecalImpl(el, parent, context);
		case CONNECTED:
			switch (sizing) {
			case MAXIMIZE:
			case MINIMIZE:
				return new ConnectedContainerImpl(el, parent, context);
			default:
				return new ConnectedLeafImpl(el, parent, context);
			}
		case LINK:
			return new ConnectionImpl(el);
		case LINK_END:
			return ((XMLElement) el.getParentNode()).getDiagramElement();
		case TERMINATOR:
			return new TerminatorImpl(el, parent);
		case UNSPECIFIED:
		case NONE:
			return null;
		default:
			throw new Kite9ProcessingException("Not implemented yet");	
		}
	}

	private static DiagramElementType getElementType(StyledKite9SVGElement in2) {
		EnumValue v = (EnumValue) in2.getCSSStyleProperty(CSSConstants.ELEMENT_TYPE_PROPERTY);
		DiagramElementType lt = (DiagramElementType) v.getTheValue();
		return lt;
	}
	
	private static DiagramElementSizing getElementSizing(StyledKite9SVGElement in2) {
		EnumValue v = (EnumValue) in2.getCSSStyleProperty(CSSConstants.ELEMENT_SIZING_PROPERTY);
		DiagramElementSizing lt = (DiagramElementSizing) v.getTheValue();
		return lt;
	}

}
