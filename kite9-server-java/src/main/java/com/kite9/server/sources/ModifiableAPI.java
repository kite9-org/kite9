package com.kite9.server.sources;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.holder.meta.Role;

/**
 * This is an API that controls persistence to some kind of backing storage.
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


	default void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
		checkUserCanWrite(by);
		commitRevisionAsBytesInner(message, by, bytes);
	}
	
	void commitRevisionAsBytesInner(String message, Authentication by, byte[] bytes);

	ModificationType getModificationType(Authentication a) throws Exception;

	/**
	 * Returns role for user, or NONE if the user isn't authenticated
	 */
	Role getAuthenticatedRole(Authentication a);

	/**
	 * Returns a userid for the underlying api.
	 */
	String getUserId(Authentication a);
	
	default void checkUserCanWrite(Authentication a) {
		Role r = getAuthenticatedRole(a);
		switch (r) {
		case EDITOR:
			return;
		default:
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

		}
	}
	
}
