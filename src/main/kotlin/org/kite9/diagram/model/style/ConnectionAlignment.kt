package org.kite9.diagram.model.style

import org.kite9.diagram.model.style.ConnectionAlignment.Measurement
import org.kite9.diagram.model.style.ConnectionAlignment

data class ConnectionAlignment(val type: Measurement, val amount: Double) {

    enum class Measurement {
        NONE, PERCENTAGE, PIXELS
    }

    companion object {
        @JvmField
		val NONE = ConnectionAlignment(Measurement.NONE, 0.0)
    }
}