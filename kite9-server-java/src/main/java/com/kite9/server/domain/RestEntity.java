package com.kite9.server.domain;

import java.util.List;

import com.kite9.pipeline.adl.holder.meta.MetaWrite;
import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.persistence.github.config.Config;
import org.jetbrains.annotations.NotNull;
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
