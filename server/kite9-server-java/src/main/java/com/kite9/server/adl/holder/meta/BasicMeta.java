package com.kite9.server.adl.holder.meta;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

/**
 * Handles allowable meta elements, that can be stored in the ADL. These get
 * output either as part of the SVG, or headers of the HTTP request.
 * 
 * @author robmoffat
 *
 */
public class BasicMeta implements MetaReadWrite {

	protected Map<String, Object> metadata = new HashMap<>();
	
	public BasicMeta(Map<String, Object> metadata) {
		super();
		this.metadata = metadata;
	}


	public static UserMeta createUser(OAuth2User oauthUser) {
		return new UserMeta() {

			@Override
			public String getId() {
				return oauthUser.getName();
			}

			@Override
			public String getIcon() {
				return oauthUser.getAttribute("avatar_url");
			}

			@Override
			public String getPage() {
				return oauthUser.getAttribute("html_url");
			}

			@Override
			public String getDisplayName() {
				String name =  oauthUser.getAttribute("name");
				return name == null ? getLogin() : name;
			}

			@Override
			public String getLogin() {
				return oauthUser.getAttribute("login");
			}
			
		};
	}
	

	@Override
	public Map<String, Object> getMetaData() {
		return metadata;
	}

	@Override
	public void setUser(UserMeta um) {
		metadata.put("user", um);
	}

	@Override
	public void setAuthor(UserMeta um) {
		metadata.put("author", um);
	}

	@Override
	public void setTopicUri(URI topic) {
		metadata.put("topic", topic);
	}
	
	@Override
	public URI getTopicUri() {
		return (URI) metadata.get("topic");
	}

	@Override
	public void setCloseUri(URI close) {
		metadata.put("close", close);
	}

	@Override
	public void setUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof OAuth2AuthenticationToken) {
			Object p = authentication.getPrincipal();
			if (p instanceof OAuth2User) {
				setUser(createUser((OAuth2User) p));
			}
		}
	}

	@Override
	public void setAuthorAndNotification(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken) {
			Object p = authentication.getPrincipal();
			if (p instanceof OAuth2User) {
				UserMeta user = createUser((OAuth2User) p);
				setAuthor(user);
				setNotification("Edit by "+user.getDisplayName());
			}
		}
	}

	public void setTitle(String title) {
		metadata.put("title", title);
	}
	
	public String getTitle() {
		return (String) metadata.getOrDefault("title", "Unnamed");
	}

	@Override
	public void setUri(URI u) {
		if (u != null) {
			metadata.put("self", u);
		}
	}
	
	public URI getUri() {
		return (URI) metadata.get("self");
	}

	@Override
	public void setCollaborators(List<UserMeta> collaborators) {
		metadata.put("collaborators", collaborators);
	}

	@Override
	public void setCommitCount(int c) {
		metadata.put("committing", c);
	}

	@Override
	public void setNotification(String message) {
		metadata.put("notification", message);
	}


	@Override
	public void setError(String message) {
		metadata.put("error", message);
	}


	@Override
	public void setRole(Role r) {
		metadata.put("role", r.toString().toLowerCase());
	}


	@Override
	public void setUploadsPath(String u) {
		metadata.put("uploads", u);
	}

}
