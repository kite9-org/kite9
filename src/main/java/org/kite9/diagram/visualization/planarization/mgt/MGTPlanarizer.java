package org.kite9.diagram.visualization.planarization.mgt;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.AbstractPlanarizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.Planarizer;
import org.kite9.diagram.visualization.planarization.mgt.builder.HierarchicalPlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor;
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructorImpl;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;

public class MGTPlanarizer extends AbstractPlanarizer implements Planarizer {

	public MGTPlanarizer(ElementMapper elementMapper) {
		super(elementMapper);
	}

	protected FaceConstructor getFaceConstructor() {
		return new FaceConstructorImpl();
	}
	
	protected RoutableHandler2D  getRoutableHandler() {
		return new PositionRoutableHandler2D();
	}

	@Override
	protected PlanarizationBuilder getPlanarizationBuilder() {
		return new HierarchicalPlanarizationBuilder(getElementMapper(), getGridPositioner());
	}

	@Override
	protected Planarization buildPlanarization(Diagram c) {
		Planarization pln = super.buildPlanarization(c);
		getFaceConstructor().createFaces((MGTPlanarization) pln);
		return pln;
	}
	
	

}
