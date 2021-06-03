package com.kite9.k9server.update;

import org.springframework.security.core.Authentication;

import com.kite9.k9server.adl.format.media.DiagramWriteFormat;
import com.kite9.k9server.adl.holder.pipeline.ADLOutput;
import com.kite9.k9server.sources.ModifiableAPI;

public interface UpdateHandler {

	public <X extends DiagramWriteFormat> ADLOutput<X> performDiagramUpdate(Update update, Authentication authentication, X f) throws Exception;

	public String getProperCause(Throwable e);

	public ModifiableAPI getModifiableAPI(Update update, Authentication authentication) throws Exception;
}