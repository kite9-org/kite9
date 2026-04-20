package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.model.position.Direction
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption

/**
 * Options that are not in live containers wait until the containers are live
 * before being added to the queue.
 *
 * @author robmoffat
 */
abstract class AbstractWaitingContainerMergeGenerator(
    gp: GroupPhase,
    ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy,
    private val liveOnly: Boolean
) : AbstractMergeGenerator(
    gp, ms, grouper
) {

    private var waitingOptions: MutableMap<Rectangular?, MutableCollection<MergeOption>> = hashMapOf()

    override fun containerIsLive(c: Rectangular) {
        if (liveOnly) {
            val waitList: Collection<MergeOption>? = waitingOptions.remove(c)
            if (waitList != null) {
                for (mo in waitList) {
                    grouper.addMergeOption(mo, getMyBestPriority(), ms)
                }
            }
        }
    }

    fun getCommonContainer(a: Group, b: Group): Rectangular {
        var ac = ms.getContainersFor(a)?.keys ?: emptySet()
        var bc = ms.getContainersFor(b)?.keys ?: emptySet()
        while (true) {
            if (ac.isEmpty() || bc.isEmpty())
                throw LogicException("Group has no containers?")
            for (container in ac) {
                if (bc.contains(container)) return container
            }

            ac = ac + ac.mapNotNull { it.getContainer() }.toSet()
            bc = bc + bc.mapNotNull { it.getContainer() }.toSet()
        }
    }

    override fun addMergeOption(
        g1: Group,
        g2: Group,
        alignedGroup: Group?,
        alignedSide: Direction?
    ) {
        if (liveOnly) {
            val c = getCommonContainer(g1, g2)
            if (ms.isContainerLive(c)) {
                super.addMergeOption(g1, g2, alignedGroup, alignedSide)
            } else {
                // add to the wait-list
                var waitList = waitingOptions[c]
                if (waitList == null) {
                    waitList = ArrayList()
                    waitingOptions[c] = waitList
                }
                val p = getMyBestPriority()
                val mo = MergeOption(g1, g2, ms.nextMergeOptionNumber(), p, alignedGroup, alignedSide)
                waitList.add(mo)
            }
        } else {
            super.addMergeOption(g1, g2, alignedGroup, alignedSide)
        }
    }
}