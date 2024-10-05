package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

/**
 * This makes sure that any time we have all the groups to complete a container, we wrap the groups in the
 * container(s) and use that instead.
 */
abstract class AbstractC2ContainerCompactionStep(cd: CompleteDisplayer, r: GroupResult) : AbstractC2BuilderCompactionStep(cd) {

    private val containerCompletionV: MutableMap<Group, MutableList<Container>> = mutableMapOf()
    private val containerCompletionH: MutableMap<Group, MutableList<Container>> = mutableMapOf()
    val allContainers : MutableList<Container> = mutableListOf()

    fun completeContainers(c: C2Compaction, g: Group, d: Dimension) {
        val completedContainers = popCompletedContainers(d, g)

        if (completedContainers.isNotEmpty()) {
            val so = c.getSlackOptimisation(d)
            val sox = c.getSlackOptimisation(d.other())
            var ss = so.getSlideablesFor(g)!!
            var ssx = sox.getSlideablesFor(g)
            completedContainers.forEach { container ->
                val cs = checkCreateElement(container, d, so, null, g)!!
                val csx = checkCreateElement(container, d.other(), sox, null, g)!!
                ss = embed(c, so, cs, ss, d, g)
                c.setupContainerIntersections(ss, csx)

                // replace the group slideable sets so we use these instead
                so.add(g, ss)

                if (ssx != null) {
                    c.setupRoutableIntersections(ss, ssx)
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

    private fun embed(c: C2Compaction, so: C2SlackOptimisation, outer: RectangularSlideableSet, inner: RoutableSlideableSet, d: Dimension, g: Group): RoutableSlideableSet {

        // first, ensure the buffer slideables are well-separated
        if (inner.bl != null) so.ensureMinimumDistance(outer.l, inner.bl!!,0)
        if (inner.br != null) so.ensureMinimumDistance(inner.br!!, outer.r, 0)

        // now make sure that the rectangulars composing the routable are well-separated
        so.getContents(inner).forEach { embed(d, outer, it, so, it.d) }

        // ensure that the intersections of the inner routable also intersect the outer
        c.propagateIntersections(inner, outer)

        val lg = if (g is LeafGroup) g else null
        val out = outer.wrapInRoutable(so, lg)
        so.contains(out, outer)
        return out
    }

    override val prefix: String
        get() = "CONS"

    override val isLoggingEnabled: Boolean
        get() = true


    init {
        // collect the set of elements represented by groups,
        // mapped to those groups.
        val topGroup = r.groups().first()
        val groupedElements = relevantElements(topGroup)
        val groupedContainers = relevantContainers(topGroup)

        val parentContainers: MutableList<Container> = mutableListOf()
        val justContainers = groupedContainers.keys

        justContainers.forEach { collectParents(it, parentContainers, justContainers ) }

        // handy map of group's parents
        val parentage = mutableMapOf<Group, MutableList<Group>>()
        groupParentage(r.groups().first(), null, parentage)

        // collect all the containers of these elements
        // and map them to the lowest group that represents their contents
        val relevantContainers = groupedContainers.keys.associateWith {
            val n = containsAnyLevel(groupedContainers, it)
            val o = containsAnyLevel(groupedElements, it)
            (o + n).distinct()
        }

        val relevantContainersV = relevantContainers
            .mapValues { (_, elements) ->
                getLowestGroup(elements, parentage, Dimension.V)
            }

        val relevantContainersH = relevantContainers
            .mapValues { (_, elements) ->
                getLowestGroup(elements, parentage, Dimension.H)
            }


        // we need to reverse this map so that we get groups-to-containers
        relevantContainersV.mapNotNull { (c, g) ->
            if (g != null) {
                val listV = containerCompletionV.getOrPut(g) { mutableListOf() }
                listV.add(c)
                listV.sortBy { -it.getDepth() }
            }
        }

        // we need to reverse this map so that we get groups-to-containers
        relevantContainersH.mapNotNull { (c, g) ->
            if (g != null) {
                val listH = containerCompletionH.getOrPut(g) { mutableListOf() }
                listH.add(c)
                listH.sortBy { -it.getDepth() }
            }
        }

        allContainers.addAll(groupedContainers.keys + parentContainers)

        if (parentContainers.isNotEmpty()) {
            val topH = containerCompletionH.getOrElse(topGroup) { emptyList<Container>() }
            val topV = containerCompletionV.getOrElse(topGroup) { emptyList<Container>() }
            containerCompletionH.put(topGroup, (topH + parentContainers).toMutableList())
            containerCompletionV.put(topGroup, (topV + parentContainers).toMutableList())
        }
    }

    fun collectParents(it: Container?, parentContainers: MutableList<Container>, justContainers: Set<Container>) {
        if (it != null) {
            if ((!justContainers.contains(it)) && (!parentContainers.contains(it))) {
                parentContainers.add(it)
            }

            collectParents(it.getContainer(), parentContainers, justContainers)
        }
    }


    private fun contains(e: DiagramElement?, c: Container) : Boolean {
        return when (e) {
            null -> false
            c -> true
            else -> contains(e.getParent(), c)
        }
    }

    private fun <X : DiagramElement> containsAnyLevel(groups: Map<X, List<Group>>, c: Container) : List<Group> {
        return groups.flatMap { (de, groups) ->
            if (contains(de, c)) {
                groups
            } else {
                emptyList()
            }
        }
    }

    private fun getLowestGroup(groupsIn: List<Group>, parentage: Map<Group, List<Group>>, axis: Dimension) : Group? {
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

    private fun <X, Y> mergeMaps(a: Map<X, List<Y>>, b: Map<X, List<Y>>) : Map<X, List<Y>> {
        return (a.keys + b.keys).associateWith {
            val aList = a[it] ?: emptyList()
            val bList = b[it] ?: emptyList()
            (aList + bList).toSet().toList()
        }
    }

    private fun relevantElements(topGroup: Group) : Map<DiagramElement, List<Group>> {
        return when (topGroup) {
            is CompoundGroup -> mergeMaps(relevantElements(topGroup.a),(relevantElements(topGroup.b)))
            is LeafGroup -> {
                val c = topGroup.connected
                if (c != null) {
                    mapOf(c to listOf(topGroup))
                } else {
                    mapOf()
                }
            }
            else -> mapOf()
        }
    }

    private fun relevantContainers(topGroup: Group) : Map<Container, List<Group>> {
        return when (topGroup) {
            is CompoundGroup -> mergeMaps(relevantContainers(topGroup.a),(relevantContainers(topGroup.b)))
            is LeafGroup -> {
                val c = topGroup.container
                if (c != null) {
                    mapOf(c to listOf(topGroup))
                } else {
                    emptyMap()
                }
            }
            else -> emptyMap()
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
        return g.axis.isHorizontal || g is LeafGroup || g.isActive()
    }

    fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical || g is LeafGroup || g.isActive()
    }

}