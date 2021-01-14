package org.kite9.diagram.visualization.planarization.rhd.grouping.generators

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.BasicMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.AbstractRuleBasedGroupingStrategy
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis.Companion.getState
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.MergePlane

/**
 * Generates some merge options for mergable containers.  We store live groups in exactly
 * the same way as the other generators, but instead of generating a single merge option
 * we generate all the ones needed to merge the whole container.   This is because it's quciker
 * to do this, and you're likely to need them.
 *
 * @author robmoffat
 */
class ContainerUndirectedNeighbourMergeGenerator(
    gp: GroupPhase,
    ms: BasicMergeState,
    grouper: GeneratorBasedGroupingStrategy
) : AbstractMergeGenerator(
    gp, ms, grouper
) {
    override fun containerIsLive(c: Container) {
        super.containerIsLive(c)
        dontDo[c] = UnorderedSet()
    }

    // keeps track of groups we've already done
    var dontDo: MutableMap<Container, MutableSet<GroupPhase.Group>> = HashMap()

    private fun generateNeighboursForContainer(
        c: Container, ms: BasicMergeState,
        grouper: GeneratorBasedGroupingStrategy, mp: MergePlane
    ) {
        log.send(if (log.go()) null else "Generating " + getCode() + " options for " + c + " in axis " + mp)
        val csi = ms.getStateFor(c)
        val contentCount = csi!!.contents.size
        val myDontDo = dontDo[c]!!
        val orderedItems: MutableList<GroupPhase.Group> = ArrayList(contentCount)
        for (group in csi.contents) {
            if (mp.matches(getState(group))) {
                myDontDo.add(group)
                orderedItems.add(group)
            }
        }
        orderedItems.sortWith { o1, o2 -> o1.groupOrdinal.compareTo(o2.groupOrdinal) }
        createNeighbourMergeOptions(orderedItems)
    }

    private fun createNeighbourMergeOptions(orderedItems: List<GroupPhase.Group>) {
        var prev: GroupPhase.Group? = null
        for (i in orderedItems.indices) {
            val current = orderedItems[i]
            if (current != null) {
                if (prev != null) {
                    addMergeOption(prev, current, null, null)
                }
                prev = current
            }
        }
    }

    override fun getMyBestPriority(): Int {
        return AbstractRuleBasedGroupingStrategy.UNCONNECTED_NEIGHBOUR
    }

    override fun generate(poll: GroupPhase.Group) {
        for (c in ms.getContainersFor(poll).keys) {
            if (ms.isContainerLive(c)) {
                if (dontDo[c]!!.contains(poll)) {
                    return
                }
                val state = getState(poll)
                when (state) {
                    MergePlane.UNKNOWN -> {
                        generateNeighboursForContainer(c, ms, grouper, MergePlane.X_FIRST_MERGE)
                        generateNeighboursForContainer(c, ms, grouper, MergePlane.Y_FIRST_MERGE)
                    }
                    MergePlane.X_FIRST_MERGE -> generateNeighboursForContainer(c, ms, grouper, MergePlane.X_FIRST_MERGE)
                    MergePlane.Y_FIRST_MERGE -> generateNeighboursForContainer(c, ms, grouper, MergePlane.Y_FIRST_MERGE)
                }
            } else if (dontDo.containsKey(c)) {
                dontDo.remove(c)
            }
        }
    }

    override fun getCode(): String {
        return "ContainerNeighbour"
    }
}