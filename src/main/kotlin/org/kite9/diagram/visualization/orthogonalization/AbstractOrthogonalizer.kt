package org.kite9.diagram.visualization.orthogonalization

import org.kite9.diagram.visualization.orthogonalization.Orthogonalizer
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Kite9Log

/**
 * Contains some utility methods that can be used by Orthogonalizers.
 *
 * @author robmoffat
 */
abstract class AbstractOrthogonalizer : Orthogonalizer, Logable {

    @JvmField
	protected var log = Kite9Log.instance(this)

    override val prefix: String
        get() = "ORTH"

    override val isLoggingEnabled: Boolean
        get() = false
}