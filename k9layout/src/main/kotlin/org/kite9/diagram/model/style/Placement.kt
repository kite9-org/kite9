package org.kite9.diagram.model.style

data class Placement(val type: Measurement, val amount: Double) : Comparable<Placement> {

    companion object {

		val NONE = Placement(Measurement.PERCENTAGE, 50.0)
    }

    /**
     * Since we expect all of the ports on the same side to use the same measure, this
     * should be in order.
     */
    override fun compareTo(other: Placement): Int {
        return this.amount.compareTo(other.amount)
    }

}