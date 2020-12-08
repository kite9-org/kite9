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

import org.kite9.diagram.common.fraction.MathUtils.pow
import java.math.BigInteger
import java.io.Serializable
import java.math.BigDecimal

/**
 * Representation of a rational number without any overflow. This class is
 * immutable.
 *
 * @version $Revision: 906251 $ $Date: 2010-02-03 16:19:54 -0500 (Wed, 03 Feb 2010) $
 * @since 2.0
 */
class BigFraction: FieldElement<BigFraction>, Comparable<BigFraction>, Serializable {

    val numerator: BigInteger
    val denominator: BigInteger

    @JvmOverloads
    constructor(numI: BigInteger, denI: BigInteger = BigInteger.ONE) {
        var num = numI
        var den = denI
        if (BigInteger.ZERO == den) {
            throw FractionException(FORBIDDEN_ZERO_DENOMINATOR)
        }
        if (BigInteger.ZERO == num) {
            numerator = BigInteger.ZERO
            denominator = BigInteger.ONE
        } else {

            // reduce numerator and denominator by greatest common denominator
            val gcd = num.gcd(den)
            if (BigInteger.ONE.compareTo(gcd) < 0) {
                num = num.divide(gcd)
                den = den.divide(gcd)
            }

            // move sign to numerator
            if (BigInteger.ZERO.compareTo(den) > 0) {
                num = num.negate()
                den = den.negate()
            }

            // store the values in the final fields
            numerator = num
            denominator = den
        }
    }


    /**
     * Create a fraction given the double value and maximum error allowed.
     *
     *
     * References:
     *
     *  * [
 * Continued Fraction](http://mathworld.wolfram.com/ContinuedFraction.html) equations (11) and (22)-(26)
     *
     *
     *
     * @param value
     * the double value to convert to a fraction.
     * @param epsilon
     * maximum error allowed. The resulting fraction is within
     * `epsilon` of `value`, in absolute terms.
     * @param maxIterations
     * maximum number of convergents.
     * @throws FractionException
     * if the continued fraction failed to converge.
     * @see .BigFraction
     */
    constructor(
        value: Double, epsilon: Double,
        maxIterations: Int
    ) : this(value, epsilon, Int.MAX_VALUE, maxIterations) {
    }

    /**
     * Create a fraction given the double value and either the maximum error
     * allowed or the maximum number of denominator digits.
     *
     *
     *
     * NOTE: This constructor is called with EITHER - a valid epsilon value and
     * the maxDenominator set to Integer.MAX_VALUE (that way the maxDenominator
     * has no effect). OR - a valid maxDenominator value and the epsilon value
     * set to zero (that way epsilon only has effect if there is an exact match
     * before the maxDenominator value is reached).
     *
     *
     *
     *
     * It has been done this way so that the same code can be (re)used for both
     * scenarios. However this could be confusing to users if it were part of
     * the public API and this constructor should therefore remain PRIVATE.
     *
     *
     * See JIRA issue ticket MATH-181 for more details:
     *
     * https://issues.apache.org/jira/browse/MATH-181
     *
     * @param value
     * the double value to convert to a fraction.
     * @param epsilon
     * maximum error allowed. The resulting fraction is within
     * `epsilon` of `value`, in absolute terms.
     * @param maxDenominator
     * maximum denominator value allowed.
     * @param maxIterations
     * maximum number of convergents.
     * @throws FractionException
     * if the continued fraction failed to converge.
     */
    private constructor(
        value: Double, epsilon: Double,
        maxDenominator: Int, maxIterations: Int
    ) {
        val overflow = Int.MAX_VALUE.toLong()
        var r0 = value
        var a0 = Math.floor(r0).toLong()
        if (a0 > overflow) {
            throw FractionException(value, a0, 1L)
        }

        // check for (almost) integer arguments, which should not go
        // to iterations.
        if (Math.abs(a0 - value) < epsilon) {
            numerator = BigInteger.valueOf(a0)
            denominator = BigInteger.ONE
            return
        }
        var p0: Long = 1
        var q0: Long = 0
        var p1 = a0
        var q1: Long = 1
        var p2: Long = 0
        var q2: Long = 1
        var n = 0
        var stop = false
        do {
            ++n
            val r1 = 1.0 / (r0 - a0)
            val a1 = Math.floor(r1).toLong()
            p2 = a1 * p1 + p0
            q2 = a1 * q1 + q0
            if (p2 > overflow || q2 > overflow) {
                throw FractionException(value, p2, q2)
            }
            val convergent = p2.toDouble() / q2.toDouble()
            if (n < maxIterations &&
                Math.abs(convergent - value) > epsilon &&
                q2 < maxDenominator
            ) {
                p0 = p1
                p1 = p2
                q0 = q1
                q1 = q2
                a0 = a1
                r0 = r1
            } else {
                stop = true
            }
        } while (!stop)
        if (n >= maxIterations) {
            throw FractionException(value, maxIterations)
        }
        if (q2 < maxDenominator) {
            numerator = BigInteger.valueOf(p2)
            denominator = BigInteger.valueOf(q2)
        } else {
            numerator = BigInteger.valueOf(p1)
            denominator = BigInteger.valueOf(q1)
        }
    }

    /**
     * Create a fraction given the double value and maximum denominator.
     *
     *
     * References:
     *
     *  * [
 * Continued Fraction](http://mathworld.wolfram.com/ContinuedFraction.html) equations (11) and (22)-(26)
     *
     *
     *
     * @param value
     * the double value to convert to a fraction.
     * @param maxDenominator
     * The maximum allowed value for denominator.
     * @throws FractionException
     * if the continued fraction failed to converge.
     */
    constructor(value: Double, maxDenominator: Int) : this(value, 0.0, maxDenominator, 100) {}

    /**
     *
     *
     * Create a [BigFraction] equivalent to the passed <tt>int</tt>, ie
     * "num / 1".
     *
     *
     * @param num
     * the numerator.
     */
    constructor(num: Int) : this(BigInteger.valueOf(num.toLong()), BigInteger.ONE) {}

    /**
     *
     *
     * Create a [BigFraction] given the numerator and denominator as simple
     * <tt>int</tt>. The [BigFraction] is reduced to lowest terms.
     *
     *
     * @param num
     * the numerator.
     * @param den
     * the denominator.
     */
    constructor(num: Int, den: Int) : this(BigInteger.valueOf(num.toLong()), BigInteger.valueOf(den.toLong())) {}

    /**
     *
     *
     * Create a [BigFraction] equivalent to the passed long, ie "num / 1".
     *
     *
     * @param num
     * the numerator.
     */
    constructor(num: Long) : this(BigInteger.valueOf(num), BigInteger.ONE) {}

    /**
     *
     *
     * Create a [BigFraction] given the numerator and denominator as simple
     * <tt>long</tt>. The [BigFraction] is reduced to lowest terms.
     *
     *
     * @param num
     * the numerator.
     * @param den
     * the denominator.
     */
    constructor(num: Long, den: Long) : this(BigInteger.valueOf(num), BigInteger.valueOf(den)) {}

    /**
     *
     *
     * Returns the absolute value of this [BigFraction].
     *
     *
     * @return the absolute value as a [BigFraction].
     */
    fun abs(): BigFraction {
        return if (BigInteger.ZERO.compareTo(numerator) <= 0) this else negate()
    }

    /**
     *
     *
     * Adds the value of this fraction to the passed [BigInteger],
     * returning the result in reduced form.
     *
     *
     * @param bg
     * the [BigInteger] to add, must'nt be `null`.
     * @return a `BigFraction` instance with the resulting values.
     * @throws NullPointerException
     * if the [BigInteger] is `null`.
     */
    fun add(bg: BigInteger?): BigFraction {
        return BigFraction(numerator.add(denominator.multiply(bg)), denominator)
    }

    /**
     *
     *
     * Adds the value of this fraction to the passed <tt>integer</tt>, returning
     * the result in reduced form.
     *
     *
     * @param i
     * the <tt>integer</tt> to add.
     * @return a `BigFraction` instance with the resulting values.
     */
    fun add(i: Int): BigFraction {
        return add(BigInteger.valueOf(i.toLong()))
    }

    /**
     *
     *
     * Adds the value of this fraction to the passed <tt>long</tt>, returning
     * the result in reduced form.
     *
     *
     * @param l
     * the <tt>long</tt> to add.
     * @return a `BigFraction` instance with the resulting values.
     */
    fun add(l: Long): BigFraction {
        return add(BigInteger.valueOf(l))
    }

    /**
     *
     *
     * Adds the value of this fraction to another, returning the result in
     * reduced form.
     *
     *
     * @param fraction
     * the [BigFraction] to add, must not be `null`.
     * @return a [BigFraction] instance with the resulting values.
     * @throws NullPointerException
     * if the [BigFraction] is `null`.
     */
    override fun add(fraction: BigFraction): BigFraction {
        if (ZERO == fraction) {
            return this
        }
        var num: BigInteger
        var den: BigInteger
        if (denominator == fraction.denominator) {
            num = numerator.add(fraction.numerator)
            den = denominator
        } else {
            num = numerator.multiply(fraction.denominator).add(fraction.numerator.multiply(denominator))
            den = denominator.multiply(fraction.denominator)
        }
        return BigFraction(num, den)
    }

    /**
     *
     *
     * Gets the fraction as a `BigDecimal`. This calculates the
     * fraction as the numerator divided by denominator.
     *
     *
     * @return the fraction as a `BigDecimal`.
     * @throws ArithmeticException
     * if the exact quotient does not have a terminating decimal
     * expansion.
     * @see BigDecimal
     */
    fun bigDecimalValue(): BigDecimal {
        return BigDecimal(numerator).divide(BigDecimal(denominator))
    }

    /**
     *
     *
     * Gets the fraction as a `BigDecimal` following the passed
     * rounding mode. This calculates the fraction as the numerator divided by
     * denominator.
     *
     *
     * @param roundingMode
     * rounding mode to apply. see [BigDecimal] constants.
     * @return the fraction as a `BigDecimal`.
     * @throws IllegalArgumentException
     * if <tt>roundingMode</tt> does not represent a valid rounding
     * mode.
     * @see BigDecimal
     */
    fun bigDecimalValue(roundingMode: Int): BigDecimal {
        return BigDecimal(numerator).divide(BigDecimal(denominator), roundingMode)
    }

    /**
     *
     *
     * Gets the fraction as a `BigDecimal` following the passed scale
     * and rounding mode. This calculates the fraction as the numerator divided
     * by denominator.
     *
     *
     * @param scale
     * scale of the `BigDecimal` quotient to be returned.
     * see [BigDecimal] for more information.
     * @param roundingMode
     * rounding mode to apply. see [BigDecimal] constants.
     * @return the fraction as a `BigDecimal`.
     * @see BigDecimal
     */
    fun bigDecimalValue(scale: Int, roundingMode: Int): BigDecimal {
        return BigDecimal(numerator).divide(BigDecimal(denominator), scale, roundingMode)
    }

    /**
     *
     *
     * Compares this object to another based on size.
     *
     *
     * @param object
     * the object to compare to, must not be `null`.
     * @return -1 if this is less than <tt>object</tt>, +1 if this is greater
     * than <tt>object</tt>, 0 if they are equal.
     * @see java.lang.Comparable.compareTo
     */
    override fun compareTo(`object`: BigFraction): Int {
        val nOd = numerator.multiply(`object`.denominator)
        val dOn = denominator.multiply(`object`.numerator)
        return nOd.compareTo(dOn)
    }

    /**
     *
     *
     * Divide the value of this fraction by the passed `BigInteger`,
     * ie "this * 1 / bg", returning the result in reduced form.
     *
     *
     * @param bg
     * the `BigInteger` to divide by, must not be
     * `null`.
     * @return a [BigFraction] instance with the resulting values.
     * @throws NullPointerException
     * if the `BigInteger` is `null`.
     * @throws ArithmeticException
     * if the fraction to divide by is zero.
     */
    fun divide(bg: BigInteger): BigFraction {
        if (BigInteger.ZERO == bg) {
            throw FractionException(FORBIDDEN_ZERO_DENOMINATOR)
        }
        return BigFraction(numerator, denominator.multiply(bg))
    }

    /**
     *
     *
     * Divide the value of this fraction by the passed <tt>int</tt>, ie
     * "this * 1 / i", returning the result in reduced form.
     *
     *
     * @param i
     * the <tt>int</tt> to divide by.
     * @return a [BigFraction] instance with the resulting values.
     * @throws ArithmeticException
     * if the fraction to divide by is zero.
     */
    fun divide(i: Int): BigFraction {
        return divide(BigInteger.valueOf(i.toLong()))
    }

    /**
     *
     *
     * Divide the value of this fraction by the passed <tt>long</tt>, ie
     * "this * 1 / l", returning the result in reduced form.
     *
     *
     * @param l
     * the <tt>long</tt> to divide by.
     * @return a [BigFraction] instance with the resulting values.
     * @throws ArithmeticException
     * if the fraction to divide by is zero.
     */
    fun divide(l: Long): BigFraction {
        return divide(BigInteger.valueOf(l))
    }

    /**
     *
     *
     * Divide the value of this fraction by another, returning the result in
     * reduced form.
     *
     *
     * @param fraction
     * the fraction to divide by, must not be `null`.
     * @return a [BigFraction] instance with the resulting values.
     * @throws NullPointerException
     * if the fraction is `null`.
     * @throws ArithmeticException
     * if the fraction to divide by is zero.
     */
    override fun divide(fraction: BigFraction): BigFraction {
        if (BigInteger.ZERO == fraction.numerator) {
            throw FractionException(FORBIDDEN_ZERO_DENOMINATOR)
        }
        return multiply(fraction.reciprocal())
    }

    /**
     *
     *
     * Gets the fraction as a <tt>double</tt>. This calculates the fraction as
     * the numerator divided by denominator.
     *
     *
     * @return the fraction as a <tt>double</tt>
     * @see java.lang.Number.doubleValue
     */
    fun doubleValue(): Double {
        return numerator.toDouble() / denominator.toDouble()
    }

    fun intValue(): Int {
        return doubleValue().toInt();
    }

    /**
     *
     *
     * Test for the equality of two fractions. If the lowest term numerator and
     * denominators are the same for both fractions, the two fractions are
     * considered to be equal.
     *
     *
     * @param other
     * fraction to test for equality to this fraction, can be
     * `null`.
     * @return true if two fractions are equal, false if object is
     * `null`, not an instance of [BigFraction], or not
     * equal to this fraction instance.
     * @see java.lang.Object.equals
     */
    override fun equals(other: Any?): Boolean {
        var ret = false
        if (this === other) {
            ret = true
        } else if (other is BigFraction) {
            val rhs = other.reduce()
            val thisOne = this.reduce()
            ret = thisOne.numerator == rhs.numerator && thisOne.denominator == rhs.denominator
        }
        return ret
    }



    /**
     *
     *
     * Access the denominator as a <tt>int</tt>.
     *
     *
     * @return the denominator as a <tt>int</tt>.
     */
    val denominatorAsInt: Int
        get() = denominator.toInt()

    /**
     *
     *
     * Access the denominator as a <tt>long</tt>.
     *
     *
     * @return the denominator as a <tt>long</tt>.
     */
    val denominatorAsLong: Long
        get() = denominator.toLong()

    /**
     *
     *
     * Access the numerator as a <tt>int</tt>.
     *
     *
     * @return the numerator as a <tt>int</tt>.
     */
    val numeratorAsInt: Int
        get() = numerator.toInt()

    /**
     *
     *
     * Access the numerator as a <tt>long</tt>.
     *
     *
     * @return the numerator as a <tt>long</tt>.
     */
    val numeratorAsLong: Long
        get() = numerator.toLong()

    /**
     *
     *
     * Gets a hashCode for the fraction.
     *
     *
     * @return a hash code value for this object.
     * @see java.lang.Object.hashCode
     */
    override fun hashCode(): Int {
        return 37 * (37 * 17 + numerator.hashCode()) + denominator.hashCode()
    }

    /**
     *
     *
     * Multiplies the value of this fraction by the passed
     * `BigInteger`, returning the result in reduced form.
     *
     *
     * @param bg
     * the `BigInteger` to multiply by.
     * @return a `BigFraction` instance with the resulting values.
     * @throws NullPointerException
     * if the bg is `null`.
     */
    fun multiply(bg: BigInteger): BigFraction {
        return BigFraction(bg.multiply(numerator), denominator)
    }

    /**
     *
     *
     * Multiply the value of this fraction by the passed <tt>int</tt>, returning
     * the result in reduced form.
     *
     *
     * @param i
     * the <tt>int</tt> to multiply by.
     * @return a [BigFraction] instance with the resulting values.
     */
    fun multiply(i: Int): BigFraction {
        return multiply(BigInteger.valueOf(i.toLong()))
    }

    /**
     *
     *
     * Multiply the value of this fraction by the passed <tt>long</tt>,
     * returning the result in reduced form.
     *
     *
     * @param l
     * the <tt>long</tt> to multiply by.
     * @return a [BigFraction] instance with the resulting values.
     */
    fun multiply(l: Long): BigFraction {
        return multiply(BigInteger.valueOf(l))
    }

    /**
     *
     *
     * Multiplies the value of this fraction by another, returning the result in
     * reduced form.
     *
     *
     * @param fraction
     * the fraction to multiply by, must not be `null`.
     * @return a [BigFraction] instance with the resulting values.
     * @throws NullPointerException
     * if the fraction is `null`.
     */
    override fun multiply(fraction: BigFraction): BigFraction {
        return if (numerator == BigInteger.ZERO || fraction.numerator == BigInteger.ZERO) {
            ZERO
        } else BigFraction(
            numerator.multiply(fraction.numerator),
            denominator.multiply(fraction.denominator)
        )
    }

    /**
     *
     *
     * Return the additive inverse of this fraction, returning the result in
     * reduced form.
     *
     *
     * @return the negation of this fraction.
     */
    fun negate(): BigFraction {
        return BigFraction(numerator.negate(), denominator)
    }

    /**
     *
     *
     * Gets the fraction percentage as a <tt>double</tt>. This calculates the
     * fraction as the numerator divided by denominator multiplied by 100.
     *
     *
     * @return the fraction percentage as a <tt>double</tt>.
     */
    fun percentageValue(): Double {
        return numerator.divide(denominator).multiply(ONE_HUNDRED_DOUBLE).toDouble()
    }

    /**
     *
     *
     * Returns a <tt>integer</tt> whose value is
     * <tt>(this<sup>exponent</sup>)</tt>, returning the result in reduced form.
     *
     *
     * @param exponent
     * exponent to which this `BigInteger` is to be
     * raised.
     * @return <tt>this<sup>exponent</sup></tt>.
     */
    fun pow(exponent: Int): BigFraction {
        return if (exponent < 0) {
            BigFraction(denominator.pow(-exponent), numerator.pow(-exponent))
        } else BigFraction(numerator.pow(exponent), denominator.pow(exponent))
    }

    /**
     *
     *
     * Returns a `BigFraction` whose value is
     * <tt>(this<sup>exponent</sup>)</tt>, returning the result in reduced form.
     *
     *
     * @param exponent
     * exponent to which this `BigFraction` is to be raised.
     * @return <tt>this<sup>exponent</sup></tt> as a `BigFraction`.
     */
    fun pow(exponent: Long): BigFraction {
        return if (exponent < 0) {
            BigFraction(
                pow(denominator, -exponent),
                pow(numerator, -exponent)
            )
        } else BigFraction(
            pow(numerator, exponent),
            pow(denominator, exponent)
        )
    }

    /**
     *
     *
     * Returns a `BigFraction` whose value is
     * <tt>(this<sup>exponent</sup>)</tt>, returning the result in reduced form.
     *
     *
     * @param exponent
     * exponent to which this `BigFraction` is to be raised.
     * @return <tt>this<sup>exponent</sup></tt> as a `BigFraction`.
     */
    fun pow(exponent: BigInteger): BigFraction {
        if (exponent.compareTo(BigInteger.ZERO) < 0) {
            val eNeg = exponent.negate()
            return BigFraction(
                pow(
                    denominator, eNeg
                ),
                pow(numerator, eNeg)
            )
        }
        return BigFraction(
            pow(numerator, exponent),
            pow(denominator, exponent)
        )
    }

    /**
     *
     *
     * Returns a `double` whose value is
     * <tt>(this<sup>exponent</sup>)</tt>, returning the result in reduced form.
     *
     *
     * @param exponent
     * exponent to which this `BigFraction` is to be raised.
     * @return <tt>this<sup>exponent</sup></tt>.
     */
    fun pow(exponent: Double): Double {
        return Math.pow(numerator.toDouble(), exponent) /
                Math.pow(denominator.toDouble(), exponent)
    }

    /**
     *
     *
     * Return the multiplicative inverse of this fraction.
     *
     *
     * @return the reciprocal fraction.
     */
    fun reciprocal(): BigFraction {
        return BigFraction(denominator, numerator)
    }

    /**
     *
     *
     * Reduce this `BigFraction` to its lowest terms.
     *
     *
     * @return the reduced `BigFraction`. It doesn't change anything if
     * the fraction can be reduced.
     */
    fun reduce(): BigFraction {
        val gcd = numerator.gcd(denominator)
        return BigFraction(numerator.divide(gcd), denominator.divide(gcd))
    }

    /**
     *
     *
     * Subtracts the value of an [BigInteger] from the value of this one,
     * returning the result in reduced form.
     *
     *
     * @param bg
     * the [BigInteger] to subtract, must'nt be
     * `null`.
     * @return a `BigFraction` instance with the resulting values.
     * @throws NullPointerException
     * if the [BigInteger] is `null`.
     */
    fun subtract(bg: BigInteger?): BigFraction {
        return BigFraction(numerator.subtract(denominator.multiply(bg)), denominator)
    }

    /**
     *
     *
     * Subtracts the value of an <tt>integer</tt> from the value of this one,
     * returning the result in reduced form.
     *
     *
     * @param i
     * the <tt>integer</tt> to subtract.
     * @return a `BigFraction` instance with the resulting values.
     */
    fun subtract(i: Int): BigFraction {
        return subtract(BigInteger.valueOf(i.toLong()))
    }

    /**
     *
     *
     * Subtracts the value of an <tt>integer</tt> from the value of this one,
     * returning the result in reduced form.
     *
     *
     * @param l
     * the <tt>long</tt> to subtract.
     * @return a `BigFraction` instance with the resulting values, or
     * this object if the <tt>long</tt> is zero.
     */
    fun subtract(l: Long): BigFraction {
        return subtract(BigInteger.valueOf(l))
    }

    /**
     *
     *
     * Subtracts the value of another fraction from the value of this one,
     * returning the result in reduced form.
     *
     *
     * @param fraction
     * the [BigFraction] to subtract, must not be
     * `null`.
     * @return a [BigFraction] instance with the resulting values
     * @throws NullPointerException
     * if the fraction is `null`.
     */
    override fun subtract(fraction: BigFraction): BigFraction {
        if (ZERO == fraction) {
            return this
        }
        var num: BigInteger
        var den: BigInteger
        if (denominator == fraction.denominator) {
            num = numerator.subtract(fraction.numerator)
            den = denominator
        } else {
            num = numerator.multiply(fraction.denominator).subtract(fraction.numerator.multiply(denominator))
            den = denominator.multiply(fraction.denominator)
        }
        return BigFraction(num, den)
    }

    /**
     *
     *
     * Returns the `String` representing this fraction, ie
     * "num / dem" or just "num" if the denominator is one.
     *
     *
     * @return a string representation of the fraction.
     * @see java.lang.Object.toString
     */
    override fun toString(): String {
        var str: String? = null
        str = if (BigInteger.ONE == denominator) {
            numerator.toString()
        } else if (BigInteger.ZERO == numerator) {
            "0"
        } else {
            numerator.toString() + " / " + denominator
        }
        return str
    }

    companion object {
        /** A fraction representing "2 / 1".  */
        val TWO = BigFraction(2)

        /** A fraction representing "1".  */
        val ONE = BigFraction(1)

        /** A fraction representing "0".  */
        val ZERO = BigFraction(0)

        /** A fraction representing "-1 / 1".  */
        val MINUS_ONE = BigFraction(-1)

        /** A fraction representing "4/5".  */
        val FOUR_FIFTHS = BigFraction(4, 5)

        /** A fraction representing "1/5".  */
        val ONE_FIFTH = BigFraction(1, 5)

        /** A fraction representing "1/2".  */
        val ONE_HALF = BigFraction(1, 2)

        /** A fraction representing "1/4".  */
        val ONE_QUARTER = BigFraction(1, 4)

        /** A fraction representing "1/3".  */
        val ONE_THIRD = BigFraction(1, 3)

        /** A fraction representing "3/5".  */
        val THREE_FIFTHS = BigFraction(3, 5)

        /** A fraction representing "3/4".  */
        val THREE_QUARTERS = BigFraction(3, 4)

        /** A fraction representing "2/5".  */
        val TWO_FIFTHS = BigFraction(2, 5)

        /** A fraction representing "2/4".  */
        val TWO_QUARTERS = BigFraction(2, 4)

        /** A fraction representing "2/3".  */
        val TWO_THIRDS = BigFraction(2, 3)

        /** Serializable version identifier.  */
        private const val serialVersionUID = -5630213147331578515L

        /** Message for zero denominator.  */
        private const val FORBIDDEN_ZERO_DENOMINATOR = "denominator must be different from 0"

        /** `BigInteger` representation of 100.  */
        private val ONE_HUNDRED_DOUBLE = BigInteger.valueOf(100)

        /**
         *
         *
         * Creates a `BigFraction` instance with the 2 parts of a fraction
         * Y/Z.
         *
         *
         *
         *
         * Any negative signs are resolved to be on the numerator.
         *
         *
         * @param numerator
         * the numerator, for example the three in 'three sevenths'.
         * @param denominator
         * the denominator, for example the seven in 'three sevenths'.
         * @return a new fraction instance, with the numerator and denominator
         * reduced.
         * @throws ArithmeticException
         * if the denominator is `zero`.
         */
        fun getReducedFraction(
            numerator: Int,
            denominator: Int
        ): BigFraction {
            return if (numerator == 0) {
                ZERO // normalize zero.
            } else BigFraction(numerator, denominator)
        }
    }
}