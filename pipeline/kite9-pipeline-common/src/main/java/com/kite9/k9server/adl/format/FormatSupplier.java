package com.kite9.k9server.adl.format;

import com.kite9.k9server.adl.format.media.Format;
import com.kite9.k9server.adl.format.media.MediaType;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface FormatSupplier {

	Format getFormatFor(MediaType mt);
	
	List<MediaType> getMediaTypes();

	Map<String, MediaType> getMediaTypeMap();
	
	Optional<Format> getFormatFor(String path);
	
	Optional<Format> getFormatFor(URI u);

	MediaType getMediaTypeFor(URI u);

	MediaType getMediaTypeFor(String path);
	
	Format[] getPriorityOrderedFormats();

}
