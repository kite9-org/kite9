package com.kite9.pipeline.adl.format;

import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.MediaType;
import com.kite9.pipeline.uri.URI;

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
