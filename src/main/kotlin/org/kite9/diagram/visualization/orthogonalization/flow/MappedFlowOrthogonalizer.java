package org.kite9.diagram.visualization.orthogonalization.flow;

import org.kite9.diagram.visualization.orthogonalization.AbstractOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.vertex.VertexArranger;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.logging.LogicException;

/**
 * Uses a flow algorithm to convert from a Planarization to an Orthogonalization.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class MappedFlowOrthogonalizer extends AbstractOrthogonalizer implements Orthogonalizer {
	
	VertexArranger va;
	EdgeConverter clc;
	
	public MappedFlowOrthogonalizer(VertexArranger va, EdgeConverter clc) {
		super();
		this.va = va;
		this.clc = clc;
	}
	
	public abstract MappedFlowGraph createOptimisedFlowGraph(Planarization pln) ;
	
	public Orthogonalization createOrthogonalization(Planarization pln) {
		try {
			MappedFlowGraph fg = createOptimisedFlowGraph(pln);
			OrthBuilder fb = new MappedFlowGraphOrthBuilder(va, fg, clc);
			OrthogonalizationImpl orth = fb.build(pln);			
			return orth;
		} catch (LogicException le) {
			log.send("Plan: "+pln);
			throw le;
		}
	}


	protected void checkFlows(MappedFlowGraph fg) {
	}

	
}
