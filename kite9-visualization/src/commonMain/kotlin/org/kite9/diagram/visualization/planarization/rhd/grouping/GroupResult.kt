package org.kite9.diagram.visualization.planarization.rhd.grouping

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Returns details of the state of the merge, and the grouping of each container.
 * A "live" group is one which is able to be merged (as opposed to already merged).
 */
abstract class GroupResult {

	protected val containerStates: MutableMap<Rectangular, ContainerStateInfo> = mutableMapOf()

    abstract fun groups(): Collection<Group>

    open fun getStateFor(c: Rectangular): ContainerStateInfo? {
        return containerStates[c]
    }

    fun getContainers() : Collection<Rectangular> {
        return containerStates.keys
    }

    inner class ContainerStateInfo(c: Rectangular) {
		val contents: MutableSet<Group>
		val incompleteSubcontainers: MutableSet<Rectangular>
		var done = false

        init {
            contents = LinkedHashSet((c.getContents().size ?: 0) * 2)
            incompleteSubcontainers = UnorderedSet(4)
            containerStates[c] = this
        }
    }
}