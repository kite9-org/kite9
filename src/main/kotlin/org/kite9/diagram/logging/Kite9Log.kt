package org.kite9.diagram.logging

import org.kite9.diagram.logging.Logable
import org.kite9.diagram.logging.Kite9Log
import java.io.File
import java.lang.StringBuffer
import java.util.Collections
import java.io.PrintStream
import java.io.FileOutputStream
import java.io.FileNotFoundException
import java.lang.Exception
import java.util.ArrayList

/**
 * Very simple abstraction class for all logging functions.
 *
 * @author robmoffat
 */
class Kite9Log(var logFor: Logable) {
    enum class Destination {
        OFF, STREAM, FILE
    }

    fun go(): Boolean {
        return logFile == null
    }

    fun send(string: String?) {
        if (logFor.isLoggingEnabled && logFile != null) logFile!!.println(logFor.prefix + " " + string)
    }

    fun send(indent: Int, string: String?) {
        if (logFor.isLoggingEnabled && logFile != null) logFile!!.print(logFor.prefix)
        logFile!!.write(INDENT.toByteArray(), 0, 1 + indent)
        logFile!!.println(string)
    }

    fun send(prefix: String?, items: Collection<*>) {
        if (logFor.isLoggingEnabled && logFile != null) {
            logFile!!.println(logFor.prefix + " " + prefix)
            val sb = StringBuffer()
            for (o in items) {
                sb.append("\t")
                sb.append(o.toString())
                sb.append("\n")
            }
            logFile!!.println(sb.toString())
        }
    }

    fun send(prefix: String?, t: Table) {
        if (logFor.isLoggingEnabled && logFile != null) {
            val sb = StringBuffer(1000)
            t.display(sb)
            send(prefix + sb.toString())
        }
    }

    fun send(prefix: String?, items: Map<*, *>) {
        if (logFor.isLoggingEnabled && logFile != null) {
            logFile!!.println(logFor.prefix + " " + prefix)
            val t = Table()
            val keys = items.keys
            val keyList: ArrayList<Any?> = ArrayList(keys)
            Collections.sort(keyList) { o1, o2 ->
                if (o1 == null) {
                    -1
                } else if (o2 == null) {
                    1
                } else {
                    o1.toString().compareTo(o2.toString())
                }
            }
            for (`object` in keyList) {
                if (`object` != null) {
                    t.addRow(*arrayOf<Any>("\t", `object`.toString(), items[`object`].toString()))
                }
            }
            val sb = StringBuffer()
            t.display(sb)
            logFile!!.println(sb.toString())
        }
    }

    fun error(string: String?) {
        System.err.println(logFor.prefix + " " + string)
    }

    fun error(string: String?, e: Throwable) {
        System.err.println(logFor.prefix + " " + string)
        e.printStackTrace()
    }

    fun send(prefix: String?, arg0: Exception) {
        logFile!!.println(logFor.prefix + " " + prefix)
        arg0.printStackTrace(System.out)
    }

    companion object {
        private val INDENT = String(CharArray(100)).replace('\u0000', ' ')
        var logFile: PrintStream? = null
        fun setLogging(state: Destination) {
            if (state == Destination.FILE) {
                try {
                    logFile = PrintStream(FileOutputStream(File("kite9.log")))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    logFile = System.out
                }
            } else if (state == Destination.STREAM) {
                logFile = System.out
            } else {
                logFile = null
            }
        }
    }
}