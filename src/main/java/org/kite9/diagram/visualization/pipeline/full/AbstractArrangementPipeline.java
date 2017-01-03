package org.kite9.diagram.visualization.pipeline.full;

import org.kite9.diagram.common.algorithms.so.LoggingOptimisationStep;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.PluggableCompactor;
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep;
import org.kite9.diagram.visualization.compaction.position.OptimisablePositionerCompactionStep;
import org.kite9.diagram.visualization.compaction.position.optstep.EdgeSeparationOptimisationStep;
import org.kite9.diagram.visualization.compaction.position.optstep.LabelInsertionOptimisationStep;
import org.kite9.diagram.visualization.compaction.position.optstep.LeafElementSizeOptimisationStep;
import org.kite9.diagram.visualization.compaction.position.optstep.WidthOptimisationStep;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer;
import org.kite9.diagram.visualization.compaction.route.ConnectionRouteCompactionStep;
import org.kite9.diagram.visualization.compaction.route.EdgeRouteCompactionStep;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraphOrthBuilder;
import org.kite9.diagram.visualization.orthogonalization.flow.container.ContainerCornerFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.vertices.ContainerCornerVertexArranger;
import org.kite9.diagram.visualization.orthogonalization.vertices.VertexArrangementOrthogonalizationDecorator;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Planarizer;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizer;
import org.kite9.diagram.xml.DiagramXMLElement;

/**
 * Basic pipeline that can render to any form of output.
 * Stores all the intermediate steps involved in creating the
 * diagram.  Although a lot of the steps are plug-able, this represents
 * the standard approach for arranging diagrams.
 */
public abstract class AbstractArrangementPipeline implements ArrangementPipeline {

	Planarizer planarizer;
	Orthogonalizer orthogonalizer;
	Compactor compactor;

	Planarization pln;
	Orthogonalization orth;
	Compaction c;

	protected Planarization createPlanarization(DiagramXMLElement d) {
		return createPlanarizer().planarize(d.getDiagramElement());
	}

	public Planarizer createPlanarizer() {
		planarizer = new MGTPlanarizer();
		return planarizer;
	}

	protected Orthogonalization createOrthogonalization(Planarization p) {
		return createOrthogonalizer().createOrthogonalization(p);
	}

	public abstract CompleteDisplayer getDisplayer();

	public Orthogonalizer createOrthogonalizer() {
		Orthogonalizer basic = new ContainerCornerFlowOrthogonalizer(new MappedFlowGraphOrthBuilder(getDisplayer()));
		orthogonalizer = new VertexArrangementOrthogonalizationDecorator(basic, getDisplayer(),
 //				new FanInVertexArranger(getDisplayer()));
				
				new ContainerCornerVertexArranger(getDisplayer()));
		return orthogonalizer;
	}

	protected Compaction compactOrthogonalization(Orthogonalization o) {
		return createCompactor().compactDiagram(o);
	}

	public Compactor createCompactor() {
		CompactionStep[] steps = new CompactionStep[] {
				new PrioritizingRectangularizer(getDisplayer()),
				new SubGraphInsertionCompactionStep(getDisplayer()),
				new OptimisablePositionerCompactionStep(new OptimisationStep[] { 
						new EdgeSeparationOptimisationStep(getDisplayer()),
						new LabelInsertionOptimisationStep(getDisplayer()), 
						new WidthOptimisationStep(),
						new LeafElementSizeOptimisationStep(),
						//new LinkLengthReductionOptimisationStep(),
//						new EdgeAlignmentOptimisationStep(),
//						new SlackCenteringOptimisationStep(),
						new LoggingOptimisationStep(),
					}

				), 
				new EdgeRouteCompactionStep(), 
				new ConnectionRouteCompactionStep(),
				};

		compactor = new PluggableCompactor(steps);
		return compactor;
	}
	
	protected boolean getDebug() {
		return false;
	}
		
	public DiagramXMLElement arrange(DiagramXMLElement d) {
		
		pln = createPlanarization(d);
		orth = createOrthogonalization(pln);
		c = compactOrthogonalization(orth);
		return d;
	}

	public Planarization getPln() {
		return pln;
	}

	public Orthogonalization getOrth() {
		return orth;
	}

	public Compaction getC() {
		return c;
	}
	
	public Planarizer getPlanarizer() {
		return planarizer;
	}

	public Orthogonalizer getOrthogonalizer() {
		return orthogonalizer;
	}

	public Compactor getCompactor() {
		return compactor;
	}
	
	
}