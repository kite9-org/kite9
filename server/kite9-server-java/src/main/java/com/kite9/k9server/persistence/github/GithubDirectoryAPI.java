package com.kite9.k9server.persistence.github;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.kite9.k9server.domain.RestEntity;
import com.kite9.k9server.sources.DirectoryAPI;

public class GithubDirectoryAPI implements DirectoryAPI {

	String path;
	RestEntity re;
	List<Object> contents;
	
	
	public GithubDirectoryAPI(String path, RestEntity re) {
		super();
		this.path = path;
		this.re = re;
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		return re;
	}

}
