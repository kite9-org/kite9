package org.kite9.diagram.batik;

import org.kite9.diagram.common.elements.factory.DiagramElementFactory;
import org.kite9.diagram.dom.elements.XMLDiagramElementFactory;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;

public class BatikArrangementPipeline extends AbstractArrangementPipeline {
	
	private final BatikDisplayer displayer;
	private final XMLDiagramElementFactory factory;

	public BatikArrangementPipeline(XMLDiagramElementFactory factory, BatikDisplayer displayer) {
		super();
		this.displayer = displayer;
		this.factory = factory;
	}

	@Override
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

	@Override
	public DiagramElementFactory getDiagramElementFactory() {
		return factory;
	}
}
