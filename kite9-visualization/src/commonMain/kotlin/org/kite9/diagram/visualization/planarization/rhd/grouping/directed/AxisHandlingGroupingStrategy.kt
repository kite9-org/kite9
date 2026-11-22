package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.common.elements.factory.DiagramElementFactory
import org.kite9.diagram.common.elements.grid.GridPositioner
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.ConnectedRectangular
import org.kite9.diagram.model.DiagramElement
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.Containers
import org.kite9.diagram.model.LinkNode
import org.kite9.diagram.model.RectangularLinkNode
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LayoutSetPoint
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedCompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.group.DirectedLinkManager
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * For directed merges, this strategy allows groups to be created which run x-axis-first
 * merging and y-axis-first merging, and then once directed merges are exhausted for both,
 * combines them together.
 *
 *
 * @author robmoffat
 */
abstract class AxisHandlingGroupingStrategy(
    top: DiagramElement,
    elements: Int,
    ch: ContradictionHandler,
    gp: GridPositioner,
    ef: DiagramElementFactory<*>,
    val ms: DirectedMergeState
) : AbstractRuleBasedGroupingStrategy(top, elements, ch, gp, ef) {

    override fun groupChangedContainer(ms: BasicMergeState, g: Group) {
        g.linkManager.notifyContainerChange()
        g.processAllLeavingLinks(true, g.linkManager.allMask(), object : LinkProcessor {
            override fun process(
                originatingGroup: Group,
                destinationGroup: Group,
                ld: LinkDetail
            ) {
                destinationGroup.linkManager.notifyContainerChange(g)
            }
        })
    }


    override fun createCompoundGroup(ms: BasicMergeState, mo: MergeOption): CompoundGroup {
        val out = createCompoundGroup(mo.mk.a, mo.mk.b, false, mo)
        identifyGroupDirection(out, ms)

        log.send(
            if (log.go()) null else """Compound Group ${out.groupNumber} created: 
            ${out.a}
            ${out.b}
            ${out.getLayout()}
            axis:${out.axis}
            links:""", (out.linkManager).links
                )

        writeGroup(out, mo)
        return out
    }

    private fun checkForInternalContradictions(
        out: CompoundGroup,
        ld1: LinkDetail?,
        ld2: LinkDetail?,
        ms: BasicMergeState
    ) {
        if (ld1 == null && ld2 == null) {
            return
        }

        // added for contradictions - break the least important one first.
        val ld = if (ld1!!.linkRank > ld2!!.linkRank) ld2 else ld1

        // check that internals don't contradict the axis of the group
        if (isLinkAgainstAxis(out, ld)) {
            val aContainerMap = ms.getContainersFor(out.a)!!
            val bContainerMap = ms.getContainersFor(out.b)!!
            val expandingContainers: MutableSet<Rectangular> = UnorderedSet(aContainerMap.keys)
            expandingContainers.retainAll(bContainerMap.keys)
            val axisChecker: LinkProcessor = object : LinkProcessor {
                /**
                 * The aim of this test is to make sure we don't allow a container to be non-square
                 */
                override fun process(
                    originatingGroup: Group,
                    destinationGroup: Group,
                    ld: LinkDetail
                ) {
                    if (isLinkAgainstAxis(out, ld)) {
                        val from = (originatingGroup as LeafGroup).container
                        val to = (destinationGroup as LeafGroup).container
                        val fromExpanded = getFirstExpandingContainer(ms, from)
                        val toExpanded = getFirstExpandingContainer(ms, to)
                        if (fromExpanded != null && !isParentOrSelf(to, fromExpanded)) {
                            // from has been expanded, from outside itself
                            ms.contradictionHandler.setContradicting(ld.connections, false)
                            return
                        }
                        if (toExpanded != null && !isParentOrSelf(from, toExpanded)) {
                            // from has been expanded, from outside itself
                            ms.contradictionHandler.setContradicting(ld.connections, false)
                            return
                        }
                    }
                }

                private fun isParentOrSelf(x: DiagramElement?, parent: DiagramElement): Boolean {
                    return if (x === parent) {
                        true
                    } else if (x == null) {
                        false
                    } else {
                        isParentOrSelf(x.getContainer(), parent)
                    }
                }

                private fun getFirstExpandingContainer(ms: BasicMergeState?, from: Rectangular?): Rectangular? {
                    return if (expandingContainers.contains(from)) {
                        from
                    } else if (from is ConnectedRectangular) {
                        getFirstExpandingContainer(ms, from.getContainer())
                    } else {
                        null
                    }
                }
            }
            ld1.processLowestLevel(axisChecker)
        }
    }

    private fun isLinkAgainstAxis(out: CompoundGroup, ld: LinkDetail): Boolean {
        return DirectedGroupAxis.getState(out) === MergePlane.X_FIRST_MERGE && isVerticalDirection(ld.direction) ||
                DirectedGroupAxis.getState(out) === MergePlane.Y_FIRST_MERGE && isHorizontalDirection(ld.direction)
    }

    private fun identifyGroupDirection(out: DirectedCompoundGroup, ms: BasicMergeState) {
        val c = Containers.getCommonContainers(ms, out.a, out.b)
        val layoutDirection = getAxisLayoutForContainers(c)
        val axisAligned = out.axis.isAxisAligned

        if (axisAligned) {
            val lda = out.internalLinkA
            val ldb = out.internalLinkB
            checkForInternalContradictions(out, out.internalLinkA, out.internalLinkB, ms)
            val internalLinkDirection = if (lda != null && ldb != null) {
                val layout = getLayoutForDirection(
                    ms.contradictionHandler.checkContradiction(
                        lda.direction, lda.isOrderingLink, lda.linkRank, lda.connections,
                        reverse(ldb.direction), ldb.isOrderingLink, ldb.linkRank, ldb.connections,
                        layoutDirection
                    )
                )
                layout
            } else if (lda != null) {
                getLayoutForDirection(lda.direction)
            } else if (ldb != null) {
                getLayoutForDirection(ldb.direction)
            } else {
                null
            }

            if (internalLinkDirection != null) {
                out.setLayout(internalLinkDirection, LayoutSetPoint.ON_CREATION)
            }
        }

        // we may be able to establish a layout from one or more of the
        // containers that the groups are in.  Layout will be horizontal or vertical,
        // unless there is a contradiction.

        fun containsRectangulars(g: Group) : Boolean {
            return when (g) {
                is LeafGroup -> g.connected is RectangularLinkNode
                is CompoundGroup -> containsRectangulars(g.a) || containsRectangulars(g.b)
            }
        }

        if ((out.getLayout() == null) && (layoutDirection != null) && containsRectangulars(out.a) && containsRectangulars(out.b)){
            out.setLayout(layoutDirection, LayoutSetPoint.ON_CREATION)
        }
    }

    private fun getAxisLayoutForContainers(cc: Set<Rectangular>): Layout? {
        if (cc.isEmpty()) return null

        val layoutDirections = cc.map { c ->
            // sanitize to a single axis
            when (val layoutDirection = c.getLayout()) {
                Layout.LEFT, Layout.RIGHT -> Layout.HORIZONTAL
                Layout.UP, Layout.DOWN -> Layout.VERTICAL
                else -> layoutDirection
            }
        }

        // ok, so we should have either nulls, horizontals or verticals only.
        if (layoutDirections.contains(Layout.HORIZONTAL) &&
            layoutDirections.contains(Layout.VERTICAL)
        ) {
            throw LogicException("Two live containers with conflicting layouts - not handled yet")
        }

        return layoutDirections.filterNotNull().firstOrNull()
    }

    private fun writeGroup(g: CompoundGroup, mo: MergeOption) {
        if (!log.go()) {
            LAST_MERGE_DEBUG =
                """$LAST_MERGE_DEBUG${g.groupNumber}	${g.a}	${g.b}	${g.axis}	${mo.priority}
"""
        }
    }

    /**
     * Once all the directed merges have been completed for a group, (say X_FIRST) it should
     * be merged with the group going the other way (Y_FIRST) if possible.
     */
    protected fun checkForCompleteAxisMerges(ms: BasicMergeState, a: Group) {
        val dms = ms as DirectedMergeState
        if (dms.completedDirectionalMerge(a)) {
            val b = dms.getCompoundGroupWithSameContents(a)
            if (b != null && b !== a) {
                // perform the merge in a fairly normal way.
                val out = createCompoundGroup(a, b, true, null, a.size)
                val axis = out.axis as DirectedGroupAxis
                val lm = out.linkManager as DirectedLinkManager
                axis.isHorizontal = false
                axis.isVertical = false
                axis.state = MergePlane.UNKNOWN

                log.send(
                    if (log.go()) null else """Compound Group ${out.groupNumber} created: 
	${out.a}
	${out.b}
	 NON-LAYOUT 
	axis:${out.axis}
	links:""", lm.links
                )

                setBothParents(out, a.axis as DirectedGroupAxis)
                setBothParents(out, b.axis as DirectedGroupAxis)
                doCompoundGroupInsertion(ms, out, true)
            }
        }
    }

    override fun doCompoundGroupInsertion(
        ms: BasicMergeState,
        combined: CompoundGroup,
        skipContainerCompletionCheck: Boolean
    ) {
        super.doCompoundGroupInsertion(ms, combined, skipContainerCompletionCheck)
        if (!skipContainerCompletionCheck) {
            checkForCompleteAxisMerges(ms, combined)
        }
    }


    /**
     * When a merge option creates a horizontal or vertical combined group, then
     * the underlying groups are not removed necessarily - they are kept around
     * so that they can be merged in the other direction.
     */
    override fun removeOldGroups(ms: BasicMergeState, combined: CompoundGroup) {
        checkRemoveGroup(combined.a, combined, ms)
        checkRemoveGroup(combined.b, combined, ms)
    }

    private fun checkRemoveGroup(a: Group, cg: CompoundGroup, ms: BasicMergeState) {
        val aType = DirectedGroupAxis.getType(a)
        val cgType = DirectedGroupAxis.getType(cg)
        if (cgType.state === MergePlane.X_FIRST_MERGE) {
            aType.vAxisParentGroup = cg
            if (aType.state === MergePlane.X_FIRST_MERGE || aType.state === MergePlane.Y_FIRST_MERGE) {
                aType.active = false
                if (aType.hAxisParentGroup == null) {
                    aType.hAxisParentGroup = cg
                }
            } else {
                aType.state = MergePlane.Y_FIRST_MERGE
                axisChanged(a)
            }
        } else if (cgType.state === MergePlane.Y_FIRST_MERGE) {
            aType.hAxisParentGroup = cg
            if (aType.state === MergePlane.X_FIRST_MERGE || aType.state === MergePlane.Y_FIRST_MERGE) {
                aType.active = false
                if (aType.vAxisParentGroup == null) {
                    aType.vAxisParentGroup = cg
                }
            } else {
                aType.state = MergePlane.X_FIRST_MERGE
                axisChanged(a)
            }
        } else {
            // undirected / single axis merge
            setBothParents(cg, aType)
        }
        if (!aType.active) {
            ms.removeLiveGroup(a)
        } else {
            // group is being kept, so we need to make sure it's in the group
            // lists
            ms.addLiveGroup(a)
        }
    }

    private fun setBothParents(cg: CompoundGroup, aType: DirectedGroupAxis) {
        aType.hAxisParentGroup = cg
        aType.vAxisParentGroup = cg
        aType.active = false
    }

    private fun axisChanged(a: Group) {
        a.linkManager.notifyAxisChange()
    }

    private fun isSingleDirectedAxisMerge(p: Int?): Boolean {
        val rightPriorities = listOf(
            AXIS_SINGLE_NEIGHBOUR,
            AXIS_SINGLE_NEIGHBOUR + ContainerMergeType.JOINING_EXTRA_CONTAINERS.priorityAdjustment,
            AXIS_SINGLE_NEIGHBOUR + ContainerMergeType.NO_LIVE_CONTAINER.priorityAdjustment
        )
        return rightPriorities.contains(p)
    }

    override fun createLeafGroup(gln: LinkNode, ord: Rectangular): LeafGroup {
        containerCount++
        val out = DirectedLeafGroup(gln, ord, groupCount, hashCodeGenerator.nextInt(), log, ms)
        groupCount++
        allGroups.add(out)
        return out
    }

    override fun createCompoundGroup(a: Group, b: Group, treatAsComplete: Boolean, mo: MergeOption?, size: Int): DirectedCompoundGroup {
        val hashCode = if (!treatAsComplete) {
            // this is done so that a different compound group containing the same leaves can
            // occupy the same position in a hashmap
            a.hashCode() + b.hashCode()
        } else {
            hashCodeGenerator.nextInt()
        }
        val out = DirectedCompoundGroup(a, b, treatAsComplete, groupCount, size, hashCode, ms, log, mo?.alignedDirection, isSingleDirectedAxisMerge(mo?.priority))
        groupCount++
        return out
    }

    override fun isContainerCompleteInner(c: Rectangular, ms: BasicMergeState): Boolean {
        val csi = ms.getStateFor(c)
        if (csi!!.contents.size < 2) {
            csi.done = true
            return true
        }
        if (csi.contents.size > 2) {
            return false
        }

        // test one of each axis
        val groups: Iterator<Group> =
            csi.contents.iterator()
        val first = groups.next()
        val second = groups.next()
        val fax = DirectedGroupAxis.getState(first)
        val sax = DirectedGroupAxis.getState(second)
        return fax === MergePlane.X_FIRST_MERGE && sax === MergePlane.Y_FIRST_MERGE ||
                fax === MergePlane.Y_FIRST_MERGE && sax === MergePlane.X_FIRST_MERGE
    }

    companion object {

		var LAST_MERGE_DEBUG: String? = null
    }

    init {
        if (!log.go()) {
            LAST_MERGE_DEBUG = ""
        } else {
            LAST_MERGE_DEBUG = null
        }
    }
}