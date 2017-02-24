package org.kite9.diagram.visualization.batik.element;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.kite9.diagram.adl.Decal;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.sizing.ScaledGraphics;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visualization.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.visualization.batik.node.IdentifiableGraphicsNode;
import org.kite9.diagram.xml.StyledKite9SVGElement;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AdaptiveScaleSVGGraphicsImpl extends AbstractRectangularDiagramElement implements ScaledGraphics, Decal {

	public AdaptiveScaleSVGGraphicsImpl(StyledKite9SVGElement el, DiagramElement parent, Kite9BridgeContext ctx) {
		super(el, parent, ctx);
	}

	/**
	 * This element is allowed to contain SVG
	 */
	@Override
	protected IdentifiableGraphicsNode initMainGraphicsLayer() {
		IdentifiableGraphicsNode out =  super.initMainGraphicsLayer();
		initSVGGraphicsContents(out);
		return out;
	}

	@Override
	public void setParentSize(double[] x, double[] y) {
		// iterate over the contained svg and overwrite any variables
		performReplace(theElement.getChildNodes(), x, y);
		RectangleRenderingInformation rri = getRenderingInformation();
		RectangleRenderingInformation parent = getContainer().getRenderingInformation();
		rri.setSize(parent.getSize());
		rri.setPosition(parent.getPosition());
	}

	private void performReplace(NodeList nodeList, double[] x, double[] y) {
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);

			if (n instanceof Element) {
				performReplace(n.getChildNodes(), x, y);
				for (int j = 0; j < n.getAttributes().getLength(); j++) {
					Attr a = (Attr) n.getAttributes().item(j);
					performValueReplace(a, x, y);
				}
			} 
		}
	}

	private void performValueReplace(Attr a, double[] x, double[] y) {
		Pattern p = Pattern.compile("\\{([xXyY])([0-9]+)\\}");
		
		String input = a.getValue();
		Matcher m = p.matcher(input);
		StringBuilder out = new StringBuilder();
		int place = 0;
		while (m.find()) {
			out.append(input.substring(place, m.start()));
			
			String dimension = m.group(1).toLowerCase();
			String indexStr = m.group(2);
			int index = Integer.parseInt(indexStr);
			double v = "x".equals(dimension) ? x[index] : y[index];
			out.append(v);
			place = m.end();
		}
		
		out.append(input.substring(place));
		a.setValue(out.toString());
	}
	
	

}
