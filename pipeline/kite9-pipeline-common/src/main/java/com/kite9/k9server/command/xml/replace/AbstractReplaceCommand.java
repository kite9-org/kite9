package com.kite9.k9server.command.xml.replace;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.command.CommandException;
import com.kite9.k9server.command.xml.AbstractADLCommand;

public abstract class AbstractReplaceCommand<E, T> extends AbstractADLCommand {

	public String fragmentId;
	public String from, to;
	
	protected abstract T getFromContent(ADLDom context);
	
	protected abstract T getToContent(ADLDom context);
	
	protected abstract E getExistingContent(ADLDom in);
	
	@Override
	public Mismatch applyCommand(ADLDom in) throws CommandException {
		checkProperties();
		E existing = getExistingContent(in);
		T fromContent = getFromContent(in);
		
		Mismatch m= same(existing, fromContent);
		if (m != null) {
			return m;
		}
	
		doReplace(in, existing, getToContent(in), fromContent);
		return null;
	}

	protected void checkProperties() {
		ensureNotNull("fragmentId", fragmentId);
	}

	protected abstract void doReplace(ADLDom on, E site, T toContent, T fromContent);

	protected Mismatch same(E existing, T with) {
		if (existing.equals(with)) {
			return null;
		} else {
			return () -> "Diagram has changed since move command issued.  Expected "+with+" got "+existing;
		}
	}

	@Override
	public Mismatch undoCommand(ADLDom in) throws CommandException {
		checkProperties();
		E existing = getExistingContent(in);
		T toContent = getToContent(in);

		Mismatch m = same(existing, toContent);
		
		if (m != null) {
			return m;
		} 
		
		
		doReplace(in, existing, getFromContent(in), toContent);
		return null;
	}
	
	
}
