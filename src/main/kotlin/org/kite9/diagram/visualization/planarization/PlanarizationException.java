package org.kite9.diagram.visualization.planarization;

import org.kite9.diagram.logging.LogicException;

public class PlanarizationException extends LogicException {

	private static final long serialVersionUID = 3493217216660687045L;

	Planarization pln;
	
	public PlanarizationException(String string, Planarization pln, Exception e) {
		super(string, e);
		this.pln = pln;
	}
	
	public Planarization getPlanarization() {
		return pln;
	}

}
