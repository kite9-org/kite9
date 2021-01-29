package org.kite9.diagram.visualization.pipeline

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositionerImpl
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.mapping.ElementMapperImpl
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction.Compaction
import org.kite9.diagram.visualization.compaction.Compactor
import org.kite9.diagram.visualization.compaction.PluggableCompactor
import org.kite9.diagram.visualization.compaction.align.*
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep
import org.kite9.diagram.visualization.compaction.position.ConnectionRouteCompactionStep
import org.kite9.diagram.visualization.compaction.position.GridCellPositionCompactionStep
import org.kite9.diagram.visualization.compaction.position.RectangularPositionCompactionStep
import org.kite9.diagram.visualization.compaction.rect.HierarchicalCompactionStep
import org.kite9.diagram.visualization.compaction.rect.InnerFaceWithEmbeddingRectangularizer
import org.kite9.diagram.visualization.compaction.rect.PopOutRectangularizer
import org.kite9.diagram.visualization.compaction.slideable.DiagramSizeCompactionStep
import org.kite9.diagram.visualization.compaction.slideable.LoggingOptimisationStep
import org.kite9.diagram.visualization.compaction.slideable.MaximizeCompactionStep
import org.kite9.diagram.visualization.compaction.slideable.MinimizeCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization
import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer
import org.kite9.diagram.visualization.orthogonalization.flow.container.ContainerCornerFlowOrthogonalizer
import org.kite9.diagram.visualization.orthogonalization.vertex.ContainerContentsArranger
import org.kite9.diagram.visualization.planarization.Planarization
import org.kite9.diagram.visualization.planarization.Planarizer
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizer

/**
 * Basic pipeline that can render to any form of output.
 * Stores all the intermediate steps involved in creating the
 * diagram.  Although a lot of the steps are plug-able, this represents
 * the standard approach for arranging diagrams.
 */
abstract class AbstractArrangementPipeline : ArrangementPipeline {

    var planarizer: Planarizer? = null
    var orthogonalizer: Orthogonalizer? = null
    var compactor: Compactor? = null
    var pln: Planarization? = null
    var orth: Orthogonalization? = null
    var c: Compaction? = null
    var em: ElementMapper? = null
    var factory: DiagramElementFactory<*>? = null

    protected fun createPlanarization(d: Diagram?): Planarization {
        return createPlanarizer().planarize(d!!)
    }

    fun createPlanarizer(): Planarizer {
        planarizer = MGTPlanarizer(elementMapper)
        return planarizer!!
    }

    protected fun createOrthogonalization(p: Planarization): Orthogonalization {
        return createOrthogonalizer().createOrthogonalization(p)
    }

    abstract val displayer: CompleteDisplayer

    abstract val diagramElementFactory: DiagramElementFactory<*>

    val elementMapper: ElementMapper
        get() {
            if (em == null) {
                em = ElementMapperImpl(GridPositionerImpl(diagramElementFactory))
            }
            return em!!
        }

    fun createOrthogonalizer(): Orthogonalizer {
        val va = ContainerContentsArranger(elementMapper)
        val clc = va.getContainerLabelConverter()
        orthogonalizer = ContainerCornerFlowOrthogonalizer(
            va, clc
        )
        return orthogonalizer!!
    }

    protected fun compactOrthogonalization(o: Orthogonalization): Compaction {
        return createCompactor().compactDiagram(o)
    }

    fun createCompactor(): Compactor {
        val cd = displayer
        val steps = arrayOf(
            HierarchicalCompactionStep(cd),
            InnerFaceWithEmbeddingRectangularizer(cd),
            SubGraphInsertionCompactionStep(cd),
            PopOutRectangularizer(cd),
            SubGraphInsertionCompactionStep(cd),  //				new LoggingOptimisationStep(cd),
            MinimizeCompactionStep(cd),
            DiagramSizeCompactionStep(cd),
            LoggingOptimisationStep(cd),
            MaximizeCompactionStep(cd),  //				new LoggingOptimisationStep(cd),
            AlignmentCompactionStep(cd, arrayOf<Aligner>(LeftRightAligner(), CenteringAligner())),
            ConnectionAlignmentCompactionStep(),
            ConnectionRouteCompactionStep(),
            RectangularPositionCompactionStep(cd),
            GridCellPositionCompactionStep(),
            LoggingOptimisationStep(cd)
        )
        compactor = PluggableCompactor(steps)
        return compactor!!
    }

    protected val debug: Boolean
        protected get() = false

    override fun arrange(d: Diagram): Diagram {
        val pln = createPlanarization(d)
        this.pln = pln
        val orth = createOrthogonalization(pln)
        this.orth = orth
        val c = compactOrthogonalization(orth)
        this.c = c;
        return d
    }
}