package org.kite9.diagram.batik;

import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.AbstractArrangementPipeline;

public class BatikArrangementPipeline extends AbstractArrangementPipeline {
	
	private final BatikDisplayer displayer;

	public BatikArrangementPipeline(BatikDisplayer displayer) {
		super();
		this.displayer = displayer;
	}

	@Override
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

}
