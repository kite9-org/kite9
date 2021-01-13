package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.model.position.Layout
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.getLayoutForDirection
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.isHorizontalDirection
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Companion.isVerticalDirection
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.MergeOption
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager
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
abstract class AxisHandlingGroupingStrategy(ms: BasicMergeState) : AbstractRuleBasedGroupingStrategy() {
    protected var ms: BasicMergeState
    var hack = Kite9Log(object : Logable {
        override val isLoggingEnabled: Boolean
            get() = true
        override val prefix: String
            get() = "DGA "
    })

    override fun groupChangedContainer(ms: BasicMergeState, g: GroupPhase.Group) {
        g.linkManager.notifyContainerChange()
        g.processAllLeavingLinks(true, g.linkManager.allMask(), object : LinkProcessor {
            override fun process(
                originatingGroup: GroupPhase.Group,
                destinationGroup: GroupPhase.Group,
                ld: LinkDetail
            ) {
                destinationGroup.linkManager.notifyContainerChange(g)
            }
        })
    }

    protected fun initLeafGroupAxis(toAdd: LeafGroup?) {}
    override fun createAxis(): DirectedGroupAxis {
        return DirectedGroupAxis(hack)
    }

    override fun createLinkManager(): LinkManager {
        return DirectedLinkManager(ms)
    }

    override fun createCompoundGroup(gp: GroupPhase?, ms: BasicMergeState?, mo: MergeOption?): CompoundGroup? {
        val used = buildCompoundAxis(mo!!.mk.a, mo.mk.b, mo.alignedDirection)
        val lm = createLinkManager()
        val out = gp!!.CompoundGroup(mo.mk.a, mo.mk.b, used, lm, false)
        identifyGroupDirection(
            out, gp,
            ms
        )
        log.send(
            if (log.go()) null else """Compound Group ${out.groupNumber} created: 
	${out.a}
	${out.b}
	${out.layout}
	axis:${out.getAxis()}
	links:""", (lm as DirectedLinkManager).getLinks()
        )
        writeGroup(out, mo)
        return out
    }

    private fun checkForInternalContradictions(
        out: CompoundGroup,
        ld1: LinkDetail?,
        ld2: LinkDetail?,
        ms: BasicMergeState?
    ) {
        if (ld1 == null && ld2 == null) {
            return
        }

        // added for contradictions - break the least important one first.
        val ld = if (ld1!!.linkRank > ld2!!.linkRank) ld2 else ld1

        // check that internals don't contradict the axis of the group
        if (isLinkAgainstAxis(out, ld)) {
            val aContainerMap = ms!!.getContainersFor(out.a)
            val bContainerMap = ms.getContainersFor(out.b)
            val expandingContainers: MutableSet<Container?> = UnorderedSet(aContainerMap.keys)
            expandingContainers.retainAll(bContainerMap.keys)
            val axisChecker: LinkProcessor = object : LinkProcessor {
                /**
                 * The aim of this test is to make sure we don't allow a container to be non-square
                 */
                override fun process(
                    originatingGroup: GroupPhase.Group,
                    destinationGroup: GroupPhase.Group,
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

                private fun isParentOrSelf(x: Container?, parent: Container): Boolean {
                    return if (x === parent) {
                        true
                    } else if (x == null) {
                        false
                    } else {
                        isParentOrSelf(x.getContainer(), parent)
                    }
                }

                private fun getFirstExpandingContainer(ms: BasicMergeState?, from: Container?): Container? {
                    return if (expandingContainers.contains(from)) {
                        from
                    } else if (from is Connected) {
                        getFirstExpandingContainer(ms, from.getContainer())
                    } else {
                        null
                    }
                }
            }
            ld1.processLowestLevel(axisChecker)
        }
    }

    private fun isLinkAgainstAxis(out: CompoundGroup, ld: LinkDetail?): Boolean {
        return DirectedGroupAxis.getState(out) === MergePlane.X_FIRST_MERGE && isVerticalDirection(ld!!.direction) ||
                DirectedGroupAxis.getState(out) === MergePlane.Y_FIRST_MERGE && isHorizontalDirection(ld!!.direction)
    }

    fun identifyGroupDirection(out: CompoundGroup, gp: GroupPhase?, ms: BasicMergeState?) {
        val c = getCommonContainer(out)
        val layoutDirection = getAxisLayoutForContainer(c)
        val lda = out.internalLinkA
        val ldb = out.internalLinkB
        checkForInternalContradictions(out, out.internalLinkA, out.internalLinkB, ms)
        if (lda != null && ldb != null) {
            val layout = getLayoutForDirection(
                ms!!.contradictionHandler.checkContradiction(
                    lda.direction, lda.isOrderingLink, lda.linkRank, lda.connections,
                    reverse(ldb.direction), ldb.isOrderingLink, ldb.linkRank, ldb.connections,
                    layoutDirection
                )
            )
            out.layout = layout
        } else if (lda != null) {
            out.layout = getLayoutForDirection(lda.direction)
        } else if (ldb != null) {
            out.layout = getLayoutForDirection(ldb.direction)
        }


        // we may be able to establish a layout from one or more of the 
        // containers that the groups are in.  Layout will be horizontal or vertical,
        // unless there is a contradiction.
        val layoutNeeded = c != null
        if (!layoutNeeded && out.layout == null) {
            DirectedGroupAxis.getType(out).setLayoutRequired(false)
        } else if (out.layout == null) {
            out.layout = layoutDirection
        } else if (out.layout === Layout.UP || out.layout === Layout.DOWN) {
            if (layoutDirection === Layout.HORIZONTAL) {
                out.layout = Layout.HORIZONTAL
            }
        } else if (out.layout === Layout.LEFT || out.layout === Layout.RIGHT) {
            if (layoutDirection === Layout.VERTICAL) {
                out.layout = Layout.VERTICAL
            }
        } else if (out.layout !== layoutDirection) {
            throw LogicException("Layout contradiction")
        }
    }

    private fun getAxisLayoutForContainer(c: Container?): Layout? {
        if (c == null) return null
        var layoutDirection = c.getLayout()

        // sanitize to a single axis
        if (layoutDirection != null) {
            when (layoutDirection) {
                Layout.LEFT, Layout.RIGHT -> layoutDirection = Layout.HORIZONTAL
                Layout.UP, Layout.DOWN -> layoutDirection = Layout.VERTICAL
                else -> {
                }
            }
        }
        return layoutDirection
    }

    /**
     * Attempts to find a container shared by both a and b, in which both a and b actually have contents.
     * If there is no common content, then it returns false.
     */
    private fun getCommonContainer(out: CompoundGroup): Container? {
        val a2cs = ms.getContainersFor(out.a)
        val commonContainers = ms.getContainersFor(out.b)
        var common: Container? = null
        for ((container, value) in a2cs) {
            if (value.hasContent() && ms.isContainerLive(container)) {
                val bContained = commonContainers[container]
                if (bContained != null && bContained.hasContent()) {
                    if (common == null || common.getLayout() == null) {
                        common = container
                    }
                }
            }
        }
        return common
    }

    private fun writeGroup(g: CompoundGroup, mo: MergeOption?) {
        if (!hack.go()) {
            LAST_MERGE_DEBUG =
                """$LAST_MERGE_DEBUG${g.groupNumber}	${g.a}	${g.b}	${g.getAxis()}	${mo!!.priority}
"""
        }
    }

    /**
     * Once all the directed merges have been completed for a group, (say X_FIRST) it should
     * be merged with the group going the other way (Y_FIRST) if possible.
     */
    protected fun checkForCompleteAxisMerges(gp: GroupPhase, ms: BasicMergeState, a: GroupPhase.Group) {
        val dms = ms as DirectedMergeState
        if (dms.completedDirectionalMerge(a)) {
            val b = dms.getCompoundGroupWithSameContents(a)
            if (b != null && b !== a) {
                // perform the merge in a fairly normal way.
                val type = createAxis()
                type.setHorizontal(false)
                type.setVertical(false)
                type.state = MergePlane.UNKNOWN
                val lm = createLinkManager()
                val out = gp.CompoundGroup(a, b, type, lm, true)
                log.send(
                    if (log.go()) null else """Compound Group ${out.groupNumber} created: 
	${out.a}
	${out.b}
	 NON-LAYOUT 
	axis:${out.getAxis()}
	links:""", (lm as DirectedLinkManager).getLinks()
                )
                out.size = a.size
                setBothParents(out, a.getAxis() as DirectedGroupAxis)
                setBothParents(out, b.getAxis() as DirectedGroupAxis)
                doCompoundGroupInsertion(gp, ms, out, true)
            }
        }
    }

    override fun doCompoundGroupInsertion(
        gp: GroupPhase,
        ms: BasicMergeState,
        combined: CompoundGroup,
        skipContainerCompletionCheck: Boolean
    ) {
        super.doCompoundGroupInsertion(gp, ms, combined, skipContainerCompletionCheck)
        if (!skipContainerCompletionCheck) {
            checkForCompleteAxisMerges(gp, ms, combined)
        }
    }

    private fun buildCompoundAxis(
        a: GroupPhase.Group,
        b: GroupPhase.Group,
        alignedDirection: Direction?
    ): DirectedGroupAxis {
        val used = createAxis()
        var axis = DirectedGroupAxis.getMergePlane(a, b)
        if (axis === MergePlane.UNKNOWN) {
            if (alignedDirection != null) {
                axis = when (alignedDirection) {
                    Direction.UP, Direction.DOWN -> MergePlane.Y_FIRST_MERGE
                    Direction.LEFT, Direction.RIGHT -> MergePlane.X_FIRST_MERGE
                }
            }
        }
        if (axis == null) {
            throw LogicException("Illegal merge")
        }
        when (axis) {
            MergePlane.X_FIRST_MERGE -> {
                used.state = MergePlane.X_FIRST_MERGE
                used.setHorizontal(false)
                used.setVertical(true)
            }
            MergePlane.Y_FIRST_MERGE -> {
                used.state = MergePlane.Y_FIRST_MERGE
                used.setHorizontal(true)
                used.setVertical(false)
            }
            MergePlane.UNKNOWN -> {
                used.state = MergePlane.UNKNOWN
                used.setVertical(true)
                used.setHorizontal(true)
            }
        }
        return used
    }

    /**
     * When a merge option creates a horizontal or vertical combined group, then
     * the underlying groups are not removed necessarily - they are kept around
     * so that they can be merged in the other direction.
     */
    override fun removeOldGroups(gp: GroupPhase?, ms: BasicMergeState?, combined: CompoundGroup?) {
        checkRemoveGroup(gp, combined!!.a, combined, ms)
        checkRemoveGroup(gp, combined.b, combined, ms)
    }

    private fun checkRemoveGroup(gp: GroupPhase?, a: GroupPhase.Group, cg: CompoundGroup?, ms: BasicMergeState?) {
        val aType = DirectedGroupAxis.getType(a)
        val cgType = DirectedGroupAxis.getType(cg)
        if (cgType.state === MergePlane.X_FIRST_MERGE) {
            aType.vertParentGroup = cg
            if (aType.state === MergePlane.X_FIRST_MERGE || aType.state === MergePlane.Y_FIRST_MERGE) {
                aType.active = false
                if (aType.horizParentGroup == null) {
                    aType.horizParentGroup = cg
                }
            } else {
                aType.state = MergePlane.Y_FIRST_MERGE
                axisChanged(a)
            }
        } else if (cgType.state === MergePlane.Y_FIRST_MERGE) {
            aType.horizParentGroup = cg
            if (aType.state === MergePlane.X_FIRST_MERGE || aType.state === MergePlane.Y_FIRST_MERGE) {
                aType.active = false
                if (aType.vertParentGroup == null) {
                    aType.vertParentGroup = cg
                }
            } else {
                aType.state = MergePlane.X_FIRST_MERGE
                axisChanged(a)
            }
        } else {
            // undirected / single axis merge
            setBothParents(cg, aType)
        }
        if (!aType.isActive) {
            ms!!.removeLiveGroup(a)
        } else {
            // group is being kept, so we need to make sure it's in the group
            // lists
            ms!!.addLiveGroup(a)
        }
    }

    private fun setBothParents(cg: CompoundGroup?, aType: DirectedGroupAxis) {
        aType.horizParentGroup = cg
        aType.vertParentGroup = cg
        aType.active = false
    }

    private fun axisChanged(a: GroupPhase.Group) {
        a.linkManager.notifyAxisChange()
    }

    override fun isContainerCompleteInner(c: Container, ms: BasicMergeState): Boolean {
        val csi = ms.getStateFor(c)
        if (csi!!.contents.size < 2) {
            csi.done = true
            return true
        }
        if (csi.contents.size > 2) {
            return false
        }

        // test one of each axis
        val groups: Iterator<GroupPhase.Group> =
            csi.contents.iterator()
        val first = groups.next()
        val second = groups.next()
        val fax = DirectedGroupAxis.getState(first)
        val sax = DirectedGroupAxis.getState(second)
        return fax === MergePlane.X_FIRST_MERGE && sax === MergePlane.Y_FIRST_MERGE ||
                fax === MergePlane.Y_FIRST_MERGE && sax === MergePlane.X_FIRST_MERGE
    }

    companion object {
        @JvmField
		var LAST_MERGE_DEBUG: String? = null
    }

    init {
        if (!hack.go()) {
            LAST_MERGE_DEBUG = ""
        } else {
            LAST_MERGE_DEBUG = null
        }
        this.ms = ms
    }
}