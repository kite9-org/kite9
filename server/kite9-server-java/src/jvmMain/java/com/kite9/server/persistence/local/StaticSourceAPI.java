package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.sources.ModifiableAPI;

/**
 * This is used for returning static files from the kite9 server.
 * 
 * @author rob@kite9.com
 *
 */
public class StaticSourceAPI implements ModifiableAPI {
	
	private final K9MediaType underlying;
	private byte[] bytes;
	private K9URI sourceUri;

	public StaticSourceAPI(K9MediaType underlying, byte[] bytes, K9URI sourceUri) {
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
	public K9URI getUnderlyingResourceURI() {
		return sourceUri;
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
		throw new UnsupportedOperationException("Can't change static files");
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		throw new UnsupportedOperationException("Only works for directories");
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		return SourceType.FILE;
	}

	

}