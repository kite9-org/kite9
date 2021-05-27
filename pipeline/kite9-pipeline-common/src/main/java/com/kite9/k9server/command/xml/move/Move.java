package com.kite9.k9server.command.xml.move;

import org.kite9.diagram.dom.elements.ADLDocument;
import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;
import com.kite9.k9server.command.xml.AbstractADLCommand;

public class Move extends AbstractADLCommand {

	public String moveId;  	// guy we are moving
	public String from, fromBefore, to, toBefore;
	
	@Override
	public Mismatch applyCommand(ADLDom in) throws CommandException {
		return move(in, moveId, from, fromBefore, to, toBefore);		
	}
	
	
	private Mismatch move(ADLDom in, String moveId, String from, String fromBefore, String to, String toBefore) {
		checkProperties();
		ADLDocument doc = in.getDocument();
		Element moveEl = doc.getElementById(moveId);
		Element toEl = doc.getElementById(to);
		Element toBeforeEl = doc.getElementById(toBefore);
		
		if ((moveEl == null) || (toEl == null)) {
			return () -> "Element missing, moveEl="+moveEl+", toEl="+toEl;
		}
		
		if (toBeforeEl == null) {
			toEl.appendChild(moveEl);
		} else {
			toEl.insertBefore(moveEl, toBeforeEl);
		}
		
		LOG.info("Completed move into "+to);
		return null;
	}

	
	protected void checkProperties() {
		ensureNotNull("moveId", moveId);
		ensureNotNull("from", from);
		ensureNotNull("to", to);
	}

	@Override
	public Mismatch undoCommand(ADLDom in) throws CommandException {
		return move(in, moveId, to, toBefore, from, fromBefore);		
	}



}
