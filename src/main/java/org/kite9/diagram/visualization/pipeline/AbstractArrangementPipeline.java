package org.kite9.diagram.visualization.pipeline;

import org.kite9.diagram.common.elements.grid.GridPositionerImpl;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.mapping.ElementMapperImpl;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.PluggableCompactor;
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep;
import org.kite9.diagram.visualization.compaction.position.ConnectionRouteCompactionStep;
import org.kite9.diagram.visualization.compaction.position.RectangularPositionCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.HierarchicalCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.InnerFaceWithEmbeddingRectangularizer;
import org.kite9.diagram.visualization.compaction.rect.NonEmbeddedFaceRectangularizer;
import org.kite9.diagram.visualization.compaction.slideable.CenteringAlignmentCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.DiagramSizeCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LeftRightAlignmentCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LoggingOptimisationStep;
import org.kite9.diagram.visualization.compaction.slideable.MaximizeCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.MinimizeCompactionStep;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.edge.EdgeConverter;
import org.kite9.diagram.visualization.orthogonalization.flow.container.ContainerCornerFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.vertex.ContainerContentsArranger;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Planarizer;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizer;

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

	protected Planarization createPlanarization(Diagram d) {
		return createPlanarizer().planarize(d);
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
		ContainerContentsArranger va = new ContainerContentsArranger(getElementMapper());
		EdgeConverter clc = va.getContainerLabelConverter();
		orthogonalizer = new ContainerCornerFlowOrthogonalizer(
						va, clc);
		return orthogonalizer;
	} 

	protected Compaction compactOrthogonalization(Orthogonalization o) {
		return createCompactor().compactDiagram(o);
	}

	public Compactor createCompactor() {
		CompleteDisplayer cd = getDisplayer();
		CompactionStep[] steps = new CompactionStep[] {
				new HierarchicalCompactionStep(cd),
				new InnerFaceWithEmbeddingRectangularizer(cd),
				new SubGraphInsertionCompactionStep(cd),
				new NonEmbeddedFaceRectangularizer(cd),
				new SubGraphInsertionCompactionStep(cd),
//				new LoggingOptimisationStep(cd),
				new MinimizeCompactionStep(getDisplayer()),
				new DiagramSizeCompactionStep(cd), 
//				new LoggingOptimisationStep(cd),
				new MaximizeCompactionStep(cd),
//				new LoggingOptimisationStep(cd),
				new LeftRightAlignmentCompactionStep(cd),
				new CenteringAlignmentCompactionStep(cd),
				new ConnectionRouteCompactionStep(),
				new RectangularPositionCompactionStep(cd),
				new LoggingOptimisationStep(cd)
				};

		compactor = new PluggableCompactor(steps);
		return compactor;
	}
	
	protected boolean getDebug() {
		return false;
	}
		
	public Diagram arrange(Diagram d) {
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