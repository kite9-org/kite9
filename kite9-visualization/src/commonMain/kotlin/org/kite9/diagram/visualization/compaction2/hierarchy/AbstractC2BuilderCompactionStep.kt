package org.kite9.diagram.visualization.compaction2.hierarchy

import org.kite9.diagram.common.elements.Dimension
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.AlignedRectangular
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.PlacementPositioned
import org.kite9.diagram.model.Port
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.model.style.BorderTraversal
import org.kite9.diagram.model.style.ContainerPosition
import org.kite9.diagram.model.style.GridContainerPosition
import org.kite9.diagram.model.style.Placement
import org.kite9.diagram.visualization.compaction.Side
import org.kite9.diagram.visualization.compaction2.AbstractC2CompactionStep
import org.kite9.diagram.visualization.compaction2.C2Compaction
import org.kite9.diagram.visualization.compaction2.C2SlackOptimisation
import org.kite9.diagram.visualization.compaction2.C2Slideable
import org.kite9.diagram.visualization.compaction2.anchors.IntersectAnchor
import org.kite9.diagram.visualization.compaction2.anchors.Permeability
import org.kite9.diagram.visualization.compaction2.anchors.Purpose
import org.kite9.diagram.visualization.compaction2.anchors.RectAnchor
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RectangularSlideableSetImpl
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSet
import org.kite9.diagram.visualization.compaction2.sets.RoutableSlideableSetImpl
import org.kite9.diagram.visualization.display.CompleteDisplayer
import org.kite9.diagram.visualization.planarization.rhd.grouping.TemporaryContainerHub
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup

abstract class AbstractC2BuilderCompactionStep(cd: CompleteDisplayer, val gp: GridPositioner) : AbstractC2CompactionStep(cd) {

    private val gridRectSlideables = mutableMapOf<Triple<Container, Int, Dimension>, C2Slideable>()
    private val gridIntersectSlideables = mutableMapOf<Triple<Container, Int, Dimension>, C2Slideable>()

    /**
     * This is used to create a RectangularSlideableSet from a diagram element
     * where the element doesn't have a Group
     */
    protected fun checkCreateElement(
        de: Rectangular,
        d: Dimension,
        cso: C2SlackOptimisation,
        cExisting: C2Slideable?,
        topGroup: Group?,
    ): RectangularSlideableSet {

        fun addToGridRectSlideables(c: Container,
                                    lineNumber: Int,
                                    d: Dimension,
                                    s: Side,
                                    p: Permeability) : C2Slideable {
            val key = Triple(c, lineNumber, d)
            val existing = gridRectSlideables.get(key)
            if (existing == null) {
                val new = C2Slideable(cso, d, de, s, p)
                gridRectSlideables[key] = new
                return new
            } else {
                // just add the anchor
                existing.addRectAnchor(RectAnchor(de, s, p))
                return existing
            }
        }

        var ss = cso.getSlideablesFor(de)

        if (ss == null) {
            val parentLayoutIsGrid = (de.getParent() as? Container)?.getLayout() == Layout.GRID
            val myLayoutIsGrid = (de as? Container)?.getLayout() == Layout.GRID
            log.send("Creating $de")

            // ensure we've laid out the grid if one is needed
            if (parentLayoutIsGrid) {
                gp.placeOnGrid(de.getParent() as Container)
            } else if (myLayoutIsGrid) {
                gp.placeOnGrid(de)
            }

            // we need to create these then
            val ms = getMinimumDistanceBetween(de, Side.START, de, Side.END, d, null, false)
            val lp = getRectangularPermeability(de, d, Side.START)
            val rp = getRectangularPermeability(de, d, Side.END)

            val l =
                if (myLayoutIsGrid) {
                    addToGridRectSlideables(de, 0, d, Side.START, lp)
                } else if (parentLayoutIsGrid) {
                    val place = gp.getPlaceOnGrid(de, d, Side.START)
                    addToGridRectSlideables(de.getParent() as Container, place, d, Side.START, lp)
            } else {
                C2Slideable(cso, d, de, Side.START, lp)
            }

            val r =
                if (myLayoutIsGrid) {
                    val place = gp.getMaxPlace(de, d)
                    addToGridRectSlideables(de, place, d, Side.END, rp)
                } else if (parentLayoutIsGrid) {
                    val place = gp.getPlaceOnGrid(de, d, Side.END)
                    addToGridRectSlideables( de.getParent() as Container, place, d,Side.END, rp)
            } else {
                C2Slideable(cso, d, de, Side.END, rp)
            }

            ss = RectangularSlideableSetImpl(de, l, r)

            val position = if (de is AlignedRectangular) de.getConnectionAlignment(d) else Placement.NONE

            ensureCentreSlideablePosition(cso, ss, cExisting, position)
            cso.add(de, ss)

            cso.ensureMinimumDistance(l, r, ms.toInt())

            if (de is Container) {
                checkCreateElementContentItems(cso, de, d, de.getLayout(), ss, topGroup)
            }

            log.send("Created RectangularSlideableSetImpl: ${ss.e}", ss.getAll())

        }

        return ss
    }

    protected fun checkCreateIntersectionOnly(cso: C2SlackOptimisation, g: LeafGroup, c: Container, d: Dimension) : RoutableSlideableSet {

        fun getGridPosition(c: Connected) : Pair<Int, Int>? {
            return if (c is TemporaryContainerHub) {
                c.gridPosition
            } else {
                null
            }
        }

        fun createOrReuseIntersectionSlideable(gridMidpoint: Pair<Int, Int>?, purpose: Purpose) : C2Slideable {
            return if (gridMidpoint != null) {
                val gridContainer = c.getParent() as Container
                val idx = if (d == Dimension.H) gridMidpoint.first else gridMidpoint.second
                val key = Triple(gridContainer, idx, d)
                val existing = gridIntersectSlideables[key]
                if (existing != null) {
                    existing.addIntersectAnchor(IntersectAnchor(g.connected, purpose))
                    existing
                } else {
                    val out = C2Slideable(cso, d,  g.connected, purpose)
                    gridIntersectSlideables[key] = out
                    out
                }
            } else {
                C2Slideable(cso, d,  g.connected, purpose)
            }
        }

        val out = if (g.connected is PlacementPositioned) {
            val purpose = if (g.connected is Port) Purpose.PORT else Purpose.CONTAINER_LAYOUT_MIDPOINT
            val gridMidpoint = getGridPosition(g.connected)
            val ic = createOrReuseIntersectionSlideable(gridMidpoint, purpose)
            val out2 = RoutableSlideableSetImpl(ic, null, null)
            cso.add(g.connected as PlacementPositioned, out2)
            out2
        } else {
            throw LogicException("So what is it?")
        }

        log.send("Created a RoutableSlideableSet for $c: ", out.getAll())
        return out
    }


    /**
     * This works out whether we can route connections through this element (and in which direction).
     */
    fun getRectangularPermeability(de: Rectangular, d: Dimension, s: Side): Permeability {
        val increasing = if (s == Side.START) false else true
        val direction = Direction.getDirection(d, increasing)
        val rule = if (de is Container) {
            val bt = de.getTraversalRule(direction)
            when (bt) {
                BorderTraversal.ALWAYS -> Permeability.ALL
                BorderTraversal.LEAVING ->
                    if (increasing) {
                        Permeability.INCREASING
                    } else {
                        Permeability.DECREASING
                    }
                BorderTraversal.PREVENT -> Permeability.NONE
            }
        } else {
            Permeability.NONE
        }

        return rule
    }

    private fun checkCreateElementContentItems(
        cso: C2SlackOptimisation,
        de: Container,
        d: Dimension,
        l: Layout?,
        container: RectangularSlideableSet,
        topGroup: Group?
    ) {

        val contents = de.getContents()
            .filterIsInstance<ConnectedRectangular>()

        val relyOnGroupLayout = usingGroups(contents, topGroup)
        val contentMap = contents.map { it to checkCreateElement(it, d, cso, null, topGroup) }

        if (l != Layout.GRID) {
            // make sure contents are inside their containers.
            contentMap.forEach { (e, v) -> embed(d, container, v, cso, e) }
        }

        // ensure internal ordering
        if (!relyOnGroupLayout) {
            when (l) {
                Layout.RIGHT, Layout.HORIZONTAL, null -> if (d == Dimension.H) setupInternalOrdering(contentMap, d, cso)
                Layout.LEFT -> if (d == Dimension.H) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.DOWN, Layout.VERTICAL -> if (d == Dimension.V) setupInternalOrdering(contentMap, d, cso)
                Layout.UP -> if (d == Dimension.V) setupInternalOrdering(contentMap.reversed(), d, cso)
                Layout.GRID -> { // handled by checkCreateElement
                }
            }
        } else {
            log.send("Using groups: $de")
        }
    }

    abstract fun usingGroups(contents: List<ConnectedRectangular>, topGroup: Group?): Boolean


    fun createRoutableNeighbours(c: C2Compaction,
                                 hss: RoutableSlideableSet?,
                                 vss: RoutableSlideableSet?,
                                 hr: RectangularSlideableSet?,
                                 vr: RectangularSlideableSet?) {
        if ((vss != null) && (hss != null)) {
            c.addNeighbour(hss.bl, vss.bl, vss.c)
            c.addNeighbour(hss.bl, vss.c, vss.br)

            c.addNeighbour(hss.br, vss.bl, vss.c)
            c.addNeighbour(hss.br, vss.c, vss.br)

            c.addNeighbour(vss.bl, hss.bl, hss.c)
            c.addNeighbour(vss.bl, hss.c, hss.br)

            c.addNeighbour(vss.br, hss.bl, hss.c)
            c.addNeighbour(vss.br, hss.c, hss.br)

            if (hr != null) {
                c.addNeighbour(vss.c, hss.bl, hr.l)
                c.addNeighbour(vss.c, hss.br, hr.r)
            }

            if (vr != null) {
                c.addNeighbour(hss.c, vss.bl, vr.l)
                c.addNeighbour(hss.c, vss.br, vr.r)
            }
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