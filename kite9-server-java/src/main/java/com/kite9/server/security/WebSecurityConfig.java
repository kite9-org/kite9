package com.kite9.server.security;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Configuration of security protocols and the url patterns to match them.
 * 
 * @author robmoffat
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

	/**
	 * Allows access to pretty much everything. /github endpoint is secured by
	 * oauth2 at the moment (via github).
	 */
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable());
		http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

		http.authorizeHttpRequests(authz -> authz
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
				.anyRequest().permitAll());

		http.oauth2Login(oauth2 -> oauth2.loginPage("/github"));

		http.cors(cors -> cors.configurationSource(request -> {
			CorsConfiguration config = new CorsConfiguration();
			config.addAllowedOrigin("*");
			config.setAllowedMethods(Arrays.asList("*"));
			return config;
		}));

		return http.build();
	}

}
