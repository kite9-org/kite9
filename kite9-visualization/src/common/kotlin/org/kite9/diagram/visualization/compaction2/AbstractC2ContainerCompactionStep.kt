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

        if (completedContainers.isNotEmpty()) {
            val so = c.getSlackOptimisation(d)
            var ss = so.getSlideablesFor(g)
            if (ss != null) {
                completedContainers.forEach { container ->
                    val cs = so.getSlideablesFor(container)!!

                    ss = embed(so, cs, ss!!, d)

                    // replace the group slideable sets so we use these instead
                    so.add(g, ss!!)
                }
            }
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
        val allContainers = relevantContainers(r.groups().first()).toSet()

        // handy map of group's parents
        val parentage = mutableMapOf<Group, MutableList<Group>>()
        groupParentage(r.groups().first(), null, parentage)

        // collect all the containers of these elements
        // and map them to the lowest group that represents their contents
        val relevantContainers = allContainers.associateWith {
            val o = containsAnyLevel(groupedElements, it)
            o
        }

        val relevantContainersV = relevantContainers
            .mapValues { (c, elements) ->
                getLowestGroup(elements, parentage, Dimension.V, c )
            }

        val relevantContainersH = relevantContainers
            .mapValues { (c, elements) ->
                getLowestGroup(elements, parentage, Dimension.H, c )
            }


        // we need to reverse this map so that we get groups-to-containers
        relevantContainersV.mapNotNull { (c, g) ->
            if (g != null) {
                val listV = containerCompletionV.getOrPut(g) { mutableListOf() }
                if (c != null) {
                    listV.add(c)
                    listV.sortBy { -it.getDepth() }
                }
            }
        }

        // we need to reverse this map so that we get groups-to-containers
        relevantContainersH.mapNotNull { (c, g) ->
            if (g != null) {
                val listH = containerCompletionH.getOrPut(g) { mutableListOf() }
                if (c != null) {
                    listH.add(c)
                    listH.sortBy { -it.getDepth() }
                }
            }
        }
    }

    private fun contains(e: DiagramElement?, c: Container) : Boolean {
        return if (e==null) {
            false;
        } else if (e == c) {
            return true;
        } else {
            return contains(e.getParent(), c)
        }
    }

    private fun containsAnyLevel(groups: Map<DiagramElement?, List<Group>>, c: Container) : List<Group> {
        return groups.flatMap { (de, groups) ->
            if (contains(de, c)) {
                groups
            } else {
                emptyList<Group>()
            }
        }
    }

    private fun getLowestGroup(groupsIn: List<Group>, parentage: Map<Group, List<Group>>, axis: Dimension, container: Container?) : Group? {
        var groups = groupsIn.filter { matchesAxis(axis, it) }.toSet()

        while (groups.size > 1) {
            val lowestGroupHeight = groups.minOf { it.height }
            groups = groups
                .flatMap { if (it.height == lowestGroupHeight) parentage[it].orEmpty() else listOf(it) }
                .filter { matchesAxis(axis, it) }
                .toSet()
        }

        return groups.firstOrNull()
    }

    private fun relevantElements(topGroup: Group) : Map<DiagramElement?, List<Group>> {
        return when (topGroup) {
            is CompoundGroup -> relevantElements(topGroup.a).plus(relevantElements(topGroup.b))
            is LeafGroup -> if (topGroup.connected != null) {
                mapOf(topGroup.connected to listOf(topGroup))
            } else {
                mapOf()
            }
            else -> mapOf()
        }
    }

    private fun relevantContainers(topGroup: Group) : List<Container> {
        return when (topGroup) {
            is CompoundGroup -> relevantContainers(topGroup.a).plus(relevantContainers(topGroup.b))
            is LeafGroup -> if (topGroup.container != null) {
                listOf(topGroup.container!!)
            } else {
                emptyList()
            }
            else -> emptyList()
        }
    }

    private fun groupParentage(g: Group, p: Group?, parentage: MutableMap<Group, MutableList<Group>>) {
        val parents = parentage.getOrPut(g) { mutableListOf() }

        if (p != null) {
            parents.add(p)
        }

        if (g is CompoundGroup) {
            groupParentage(g.a, g, parentage)
            groupParentage(g.b, g, parentage)
        }
    }

    private fun matchesAxis(axis: Dimension, g: Group) : Boolean {
        return when (axis) {
            Dimension.V -> verticalAxis(g)
            Dimension.H -> horizontalAxis(g)
        }
    }
    fun combiningAxis(g: CompoundGroup) : Boolean {
        val hasChildHorizontal = horizontalAxis(g.a) || horizontalAxis(g.b)
        val hasChildVertical = verticalAxis(g.a) || verticalAxis(g.b)

        return !horizontalAxis(g) && !verticalAxis(g) && hasChildHorizontal && hasChildVertical
    }

    fun horizontalAxis(g: Group): Boolean {
        return g.axis.isHorizontal || g is LeafGroup
    }

    fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical || g is LeafGroup
    }

}