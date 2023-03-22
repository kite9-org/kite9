package org.kite9.diagram.logging

interface Kite9Log {

    enum class Destination {
        OFF, STREAM, FILE
    }

    fun go(): Boolean
    fun send(string: String?)
    fun send(indent: Int, string: String?)
    fun send(prefix: String?, items: Collection<*>)
    fun send(prefix: String?, t: Table)
    fun send(prefix: String?, items: Map<*, *>)
    fun error(string: String?)
    fun error(string: String?, e: Throwable)

    companion object {

        var factory : ((l: Logable) -> Kite9Log)? = null

        fun instance(l: Logable) : Kite9Log {
            return factory?.invoke(l)!!
        }
    }

}