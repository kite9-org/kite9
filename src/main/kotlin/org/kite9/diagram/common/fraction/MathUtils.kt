package org.kite9.diagram.common.fraction

import kotlin.Throws
import java.math.BigInteger
import java.lang.IllegalArgumentException

object MathUtils {
    /**
     * Raise a BigInteger to a long power.
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @exception IllegalArgumentException if e is negative
     */
    @Throws(IllegalArgumentException::class)
    @JvmStatic
    fun pow(k: BigInteger, e: Long): BigInteger {
        var e = e
        if (e < 0) {
            throw RuntimeException("cannot raise an integral value to a negative power ({0}^{1})")
        }
        var result = BigInteger.ONE
        var k2p = k
        while (e != 0L) {
            if (e and 0x1 != 0L) {
                result = result.multiply(k2p)
            }
            k2p = k2p.multiply(k2p)
            e = e shr 1
        }
        return result
    }

    /**
     * Raise a BigInteger to a BigInteger power.
     * @param k number to raise
     * @param e exponent (must be positive or null)
     * @return k<sup>e</sup>
     * @exception IllegalArgumentException if e is negative
     */
    @JvmStatic
    @Throws(IllegalArgumentException::class)
    fun pow(k: BigInteger, e: BigInteger): BigInteger {
        var e = e
        if (e.compareTo(BigInteger.ZERO) < 0) {
            throw FractionException(
                "cannot raise an integral value to a negative power ({0}^{1})");
        }
        var result = BigInteger.ONE
        var k2p = k
        while (BigInteger.ZERO != e) {
            if (e.testBit(0)) {
                result = result.multiply(k2p)
            }
            k2p = k2p.multiply(k2p)
            e = e.shiftRight(1)
        }
        return result
    }
}