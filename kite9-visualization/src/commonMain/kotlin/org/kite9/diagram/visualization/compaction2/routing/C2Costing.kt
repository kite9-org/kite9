package org.kite9.diagram.visualization.compaction2.routing

import kotlin.math.max

enum class CostFreeTurn { CLOCKWISE, ANTICLOCKWISE }

data class C2Costing(val allEdgeCrossings : Int = 0,
                     val turns: Int = 0,
                     val costFreeTurn: CostFreeTurn? = null,
                     val steps: Int = 1,
                     val illegalEdgeCrossings : Int = 0,
                     val totalWeightedDistance : Int = 0,
                     val minimumPossibleDistance: Int = 0,
                     val expensiveAxisDistance : Int = 0,
                     val containerDepth: Int) : Comparable<C2Costing> {

    constructor(startContainerDepth: Int) : this(0,0,null, 0,0,0,0, 0, startContainerDepth)
    constructor(c: C2Costing,
                allEdgeCrossings : Int = c.allEdgeCrossings,
                turns: Int = c.turns,
                costFreeTurn: CostFreeTurn? = c.costFreeTurn,
                illegalEdgeCrossings : Int = c.illegalEdgeCrossings,
                totalDistance : Int = c.totalWeightedDistance,
                minimumPossibleDistance: Int = c.minimumPossibleDistance,
                expensiveAxisDistance : Int = c.expensiveAxisDistance,
                containerDepth: Int = c.containerDepth) : this(
        allEdgeCrossings,
        turns,
        costFreeTurn,
        c.steps + 1,
        illegalEdgeCrossings,
        totalDistance,
        minimumPossibleDistance,
        expensiveAxisDistance,
        containerDepth)

    fun addTurn(d: CostFreeTurn) : C2Costing {
        return if (d == this.costFreeTurn) {
            // complete 180
            C2Costing(this, turns = this.turns + 2, costFreeTurn = d)
        } else {
            // currently just a dog-leg or straight, count as zero
            C2Costing(this, costFreeTurn = d)
        }
    }

    fun addDistance(stride: Int, left: Int, expensive: Boolean) : C2Costing {
        val travelledDistance = this.totalWeightedDistance + (stride * containerDepth)
        val expensiveDistance = this.expensiveAxisDistance + if (expensive) stride else 0
        val possibleDistance = max(travelledDistance + left, this.minimumPossibleDistance)
        val newCostFreeTurn = if (stride > 0) { null } else { this.costFreeTurn }
        val newTurns = if ((this.costFreeTurn != null) && (stride > 0)) this.turns + 1 else this.turns
        return C2Costing(this,
            minimumPossibleDistance = possibleDistance,
            totalDistance = travelledDistance,
            turns = newTurns,
            costFreeTurn = newCostFreeTurn,
            expensiveAxisDistance = expensiveDistance)
    }

    fun addStep(): C2Costing {
        return C2Costing(this)
    }

    fun addCrossing(legal: Boolean, newContainerDepth: Int) : C2Costing {
        val crossings = this.allEdgeCrossings + 1
        val illegalCrossings = this.illegalEdgeCrossings + if (legal) 0 else 1
        return C2Costing(this,
            allEdgeCrossings = crossings,
            illegalEdgeCrossings = illegalCrossings,
            containerDepth = newContainerDepth
        )
    }

    override fun compareTo(other: C2Costing): Int {
        // expensive (i.e. more important than crossings)
        if (expensiveAxisDistance != other.expensiveAxisDistance) {
            return expensiveAxisDistance.compareTo(other.expensiveAxisDistance)
        }

        if (illegalEdgeCrossings != other.illegalEdgeCrossings) {
            return illegalEdgeCrossings.compareTo(other.illegalEdgeCrossings)
        }

        // route with minimum amount of crossing
        if (allEdgeCrossings != other.allEdgeCrossings) {
            return allEdgeCrossings.compareTo(other.allEdgeCrossings)
        }

        // try to minimize turns
        if (turns != other.turns) {
            return turns.compareTo(other.turns)
        }

        // try to minimize A* minimum possible distance
        if (minimumPossibleDistance != other.minimumPossibleDistance) {
            return minimumPossibleDistance.compareTo(other.minimumPossibleDistance)
        }

        // pick the route that has gone farthest
        if (totalWeightedDistance != other.totalWeightedDistance) {
            return -totalWeightedDistance.compareTo(other.totalWeightedDistance)
        }

        if (steps != other.steps) {
            return steps.compareTo(other.steps)
        }

        return  0
   }

    override fun toString(): String {
        // reported in priority order
        return "COST[ead=$expensiveAxisDistance, ix=$illegalEdgeCrossings x=$allEdgeCrossings t=$turns mpd=$minimumPossibleDistance td=$totalWeightedDistance s=$steps cft=$costFreeTurn]"
    }

}
