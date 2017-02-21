package org.kite9.diagram.visualization.batik.element;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Text;
import org.kite9.diagram.xml.StyledKite9SVGElement;

public class ConnectedTextImpl extends AbstractConnectedDiagramElement implements Text, Connected {

	public ConnectedTextImpl(StyledKite9SVGElement el, DiagramElement parent) {
		super(el, parent);
	}

	@Override
	public String getText() {
		// to support old xml format.
		String label = theElement.getAttribute("label");
		String stereotype = theElement.getAttribute("stereotype");
		
		return label+stereotype+theElement.getTextContent();
	}

}
