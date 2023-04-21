package com.kite9.server.persistence.github.conversion;

import org.springframework.security.core.Authentication;

import com.kite9.server.domain.Organisation;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.domain.User;

public interface GithubEntityConverter {

	User getHomePage(Authentication authentication) throws Exception;

	Organisation getOrgPage(String userOrg, Authentication authentication) throws Exception;

	RestEntity getDirectoryPage(DirectoryDetails dd, Authentication authentication) throws Exception;

}