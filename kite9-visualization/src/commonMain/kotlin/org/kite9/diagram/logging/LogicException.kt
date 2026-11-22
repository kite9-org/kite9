package org.kite9.diagram.logging


/**
 * LogicExceptions are thrown to represent an underlying bug in Kite9
 * Visualization (as opposed to user-error).
 *
 * @author robmoffat
 */
open class LogicException : RuntimeException {
    constructor() : super()
    constructor(arg0: String?, arg1: Throwable?) : super(arg0, arg1)
    constructor(arg0: String?) : super(arg0)
    constructor(arg0: Throwable?) : super(arg0)
}