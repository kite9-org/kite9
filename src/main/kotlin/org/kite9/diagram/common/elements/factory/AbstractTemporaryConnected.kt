package org.kite9.diagram.common.elements.factory

import org.kite9.diagram.model.Connected
import org.kite9.diagram.model.Connection
import org.kite9.diagram.model.Container
import org.kite9.diagram.model.DiagramElement

abstract class AbstractTemporaryConnected(private val _id: String, private val p: DiagramElement) : AbstractDiagramElement(p), TemporaryConnected {

    private val links: Collection<Connection> = ArrayList()

    override fun getLinks(): Collection<Connection> {
        return links
    }

    override fun getConnectionTo(c: Connected): Connection? {
        for (link in getLinks()) {
            if (link.meets(c)) {
                return link
            }
        }
        return null
    }

    override fun isConnectedDirectlyTo(c: Connected): Boolean {
        return getConnectionTo(c) != null
    }

    override fun getContainer(): Container {
        return getParent() as Container
    }

    override fun getID(): String {
        return _id
    }
}