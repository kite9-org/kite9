package org.kite9.diagram.visualization.planarization.rhd.grouping.generators.merge

import org.kite9.diagram.common.algorithms.ssp.PriorityQueue
import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.merge.MergeOption
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.PriorityRule
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.merge.DirectedMergeState
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.MergeGenerator
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler
import java.util.*

/**
 * Extends the merge state for the generator based grouping strategy. Keeps a
 * list of which groups a generator has not been run on.
 *
 * @author robmoffat
 */
class GeneratorMergeState(ch: ContradictionHandler, elements: Int) : DirectedMergeState(ch) {

    var generators: List<MergeGenerator>? = null
    var rules: List<PriorityRule>? = null
    var toDo: PriorityQueue<Group>? = null

    init {

    }

    override fun initialise(capacity: Int, containers: Int, log: Kite9Log?) {
        super.initialise(capacity, containers, log)
        toDo = PriorityQueue(capacity + 1, Comparator<Group> { arg0, arg1 ->
            if (arg0.size != arg1.size) {
                arg0.size.compareTo(arg1.size)
            } else {
                arg0.linkManager.linkCount
                    .compareTo(arg1.linkManager.linkCount)
            }
        })
    }

    override fun addLiveGroup(group: Group) {
        super.addLiveGroup(group)
        toDo!!.add(group)
    }

    fun nextLiveGroup(): Group? {
        return toDo!!.remove()
    }

}