package org.kite9.diagram.js.logging

import org.kite9.diagram.logging.Kite9Log
import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Table

class JSKite9Log(val l : Logable) : Kite9Log {

    override fun go(): Boolean {
        return l.isLoggingEnabled;
    }

    override fun send(string: String?) {
        if (string != null) {
            console.log(string)
        }
    }

    override fun send(indent: Int, string: String?) {
        if (string != null) {
            console.log(" ".repeat(indent) + string)
        }
    }

    override fun send(prefix: String?, items: Collection<*>) {
//        console.log(prefix+ " "+items
//            .map { it.toString() }
//            .reduceRight { a, s -> a + s })
    }

    override fun send(prefix: String?, t: Table) {
    }

    override fun send(prefix: String?, items: Map<*, *>) {
    }

    override fun error(string: String?) {
        if (string != null) {
            console.error(string)
        }
    }

    override fun error(string: String?, e: Throwable) {
        if (string != null) {
            console.error(string)
        }
        console.error(e)
    }
}