package org.kite9.diagram.visualization.batik;

import org.apache.batik.bridge.Bridge;

public class Kite9ElementGroupBridge extends AbstractKite9GraphicsNodeBridge {

	public Kite9ElementGroupBridge(GraphicsNodeLookup lookup) {
		super(lookup);
	}

	@Override
	public Bridge getInstance() {
		return new Kite9ElementGroupBridge(lookup);
	}
	
	

}
