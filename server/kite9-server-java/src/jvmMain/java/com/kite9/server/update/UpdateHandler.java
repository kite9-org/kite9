package com.kite9.server.update;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.sources.ModifiableAPI;

public interface UpdateHandler {

	ADLDom performDiagramUpdate(Update update, Authentication authentication) throws Exception;

	String getProperCause(Throwable e);

	ModifiableAPI getModifiableAPI(Update update, Authentication authentication) throws Exception;
}