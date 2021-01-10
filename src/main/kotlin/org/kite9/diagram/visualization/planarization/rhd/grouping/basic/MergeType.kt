package org.kite9.diagram.visualization.planarization.rhd.grouping.basic

/**
 * There are basically 3 types of merge.
 *
 *
 *  1. Linked merge:  where we are combining two groups together which share links between each other.  This is the best kind.
 *  1. Aligned merge:  where we combine two groups together which have a common link to a third group.   This is also useful, especially when the links are directed.
 *  1. Neighbour merge: where we combine two groups based on the fact that they are in the same container.  This is kind of a last-ditch option.
 *
 *
 * @author robmoffat
 */
enum class MergeType {

    LINKED, ALIGNED, NEIGHBOUR

}