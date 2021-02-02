package org.kite9.diagram.model.style

data class ConnectionAlignment(val type: Measurement, val amount: Double) {

    enum class Measurement {
        NONE, PERCENTAGE, PIXELS
    }

    companion object {

		val NONE = ConnectionAlignment(Measurement.NONE, 0.0)
    }
}