package com.kite9.k9server.adl.format.media;

/**
 * Used for CSS, XML, JS.
 *
 */
public class StaticFormat implements Format {
	
	private String extension;
	private MediaType[] mediaTypes;

	public StaticFormat(String extension, MediaType[] mediaTypes) {
		super();
		this.extension = extension;
		this.mediaTypes = mediaTypes;
	}
	
	@Override
	public MediaType[] getMediaTypes() {
		return mediaTypes;
	}

	@Override
	public String getExtension() {
		return extension;
	}

}
