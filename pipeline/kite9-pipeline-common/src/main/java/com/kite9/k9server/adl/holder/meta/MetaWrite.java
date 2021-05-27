package com.kite9.k9server.adl.holder.meta;

import java.net.URI;
import java.util.List;

import org.springframework.security.core.Authentication;

public interface MetaWrite {
	
	public static String CONTENT_CHANGED = "content-changed";

	public void setUser(UserMeta a);
	
	public void setUser();
	
	public void setAuthor(UserMeta a);
	
	public void setAuthorAndNotification(Authentication a);
	
	public void setTopicUri(URI topic);
	
	public void setCloseUri(URI close);
	
	public void setTitle(String title);
	
	public void setUri(URI u);
	
	public void setCollaborators(List<UserMeta> collaborators);
	
	public void setNotification(String message);
	
	public void setError(String message);
	
	public void setCommitCount(int c);
	
	public void setRole(Role r);
	
	public void setUploadsPath(String u);

	
}
