package org.kite9.diagram.visualization.pipeline

import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositionerImpl
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.elements.mapping.ElementMapperImpl
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction2.C2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2PluggableCompactor
import org.kite9.diagram.visualization.compaction2.align.C2AlignmentCompactionStep
import org.kite9.diagram.visualization.compaction2.align.C2CenteringAligner
import org.kite9.diagram.visualization.compaction2.align.C2LeftRightAligner
import org.kite9.diagram.visualization.compaction2.hierarchy.C2HierarchicalCompactionStep
import org.kite9.diagram.visualization.compaction2.logging.C2LoggingCompactionStep
import org.kite9.diagram.visualization.compaction2.hierarchy.C2RectangularPositionCompactionStep
import org.kite9.diagram.visualization.compaction2.labels.C2ContainerLabelCompactionStep
import org.kite9.diagram.visualization.compaction2.routing.C2ConnectionPositionCompactionStep
import org.kite9.diagram.visualization.compaction2.routing.C2ConnectionRouterCompactionStep
import org.kite9.diagram.visualization.compaction2.sizing.C2DiagramSizeCompactionStep
import org.kite9.diagram.visualization.compaction2.sizing.C2MaximizeCompactionStep
import org.kite9.diagram.visualization.compaction2.sizing.C2MinimizeCompactionStep
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader
import org.kite9.diagram.visualization.planarization.rhd.Util
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.*
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AxisHandlingGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategyImpl
import org.kite9.diagram.visualization.planarization.rhd.layout.DirectionLayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.LayoutStrategy
import org.kite9.diagram.visualization.planarization.rhd.layout.MostNetworkedFirstLayoutQueue
import org.kite9.diagram.visualization.planarization.rhd.links.BasicContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D

class NGArrangementPipeline(private val diagramElementFactory: DiagramElementFactory<*>,
                            private val displayer: CompleteDisplayer) : ArrangementPipeline, Logable {

    private val log = Kite9Log.instance(this)

    var em: ElementMapper? = null
    private var mr: GroupResult? = null
    private var rr: RoutableReader? = null

    override fun arrange(d: Diagram): Diagram {
        val mr = buildGrouping(d)
        this.mr = mr

        if (mr.groups().size > 1) {
            throw LogicException("Should end up with a single group")
        }

        layout(mr)
        val compactor = createCompactor(mr)
        compactor.compactDiagram(d, mr)
        return d
    }

    private fun layout(mr: GroupResult) {
        val topGroup: Group = mr.groups().iterator().next()
        val routableReader = PositionRoutableHandler2D()
        val layout: LayoutStrategy = DirectionLayoutStrategy(routableReader)
        layout.layout(mr, MostNetworkedFirstLayoutQueue(topGroup.groupNumber))
        outputGroupInfo(topGroup, 1, routableReader)
        this.rr = routableReader
    }

    private fun outputGroupInfo(g: Group, spc: Int, rr: RoutableHandler2D) {
        val sb: StringBuilder = StringBuilder(spc)
        for (i in 0 until spc) {
            sb.append(" ")
        }
        val line = if (g is CompoundGroup) {
            val axis = g.axis as DirectedGroupAxis
            val l: Layout? = g.layout
            val h = if (g.axis.isHorizontal) "h" else " "
            val v = if (g.axis.isVertical) "v" else " "
            (sb.toString() + g.groupNumber +
                    " " + axis
                    + "   " + rr.getPlacedPosition(g) + "  " + l + " $h $v " + (if (g.axis.isLayoutRequired) "LR " else " ")
                    + (g.a.groupNumber).toString() + " " + (g.b.groupNumber))
        } else {
            (sb.toString() + g.groupNumber +
                    " " + g.toString())
        }
        log.send(line)
        AxisHandlingGroupingStrategy.LAST_MERGE_DEBUG += line + "\n"
        if (g is AbstractCompoundGroup) {
            outputGroupInfo(g.a, spc + 1, rr)
            outputGroupInfo(g.b, spc + 1, rr)
        }
    }

    private val elementMapper: ElementMapper
        get() {
            if (em == null) {
                em = ElementMapperImpl(GridPositionerImpl(diagramElementFactory))
            }
            return em!!
        }

    private fun buildGrouping(d: Diagram) : GroupResult {
        val elements: Int = Util.countConnectedElements(d)
        val ch: ContradictionHandler = BasicContradictionHandler(elementMapper)
        val strategy = GeneratorBasedGroupingStrategyImpl(d, elements, ch, elementMapper.getGridPositioner(), elementMapper, diagramElementFactory)
        strategy.buildInitialGroups()
        return strategy.group()
   }

    private fun createCompactor(mr: GroupResult): C2PluggableCompactor {
        val cd = displayer
//
//            GridCellPositionCompactionStep(),
//
        // essential compaction steps
        val gp = elementMapper.getGridPositioner()
        val steps = arrayOf<C2CompactionStep>(
            C2HierarchicalCompactionStep(cd, mr),
            C2LoggingCompactionStep(cd),
            C2ConnectionRouterCompactionStep(cd, gp),
            C2ContainerLabelCompactionStep(cd),
            C2MinimizeCompactionStep(cd),
            C2LoggingCompactionStep(cd),
            C2DiagramSizeCompactionStep(cd),
            C2LoggingCompactionStep(cd),
            C2MaximizeCompactionStep(cd),
            C2AlignmentCompactionStep(cd, arrayOf(C2LeftRightAligner(), C2CenteringAligner())),
            C2LoggingCompactionStep(cd),
            C2RectangularPositionCompactionStep(cd),
            C2ConnectionPositionCompactionStep(cd)
        )

        return C2PluggableCompactor(steps)
    }

    override val prefix: String
        get() = "NGA "

    override val isLoggingEnabled: Boolean
        get() = true

    fun getGrouping(): GroupResult? {
        return mr;
    }

    fun getRoutableReader(): RoutableReader? {
        return rr;
    }
}
