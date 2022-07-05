package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;
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
	public ModificationType getModificationType(Authentication a) {
		return ModificationType.MODIFIABLE;
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
	public K9URI getKite9ResourceURI() {
		return sourceUri;
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	

}