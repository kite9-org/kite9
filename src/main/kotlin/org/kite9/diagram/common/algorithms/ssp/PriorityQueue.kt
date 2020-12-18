package org.kite9.diagram.common.algorithms.ssp

import org.kite9.diagram.logging.LogicException
import java.util.*
import kotlin.collections.ArrayDeque


/**
 * Taken from Java standard Library and turned into Kotlin.
 * Only works with comparable elements now, for simplicity.
 *
 * @author Josh Bloch, Doug Lea
 * @param <E> the type of elements held in this queue
</E> */
class PriorityQueue<E>(initialCapacity: Int, val comparator: Comparator<in E> ? = null) {

    private val MAX_ARRAY_SIZE = Int.MAX_VALUE - 8

    private var queue : Array<Any?> = arrayOfNulls<Any?>(initialCapacity)
    private var modCount = 0
    var size : Int = 0

    fun size(): Int {
        return size;
    }

    fun peek(): E? {
        return queue[0] as E?
    }

    /**
     * Increases the capacity of the array.
     *
     * @param minCapacity the desired minimum capacity
     */
    private fun grow(minCapacity: Int) {
        val oldCapacity: Int = queue.size
        // Double size if small; else grow by 50%
        var newCapacity = oldCapacity + if (oldCapacity < 64) oldCapacity + 2 else oldCapacity shr 1
        // overflow-conscious code
        if (newCapacity - MAX_ARRAY_SIZE > 0) newCapacity = hugeCapacity(minCapacity)
        queue = queue.copyOf(newCapacity)
    }

    fun add(e: E) : Boolean {
        modCount++
        val i = size
        if (i >= queue.size) grow(i + 1)
        siftUp(i, e)
        size = i + 1
        return true
    }

    fun remove(): E? {
        val es = queue;

        if (size == 0) {
            return null
        }

        var result = es[0] as E
        modCount++;
        val n = --size
        val x = es[n];
        if (n > 0) {
            if (comparator == null)
                siftDownComparable(0, x, es, n)
            else
                siftDownUsingComparator(0, x as E, es, n, comparator)
        }

        return result;
    }

    private fun siftDown(k: Int, x: E) {
        if (comparator != null) siftDownUsingComparator(k, x, queue, size, comparator)
            else siftDownComparable(k, x, queue, size)
    }

    private fun <T> siftDownComparable(k: Int, x: T, es: Array<Any?>, n: Int) {
        // assert n > 0;
        var k = k
        val key = x as Comparable<T>
        val half = n ushr 1 // loop while a non-leaf
        while (k < half) {
            var child = (k shl 1) + 1 // assume left child is least
            var c = es[child]
            val right = child + 1
            if (right < n &&
                (c as Comparable<T>).compareTo(es[right] as T) > 0
            ) c = es[right.also { child = it }]
            if (key.compareTo(c as T) <= 0) break
            es[k] = c
            k = child
        }
        es[k] = key
    }

    private fun <T> siftDownUsingComparator(k: Int, x: T, es: Array<Any?>, n: Int, cmp: Comparator<in T>) {
        // assert n > 0;
        var k = k
        val half = n ushr 1
        while (k < half) {
            var child = (k shl 1) + 1
            var c = es[child]
            val right = child + 1
            if (right < n && cmp.compare(c as T, es[right] as T) > 0) c = es[right.also { child = it }]
            if (cmp.compare(x, c as T) <= 0) break
            es[k] = c
            k = child
        }
        es[k] = x!!
    }

    private fun siftUp(k: Int, x: E) {
        if (comparator != null) siftUpUsingComparator(k, x, queue, comparator) else siftUpComparable(k, x, queue)
    }

    private fun <T> siftUpComparable(k: Int, x: T, es: Array<Any?>) {
        var k = k
        val key = x as Comparable<T>
        while (k > 0) {
            val parent = k - 1 ushr 1
            val e = es[parent]
            if (key.compareTo(e as T) >= 0) break
            es[k] = e
            k = parent
        }
        es[k] = key
    }

    private fun <T> siftUpUsingComparator(k: Int, x: T, es: Array<Any?>, cmp: Comparator<in T>) {
        var k = k
        while (k > 0) {
            val parent = k - 1 ushr 1
            val e = es[parent]
            if (cmp.compare(x, e as T) >= 0) break
            es[k] = e
            k = parent
        }
        es[k] = x
    }

    private fun hugeCapacity(minCapacity: Int): Int {
        if (minCapacity < 0) throw OutOfMemoryError()
        return if (minCapacity > MAX_ARRAY_SIZE) Int.MAX_VALUE else MAX_ARRAY_SIZE
    }



}