package org.kite9.diagram.common.algorithms.ssp

import org.kite9.diagram.logging.Logable

/**
 * Abstract implementation of the Dijkstra Successive Shortest Path algorithm.
 *
 * @author robmoffat
 */
abstract class AbstractSSP<P : PathLocation<P>> : Logable {

    open fun getLocation(path: P): Any {
        return path.getLocation()
    }

    @Throws(NoFurtherPathException::class)
    open fun createShortestPath(): P {
        val s = createState()
        lastState = s
        try {
            createInitialPaths(s)
            while (true) {
                var r: P
                do {
                    r = s.remove()
                } while (!r.isActive())
                if (pathComplete(r)) {
                    return r
                }
                generateSuccessivePaths(r, s)
            }
        } catch (e: Exception) {
            throw NoFurtherPathException(e)
        }
    }

    protected open fun createState(): State<P> {
        return State(this)
    }

    /**
     * Returns true if the path arrives at its destination
     */
    protected abstract fun pathComplete(r: P): Boolean

    /**
     * Generates successive valid paths from r, and adds them to the list of
     * paths.
     */
    protected abstract fun generateSuccessivePaths(r: P, s: State<P>)
    protected abstract fun createInitialPaths(s: State<P>)

    override val prefix: String
        get() = "SSP "

    override val isLoggingEnabled: Boolean
        get() = true

    var lastState: State<P>? = null
        private set
}