package org.kite9.diagram.visualization.compaction2

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.Diagram
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This makes sure that any time we have all the groups to complete a container, we wrap the groups in the
 * container(s) and use that instead.
 */
abstract class AbstractC2ContainerCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2CompactionStep(cd) {

    private val containerCompletionV: MutableMap<Group, MutableList<Container>> = mutableMapOf()
    private val containerCompletionH: MutableMap<Group, MutableList<Container>> = mutableMapOf()

    fun completeContainers(c: C2Compaction, g: Group, d: Dimension) {
        val completedContainers = popCompletedContainers(d, g)
        val so = c.getSlackOptimisation(d)
        var ss = so.getSlideablesFor(g)!!

        completedContainers.forEach { container ->
            val cs = so.getSlideablesFor(container)!!

            ss = embed(so, cs, ss, d)

            // replace the group slideable sets so we use these instead
            so.add(g, ss)
        }
    }

    private fun popCompletedContainers(d: Dimension, g: Group) : List<Container> {
        return when (d) {
            Dimension.H -> containerCompletionH.remove(g) ?: emptyList()
            Dimension.V -> containerCompletionV.remove(g) ?: emptyList()
        }
    }

    private fun embed(so: C2SlackOptimisation, outer: RectangularSlideableSet, inner: RoutableSlideableSet, d: Dimension): RoutableSlideableSet {
        when (inner) {
            is RectangularSlideableSet -> embed(d, outer, inner, so, inner.d)
            is RoutableSlideableSet -> {
                // first, ensure the buffer slideables are well-separated
                so.ensureMinimumDistance(outer.l, inner.bl,0)
                so.ensureMinimumDistance(inner.br, outer.r, 0)

                // now make sure that the rectangulars composing the routable are well-separated
                inner.getRectangularSlideableSets().forEach { embed(d, outer, it, so, it.d) }
            }
        }

        return outer.wrapInRoutable(so)
    }

    override val prefix: String
        get() = "CONS"

    override val isLoggingEnabled: Boolean
        get() = true


    init {
        // collect the set of elements represented by groups,
        // mapped to those groups.
        val groupedElements = relevantElements(r.groups().first())

        // handy map of group's parents
        val parentage = mutableMapOf<Group, Group?>()
        groupParentage(r.groups().first(), null, parentage)

        // collect all the containers of these elements
        // and map them to the lowest group that represents their contents
        val relevantContainers = groupedElements.keys
            .groupBy { it?.getContainer() }
            .mapValues { (_, elements) ->
                getLowestGroup(elements
                    .mapNotNull { groupedElements[it] }
                    .toSet(), parentage )
            }

        // we need to reverse this map so that we get groups-to-containers
        relevantContainers.mapNotNull { (c, g) ->
            val listV = containerCompletionV.getOrPut(g) { mutableListOf() }
            val listH = containerCompletionH.getOrPut(g) { mutableListOf() }
            if (c != null) {
                listV.add(c)
                listH.add(c)
            }
        }

        // finally sort so that the outermost containers are done last.
        relevantContainers.values.sortedBy { - it.height }
    }

    private fun getLowestGroup(groupsIn: Set<Group>, parentage: Map<Group, Group?>) : Group {
        var groups = groupsIn
        while (groups.size > 1) {
            val lowestGroupHeight = groups.minOf { it.height }
            groups = groups
                .mapNotNull { if (it.height == lowestGroupHeight) parentage[it] else it }
                .toSet()
        }

        return groups.first()
    }

    private fun relevantElements(topGroup: Group) : Map<DiagramElement?, Group> {
        return when (topGroup) {
            is CompoundGroup -> relevantElements(topGroup.a).plus(relevantElements(topGroup.b))
            is LeafGroup -> mapOf(topGroup.connected to topGroup,
                topGroup.container to topGroup)
            else -> mapOf()
        }
    }

    private fun groupParentage(g: Group, p: Group?, parentage: MutableMap<Group, Group?>) {
        parentage[g] = p;
        if (g is CompoundGroup) {
            groupParentage(g.a, g, parentage)
            groupParentage(g.b, g, parentage)
        }
    }
}