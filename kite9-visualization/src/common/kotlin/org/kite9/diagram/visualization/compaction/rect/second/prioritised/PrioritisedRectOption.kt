package org.kite9.diagram.visualization.compaction.rect.second.prioritised

import org.kite9.diagram.logging.LogicException
import org.kite9.diagram.model.Rectangular
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep
import org.kite9.diagram.visualization.compaction.rect.VertexTurn
import org.kite9.diagram.visualization.compaction.rect.VertexTurn.TurnPriority
import kotlin.math.max

class PrioritisedRectOption(
    i: Int,
    vt1: VertexTurn,
    vt2: VertexTurn,
    vt3: VertexTurn,
    vt4: VertexTurn,
    vt5: VertexTurn,
    m: PrioritizingRectangularizer.Match,
    fromStack: MutableList<VertexTurn>,
    val acs: AbstractCompactionStep
) : RectOption(i, vt1, vt2, vt3, vt4, vt5, m, fromStack) {
    /**
     * Refers to whether the option turns back on itself (G), or continues the way it was going (U).
     * @author robmoffat
     */
    enum class TurnShape {
        U, G
    }

    enum class GrowthRisk {
        ZERO, LOW, HIGH
    }

    enum class TurnType(val cost: Int, val meetsTurnPriority: TurnPriority, val growthLikelihood: GrowthRisk) {
        CONNECTION_FAN(-100000, TurnPriority.CONNECTION, GrowthRisk.ZERO),
        CONTAINER_LABEL_MAXIMIZE(-30000, TurnPriority.MAXIMIZE_RECTANGULAR, GrowthRisk.ZERO),
        EXTEND_PREFERRED(0, TurnPriority.MAXIMIZE_RECTANGULAR, GrowthRisk.ZERO),
        MINIMIZE_RECT_SIDE_PART_G(20000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.LOW),  // lines up connecteds joining to a connection
        CONNECTION_ZERO(40000, TurnPriority.CONNECTION, GrowthRisk.ZERO),
        CONNECTION_LOW(50000, TurnPriority.CONNECTION, GrowthRisk.LOW),
        CONNECTION_HIGH(50000, TurnPriority.CONNECTION, GrowthRisk.HIGH),
        MINIMIZE_RECT_ZERO(60000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.ZERO),
        MINIMIZE_RECT_LOW(60000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.LOW),
        MINIMIZE_RECT_HIGH(70000, TurnPriority.MINIMIZE_RECTANGULAR, GrowthRisk.HIGH);

    }

    var type: TurnType = calculateType()

    /**
     * Lower scores are better rect options
     */
    override fun calculateScore(): Int {
        // do safe ones last
        val pushOut = calculatePushOut()
        val typeCost = type.cost
        val deduction = getDeduction(pushOut)
        return pushOut + typeCost + deduction
    }

    private fun getDeduction(pushOut: Int): Int {
        return if (pushOut == 0 && type.growthLikelihood == GrowthRisk.ZERO) {
            -30000
        } else {
            0
        }
    }

    override fun rescore() {
        type = calculateType()
        super.rescore()
    }

    private fun calculatePushOut(): Int {
        val par = par
        val meets = meets
        val tp = meets.turnPriority
        val meetsLength = meets.getLength(true)
        return if (turnShape == TurnShape.G) {
            val parLength = par.getLength(true)
            var meetsExtension = 0.0
            meetsExtension = acs.getMinimumDistance(post.slideable, extender.slideable, meets.slideable, true)
            val distance = max(0.0, parLength + meetsExtension - meetsLength).toInt()
            distance * tp.costFactor
        } else {
            val parLength = par.getLength(true)
            val distance = max(0.0, parLength - meetsLength).toInt()
            distance * tp.costFactor
        }
    }

    fun calculateType(): TurnType {
        val extender = extender
        val meets = meets
        val par = par
        if (extender.isFanTurn(par)) {
            val parDirection = getTurnDirection(par)
            val fanDirections = extender.innerFanVertex!!.fanSides
            if (fanDirections[0] === parDirection) {
                return TurnType.CONNECTION_FAN
            }
        }
        if (meets.turnPriority === TurnPriority.MAXIMIZE_RECTANGULAR || inside(par, meets)) {
            return if (par.isContainerLabelOnSide(extender.direction)) {
                TurnType.CONTAINER_LABEL_MAXIMIZE
            } else {
                TurnType.EXTEND_PREFERRED
            }
        }
        return if (turnShape == TurnShape.U) {
            getUShapedTypes(meets, link, par)
        } else {
            getGShapedTypes(meets, link, par, post.turnPriority)
        }
    }

    private fun inside(par: VertexTurn, meets: VertexTurn): Boolean {
        val containers = meets.slideable.rectangulars
        val count = par.slideable.rectangulars
            .filter { r: Rectangular ->
                if (containers.contains(r.getParent())) {
                    return@filter true
                }
            false
        }.count()
        return count > 0
    }

    private fun getUShapedTypes(meetsTurn: VertexTurn, linkTurn: VertexTurn, parTurn: VertexTurn): TurnType {
        val meetsPriority = meetsTurn.turnPriority
        if (meetsPriority === TurnPriority.MAXIMIZE_RECTANGULAR) {
            return TurnType.EXTEND_PREFERRED
        } else if (meetsPriority === TurnPriority.MINIMIZE_RECTANGULAR) {
            return getMinimizeTurnType(parTurn)
        } else if (meetsPriority === TurnPriority.CONNECTION) {
            return getConnectionTurnType(parTurn)
        }
        throw LogicException()
    }

    private fun getMinimizeTurnType(parTurn: VertexTurn): TurnType {
        return if (parTurn.isNonExpandingLength) {
            TurnType.MINIMIZE_RECT_ZERO
        } else if (parTurn.turnPriority === TurnPriority.MINIMIZE_RECTANGULAR) {
            TurnType.MINIMIZE_RECT_LOW
        } else {
            TurnType.MINIMIZE_RECT_HIGH
        }
    }

    private fun getConnectionTurnType(parTurn: VertexTurn): TurnType {
        return if (parTurn.isNonExpandingLength) {
            TurnType.CONNECTION_ZERO
        } else if (parTurn.turnPriority !== TurnPriority.MAXIMIZE_RECTANGULAR) {
            TurnType.CONNECTION_LOW
        } else {
            TurnType.CONNECTION_HIGH
        }
    }

    private fun getGShapedTypes(meets: VertexTurn, link: VertexTurn, par: VertexTurn, post: TurnPriority): TurnType {
        val meetsTurnPriority = meets.turnPriority
        return if (meetsTurnPriority === TurnPriority.CONNECTION) {
            getConnectionTurnType(par)
        } else if (meetsTurnPriority === TurnPriority.MAXIMIZE_RECTANGULAR) {
            TurnType.EXTEND_PREFERRED
        } else if (meetsTurnPriority === TurnPriority.MINIMIZE_RECTANGULAR) {
            when (link.turnPriority) {
                TurnPriority.CONNECTION -> if (post === TurnPriority.CONNECTION) {
                    TurnType.MINIMIZE_RECT_SIDE_PART_G
                } else {
                    getMinimizeTurnType(par)
                }
                TurnPriority.MINIMIZE_RECTANGULAR -> getMinimizeTurnType(par)
                TurnPriority.MAXIMIZE_RECTANGULAR ->                // can be for keys, apparently
                    TurnType.EXTEND_PREFERRED
                else -> TurnType.EXTEND_PREFERRED
            }
        } else {
            throw LogicException()
        }
    }

    val turnShape: TurnShape
        get() = if (post.direction === extender.direction) TurnShape.U else TurnShape.G

    override fun toString(): String {
        return """
            
            [RO: $i(${initialScore}), meetsType = $type, extender = ${extender.slideable}]
            """.trimIndent()
    }

    init {
        rescore()
    }
}