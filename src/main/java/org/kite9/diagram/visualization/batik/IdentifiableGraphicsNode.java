package org.kite9.diagram.visualization.batik;

import java.awt.Graphics2D;

import org.apache.batik.gvt.CompositeGraphicsNode;
import org.apache.batik.svggen.SVGGraphics2D;

public class IdentifiableGraphicsNode extends CompositeGraphicsNode {

	private String id;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public IdentifiableGraphicsNode() {
		super();
	}
	
	

	@Override
	public void paint(Graphics2D g2d) {
		System.out.println("painting " +id);
		((ElementManagingSVGGraphics2D)g2d).createGroup(id);
		super.paint(g2d);
		((ElementManagingSVGGraphics2D)g2d).finishGroup(id);
		System.out.println("finished painting " +id);
	}
	

	@Override
	public void primitivePaint(Graphics2D g2d) {
		System.out.println("starting primitive paint"+id);
		super.primitivePaint(g2d);
		System.out.println("ending primitive paint"+id);
	}
	
	
	
}
