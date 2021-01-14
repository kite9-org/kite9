package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.model.position.Direction.Companion.reverse
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getType
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkDetail
import org.kite9.diagram.visualization.planarization.rhd.links.LinkManager.LinkProcessor

/**
 * Generates merge options where the group passed in is a member of the alignedGroup, g1, or g2 trio.
 * Sadly, this involves a lot more iteration than a g1 + g2 duo.
 *
 * @author robmoffat
 */
abstract class AbstractAlignedMergeGenerator(
    gp: GroupPhase, ms: BasicMergeState, grouper: GeneratorBasedGroupingStrategy,
    liveOnly: Boolean
) : AbstractWaitingContainerMergeGenerator(gp, ms, grouper, liveOnly) {

    protected abstract fun getAlignmentDirections(g1: GroupPhase.Group?): Set<Direction>
    protected abstract fun processPossibleAligningGroups(g1: GroupPhase.Group?, d: Direction?, lp: LinkProcessor?)
    override fun generate(poll: GroupPhase.Group) {
        log.send(if (log.go()) null else "Generating " + getCode() + " options for " + poll)
        val alignmentDirections = getAlignmentDirections(poll)
        for (d in alignmentDirections) {
            generateFromAlignedGroup(gp, poll, d, ms)
            generateFromG1Group(gp, poll, d, ms)
        }
    }

    protected fun generateFromAlignedGroup(
        gp: GroupPhase?,
        alignedGroup: GroupPhase.Group?,
        d: Direction?,
        ms: BasicMergeState
    ) {
//		if (alignedGroup.getLinkCount() < 2)
//			return;
        val axis = getType(alignedGroup!!)
        val mp = getState(alignedGroup)
        processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, d, object : LinkProcessor {
            override fun process(originatingGroup: GroupPhase.Group, g1w: GroupPhase.Group, ld: LinkDetail) {
                val g1 = grouper.getWorkingGroup(g1w)
                val g1state = getState(g1!!)
                if (ms.isLiveGroup(g1) && mp.matches(g1state)) {
                    processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, d, object : LinkProcessor {
                        override fun process(originatingGroup: GroupPhase.Group, g2: GroupPhase.Group, ld: LinkDetail) {
                            if (g2.groupNumber > g1.groupNumber) {
                                testAndAddAlignedTrio(g1, g2, alignedGroup, d, mp)
                            }
                        }
                    })
                }
            }
        })
    }

    protected fun generateFromG1Group(gp: GroupPhase?, g1: GroupPhase.Group?, d: Direction?, ms: BasicMergeState?) {
//		if (alignedGroup.getLinkCount() < 2)
//			return;
        val axis = getType(g1!!)
        val mp = getState(g1)
        processAlignedGroupsInAxis(g1, ms, axis, mp, d, object : LinkProcessor {
            override fun process(originatingGroup: GroupPhase.Group, alignedGroupw: GroupPhase.Group, ld: LinkDetail) {
                val alignedGroup = grouper.getWorkingGroup(alignedGroupw)
                val rd = reverse(d)

                //	if (ms.isLiveGroup(alignedGroup) && mp.matches(mp)) {
                processAlignedGroupsInAxis(alignedGroup, ms, axis, mp, rd, object : LinkProcessor {
                    override fun process(originatingGroup: GroupPhase.Group, g2: GroupPhase.Group, ld: LinkDetail) {
                        testAndAddAlignedTrio(g1, g2, alignedGroup, rd, mp)
                    }
                })
                //	}
            }
        })
    }

    protected fun testAndAddAlignedTrio(
        g1: GroupPhase.Group?,
        g2: GroupPhase.Group?,
        alignedGroup: GroupPhase.Group?,
        alignedSide: Direction?,
        mp: MergePlane
    ) {
        var g2 = g2
        g2 = grouper.getWorkingGroup(g2)
        val g2state = getState(g2!!)
        if (g2 !== g1 && ms.isLiveGroup(g2) && mp.matches(g2state)) {
            addMergeOption(g1!!, g2, alignedGroup, alignedSide)
        }
    }

    protected abstract fun processAlignedGroupsInAxis(
        alignedGroup: GroupPhase.Group?, ms: BasicMergeState?,
        axis: DirectedGroupAxis?, mp: MergePlane?, d: Direction?, lp: LinkProcessor?
    )
}