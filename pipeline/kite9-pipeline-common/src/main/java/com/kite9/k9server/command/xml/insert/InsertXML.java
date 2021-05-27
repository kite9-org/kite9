package com.kite9.k9server.command.xml.insert;

import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;

/**
 * As well as inserting, you can also use this to "surround" an element.  Not sure if
 * we'll use that yet, as you can achive the same result with move.
 * 
 * @author robmoffat
 *
 */
public class InsertXML extends AbstractInsertCommand {

	public String base64Element;

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
		Element out = copyWithoutContainedIds(decodeElement(base64Element, in));
		if (newId != null) {
			replaceIds(out, newId);
		}
		return out;
	}

	@Override
	protected void checkProperties() {
		super.checkProperties();
		ensureNotNull("base64Element", fragmentId);
	}


}
