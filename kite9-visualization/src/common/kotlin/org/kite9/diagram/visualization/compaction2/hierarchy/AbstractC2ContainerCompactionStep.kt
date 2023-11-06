package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.*
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

    private val containerCompletion: MutableMap<Group, MutableList<Container>> = mutableMapOf()

    override fun compact(c: C2Compaction, g: Group) {
        val completedContainers = containerCompletion[g]
        val sov = c.getSlackOptimisation(Dimension.V)
        var ssv = sov.getSlideablesFor(g)!!

        val soh = c.getSlackOptimisation(Dimension.H)
        var ssh = soh.getSlideablesFor(g)!!

        completedContainers?.forEach { container ->
            val csv = sov.getSlideablesFor(container)!!
            val csh = soh.getSlideablesFor(container)!!
            ssv = embed(sov, csv, ssv, Dimension.V)
            ssh = embed(soh, csh, ssh, Dimension.H)

            // replace the group slideable sets so we use these instead
            sov.add(g, ssv)
            soh.add(g, ssh)
        }
    }

    private fun embed(so: C2SlackOptimisation, outer: RectangularSlideableSet, inner: RoutableSlideableSet, d: Dimension): RoutableSlideableSet {
        embed(d, outer, inner, so)
        return outer.wrapInRoutable(so)

//        when (inner) {
//            is RectangularSlideableSet -> embed(d, outer, inner, so)
//            is RoutableSlideableSet -> {
//                // first, ensure the buffer slideables are well-separated
//                so.ensureMinimumDistance(outer.l, inner.bl,0)
//                so.ensureMinimumDistance(inner.br, outer.r, 0)
//
//                // now make sure that the rectangulars composing the routable are well-separated
//                inner.getRectangularSlideableSets().forEach { embed(d, outer, it, so) }
//            }
//        }

//        return outer
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
            val list = containerCompletion.getOrPut(g) { mutableListOf() }
            if (c != null) {
                list.add(c)
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