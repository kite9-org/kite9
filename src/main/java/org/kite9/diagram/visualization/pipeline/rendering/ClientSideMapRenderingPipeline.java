package org.kite9.diagram.visualization.pipeline.rendering;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.format.Renderer;
import org.kite9.diagram.xml.DiagramXMLElement;

/**
 * Outputs details for use as a HTML client-side map, for use with the PNG format.
 * 
 * @author robmoffat
 * 
 */
public class ClientSideMapRenderingPipeline implements Renderer<String> {

	public static final NumberFormat NUMBER_FORMAT = new DecimalFormat("0");

	public ClientSideMapRenderingPipeline() {
	}

	public String render(DiagramXMLElement d) {
		final StringBuilder out = new StringBuilder(1000);

		DiagramElementVisitor vis = new DiagramElementVisitor();
		vis.visit(d.getDiagramElement(), new VisitorAction() {

			public void visit(DiagramElement rde) {
				if (rde.getRenderingInformation() instanceof RectangleRenderingInformation) {
					RectangleRenderingInformation rri = (RectangleRenderingInformation) rde
							.getRenderingInformation();
					out.append("\t<area shape=\"rect\" coords=\"");
					out.append(format(rri.getPosition().x()));
					out.append(",");
					out.append(format(rri.getPosition().y()));
					out.append(",");
					out.append(format((rri.getPosition().x() + rri.getSize().x())));
					out.append(",");
					out.append(format((rri.getPosition().y() + rri.getSize().y())));
					out.append("\" id=\"");
					out.append(rde.getID());
					out.append("\" />\n");
				}
			}

			private String format(double x) {
				return NUMBER_FORMAT.format(x);
			}

		});

		return out.toString();
	}
}
