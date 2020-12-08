package org.kite9.diagram.common.fraction

import kotlin.Throws
import java.lang.ArithmeticException

/**
 * Interface representing [field](http://mathworld.wolfram.com/Field.html) elements.
 * @param <T> the type of the field elements
 * @see Field
 *
 * @version $Revision: 811685 $ $Date: 2009-09-05 13:36:48 -0400 (Sat, 05 Sep 2009) $
 * @since 2.0
</T> */
interface FieldElement<T> {
    /** Compute this + a.
     * @param a element to add
     * @return a new element representing this + a
     */
    fun add(a: T): T

    /** Compute this - a.
     * @param a element to subtract
     * @return a new element representing this - a
     */
    fun subtract(a: T): T

    /** Compute this  a.
     * @param a element to multiply
     * @return a new element representing this  a
     */
    fun multiply(a: T): T

    /** Compute this  a.
     * @param a element to add
     * @return a new element representing this  a
     * @exception ArithmeticException if a is the zero of the
     * additive operation (i.e. additive identity)
     */
    @Throws(ArithmeticException::class)
    fun divide(a: T): T
}