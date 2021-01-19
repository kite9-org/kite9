package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Container
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

    private var waitingOptions: MutableMap<Container?, MutableCollection<MergeOption>> = hashMapOf()

    override fun containerIsLive(c: Container) {
        if (liveOnly) {
            val waitList: Collection<MergeOption>? = waitingOptions.remove(c)
            if (waitList != null) {
                for (mo in waitList) {
                    grouper.addMergeOption(mo, getMyBestPriority(), ms)
                }
            }
        }
    }

    fun getCommonContainer(a: Group, b: Group): Container? {
        val ac = ms.getContainersFor(a)
        val bc = ms.getContainersFor(b)
        if (ac == null || bc == null) throw LogicException("Group has no containers?")
        val itc: Set<Container> = if (ac.size < bc.size) ac.keys else bc.keys
        val inc: Set<Container> = if (ac.size < bc.size) bc.keys else ac.keys
        for (container in itc) {
            if (inc.contains(container)) return container
        }
        return null
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