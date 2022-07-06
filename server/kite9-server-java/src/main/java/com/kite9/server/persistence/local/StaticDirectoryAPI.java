package com.kite9.server.persistence.local;

import java.io.InputStream;
import java.net.URISyntaxException;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.sources.SourceAPI;

public class StaticDirectoryAPI implements SourceAPI {
	
	private final K9URI sourceUri;
	private final String path;
	private final AbstractPublicEntityConverter ec;

	public StaticDirectoryAPI(K9URI sourceUri, String path, AbstractPublicEntityConverter ec) throws URISyntaxException {
		this.path = path;
		this.ec = ec;
		this.sourceUri = sourceUri;
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		return ec.handleEntityContent(a, path);
	}

	@Override
	public SourceType getSourceType(Authentication a) throws Exception {
		return SourceType.DIRECTORY;
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		throw new UnsupportedOperationException("Can't stream content for directory");
	}

	@Override
	public K9MediaType getMediaType() {
		throw new UnsupportedOperationException("no media type for directory");
	}

	@Override
	public K9URI getKite9ResourceURI() {
		return sourceUri;
	}

}