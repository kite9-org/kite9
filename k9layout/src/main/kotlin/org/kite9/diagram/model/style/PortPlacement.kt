package org.kite9.diagram.model.style

data class PortPlacement (
    val measurement : Measurement = Measurement.PERCENTAGE,
    val amount : Float = 50.0F) : Comparable<PortPlacement> {

    /**
     * Since we expect all of the ports on the same side to use the same measure, this
     * should be in order. If ports are not the same measure, we'll just use whatever the first port on
     * the side is anyway.
     */
    override fun compareTo(other: PortPlacement): Int {
        return this.amount.compareTo(other.amount)
    }

}
