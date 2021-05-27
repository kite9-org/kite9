package com.kite9.k9server.command.xml.insert;

import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;

public class InsertUrl extends AbstractInsertCommand {

	public String uriStr;		// to insert.	

	@Override
	public Mismatch applyCommand(ADLDom in) throws CommandException {
		checkProperties();
		return doInsert(in);
	}

	@Override
	public Mismatch undoCommand(ADLDom in) throws CommandException {
		checkProperties();
		return doDelete(in);
	}
	
	@Override
	protected Element getContents(ADLDom in) {
		Element out = getForeignElementCopy(in.getDocument(), in.getUri(), uriStr, true, in);
		replaceIds(out, newId);
		return out;
	}

	@Override
	protected void checkProperties() {
		super.checkProperties();
		ensureNotNull("uriStr", uriStr);
		ensureNotNull("newId", newId);
	}
	
}
