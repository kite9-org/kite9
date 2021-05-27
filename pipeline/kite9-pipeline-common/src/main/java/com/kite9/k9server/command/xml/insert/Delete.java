package com.kite9.k9server.command.xml.insert;

import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;

/**
 * Delete is basically the opposite of insert; you have to say what you're deleting so it can be
 * checked/undone.
 * 
 * @author robmoffat
 *
 */
public class Delete extends AbstractInsertCommand {

	public String base64Element;
	
	@Override
	public Mismatch applyCommand(ADLDom in) throws CommandException {
		checkProperties();
		return doDelete(in);
	}

	@Override
	public Mismatch undoCommand(ADLDom in) throws CommandException {
		checkProperties();
		return doInsert(in);
	}

	@Override
	protected Element getContents(ADLDom in) {
		return copyWithoutContainedIds(decodeElement(base64Element, in));
	}

	@Override
	protected void checkProperties() {
		super.checkProperties();
		ensureNotNull("base64Element", fragmentId);
	}

}
