package com.kite9.server.persistence.github.conversion;

import com.kite9.server.domain.Organisation;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.domain.User;
import com.kite9.server.persistence.github.config.Config;

public interface GithubEntityConverter {

	User getHomePage(HomeDetails hd) throws Exception;

	Organisation getOrgPage(OrgDetails od) throws Exception;

	RestEntity getDirectoryPage(DirectoryDetails dd, Config c) throws Exception;

}