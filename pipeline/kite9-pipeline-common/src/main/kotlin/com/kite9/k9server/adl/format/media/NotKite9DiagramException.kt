package com.kite9.k9server.adl.format.media

import org.kite9.diagram.logging.Kite9ProcessingException

class NotKite9DiagramException : Kite9ProcessingException {

    constructor(arg0: String?, arg1: Throwable?) : super(arg0!!, arg1) {}
    constructor(string: String?) : super(string) {}

}