import kotlin.math.max

data class C2Costing(val allEdgeCrossings : Int = 0,
                     val turns: Int = 0,
                     val steps: Int = 1,
                     val illegalEdgeCrossings : Int = 0,
                     val totalDistance : Int = 0,
                     val minimumPossibleDistance: Int = 0,
                     val expensiveAxisDistance : Int = 0) : Comparable<C2Costing> {

    constructor() : this(0,0,0,0,0,0) {}
    constructor(c: C2Costing,
                allEdgeCrossings : Int = c.allEdgeCrossings,
                turns: Int = c.turns,
                illegalEdgeCrossings : Int = c.illegalEdgeCrossings,
                totalDistance : Int = c.totalDistance,
                minimumPossibleDistance: Int = c.minimumPossibleDistance,
                expensiveAxisDistance : Int = c.expensiveAxisDistance) : this(
        allEdgeCrossings,
        turns,
        c.steps + 1,
        illegalEdgeCrossings,
        totalDistance,
        minimumPossibleDistance,
        expensiveAxisDistance)

    fun addTurn() : C2Costing {
        return C2Costing(this, turns = this.turns + 1)
    }

    fun addDistance(stride: Int, left: Int, expensive: Boolean) : C2Costing {
        val travelledDistance = this.totalDistance + stride
        val expensiveDistance = this.expensiveAxisDistance + if (expensive) stride else 0
        val possibleDistance = max(travelledDistance + left, this.minimumPossibleDistance)
        return C2Costing(this,
            minimumPossibleDistance = possibleDistance,
            totalDistance = travelledDistance,
            expensiveAxisDistance = expensiveDistance)
    }

    fun addStep(): C2Costing {
        return C2Costing(this)
    }

    fun addCrossing(legal: Boolean) : C2Costing {
        val crossings = this.allEdgeCrossings + 1
        val illegalCrossings = this.illegalEdgeCrossings + if (legal) 0 else 1
        return C2Costing(this,
            allEdgeCrossings = crossings,
            illegalEdgeCrossings = illegalCrossings)
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
        if (totalDistance != other.totalDistance) {
            return -totalDistance.compareTo(other.totalDistance)
        }

        if (steps != other.steps) {
            return steps.compareTo(other.steps)
        }

        return  0
   }

    override fun toString(): String {
        // reported in priority order
        return "COST[ead=$expensiveAxisDistance, ix=$illegalEdgeCrossings x=$allEdgeCrossings t=$turns mpd=$minimumPossibleDistance td=$totalDistance s=$steps]"
    }

}
