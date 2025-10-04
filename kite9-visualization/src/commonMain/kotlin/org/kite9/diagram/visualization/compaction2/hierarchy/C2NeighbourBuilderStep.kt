package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Positioned
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

class C2NeighbourBuilderStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override fun compact(c: C2Compaction, g: Group) {
        val d = c.getDiagram()
        buildNeighbours(c, d, null, null)
        c.joinOverlappingNeighbourGroups()
    }

    private fun buildNeighbours(c: C2Compaction, d: DiagramElement, hss: RoutableSlideableSet?, vss: RoutableSlideableSet?) {
        val hso = c.getSlackOptimisation(Dimension.H)
        val vso = c.getSlackOptimisation(Dimension.V)

        if (d is Positioned) {
            val hr = hso.getSlideablesFor(d)
            val vr = vso.getSlideablesFor(d)
            if ((hr != null) && (vr != null)) {
                if ((hss != null) && (vss != null)) {
                    c.setupRectangularIntersections(hr, vr, hss, vss)
                }

                if (d is Container) {
                    val hssi = hso.getContents(hr)
                    val vssi = vso.getContents(vr)

                    d.getContents().forEach { buildNeighbours(c, it, hssi, vssi) }

                    if ((hssi != null) && (vssi != null)) {
                        c.propagateIntersectionsBetweenRoutableAndOuterRectangular(hssi, vssi, hr, vr)
                    }
                }

                val hcs = hso.getContainers(hr)
                val vcs = vso.getContainers(vr)
                val cs = hcs.flatMap { h -> vcs.map { v-> Pair(h,v) } }.toSet()

                cs.forEach { (h, v) ->
                    c.setupRoutableIntersections(h, v)
                    c.propagateIntersectionsFromRectangularToOuterRoutable(hr, vr, h, v)
                }
            }
        }
    }


    override val prefix: String
        get() = "NBCS"

    override val isLoggingEnabled: Boolean
        get() = true

}