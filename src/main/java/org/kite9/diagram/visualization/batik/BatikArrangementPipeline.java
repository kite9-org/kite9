package org.kite9.diagram.visualization.batik;

import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.pipeline.full.AbstractArrangementPipeline;

public class BatikArrangementPipeline extends AbstractArrangementPipeline {
	
	private BatikDisplayer displayer = new BatikDisplayer(false, 20);

	@Override
	public CompleteDisplayer getDisplayer() {
		return displayer;
	}

}
