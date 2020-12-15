package org.kite9.diagram.common.algorithms.ssp

import java.lang.RuntimeException

/**
 * This is caused when there is an out of memory error, or a limit on the size of the ssp
 * state gets too big.
 *
 * @author robmoffat
 */
class SSPTooLargeException : RuntimeException {

    constructor(message: String) : super(message) {}
}