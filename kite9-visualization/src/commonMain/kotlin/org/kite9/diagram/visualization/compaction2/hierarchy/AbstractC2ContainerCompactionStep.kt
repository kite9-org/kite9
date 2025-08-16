package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.anchors.BlockAnchor
import org.kite9.diagram.visualization.compaction2.anchors.Permeability
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
    private val allContainers : MutableList<Container> = mutableListOf()

    fun completeContainers(c: C2Compaction, g: Group, d: Dimension) {
        val completedContainers = popCompletedContainers(d, g)

        if (completedContainers.isNotEmpty()) {
            val so = c.getSlackOptimisation(d)
            val sox = c.getSlackOptimisation(d.other())
            val potentialSets = so.getSlideablesFor(g)
            var soInnerRoutable = potentialSets.first()!!   // the routable inside the container

            completedContainers.forEach { container ->
                // make sure the inner orbitals can't be crossed by intersections
                // on this container
                soInnerRoutable.bl?.addBlockAnchor(BlockAnchor(container, Side.START, Permeability.DECREASING))
                soInnerRoutable.br?.addBlockAnchor(BlockAnchor(container, Side.END, Permeability.INCREASING))

                // these are the container itself
                val soContainer = checkCreateElement(container, d, so, null, g)
                val soxContainer = checkCreateElement(container, d.other(), sox, null, g)
                ensureRectangularEmbedding(soContainer, so)
                c.setupRectangularIntersections(soContainer, soxContainer)

                // the routable inside the container
                val soxInnerRoutable = sox.getContents(soxContainer)

                if (soxInnerRoutable != null) {
                    c.setupRoutableIntersections(soxInnerRoutable, soInnerRoutable)
                    c.propagateIntersectionsRoutableWithRectangular(soInnerRoutable, soxInnerRoutable, soContainer, soxContainer)
                }

                val soOuterRoutable = embedInContainerAndWrap(c, so, soContainer, soInnerRoutable, d, g)
                if (soOuterRoutable != null) {
                    soInnerRoutable = soOuterRoutable
                }

                // routable outside the container
                val soxOuterRoutable = sox.getContainer(soxContainer)
                if (soxOuterRoutable != null) {
                    c.setupRoutableIntersections(soxOuterRoutable, soInnerRoutable)
                    c.propagateIntersectionsRoutableWithRectangular(soInnerRoutable, soxOuterRoutable, soContainer, soxContainer)
                }

            }
        }
    }


    private fun ensureRectangularEmbedding(rect: RectangularSlideableSet, so: C2SlackOptimisation) {
        val d = rect.e
        val intersects = so.getAllSlideables().filter { it.getIntersectingElements().contains(d) }
        intersects.forEach {
            val tDist = getMinimumDistanceBetween(d, Side.START, d, Side.END, rect.l.dimension, null, false)
            so.ensureMinimumDistance(rect.l, it, (tDist/2.0).toInt())
            so.ensureMinimumDistance(it, rect.r, (tDist/2.0).toInt())
        }
    }

    private fun popCompletedContainers(d: Dimension, g: Group) : List<Container> {
        return when (d) {
            Dimension.H -> containerCompletionH.remove(g) ?: emptyList()
            Dimension.V -> containerCompletionV.remove(g) ?: emptyList()
        }
    }

    private fun embedInContainerAndWrap(c: C2Compaction, so: C2SlackOptimisation, outer: RectangularSlideableSet, inner: RoutableSlideableSet, d: Dimension, g: Group): RoutableSlideableSet? {
        so.contains(outer, inner)
        val margin = getMargin(d, outer.e)

        // first, ensure the buffer slideables are well-separated
        if (inner.bl != null) so.ensureMinimumDistance(outer.l, inner.bl!!,margin.first / 2)
        if (inner.br != null) so.ensureMinimumDistance(inner.br!!, outer.r, margin.second / 2)

        // now make sure that the rectangulars composing the routable are well-separated
        so.getContents(inner).forEach { embed(d, outer, it, so, it.e) }

        val out = outer.wrapInRoutable()
        if (out != null) {
            so.contains(out, outer)
            so.add(g, out)
        }
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
            .mapValues { (c, elements) ->
                getLowestGroup(elements, parentage, Dimension.V, c)
            }

        val relevantContainersH = relevantContainers
            .mapValues { (c, elements) ->
                getLowestGroup(elements, parentage, Dimension.H, c)
            }


        // we need to reverse this map so that we get groups-to-containers
        relevantContainersV.mapNotNull { (c, g) ->
            val listV = containerCompletionV.getOrPut(g) { mutableListOf() }
            listV.add(c)
            listV.sortBy { -it.getDepth() }
        }

        // we need to reverse this map so that we get groups-to-containers
        relevantContainersH.mapNotNull { (c, g) ->
            val listH = containerCompletionH.getOrPut(g) { mutableListOf() }
            listH.add(c)
            listH.sortBy { -it.getDepth() }
        }

        allContainers.addAll(groupedContainers.keys + parentContainers)

        if (parentContainers.isNotEmpty()) {
            val topH = containerCompletionH.getOrElse(topGroup) { emptyList() }
            val topV = containerCompletionV.getOrElse(topGroup) { emptyList() }
            containerCompletionH[topGroup] = (topH + parentContainers).toMutableList()
            containerCompletionV[topGroup] = (topV + parentContainers).toMutableList()
        }
    }

    private fun collectParents(it: Container?, parentContainers: MutableList<Container>, justContainers: Set<Container>) {
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

    private fun <X : DiagramElement> containsAnyLevel(groups: Map<X, List<LeafGroup>>, c: Container) : List<LeafGroup> {
        return groups.flatMap { (de, groups) ->
            if (contains(de, c)) {
                groups
            } else {
                emptyList()
            }
        }
    }

    private fun hasOnlyCombiningAxis(groups: Set<Group>): Boolean {
        val out = (groups.size == 1) && (combiningAxis(groups.first()))
        return out
    }

    private fun getLowestGroup(groupsIn: List<LeafGroup>, parentage: Map<Group, List<Group>>, axis: Dimension, forContainer: Container) : Group {
        var groups : Set<Group> = groupsIn
            .filter { matchesAxis(axis, it) }
            .filter { !((it.container == forContainer) && (it.connected == null))}
            .toSet()

        while ((groups.size > 1) || (hasOnlyCombiningAxis(groups))) {
            val lowestGroupHeight = groups.minOf { it.height }
            groups = groups
                .flatMap { if (it.height == lowestGroupHeight) parentage[it].orEmpty() else listOf(it) }
                .filter { matchesAxis(axis, it) || combiningAxis(it) }
                .toSet()
        }

        return groups.first()
    }

    private fun <X, Y> mergeMaps(a: Map<X, List<Y>>, b: Map<X, List<Y>>) : Map<X, List<Y>> {
        return (a.keys + b.keys).associateWith {
            val aList = a[it] ?: emptyList()
            val bList = b[it] ?: emptyList()
            (aList + bList).toSet().toList()
        }
    }

    private fun relevantElements(topGroup: Group) : Map<DiagramElement, List<LeafGroup>> {
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
        }
    }

    private fun relevantContainers(topGroup: Group) : Map<Container, List<LeafGroup>> {
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
    private fun combiningAxis(g: Group) : Boolean {
        return if (g is CompoundGroup) {
            val hasChildHorizontal = horizontalAxis(g.a) || horizontalAxis(g.b)
            val hasChildVertical = verticalAxis(g.a) || verticalAxis(g.b)

            !horizontalAxis(g) && !verticalAxis(g) && hasChildHorizontal && hasChildVertical
        } else {
            false
        }
    }

    fun horizontalAxis(g: Group): Boolean {
        return g.axis.isHorizontal || g is LeafGroup
    }

    fun verticalAxis(g: Group): Boolean {
        return g.axis.isVertical || g is LeafGroup
    }

}