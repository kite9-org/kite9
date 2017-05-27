package org.kite9.diagram.batik.element;

import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.style.DiagramElementFactory;
import org.kite9.diagram.model.style.DiagramElementSizing;
import org.kite9.diagram.model.style.DiagramElementType;
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
		if (in instanceof StyledKite9SVGElement) {
			StyledKite9SVGElement in2 = (StyledKite9SVGElement) in;
			DiagramElementType lt = getElementType(in2);
			DiagramElementSizing sizing = getElementSizing(in2);
			AbstractXMLDiagramElement out = instantiateDiagramElement(parent, in2, lt, sizing);
			if (out != null) {
			//	out.ensureInitialized();
				context.handleTemplateElement(in, out);
			}
			return out;
		} else {
			throw new Kite9ProcessingException("Don't know how to create diagram element from "+in);
		}
	}

	private AbstractXMLDiagramElement instantiateDiagramElement(DiagramElement parent, StyledKite9SVGElement el, DiagramElementType lt, DiagramElementSizing sizing) {
		switch (lt) {
		case DIAGRAM:
			if (parent != null) {
				throw new Kite9ProcessingException("Can't nest type 'diagram' @ "+el.getID());
			}
			return new DiagramImpl(el, context);
		case LABEL:
			switch (sizing) {
			case MAXIMIZE:
			case MINIMIZE:
				return new LabelContainerImpl(el, parent, context);
			default:
				return new LabelLeafImpl(el, parent, context);
			}
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
			return new ConnectionImpl(el, parent, context);
		case LINK_END:
			return (AbstractXMLDiagramElement) ((Kite9XMLElement) el.getParentNode()).getDiagramElement();
		case TERMINATOR:
			return new TerminatorImpl(el, parent, context);
		case NONE:
			return null;
		case UNSPECIFIED:
		default:
			throw new Kite9ProcessingException("Don't know how to process element: "+el+" with type "+lt);	
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
