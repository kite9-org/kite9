package com.kite9.server.adl.format.media;

import java.util.List;

import org.jetbrains.annotations.NotNull;

import com.kite9.pipeline.adl.format.media.K9MediaType;

/**
 * Used for Hateoas, JSON.
 */
public class EntityFormat implements RESTWriteFormat {
	
	private String extension;
	private List<K9MediaType> mediaTypes;

	public EntityFormat(String extension, List<K9MediaType> mediaTypes) {
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}

	public String getExtension() {
		return extension;
	}

	@NotNull
	@Override
	public String getFormatIdentifier() {
		return getMediaTypes().get(0).toString();
	}
}
