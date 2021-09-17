package com.kite9.server.persistence.github;

import java.util.List;

import com.kite9.server.domain.RestEntity;
import org.springframework.security.core.Authentication;

import com.kite9.server.sources.DirectoryAPI;

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
