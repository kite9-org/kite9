package com.kite9.server.persistence.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.meta.Role;

public abstract class AbstractCachingModifiableDiagramAPI implements CachingModifiableDiagramAPI {

	private long lastAccessTime;
	private final long occupancyTimeMs;
	
	protected Map<String, Role> goodEmails = new HashMap<>();

	protected void updateAccessTime() {
		lastAccessTime = System.currentTimeMillis();
	}

	public AbstractCachingModifiableDiagramAPI(long occupancyTimeMs) {
		super();
		this.occupancyTimeMs = occupancyTimeMs;
	}

	@Override
	public boolean canEvict() {
		return System.currentTimeMillis() > lastAccessTime + occupancyTimeMs;
	}

	public Role getAuthenticatedRole(Authentication a) {
		if (a != null) {
			String at = getUserId(a);
			if ((at != null) && (goodEmails.containsKey(at))) {
				return goodEmails.get(at);
			} else {
				Role r = getAuthenticatedRoleInner(a);
				if (r != Role.NONE) {
					goodEmails.put(at, r);
					return r;
				}
			} 
		}

		return Role.NONE;
	}
	
	protected abstract Role getAuthenticatedRoleInner(Authentication a);


	
}