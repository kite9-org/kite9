package org.kite9.diagram.visualization.orthogonalization.flow;

import org.kite9.diagram.common.algorithms.fg.FlowGraph;
import org.kite9.diagram.visualization.orthogonalization.AbstractOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.OrthogonalizationImpl;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.framework.logging.LogicException;

/**
 * Uses a flow algorithm to convert from a Planarization to an Orthogonalization.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public abstract class FlowOrthogonalizer<X extends FlowGraph> extends AbstractOrthogonalizer implements Orthogonalizer {

	OrthBuilder<X> fb;
	
	public FlowOrthogonalizer(OrthBuilder<X> fb) {
		super();
		this.fb = fb;
	}
	
	public abstract X createOptimisedFlowGraph(Planarization pln) ;
	
	public Orthogonalization createOrthogonalization(Planarization pln) {
		try {
			X fg = createOptimisedFlowGraph(pln);
			OrthogonalizationImpl orth = fb.build(pln, fg);
			createDartOrdering(pln, orth);
			if (orth.cornerDarts.size()>0) {
				// this is not necessary for the Tamassia algorithm, so ignore.
				orderDartsFromCorner(orth.cornerDarts, orth);
			}
			
			return orth;
		} catch (LogicException le) {
			log.send("Plan: "+pln);
			throw le;
		}
	}


	protected void checkFlows(X fg) {
	}

	
}
