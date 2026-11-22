package org.kite9.diagram.visualization.planarization.rhd.grouping.directed

enum class ContainerMergeType(val priorityAdjustment: Int) {

    WITHIN_LIVE_CONTAINER(0), JOINING_EXTRA_CONTAINERS(20), NO_LIVE_CONTAINER(30);

}