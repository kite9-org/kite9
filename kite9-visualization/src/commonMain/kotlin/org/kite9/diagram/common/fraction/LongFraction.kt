/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kite9.diagram.common.fraction

/**
 * Representation of a rational number without any overflow. This class is immutable.
 *
 * @version $Revision: 906251 $ $Date: 2010-02-03 16:19:54 -0500 (Wed, 03 Feb 2010) $
 * @since 2.0
 */
class LongFraction(n: Long, d: Long) : Comparable<LongFraction> {

    val numerator: Long
    val denominator: Long

    init {
        var num = n
        var den = d
        if (den <= 0L) {
            throw FractionException(FORBIDDEN_ZERO_DENOMINATOR)
        }
        if (num <= 0L) {
            numerator = 0L
            denominator = 1L
        } else {

            // reduce numerator and denominator by greatest common denominator
            val gcd = gcd(num, den)
            if (gcd > 1) {
                num = num / gcd
                den = den / gcd
            }

            // store the values in the final fields
            numerator = num
            denominator = den
        }
    }

    /**
     *
     * Adds the value of this fraction to another, returning the result in reduced form.
     *
     * @param fraction the [LongFraction] to add, must not be `null`.
     * @return a [LongFraction] instance with the resulting values.
     * @throws NullPointerException if the [LongFraction] is `null`.
     */
    fun add(fraction: LongFraction): LongFraction {
        if (ZERO == fraction) {
            return this
        }
        var num: Long
        var den: Long
        if (denominator == fraction.denominator) {
            num = numerator + fraction.numerator
            den = denominator
        } else {
            num = numerator * fraction.denominator + fraction.numerator * denominator
            den = denominator * fraction.denominator
        }
        return LongFraction(num, den)
    }

    /**
     *
     * Compares this object to another based on size.
     *
     * @param `object` the object to compare to, must not be `null`.
     * @return -1 if this is less than <tt>object</tt>, +1 if this is greater than <tt>object</tt>,
     * 0 if they are equal.
     * @see java.lang.Comparable.compareTo
     */
    override fun compareTo(other: LongFraction): Int {
        val nOd = numerator * other.denominator
        val dOn = denominator * other.numerator
        return nOd.compareTo(dOn)
    }

    /**
     *
     * Test for the equality of two fractions. If the lowest term numerator and denominators are the
     * same for both fractions, the two fractions are considered to be equal.
     *
     * @param other fraction to test for equality to this fraction, can be `null`.
     * @return true if two fractions are equal, false if object is `null`, not an instance of
     * [LongFraction], or not equal to this fraction instance.
     * @see java.lang.Object.equals
     */
    override fun equals(other: Any?): Boolean {
        var ret = false
        if (this === other) {
            ret = true
        } else if (other is LongFraction) {
            val rhs = other.reduce()
            val thisOne = this.reduce()
            ret = thisOne.numerator == rhs.numerator && thisOne.denominator == rhs.denominator
        }
        return ret
    }

    /**
     *
     * Gets a hashCode for the fraction.
     *
     * @return a hash code value for this object.
     * @see java.lang.Object.hashCode
     */
    override fun hashCode(): Int {
        return 37 * (37 * 17 + numerator.hashCode()) + denominator.hashCode()
    }

    /**
     *
     * Multiplies the value of this fraction by another, returning the result in reduced form.
     *
     * @param fraction the fraction to multiply by, must not be `null`.
     * @return a [LongFraction] instance with the resulting values.
     * @throws NullPointerException if the fraction is `null`.
     */
    fun multiply(fraction: LongFraction): LongFraction {
        return if (numerator == 0L || fraction.numerator == 0L) {
            ZERO
        } else LongFraction(numerator * fraction.numerator, denominator * fraction.denominator)
    }

    fun multiply(n: Int): LongFraction {
        return if (numerator == 0L || n == 0) {
            ZERO
        } else LongFraction(numerator * n, denominator)
    }

    /**
     *
     * Reduce this `BigFraction` to its lowest terms.
     *
     * @return the reduced `BigFraction`. It doesn't change anything if the fraction can be reduced.
     */
    fun reduce(): LongFraction {
        val gcd = gcd(numerator, denominator)
        return LongFraction(numerator / gcd, denominator / gcd)
    }

    /**
     *
     * Subtracts the value of another fraction from the value of this one, returning the result in
     * reduced form.
     *
     * @param fraction the [LongFraction] to subtract, must not be `null`.
     * @return a [LongFraction] instance with the resulting values
     * @throws NullPointerException if the fraction is `null`.
     */
    fun subtract(fraction: LongFraction): LongFraction {
        if (ZERO == fraction) {
            return this
        }
        var num: Long
        var den: Long
        if (denominator == fraction.denominator) {
            num = numerator - fraction.numerator
            den = denominator
        } else {
            num = numerator * fraction.denominator - fraction.numerator * denominator
            den = denominator * fraction.denominator
        }
        return LongFraction(num, den)
    }

    fun doubleValue(): Double {
        return numerator.toDouble() / denominator.toDouble()
    }

    fun intValue(): Int {
        return (numerator / denominator).toInt()
    }

    /**
     *
     * Returns the `String` representing this fraction, ie "num / dem" or just "num" if the
     * denominator is one.
     *
     * @return a string representation of the fraction.
     * @see java.lang.Object.toString
     */
    override fun toString(): String {
        var str: String? = null
        str =
                if (1L == denominator) {
                    numerator.toString()
                } else if (0L == numerator) {
                    "0"
                } else {
                    numerator.toString() + " / " + denominator
                }
        return str
    }

    companion object {

        /** A fraction representing "1". */
        val ONE = LongFraction(1, 1)

        /** A fraction representing "0". */
        val ZERO = LongFraction(0, 1)

        /** A fraction representing "1/2". */
        val ONE_HALF = LongFraction(1, 2)

        /** Message for zero denominator. */
        private const val FORBIDDEN_ZERO_DENOMINATOR = "denominator must be different from 0"

        fun getReducedFraction(numerator: Long, denominator: Long): LongFraction {
            return if (numerator == 0L) {
                ZERO // normalize zero.
            } else LongFraction(numerator, denominator)
        }

        fun getReducedFraction(numerator: Int, denominator: Int): LongFraction {
            return getReducedFraction(numerator.toLong(), denominator.toLong())
        }

        fun gcd(a: Long, b: Long): Long {
            return if (b == 0L) a else gcd(b, a % b)
        }
    }
}
