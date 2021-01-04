package org.kite9.diagram;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Dimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.visitors.DiagramChecker;

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
		RenderingInformation ria = a.getRenderingInformation();
		RenderingInformation rib = b.getRenderingInformation();
		
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
