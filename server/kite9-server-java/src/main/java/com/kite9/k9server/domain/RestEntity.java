package com.kite9.k9server.domain;

import java.util.List;

import org.springframework.hateoas.RepresentationModel;

/**
 * This is used to display information about the object within the admin screens.
 */
public abstract class RestEntity extends RepresentationModel<RestEntity> {
	
	public abstract String getTitle();
	
	public abstract String getDescription();
	
	public abstract String getIcon();
		
	public abstract String getType();
	
	public abstract String getCommands();

	public abstract List<RestEntity> getParents();

}
