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
			switch (lt) {
			case DIAGRAM:
				if (parent != null) {
					throw new Kite9ProcessingException("Can't nest type 'diagram' @ "+in.getID());
				}
				return new DiagramImpl(in2, context);
			case LABEL:
				return new LabelImpl(in2, parent);
			case CONNECTED:
				DiagramElementSizing sizing = getElementSizing(in2);
				switch (sizing) {
				case MAXIMIZE:
				case MINIMIZE:
					return new ConnectedContainerImpl(in2, parent, context);
				case DECAL:
					throw new UnsupportedOperationException();
				case FIXED_SIZE:
					return new FixedSizeSVGGraphicsImpl(in2, parent, context);
				case TEXT:
				case UNSPECIFIED:
				default:
					return new ConnectedTextImpl(in2, parent);
				}
			case LINK:
				return new ConnectionImpl(in2);
			case LINK_END:
				return ((XMLElement) in.getParentNode()).getDiagramElement();
			case TERMINATOR:
				return new TerminatorImpl(in2, parent);
			case UNSPECIFIED:
			case NONE:
				return null;
			default:
				throw new Kite9ProcessingException("Not implemented yet");	
			}
			
		} else {
			throw new Kite9ProcessingException("Don't know how to create diagram element from "+in);
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
