package com.kite9.k9server.command;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.xml.insert.Delete;
import com.kite9.k9server.command.xml.insert.InsertUrl;
import com.kite9.k9server.command.xml.insert.InsertUrlLink;
import com.kite9.k9server.command.xml.insert.InsertXML;
import com.kite9.k9server.command.xml.move.ADLMoveCells;
import com.kite9.k9server.command.xml.move.Move;
import com.kite9.k9server.command.xml.replace.ReplaceAttr;
import com.kite9.k9server.command.xml.replace.ReplaceStyle;
import com.kite9.k9server.command.xml.replace.ReplaceTag;
import com.kite9.k9server.command.xml.replace.ReplaceTagUrl;
import com.kite9.k9server.command.xml.replace.ReplaceText;
import com.kite9.k9server.command.xml.replace.ReplaceXML;

/**
 * Performs some change on the ADL.
 *  
 * @author robmoffat
 *
 */
@JsonTypeInfo(use=Id.NAME, include=As.PROPERTY, property="type", visible=true)
@JsonSubTypes({
	// replace actions
	@Type(ReplaceText.class),
	@Type(ReplaceAttr.class),
	@Type(ReplaceXML.class),
	@Type(ReplaceStyle.class),
	@Type(ReplaceTag.class),
	@Type(ReplaceTagUrl.class),
	
	@Type(Move.class),
	@Type(ADLMoveCells.class),
	
	@Type(Delete.class), 
	@Type(InsertUrl.class),
	@Type(InsertUrlLink.class),
	@Type(InsertXML.class)

})

@JsonAutoDetect(fieldVisibility=Visibility.ANY, 
	getterVisibility=Visibility.NONE, 
	setterVisibility =Visibility.NONE)
public interface Command {
	
	@FunctionalInterface
	static interface Mismatch {
		
		public String explain();
		
	}
		
	/**
	 * Returns null if the command made some change, or an explanation if it was unable to complete due to mismatching preconditions
	 */
	public Mismatch applyCommand(ADLDom in) throws CommandException;
	
	/**
	 * Returns null if the command made some change, or an explanation if it was unable to complete due to mismatching preconditions
	 */
	public Mismatch undoCommand(ADLDom in) throws CommandException;
	
}
