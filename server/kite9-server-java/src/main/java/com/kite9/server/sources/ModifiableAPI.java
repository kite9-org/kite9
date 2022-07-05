package com.kite9.server.sources;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.meta.Role;

/**
 * This is an API that controls peristence to some kind of backing storage.
 * 
 * Generally this is diagrams, but we also use it for file upload (creatable)
 * 
 * @author robmoffat
 *
 */
public interface ModifiableAPI extends SourceAPI {

	public enum ModificationType { 
		/** Doesn't exist yet, but the user has the rights to create it */
		CREATABLE,		
	
		/** Exists, and user has rights to modify it */
		MODIFIABLE, 
		
		/** User lacks permission to change */
		VIEWONLY
	};
	
	
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes);
	
	public ModificationType getModificationType(Authentication a);

	/**
	 * Returns role for user, or NONE if the user isn't authenticated
	 */
	public Role getAuthenticatedRole(Authentication a);

	/**
	 * Returns a userid for the underlying api.
	 */
	public String getUserId(Authentication a);
	
}
