package com.kite9.k9server.adl.format;

import com.kite9.k9server.adl.holder.ADLFactory;
import com.kite9.k9server.adl.format.media.*;
import com.kite9.k9server.uri.URI;
import org.kite9.diagram.logging.Kite9ProcessingException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BasicFormatSupplier implements FormatSupplier {

	private final Format[] formats;
	private final MediaType[] mediaTypes;

	public BasicFormatSupplier(ADLFactory adlFactory) {
		super();
		this.formats = new Format[] { 
			// things that could be diagrams
			new ADLFormat(adlFactory), 
			new SVGFormat(adlFactory),
			new PNGFormat(adlFactory), 
			new EditableSVGFormat(),
			
			// entities
			new EntityFormat("hal", new MediaType[] {Kite9MediaTypes.HAL_JSON}),
			new EntityFormat("json", new MediaType[] {Kite9MediaTypes.APPLICATION_JSON}),
			
			// static stuff
			new HTMLFormat(),
			new StaticFormat("xml", new MediaType[] { Kite9MediaTypes.TEXT_XML, Kite9MediaTypes.APPLICATION_XML }),
			new StaticFormat("xslt", new MediaType[] { Kite9MediaTypes.XSLT }),
			new StaticFormat("css", new MediaType[] { Kite9MediaTypes.CSS}), 
			new StaticFormat("js", new MediaType[] { Kite9MediaTypes.JS}),
			new StaticFormat("jpeg", new MediaType[] { Kite9MediaTypes.JPEG }),
			new StaticFormat("jpg", new MediaType[] { Kite9MediaTypes.JPEG }),
							
			// fonts
			new StaticFormat("woff", new MediaType[] { Kite9MediaTypes.WOFF}),
			new StaticFormat("woff2", new MediaType[] { Kite9MediaTypes.WOFF2}),
			new StaticFormat("eot", new MediaType[] { Kite9MediaTypes.EOT}),
			new StaticFormat("otf", new MediaType[] { Kite9MediaTypes.OTF}),
			new StaticFormat("ttf", new MediaType[] { Kite9MediaTypes.TTF}),
			};
				
		mediaTypes = Arrays.stream(formats)
				.flatMap(f -> f.getMediaTypes().stream())
				.collect(Collectors.toList())
				.toArray(new MediaType[] {});
	}

	@Override
	public Format getFormatFor(MediaType mt) {
		// look for exact match first
		for (Format format : formats) {
			for (MediaType m : format.getMediaTypes()) {
				if (m.equals(mt)) {
					return format;
				}
			}
		}
		
		// return anything suitable
		for (Format format : formats) {
			for (MediaType mtProvided : format.getMediaTypes()) {
				if (mtProvided.isCompatibleWith(mt)) {
					return format;
				}
			}
		}

		throw new IllegalArgumentException("Format not supported:" + mt);
	}

	public List<MediaType> getMediaTypes() {
		return Arrays.asList(mediaTypes);
	}

	@Override
	public Map<String, MediaType> getMediaTypeMap() {
		return Arrays.stream(formats)
				.collect(Collectors.toMap(Format::getExtension, f -> f.getMediaTypes().get(0)));
	}

	@Override
	public Optional<Format> getFormatFor(String path) {
		for (Format format : formats) {
			if (path.endsWith(format.getExtension())) {
				return Optional.of(format);
			}
		}

		return Optional.empty();
	}

	@Override
	public Optional<Format> getFormatFor(URI u) {
		return getFormatFor(u.getPath());
	}
	

	public MediaType getMediaTypeFor(URI sourceUri) {
		String path = sourceUri.getPath();
		return getMediaTypeFor(path);
	}

	public MediaType getMediaTypeFor(String path) {
		if (path.contains(".")) {
			String ext = path.substring(path.lastIndexOf(".")+1);
			MediaType out = getMediaTypeMap().get(ext);
			if (out == null) {
				throw new Kite9ProcessingException("Don't know media type for "+ext);
			} else {
				return out;
			}
		} else {
			return null;
		}
	}

	@Override
	public Format[] getPriorityOrderedFormats() {
		return formats;
	}	

}
