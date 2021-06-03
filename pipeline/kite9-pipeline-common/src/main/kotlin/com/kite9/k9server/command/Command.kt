package com.kite9.k9server.command

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.annotation.JsonTypeInfo.As
import com.kite9.k9server.adl.holder.pipeline.ADLDom
import com.kite9.k9server.command.xml.insert.Delete
import com.kite9.k9server.command.xml.insert.InsertUrl
import com.kite9.k9server.command.xml.insert.InsertUrlWithChanges
import com.kite9.k9server.command.xml.insert.InsertXML
import com.kite9.k9server.command.xml.move.ADLMoveCells
import com.kite9.k9server.command.xml.move.Move
import com.kite9.k9server.command.xml.replace.*

/**
 * Performs some change on the ADL.
 *
 * @author robmoffat
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
    JsonSubTypes.Type(ReplaceText::class),
    JsonSubTypes.Type(ReplaceAttr::class),
    JsonSubTypes.Type(ReplaceXML::class),
    JsonSubTypes.Type(ReplaceStyle::class),
    JsonSubTypes.Type(ReplaceTag::class),
    JsonSubTypes.Type(ReplaceTagUrl::class),
    JsonSubTypes.Type(Move::class),
    JsonSubTypes.Type(ADLMoveCells::class),
    JsonSubTypes.Type(Delete::class),
    JsonSubTypes.Type(InsertUrl::class),
    JsonSubTypes.Type(InsertUrlWithChanges::class),
    JsonSubTypes.Type(InsertXML::class)
)
@JsonAutoDetect(
    fieldVisibility = JsonAutoDetect.Visibility.ANY,
    getterVisibility = JsonAutoDetect.Visibility.NONE,
    setterVisibility = JsonAutoDetect.Visibility.NONE
)
interface Command {

    fun interface Mismatch {
        fun explain(): String
    }

    /**
     * Returns null if the command made some change, or an explanation if it was unable to complete due to mismatching preconditions
     */
    fun applyCommand(d: ADLDom, ctx: CommandContext): Mismatch?

    /**
     * Returns null if the command made some change, or an explanation if it was unable to complete due to mismatching preconditions
     */
    fun undoCommand(d: ADLDom, ctx: CommandContext): Mismatch?
}