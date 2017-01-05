package org.kite9.diagram.visualization.planarization.rhd.position;

import java.util.Comparator;

import org.junit.Test;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.diagram.visualization.planarization.grid.GridPositionerImpl;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapperImpl;

public class TestVertexPositioner {

	@Test
	public void testPositioningOfCentralVertex() {
		GridPositioner gp = new GridPositionerImpl();
		ElementMapper em = new ElementMapperImpl(gp);
		RoutableHandler2D rh = new PositionRoutableHandler2D();
		VertexPositioner vp = new VertexPositionerImpl(em, rh, new Comparator<DiagramElement>() {
			
			@Override
			public int compare(DiagramElement o1, DiagramElement o2) {
				return 1;
			}
		});
		
		Object a = new Object();
		Object b = new Object();
		
		rh.setPlacedPosition(a, rh.createRouting(new BasicBounds(0, .5), new BasicBounds(0, 1)));
		rh.setPlacedPosition(b, rh.createRouting(new BasicBounds(.5, 1), new BasicBounds(0, 1)));
		
	}
	
}

