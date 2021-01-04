package org.kite9.diagram.logging

import org.kite9.diagram.model.DiagramElement
import java.lang.StringBuffer
import java.util.ArrayList

/**
 * Tabular displayer
 *
 */
class Table {
    var widths: MutableList<Int> = ArrayList()
    var rows: MutableList<Array<String?>> = ArrayList()
    fun addRow(vararg items: Any?) {
        val longRow: MutableList<String?> = ArrayList()
        for (`object` in items) {
            if (`object` == null) {
                longRow.add("null")
            } else if (`object`.javaClass.isArray) {
                val length = java.lang.reflect.Array.getLength(`object`)
                for (i in 0 until length) {
                    val item = java.lang.reflect.Array.get(`object`, i)
                    longRow.add(item.toString())
                }
            } else if (`object` is Collection<*>) {
                for (object2 in `object`) {
                    longRow.add(object2?.toString() ?: "")
                }
            } else {
                longRow.add(`object`.toString())
            }
        }
        addArrayRow(longRow.toTypedArray())
    }

    fun removeLastRow() {
        rows.removeAt(rows.size - 1)
    }

    fun addArrayRow(row: Array<String?>) {
        rows.add(row)
        var col = 0
        for (string in row) {
            if (widths.size > col) {
                widths[col] = if (widths[col] > string!!.length) widths[col] else string.length
            } else {
                widths.add(string!!.length)
            }
            col++
        }
    }

    fun <T> addObjectRow(row: Array<T>) {
        val items = arrayOfNulls<String>(row.size)
        for (i in items.indices) {
            items[i] = row[i].toString()
        }
        addArrayRow(items)
    }

    fun addDoubleRow(row: DoubleArray) {
        val items = arrayOfNulls<String>(row.size)
        for (i in items.indices) {
            items[i] = java.lang.Double.toString(row[i])
        }
        addArrayRow(items)
    }

    fun display(sb: StringBuffer) {
        for (row in rows) {
            var colno = 0
            for (col in row) {
                val width = widths[colno] + 1
                sb.append(col)
                for (i in col!!.length until width) {
                    sb.append(" ")
                }
                colno++
            }
            sb.append("\n")
        }
    }
}