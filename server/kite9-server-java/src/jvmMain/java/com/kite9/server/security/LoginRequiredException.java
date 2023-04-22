package com.kite9.server.security;

import org.springframework.security.core.AuthenticationException;

import com.kite9.pipeline.uri.K9URI;

/**
 * Throw this if you want the user to log in - will redirect to the login page.
 */
@SuppressWarnings("serial") 
public class LoginRequiredException extends AuthenticationException {

	public enum Type { 
		GITHUB("/oauth2/authorization/github"), GENERIC("/login");
		
		Type(String path) {
			this.path = path;
		}
		
		public final String path;
	
	} 
	
	private final Type t;
	private final K9URI redirectUri;
	
	public LoginRequiredException(Type t, K9URI u) {
		super("Login required to access "+u);
		this.t = t;
		this.redirectUri = u;
	}

	public Type getType() {
		return t;
	}

	public K9URI getRedirectUri() {
		return redirectUri;
	}
	
	
}
