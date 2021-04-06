package org.kite9.diagram.model.style

@Deprecated("Move to ports")
data class ConnectionAlignment(val type: Measurement, val amount: Double) {

    companion object {

		val NONE = ConnectionAlignment(Measurement.NONE, 0.0)
    }
}