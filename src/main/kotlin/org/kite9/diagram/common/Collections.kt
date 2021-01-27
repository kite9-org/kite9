package org.kite9.diagram.common

object Collections {

    fun <T> leftShift(l: Array<T>, d: Int): Array<T> {
        val newList = l.copyOf()
        var shift = d
        if (shift > l.size) shift %= l.size
        l.forEachIndexed { index, value ->
            val newIndex = (index + (l.size - shift)) % l.size
            newList[newIndex] = value
        }
        return newList
    }

    fun <T> leftShiftList(l: MutableList<T>, d: Int) {
        var scratch : T? = null
        l.forEachIndexed { index, value ->
            if (index == 0) {
                scratch = l[index]
            }
            val newIndex = (index + (l.size - d)) % l.size
            if (newIndex == 0) {
                l[index] = scratch!!
            } else {
                l[index] = l[newIndex]
            }
        }
    }
}