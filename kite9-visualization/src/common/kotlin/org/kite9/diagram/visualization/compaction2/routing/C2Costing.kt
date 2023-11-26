data class C2Costing(  val legalEdgeCrossCost : Int = 0,
                       val turns: Int = 0,
                       val steps: Int = 1,
                       val illegalEdgeCrossings : Int = 0,
                       val minimumTravelledDistance : Int = 0,
                       val minimumPossibleDistance: Int = 0,
                       val minimumExpensiveAxisDistance : Int = 0,
                       val minimumBoundedAxisDistance : Int = 0) : Comparable<C2Costing> {

    constructor() : this(0,0,0,0,0,0) {}
    constructor(c: C2Costing,
                legalEdgeCrossCost : Int = c.legalEdgeCrossCost,
                turns: Int = c.turns,
                illegalEdgeCrossings : Int = c.illegalEdgeCrossings,
                minimumTravelledDistance : Int = c.minimumTravelledDistance,
                minimumPossibleDistance: Int = c.minimumPossibleDistance,
                minimumExpensiveAxisDistance : Int = c.minimumExpensiveAxisDistance,
                minimumBoundedAxisDistance : Int = c.minimumBoundedAxisDistance) : this(
        legalEdgeCrossCost,
        turns,
        c.steps + 1,
        illegalEdgeCrossings,
        minimumTravelledDistance,
        minimumPossibleDistance,
        minimumExpensiveAxisDistance,
        minimumBoundedAxisDistance)

    fun addTurn() : C2Costing {
        return C2Costing(this, turns = this.turns + 1)
    }

    fun setDistances(t: Int, p: Int) : C2Costing {
        return C2Costing(this, minimumPossibleDistance = this.minimumPossibleDistance + p, minimumTravelledDistance = this.minimumTravelledDistance + t)
    }

    fun addStep(): C2Costing {
        return C2Costing(this)
    }

    override fun compareTo(other: C2Costing): Int {
        // expensive (i.e. more important than crossings)
        if (minimumExpensiveAxisDistance != other.minimumExpensiveAxisDistance) {
            return minimumExpensiveAxisDistance.compareTo(other.minimumExpensiveAxisDistance)
        }

        if (illegalEdgeCrossings != other.illegalEdgeCrossings) {
            return illegalEdgeCrossings.compareTo(other.illegalEdgeCrossings)
        }

        // route with minimum amount of crossing
        if (legalEdgeCrossCost != other.legalEdgeCrossCost) {
            return legalEdgeCrossCost.compareTo(other.legalEdgeCrossCost)
        }

        // try to minimize turns
        if (turns != other.turns) {
            return turns.compareTo(other.turns)
        }

        // try to minimize A* minimum possible distance
        if (minimumPossibleDistance != other.minimumPossibleDistance) {
            return minimumPossibleDistance.compareTo(other.minimumPossibleDistance)
        }

        // minimum actual distance
        if (minimumTravelledDistance != other.minimumTravelledDistance) {
            return minimumTravelledDistance.compareTo(other.minimumTravelledDistance)
        }

        if (steps != other.steps) {
            return steps.compareTo(other.steps)
        }

        return  0
   }

    override fun toString(): String {
        return "COST[el=$legalEdgeCrossCost t=$turns s=$steps ei=$illegalEdgeCrossings mtd=$minimumTravelledDistance mpd=$minimumPossibleDistance mbd=$minimumBoundedAxisDistance med=$minimumExpensiveAxisDistance]"
    }

}
