package com.kite9.server.adl.format.media;

import java.util.List;

import org.kite9.diagram.dom.XMLHelper;

import com.kite9.pipeline.adl.format.media.K9MediaType;

/**
 * Used for Hateoas, JSON.
 */
public class EntityFormat extends AbstractSVGFormat {
	
	private String extension;
	private List<K9MediaType> mediaTypes;

	public EntityFormat(String extension, List<K9MediaType> mediaTypes, XMLHelper helper) {
		super(helper);
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	public List<K9MediaType> getMediaTypes() {
		return mediaTypes;
	}

	public String getExtension() {
		return extension;
	}
}
