package org.kite9.diagram.functional;

import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visualization.format.pos.DiagramChecker;

public class DiagramAssert {
	
	public static void assertInDirection(Direction d, DiagramElement... e) {
		DiagramElement last = null;
		for (DiagramElement de : e) {
			if (last != null) {
				assertInDirection(last, de, d);
			}
			
			last = de;
		}
	}

	public static void assertInDirection(DiagramElement a, DiagramElement b, Direction d) {
		RenderingInformation ria = ((PositionableDiagramElement) a).getRenderingInformation();
		RenderingInformation rib = ((PositionableDiagramElement) b).getRenderingInformation();
		
		if ((ria instanceof RectangleRenderingInformation) && (rib instanceof RectangleRenderingInformation)) {
			Dimension2D posa = ((RectangleRenderingInformation)ria).getPosition();
			Dimension2D posb = ((RectangleRenderingInformation)rib).getPosition();
			Dimension2D siza = ((RectangleRenderingInformation)ria).getSize();
			Dimension2D sizb = ((RectangleRenderingInformation)rib).getSize();
			
			String message = "Direction from "+a+" to "+b+" is not "+d;
			boolean ok = true;
			switch (d) {
			case UP:
				ok= posa.getHeight() > posb.getHeight() + sizb.getHeight();
				break;
			case DOWN:
				ok =posa.getHeight() + siza.getHeight() < posb.getHeight();
				break;
			case LEFT:
				ok =posa.getWidth() > posb.getWidth() + sizb.getWidth();
				break;
			case RIGHT:
				ok = posa.getWidth()+ siza.getWidth() < posb.getWidth();
				break;
			}
			
			if (!ok) {
				throw new DiagramChecker.ExpectedLayoutException(message);
			}
		}
	}
}
