package com.kite9.server.security;

import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;


/**
 * Configuration of security protocols and the url patterns to match them.
 * 
 * @author robmoffat
 */
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
	
	/**
	 * Allows access to pretty much everything.  /github endpoint is secured by oauth2 at the moment (via github).
	 */
	@Override
	protected void configure(HttpSecurity http) throws Exception {
	//	http.csrf().disable();
		http.headers().frameOptions().disable();
		
		http.authorizeRequests()
		.requestMatchers(r -> {
			if (r.getHeader("Authorization") != null) {
				// this lets us skip oauth2 if the user provides 
				// a token that might be compatible with github.
				return false;
			}
			
			if ("POST".equals(r.getMethod())) {
				if (r.getRequestURI().startsWith("/github")) {
					// github post requests must be authenticated
					return true;
				}
			}
			
			if (r.getRequestURI().equals("/github")) {
				// we'll do login if the user looks here
				return true;
			}
			
			return false;
		}).authenticated()
		.and()
		.oauth2Login()
		.successHandler(new AuthenticationSuccessHandler() {
			
			@Override
			public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
					Authentication authentication) throws IOException, ServletException {
				// TODO Auto-generated method stub
				

			}
		});
		
		http.cors().configurationSource(new CorsConfigurationSource() {
			
			@Override
			public CorsConfiguration getCorsConfiguration(HttpServletRequest request) {
				CorsConfiguration config = new CorsConfiguration();
				config.addAllowedOrigin("*");
				config.setAllowedMethods(Arrays.asList("*"));
				return config;
			}
		});
		
	} 

}
