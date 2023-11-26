package org.kite9.diagram.visualization.compaction2.builders

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.*
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This turns the diagram's hierarchical structure of DiagramElement's into C2Slideables.
 *
 * @author robmoffat
 */
class C2ContainerBuilderCompactionStep(cd: CompleteDisplayer) : AbstractC2CompactionStep(cd) {

    override val prefix: String
        get() = "CBCS"

    override val isLoggingEnabled: Boolean
        get() = true

    var firstGroup = true;

    override fun compact(c: C2Compaction, g: Group) {
        if (firstGroup) {
            // we only need do this once for the whole group structure
            checkCreate(c.getDiagram(), Dimension.H, c.getSlackOptimisation(Dimension.H), null, g)
            checkCreate(c.getDiagram(), Dimension.V, c.getSlackOptimisation(Dimension.V), null, g)
            firstGroup = false
        }
    }

    private fun checkCreate(
        de: DiagramElement,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2BufferSlideable?,
        topGroup: Group
    ): RectangularSlideableSet? {
        log.send("Creating $de")
        if (de !is Rectangular) {
            return null
        }

        var ss = cso.getSlideablesFor(de)

        if (ss == null) {
            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)

            val l = C2RectangularSlideable(cso, d, de, Side.START)
            val r = C2RectangularSlideable(cso, d, de, Side.END)
            val c = cExisting ?: C2BufferSlideable(cso, d, setOf(), listOf(de))
            cso.ensureMinimumDistance(l, r, ms.toInt())

            ss = RectangularSlideableSetImpl(de, l, r, c)
            ensureCentreSlideablePosition(cso, ss)
            cso.add(de, ss)

            if (de is Container) {
                checkCreateItems(cso, de, d, de.getLayout(), ss, topGroup)
            }

            log.send("Created RectangularSlideableSetImpl: ${ss.d}", ss.getAll())
        }

        return ss
    }

    private fun checkCreateItems(
        cso: C2SlackOptimisation,
        de: Container,
        d: Dimension,
        l: Layout?,
        container: RectangularSlideableSet,
        topGroup: Group
    ) {

        val contents = de.getContents()
            .filterIsInstance<ConnectedRectangular>()

        val centerLine = when (l) {
            Layout.LEFT, Layout.RIGHT, Layout.HORIZONTAL -> if (d == Dimension.V) C2BufferSlideable(
                cso,
                d,
                setOf(),
                listOf(de)
            ) else null

            Layout.UP, Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.H) C2BufferSlideable(
                cso,
                d,
                setOf(),
                listOf(de)
            ) else null

            else -> null
        }

        val contentMap = contents.map { it to checkCreate(it, d, cso, centerLine, topGroup) }

        // ensure within container
        contentMap.forEach { (e, v) -> embed(d, container, v, cso, e) }

        // ensure internal ordering
        if (!usingGroups(contents, topGroup)) {
            when (l) {
                Layout.RIGHT, Layout.HORIZONTAL, null -> if (d == Dimension.H) setupInternalOrdering(contentMap, d, cso)
                Layout.LEFT -> if (d == Dimension.H) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.V) setupInternalOrdering(contentMap, d, cso)
                Layout.UP -> if (d == Dimension.V) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.GRID -> {
                    // grid, don't handle this yet
                }
            }
        } else {
            log.send("Using groups: $de")
        }
    }

    /**
     * Either content are laid out using the Group process, or they aren't connected so we need
     * to just follow layout.
     */
    private fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group) : Boolean {
        val gm = contents.map { hasGroup(it, topGroup) }
        return gm.reduceRight {  a, b -> a && b };
    }

    private fun hasGroup(item: Connected, group: Group) : Boolean {
        if (group is CompoundGroup) {
            return hasGroup(item, group.a) || hasGroup(item, group.b)
        } else if (group is LeafGroup){
            return (group.container == item) || (group.connected == item);
        } else {
            return false;
        }
    }

    private fun setupInternalOrdering(
        orderedContents: List<Pair<Rectangular, RectangularSlideableSet?>>,
        d: Dimension,
        cso: C2SlackOptimisation
    ) {
        for (i in 1 until orderedContents.size) {
            val prev = orderedContents[i - 1]
            val current = orderedContents[i]
            val dist = getMinimumDistanceBetween(prev.first, Side.END, current.first, Side.START, d, null, true)
            separateRectangular(prev.second!!, Side.END, current.second!!, Side.START, cso, dist)
        }
    }



}