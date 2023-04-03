package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.meta.Role;

/**
 * This is an API that controls persistence to some kind of backing storage.
 * <p>
 * Generally this is diagrams, but we also use it for file upload (creatable)
 * 
 * @author robmoffat
 *
 */
public interface ModifiableAPI extends SourceAPI {

	enum ModificationType {
		/** Doesn't exist yet, but the user has the rights to create it */
		CREATABLE,		
	
		/** Exists, and user has rights to modify it */
		MODIFIABLE, 
		
		/** User lacks permission to change */
		VIEWONLY
	}


	void commitRevisionAsBytes(String message, Authentication by, byte[] bytes);
	
	ModificationType getModificationType(Authentication a);

	/**
	 * Returns role for user, or NONE if the user isn't authenticated
	 */
	Role getAuthenticatedRole(Authentication a);

	/**
	 * Returns a userid for the underlying api.
	 */
	String getUserId(Authentication a);
	
}
