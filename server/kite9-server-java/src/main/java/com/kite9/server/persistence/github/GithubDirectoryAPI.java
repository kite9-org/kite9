package com.kite9.server.persistence.github;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.kite9.server.domain.RestEntity;
import com.kite9.server.sources.DirectoryAPI;

public class GithubDirectoryAPI implements DirectoryAPI {

	String path;
	String ref;
	RestEntity re;
	List<Object> contents;
	
	
	public GithubDirectoryAPI(String path, String ref, RestEntity re) {
		super();
		this.path = path;
		this.ref = ref;
		this.re = re;
	}

	@Override
	public RestEntity getEntityRepresentation(Authentication a) throws Exception {
		return re;
	}

}
