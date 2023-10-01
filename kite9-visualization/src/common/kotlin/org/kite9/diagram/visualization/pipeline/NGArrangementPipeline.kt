package org.kite9.diagram.visualization.pipeline

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositionerImpl
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.mapping.ElementMapperImpl
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.visualization.compaction.align.AlignmentCompactionStep
import org.kite9.diagram.visualization.compaction.align.CenteringAligner
import org.kite9.diagram.visualization.compaction.align.ConnectionAlignmentCompactionStep
import org.kite9.diagram.visualization.compaction.align.LeftRightAligner
import org.kite9.diagram.visualization.compaction.hierarchy.HierarchicalCompactionStep
import org.kite9.diagram.visualization.compaction.insertion.SubGraphInsertionCompactionStep
import org.kite9.diagram.visualization.compaction2.C2PluggableCompactor
import org.kite9.diagram.visualization.compaction.position.ConnectionRouteCompactionStep
import org.kite9.diagram.visualization.compaction.position.GridCellPositionCompactionStep
import org.kite9.diagram.visualization.compaction.position.RectangularPositionCompactionStep
import org.kite9.diagram.visualization.compaction.rect.first.InnerFaceWithEmbeddingRectangularizer
import org.kite9.diagram.visualization.compaction.rect.second.popout.PopOutRectangularizer
import org.kite9.diagram.visualization.compaction.slideable.DiagramSizeCompactionStep
import org.kite9.diagram.visualization.compaction.slideable.LoggingOptimisationStep
import org.kite9.diagram.visualization.compaction.slideable.MaximizeCompactionStep
import org.kite9.diagram.visualization.compaction.slideable.MinimizeCompactionStep
import org.kite9.diagram.visualization.compaction2.C2CompactionStep
import org.kite9.diagram.visualization.compaction2.hierarchy.C2HierarchicalCompactionStep
import org.kite9.diagram.visualization.compaction2.position.C2RectangularPositionCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.Util
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategyImpl
import org.kite9.diagram.visualization.planarization.rhd.layout.DirectionLayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.LayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.MostNetworkedFirstLayoutQueue
import org.kite9.diagram.visualization.planarization.rhd.links.BasicContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D

class NGArrangementPipeline(private val diagramElementFactory: DiagramElementFactory<*>,
                            private val displayer: CompleteDisplayer) : ArrangementPipeline, Logable {

    private val log = Kite9Log.instance(this)

    var em: ElementMapper? = null

    override fun arrange(d: Diagram): Diagram {
        val mr = buildGrouuping(d)

        if (!log.go()) {
            log.send("Created Groups:", mr.groups())
        }

        if (mr.groups().size > 1) {
            throw LogicException("Should end up with a single group")
        }

        if (!log.go()) {
            //outputGroupInfo(topGroup, 0)
        }

        layout(mr)

        val compactor = createCompactor(mr)
        compactor.compactDiagram(d)

    }

    private fun layout(mr: GroupResult) {
        val topGroup: Group = mr.groups().iterator().next()
        val routableReader = PositionRoutableHandler2D()
        val layout: LayoutStrategy = DirectionLayoutStrategy(routableReader)
        layout.layout(mr, MostNetworkedFirstLayoutQueue(topGroup.groupNumber))
    }

    private val elementMapper: ElementMapper
        get() {
            if (em == null) {
                em = ElementMapperImpl(GridPositionerImpl(diagramElementFactory))
            }
            return em!!
        }

    private fun buildGrouuping(d: Diagram) : GroupResult {
        val elements: Int = Util.countConnectedElements(d)
        val ch: ContradictionHandler = BasicContradictionHandler(em)
        val strategy = GeneratorBasedGroupingStrategyImpl(d, elements, ch, elementMapper.getGridPositioner(), elementMapper, diagramElementFactory)
        strategy.buildInitialGroups()
        val mr = strategy.group()
        return mr;
   }

    private fun createCompactor(mr: GroupResult): C2PluggableCompactor {
        val cd = displayer
//        val steps = arrayOf(
//            HierarchicalCompactionStep(cd),
//            InnerFaceWithEmbeddingRectangularizer(cd),
//            SubGraphInsertionCompactionStep(cd),
//            PopOutRectangularizer(cd),
//            SubGraphInsertionCompactionStep(cd),  //				new LoggingOptimisationStep(cd),
//            MinimizeCompactionStep(cd),
//            DiagramSizeCompactionStep(cd),
//            LoggingOptimisationStep(cd),
//            MaximizeCompactionStep(cd),  //				new LoggingOptimisationStep(cd),
//            AlignmentCompactionStep(cd, LeftRightAligner(), CenteringAligner()),
//            ConnectionAlignmentCompactionStep(),
//            ConnectionRouteCompactionStep(),
//            RectangularPositionCompactionStep(cd),
//            GridCellPositionCompactionStep(),
//            LoggingOptimisationStep(cd)
//        )

        // essential compaction steps
        val steps = arrayOf<C2CompactionStep>(
            C2HierarchicalCompactionStep(mr, cd),
            C2RectangularPositionCompactionStep(cd),
        )

        return C2PluggableCompactor(steps)
    }

    override val prefix: String?
        get() = "NGA "

    override val isLoggingEnabled: Boolean
        get() = true
}