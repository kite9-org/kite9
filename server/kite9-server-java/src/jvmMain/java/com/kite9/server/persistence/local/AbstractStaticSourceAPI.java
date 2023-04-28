package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.sources.ModifiableAPI;

/**
 * This is used for returning static files from the kite9 server.
 * 
 * @author rob@kite9.com
 *
 */
public abstract class AbstractStaticSourceAPI implements ModifiableAPI {
	
	private final K9MediaType underlying;
	private Object content;
	private K9URI sourceUri;
	private SourceType type;

	public AbstractStaticSourceAPI(K9MediaType underlying, byte[] bytes, K9URI sourceUri) {
		this.underlying = underlying;
		this.content = bytes;
		this.sourceUri = sourceUri;
		this.type = SourceType.FILE;
	}
	
	public AbstractStaticSourceAPI(K9MediaType underlying, K9URI sourceUri) {
		this.underlying = underlying;
		this.content = null;
		this.sourceUri = sourceUri;
		this.type = SourceType.DIRECTORY;
	}

	public K9MediaType getMediaType() {
		return underlying;
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) {
		if (type == SourceType.FILE) {
			return new ByteArrayInputStream((byte[]) content);
		} else {
			throw new UnsupportedOperationException("Can't turn a directory into a diagram");
		}
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
	public K9URI getUnderlyingResourceURI(Authentication a) {
		return sourceUri;
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
		throw new UnsupportedOperationException("Can't change static files");
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		return type;
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
	}
	
}