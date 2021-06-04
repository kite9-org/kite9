package com.kite9.server.persistence.cache;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.Authentication;

import com.kite9.server.pipeline.adl.holder.meta.Role;

public abstract class AbstractCachingModifiableDiagramAPI implements CachingModifiableDiagramAPI {
	
	public static final long OCCUPANCY_TIME_MS = 1000*60*15;

	private long lastAccessTime;
	protected Map<String, Role> goodEmails = new HashMap<>();

	protected void updateAccessTime() {
		lastAccessTime = System.currentTimeMillis();
	}

	public AbstractCachingModifiableDiagramAPI() {
		super();
	}

	@Override
	public boolean canEvict() {
		return System.currentTimeMillis() > lastAccessTime + OCCUPANCY_TIME_MS;
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