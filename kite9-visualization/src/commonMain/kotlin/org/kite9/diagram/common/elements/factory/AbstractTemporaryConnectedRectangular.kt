package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.*

abstract class AbstractTemporaryConnectedRectangular(private val _id: String, private val p: Rectangular) : AbstractDiagramElement(p), TemporaryConnectedRectangular {

    private val links: Collection<Connection> = ArrayList()

    override fun getLinks(): Collection<Connection> {
        return links
    }

    private fun firstConnectionTo(c: Connected): Connection? {
        for (link in getLinks()) {
            if (link.meets(c)) {
                return link
            }
        }
        return null
    }

    override fun isConnectedDirectlyTo(c: Connected): Boolean {
        return firstConnectionTo(c) != null
    }

    override fun getContainer(): Rectangular {
        return getParent()
    }

    override fun getParent() : Rectangular {
        return p
    }

    override fun getID(): String {
        return _id
    }
}