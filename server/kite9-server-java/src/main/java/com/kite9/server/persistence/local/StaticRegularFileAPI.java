package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.server.sources.ModifiableAPI;

public class StaticRegularFileAPI implements ModifiableAPI {
	
	private final K9MediaType underlying;
	private byte[] bytes;
	private K9URI sourceUri;

	public StaticRegularFileAPI(K9MediaType underlying, byte[] bytes, K9URI sourceUri) {
		this.underlying = underlying;
		this.bytes = bytes;
		this.sourceUri = sourceUri;
	}

	public K9MediaType getMediaType() {
		return underlying;
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) {
		return new ByteArrayInputStream(bytes);
	}

	@Override
	public Type getType(Authentication a) {
		return Type.MODIFIABLE;
	}
	
	@Override
	public Role getAuthenticatedRole(Authentication a) {
		return Role.EDITOR;
	}

	@Override
	public String getUserId(Authentication a) {
		return null;
	}

	@Override
	public K9URI getSourceLocation() {
		return sourceUri;
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
	}

	

}