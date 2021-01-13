package org.kite9.diagram.visualization.planarization.rhd.grouping

import org.kite9.diagram.common.algorithms.det.UnorderedSet
import org.kite9.diagram.model.Container
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase

/**
 * Returns details of the state of the merge, and the grouping of each container.
 * A "live" group is one which is able to be merged (as opposed to already merged).
 */
abstract class GroupResult {

	protected val containerStates: MutableMap<Container, ContainerStateInfo> = mutableMapOf()

    abstract fun groups(): Collection<GroupPhase.Group>

    open fun getStateFor(c: Container): ContainerStateInfo? {
        return containerStates[c]
    }

    inner class ContainerStateInfo(c: Container) {
		val contents: MutableSet<GroupPhase.Group>
		val incompleteSubcontainers: MutableSet<Container>
		var done = false

        init {
            contents = LinkedHashSet(c.getContents().size * 2)
            incompleteSubcontainers = UnorderedSet(4)
            containerStates[c] = this
        }
    }
}