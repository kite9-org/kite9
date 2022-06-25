package com.kite9.server.web;

import java.io.IOException;

/**
 * Throw this if you want the user to log in - will redirect to the login page.
 */
@SuppressWarnings("serial")
public class LoginRequiredException extends IOException {

	public enum Type { 
		GITHUB("/oauth2/authorization/github"), GENERIC("/login");
		
		Type(String path) {
			this.path = path;
		}
		
		public final String path;
	
	} 
	
	Type t;
	
	public LoginRequiredException(Type t) {
		this.t = t;
	}
}
