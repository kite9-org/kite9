package com.kite9.k9server.sources;

import java.net.URI;

import org.springframework.security.core.Authentication;

import com.kite9.k9server.adl.holder.meta.Role;

/**
 * This is an API that controls peristence to some kind of backing storage.
 * 
 * @author robmoffat
 *
 */
public interface ModifiableAPI extends FileAPI {

	public enum Type { 
		/** Doesn't exist yet, but the user has the rights to create it */
		CREATABLE,		
	
		/** Exists, and user has rights to modify it */
		MODIFIABLE, 
		
		/** User lacks permission to change */
		VIEWONLY
	};
	
	
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes);
	
	/**
	 * Means that this diagram already has >0 versions.
	 */
	public Type getType(Authentication a);

	/**
	 * Returns role for user, or NONE if the user isn't authenticated
	 */
	public Role getAuthenticatedRole(Authentication a);

	/**
	 * Returns a userid for the underlying api.
	 */
	public String getUserId(Authentication a);
	
	/**
	 * Underlying system URI that is being modified.
	 */
	public URI getSourceLocation();

	
	
}
