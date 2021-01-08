package org.kite9.diagram.visualization.planarization.mgt.router

/**
 * Line Routing objects are immutable.  They represent a route through the planarization
 */
interface LineRoutingInfo {

    val horizontalRunningCost: Double
    val verticalRunningCost: Double
    val runningCost: Double

}