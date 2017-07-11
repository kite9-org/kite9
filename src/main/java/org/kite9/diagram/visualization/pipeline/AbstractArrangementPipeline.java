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
import org.kite9.diagram.visualization.compaction.position.RectangularPositionCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.HierarchicalCompactionStep;
import org.kite9.diagram.visualization.compaction.rect.InnerFaceWithEmbeddingRectangularizer;
import org.kite9.diagram.visualization.compaction.rect.NonEmbeddedFaceRectangularizer;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer;
import org.kite9.diagram.visualization.compaction.slideable.CenteringAlignmentCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LeftRightAlignmentCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.LoggingOptimisationStep;
import org.kite9.diagram.visualization.compaction.slideable.MinimizeAndCenterCompactionStep;
import org.kite9.diagram.visualization.compaction.slideable.WidthCompactionStep;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.edge.ContainerLabelConverter;
import org.kite9.diagram.visualization.orthogonalization.flow.container.ContainerCornerFlowOrthogonalizer;
import org.kite9.diagram.visualization.orthogonalization.vertex.ContainerContentsArranger;
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
		ContainerContentsArranger va = new ContainerContentsArranger(getElementMapper());
		ContainerLabelConverter clc = va.getContainerLabelConverter();
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
				new MinimizeAndCenterCompactionStep(getDisplayer()),
				new WidthCompactionStep(cd), 
				new LeftRightAlignmentCompactionStep(cd),
				new CenteringAlignmentCompactionStep(cd),
				new LoggingOptimisationStep(cd),
				new ConnectionRouteCompactionStep(),
				new RectangularPositionCompactionStep(cd)
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