package org.kite9.diagram.model

/**
 * This is a marker interface for things that can be linked to in the grouping phase.
 * Any links not meeting a LinkNode must be transferred onto a LinkNode.
 */
sealed interface LinkNode : Connected {
}