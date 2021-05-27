package com.kite9.k9server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;

import com.kite9.k9server.adl.holder.meta.Role;
import com.kite9.k9server.sources.ModifiableAPI;

public class StaticRegularFileAPI implements ModifiableAPI {
	
	private final MediaType underlying;
	private byte[] bytes;
	private URI sourceUri;

	public StaticRegularFileAPI(MediaType underlying, byte[] bytes, URI sourceUri) {
		this.underlying = underlying;
		this.bytes = bytes;
		this.sourceUri = sourceUri;
	}

	public MediaType getMediaType() {
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
	public URI getSourceLocation() {
		return sourceUri;
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
	}

	

}