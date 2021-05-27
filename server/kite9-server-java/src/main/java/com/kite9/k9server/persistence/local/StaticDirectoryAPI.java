package com.kite9.k9server.persistence.local;

import java.net.URISyntaxException;

import org.springframework.security.core.Authentication;

import com.kite9.k9server.domain.RestEntity;
import com.kite9.k9server.sources.DirectoryAPI;

public class StaticDirectoryAPI implements DirectoryAPI {
	
	private final String path;
	private final AbstractPublicEntityConverter ec;

	public StaticDirectoryAPI(String path, AbstractPublicEntityConverter ec) throws URISyntaxException {
		this.path = path;
		this.ec = ec;
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		return ec.handleEntityContent(a, path);
	}

}