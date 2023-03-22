package org.kite9.diagram.common.algorithms.ssp

class NoFurtherPathException : Exception {

    constructor(cause: Throwable) : super(cause) {}

    constructor(cause: String) : super(cause) {}

}