package org.kite9.diagram.model.style

/**
 * A RectangularPosition where we specify the exact point using either absolute or relative measurement.
 */
data class MeasuredRectangularPosition(val type: Measurement, val amount: Double) : Comparable<MeasuredRectangularPosition>, RectangularPosition {

    companion object {

		val NONE = MeasuredRectangularPosition(Measurement.PERCENTAGE, 50.0)
    }

    /**
     * Since we expect all of the ports on the same side to use the same measure, this
     * should be in order.
     */
    override fun compareTo(other: MeasuredRectangularPosition): Int {
        return this.amount.compareTo(other.amount)
    }

}