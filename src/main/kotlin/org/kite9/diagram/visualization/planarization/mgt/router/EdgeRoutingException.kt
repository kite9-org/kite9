package org.kite9.diagram.visualization.planarization.mgt.router

import java.lang.RuntimeException

class EdgeRoutingException : RuntimeException {

    constructor(message: String) : super(message) {}

}