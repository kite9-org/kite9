package org.kite9.diagram.visualization.planarization.rhd.layout

/**
 * The placement approach positions the attr and then works out how much the placement 'costs'
 * in terms of the remaining connections.
 *
 * @author robmoffat
 */
interface PlacementApproach {

    fun evaluate()
    fun getScore(): Double
    fun choose()

    /**
     * Means that the placement order is the same as the numerical ordering of the group ordinals
     */
    fun isNatural(): Boolean
}