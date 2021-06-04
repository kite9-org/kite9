package com.kite9.server.adl.format.media;

import java.util.List;

/**
 * Used for Hateoas, JSON.
 */
public class EntityFormat extends AbstractSVGFormat {
	
	private String extension;
	private List<MediaType> mediaTypes;

	public EntityFormat(String extension, List<MediaType> mediaTypes) {
		super();
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	@Override
	public List<MediaType> getMediaTypes() {
		return mediaTypes;
	}


	@Override
	public String getExtension() {
		return extension;
	}

}
