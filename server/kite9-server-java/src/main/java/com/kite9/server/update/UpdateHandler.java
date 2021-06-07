package com.kite9.server.update;

import com.kite9.server.sources.ModifiableAPI;
import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;

public interface UpdateHandler {

	ADLOutput performDiagramUpdate(Update update, Authentication authentication, DiagramWriteFormat f) throws Exception;

	String getProperCause(Throwable e);

	ModifiableAPI getModifiableAPI(Update update, Authentication authentication) throws Exception;
}