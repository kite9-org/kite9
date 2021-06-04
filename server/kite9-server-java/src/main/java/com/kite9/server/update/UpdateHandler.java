package com.kite9.server.update;

import com.kite9.server.sources.ModifiableAPI;
import org.springframework.security.core.Authentication;

import com.kite9.server.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLOutput;

public interface UpdateHandler {

	public <X extends DiagramWriteFormat> ADLOutput<X> performDiagramUpdate(Update update, Authentication authentication, X f) throws Exception;

	public String getProperCause(Throwable e);

	public ModifiableAPI getModifiableAPI(Update update, Authentication authentication) throws Exception;
}