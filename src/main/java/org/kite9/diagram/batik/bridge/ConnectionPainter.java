package org.kite9.diagram.batik.bridge;

import java.awt.geom.GeneralPath;

import org.apache.batik.svggen.SVGShape;
import org.kite9.diagram.batik.format.ExtendedSVGGeneratorContext;
import org.kite9.diagram.model.Connection;
import org.kite9.framework.xml.StyledKite9SVGElement;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ConnectionPainter extends AbstractGraphicsNodePainter<Connection> {

	public ConnectionPainter(Kite9BridgeContext ctx) {
		super(ctx);
	}

	@Override
	protected void processOutput(StyledKite9SVGElement in, Element out, Document d, Connection r) {
		RoutePainter routePainter = new RoutePainter(0, 0);
		SVGShape shapeConverter = new SVGShape(ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d, null, null));
		GeneralPath gp = routePainter.drawRouting(r.getRenderingInformation(), routePainter.NULL_END_DISPLAYER, routePainter.NULL_END_DISPLAYER, routePainter.LINK_HOP_DISPLAYER, false);
		Element path = shapeConverter.toSVG(gp);
		out.appendChild(path);
	} 

}
