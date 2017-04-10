package org.kite9.diagram.visualization.pipeline;

import org.kite9.diagram.common.elements.grid.GridPositionerImpl;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.ElementMapperImpl;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.PluggableCompactor;
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep;
import org.kite9.diagram.visualization.compaction.position.ConnectionRouteCompactionStep;
import org.kite9.diagram.visualization.compaction.position.EdgeRouteCompactionStep;
import org.kite9.diagram.visualization.compaction.position.RectangularPositionCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.HierarchicalCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.PrimitiveRectangleCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer;
import org.kite9.diagram.visualization.compaction.slideable.EdgeSeparationCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LeafElementSizeCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LoggingOptimisationStep;
import org.kite9.diagram.visualization.compaction.slideable.MinimizeCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.WidthCompactionStep;
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
import org.kite9.framework.xml.DiagramKite9XMLElement;

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
	ElementMapper em;

	protected Planarization createPlanarization(DiagramKite9XMLElement d) {
		return createPlanarizer().planarize(d.getDiagramElement());
	}

	public Planarizer createPlanarizer() {
		planarizer = new MGTPlanarizer(getElementMapper());
		return planarizer;
	}

	protected Orthogonalization createOrthogonalization(Planarization p) {
		return createOrthogonalizer().createOrthogonalization(p);
	}

	public abstract CompleteDisplayer getDisplayer();
	
	public ElementMapper getElementMapper() {
		if (em == null) {
			em = new ElementMapperImpl(new GridPositionerImpl());
		}
		
		return em;
	}

	public Orthogonalizer createOrthogonalizer() {
		Orthogonalizer basic = new ContainerCornerFlowOrthogonalizer(new MappedFlowGraphOrthBuilder(getDisplayer()));
		orthogonalizer = new VertexArrangementOrthogonalizationDecorator(basic,
 //				new FanInVertexArranger(getDisplayer()));
				
				new ContainerCornerVertexArranger(getDisplayer(), getElementMapper()));
		return orthogonalizer;
	}

	protected Compaction compactOrthogonalization(Orthogonalization o) {
		return createCompactor().compactDiagram(o);
	}

	public Compactor createCompactor() {
		CompactionStep[] steps = new CompactionStep[] {
				new HierarchicalCompactionStep(),
				new PrimitiveRectangleCompactionStep(getDisplayer()),
				new PrioritizingRectangularizer(getDisplayer()),
				new SubGraphInsertionCompactionStep(getDisplayer()),
				new MinimizeCompactionStep(getDisplayer()),
//				new EdgeSeparationCompactionStep(getDisplayer()),
////						new LabelInsertionOptimisationStep(getDisplayer()), 
//				new LeafElementSizeCompactionStep(getDisplayer()),
				
//						//new LinkLengthReductionOptimisationStep(),
////						new EdgeAlignmentOptimisationStep(),
////						new SlackCenteringOptimisationStep(),
				new LoggingOptimisationStep(getDisplayer()),
//					}
//
//				), 
				new WidthCompactionStep(getDisplayer()), 
				new LoggingOptimisationStep(getDisplayer()),
				new EdgeRouteCompactionStep(), 
				new ConnectionRouteCompactionStep(),
				new RectangularPositionCompactionStep(getDisplayer())
				};

		compactor = new PluggableCompactor(steps);
		return compactor;
	}
	
	protected boolean getDebug() {
		return false;
	}
		
	public DiagramKite9XMLElement arrange(DiagramKite9XMLElement d) {
		
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