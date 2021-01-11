package org.kite9.diagram.visualization.planarization.rhd.links

import org.kite9.diagram.common.BiDirectional
import org.kite9.diagram.common.elements.grid.GridPositionerImpl.Companion.getXOccupies
import org.kite9.diagram.common.elements.grid.GridPositionerImpl.Companion.getYOccupies
import org.kite9.diagram.common.elements.mapping.ElementMapper
import org.kite9.diagram.common.range.IntegerRange
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.*
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.Tools.Companion.setConnectionContradiction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.getDirectionForLayout
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.isHorizontalDirection
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.isVerticalDirection
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail

class BasicContradictionHandler(var em: ElementMapper) : Logable, ContradictionHandler {

    private val log = Kite9Log(this)

    override fun setContradicting(connections: Iterable<BiDirectional<Connected>>, dontRender: Boolean) {
        for (bic in connections) {
            setContradiction(bic, dontRender)
        }
    }

    override fun setContradiction(bic: BiDirectional<Connected>, dontRender: Boolean) {
        log.error("Contradiction: $bic")
        if (bic is Connection) {
            setConnectionContradiction(bic, true, !dontRender)
        } else {
            // this will only get called when we are adding an illegal.
            // however, this would be setting a contradiction on a layout, so
            // we should do nothing here.
        }
    }

    override fun checkContradiction(ld1: LinkDetail?, ld2: LinkDetail?, containerLayout: Layout?): Direction? {
        return if (ld1 == null) {
            ld2!!.direction
        } else if (ld2 == null) {
            ld1!!.direction
        } else {
            checkContradiction(
                ld1.direction,
                ld1.isOrderingLink,
                ld1.linkRank,
                ld1.connections,
                ld2.direction,
                ld2.isOrderingLink,
                ld2.linkRank,
                ld2.connections,
                containerLayout
            )
        }
    }

    override fun checkContradiction(
        ad: Direction?,
        aOrdering: Boolean,
        aRank: Int,
        ac: Iterable<BiDirectional<Connected>>,
        bd: Direction?,
        bOrdering: Boolean,
        bRank: Int,
        bc: Iterable<BiDirectional<Connected>>,
        containerLayout: Layout?
    ): Direction? {
        if (containerLayout != null) {
            when (containerLayout) {
                Layout.HORIZONTAL -> {
                    if (isVerticalDirection(ad)) {
                        setContradicting(ac, false)
                    }
                    if (isVerticalDirection(bd)) {
                        setContradicting(bc, false)
                    }
                }
                Layout.VERTICAL -> {
                    if (isHorizontalDirection(ad)) {
                        setContradicting(ac, false)
                    }
                    if (isHorizontalDirection(bd)) {
                        setContradicting(bc, false)
                    }
                }
                else -> {
                }
            }
        }
        return if (ad === bd) {
            ad
        } else if (ad == null) {
            bd
        } else if (bd == null) {
            ad
        } else if (aOrdering && !bOrdering) {
            setContradicting(bc, false)
            ad
        } else if (bOrdering && !aOrdering) {
            setContradicting(ac, false)
            bd
        } else if (!bOrdering && !aOrdering) {
            if (aRank >= bRank) {
                setContradicting(bc, false)
                ad
            } else {
                setContradicting(ac, false)
                bd
            }
        } else {
            throw LogicException("Contradicting, ordering direction: $ad $bd")
        }
    }

    /**
     * Simple test to make sure that c doesn't contradict the direction of the
     * top-level container it passes through. Or, that something is connecting
     * to an element inside itself.
     */
    override fun checkForContainerContradiction(c: Connection) {
        val drawDirection = c.getDrawDirection()
        var from: DiagramElement? = c.getFrom()
        var to: DiagramElement? = c.getTo()
        if (from === to) {
            setContradiction(c, true)
        }
        if (from is Diagram || to is Diagram) {
            setContradiction(c, true)
            return
        }
        if ((from as Connected?)!!.getContainer()!!.getLayout() === Layout.GRID) {
            setContradiction(c, true)
            return
        }
        if ((to as Connected?)!!.getContainer()!!.getLayout() === Layout.GRID) {
            setContradiction(c, true)
            return
        }
        while (true) {
            val fromC = (from as Connected?)!!.getContainer()
            val toC = (to as Connected?)!!.getContainer()
            if (drawDirection != null) {

                // directed connections breaking normal layouts
                if (fromC === toC) {
                    val l = fromC!!.getLayout()
                    if (l == null) {
                        return
                    } else {
                        when (l) {
                            Layout.HORIZONTAL -> verticalContradiction(c, drawDirection)
                            Layout.VERTICAL -> horizontalContradiction(c, drawDirection)
                            Layout.UP, Layout.DOWN, Layout.LEFT, Layout.RIGHT -> checkOrdinalContradiction(
                                l,
                                drawDirection,
                                from,
                                to,
                                fromC,
                                c
                            )
                            Layout.GRID -> gridContradiction(c, drawDirection, from, to)
                        }
                    }
                }
            }

            // check for illegal containment
            if (to is Container) {
                if ((to as Container).getContents().contains(from)) {
                    setContradiction(c, true)
                }
            }
            if (from is Container) {
                if ((from as Container).getContents().contains(to)) {
                    setContradiction(c, true)
                }
            }
            if (fromC === toC) {
                return
            }
            val depthFrom = fromC!!.getDepth()
            val depthTo = toC!!.getDepth()
            if (depthFrom < depthTo) {
                to = toC
            } else if (depthFrom > depthTo) {
                from = fromC
            } else {
                to = toC
                from = fromC
            }
        }
    }

    private fun gridContradiction(c: Connection, drawDirection: Direction, fromC: Connected?, toC: Connected?) {

        // do special grid checking
        when (drawDirection) {
            Direction.LEFT -> {
                gridPositionAfterOrContradiction(
                    getXOccupies(fromC!!), getXOccupies(
                        toC!!
                    ), c
                )
                gridPositionOverlapOrContradiction(
                    getYOccupies(fromC), getYOccupies(
                        toC
                    ), c
                )
            }
            Direction.RIGHT -> {
                gridPositionAfterOrContradiction(
                    getXOccupies(toC!!), getXOccupies(
                        fromC!!
                    ), c
                )
                gridPositionOverlapOrContradiction(
                    getYOccupies(fromC), getYOccupies(
                        toC
                    ), c
                )
            }
            Direction.UP -> {
                gridPositionAfterOrContradiction(
                    getYOccupies(fromC!!), getYOccupies(
                        toC!!
                    ), c
                )
                gridPositionOverlapOrContradiction(
                    getXOccupies(fromC), getXOccupies(
                        toC
                    ), c
                )
            }
            Direction.DOWN -> {
                gridPositionAfterOrContradiction(
                    getYOccupies(toC!!), getYOccupies(
                        fromC!!
                    ), c
                )
                gridPositionOverlapOrContradiction(
                    getXOccupies(fromC), getXOccupies(
                        toC
                    ), c
                )
            }
        }
    }

    private fun gridPositionOverlapOrContradiction(a: IntegerRange, b: IntegerRange, c: Connection) {
        val fromInside = a.from <= b.from && a.from >= b.to
        val toInside = a.to <= b.from && a.to >= b.to
        if (!(fromInside || toInside)) {
            setContradiction(c, false)
        }
    }

    private fun gridPositionAfterOrContradiction(a: IntegerRange, b: IntegerRange, c: Connection) {
        if (a.to < b.from) {
            setContradiction(c, false)
        }
    }

    protected fun checkOrdinalContradiction(
        l: Layout?,
        d: Direction,
        from: Connected?,
        to: Connected?,
        fromC: Container?,
        c: Connection?
    ) {
        val ld = getDirectionForLayout(l)
        if (isHorizontalDirection(ld) != isHorizontalDirection(d)) {
            setContradiction(c!!, false)
            return
        }

        // ld and d in the same axis
        val reversed = ld !== d
        val fromI = fromC!!.getContents().indexOf(from)
        val toI = fromC.getContents().indexOf(to)
        val contradiction = if (fromI < toI) reversed else !reversed
        if (contradiction) setContradiction(c!!, false)
    }

    private fun horizontalContradiction(c: Connection, drawDirection: Direction) {
        if (isHorizontalDirection(drawDirection)) {
            setContradiction(c, false)
        }
    }

    private fun verticalContradiction(c: Connection, drawDirection: Direction) {
        if (isVerticalDirection(drawDirection)) {
            setContradiction(c, false)
        }
    }

    override val prefix: String
        get() = "CH  "
    override val isLoggingEnabled: Boolean
        get() = true
}