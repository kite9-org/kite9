package org.kite9.diagram.visualization.planarization.mgt;

import org.kite9.diagram.visualization.planarization.AbstractPlanarizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.Planarizer;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.builder.HierarchicalPlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructor;
import org.kite9.diagram.visualization.planarization.mgt.face.FaceConstructorImpl;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.xml.DiagramXMLElement;

public class MGTPlanarizer extends AbstractPlanarizer implements Planarizer {

	protected FaceConstructor getFaceConstructor() {
		return new FaceConstructorImpl();
	}
	
	protected RoutableHandler2D  getRoutableHandler() {
		return new PositionRoutableHandler2D();
	}

	@Override
	protected PlanarizationBuilder getPlanarizationBuilder(ElementMapper elementMapper) {
		return new HierarchicalPlanarizationBuilder(getRoutableHandler(), elementMapper);
	}

	@Override
	protected Planarization buildPlanarization(DiagramXMLElement c) {
		Planarization pln = super.buildPlanarization(c);
		getFaceConstructor().createFaces((MGTPlanarization) pln);
		return pln;
	}
	
	

}
