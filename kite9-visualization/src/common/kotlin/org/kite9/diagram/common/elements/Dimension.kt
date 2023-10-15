/**
 *
 */
package org.kite9.diagram.common.elements

enum class Dimension {
    V, H;

    fun isHoriz() : Boolean {
        return this == H;
    }
}