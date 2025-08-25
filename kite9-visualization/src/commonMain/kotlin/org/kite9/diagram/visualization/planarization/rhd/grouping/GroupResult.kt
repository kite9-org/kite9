package org.kite9.diagram.visualization.planarization.rhd.grouping

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.grouping.basic.group.Group

/**
 * Returns details of the state of the merge, and the grouping of each container.
 * A "live" group is one which is able to be merged (as opposed to already merged).
 */
abstract class GroupResult {

	protected val containerStates: MutableMap<Container, ContainerStateInfo> = mutableMapOf()

    abstract fun groups(): Collection<Group>

    open fun getStateFor(c: Container): ContainerStateInfo? {
        return containerStates[c]
    }

    fun getContainers() : Collection<Container> {
        return containerStates.keys
    }

    inner class ContainerStateInfo(c: Container) {
		val contents: MutableSet<Group>
		val incompleteSubcontainers: MutableSet<Container>
		var done = false

        init {
            contents = LinkedHashSet(c.getContents().size * 2)
            incompleteSubcontainers = UnorderedSet(4)
            containerStates[c] = this
        }
    }
}