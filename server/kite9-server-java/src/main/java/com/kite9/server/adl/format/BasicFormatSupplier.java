package com.kite9.server.adl.format;

import com.kite9.server.adl.format.media.*;
import com.kite9.server.pipeline.adl.format.media.*;
import com.kite9.server.pipeline.adl.holder.ADLFactory;
import com.kite9.server.pipeline.uri.URI;
import com.kite9.pipeline.adl.format.FormatSupplier;
import org.kite9.diagram.logging.Kite9ProcessingException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;

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
			new EntityFormat("hal", singletonList(Kite9MediaTypes.INSTANCE.getHAL_JSON())),
			new EntityFormat("json", singletonList(Kite9MediaTypes.INSTANCE.getAPPLICATION_JSON())),
			
			// static stuff
			new HTMLFormat(),
			new StaticFormat("xml", Arrays.asList(
					Kite9MediaTypes.INSTANCE.getTEXT_XML(),
					Kite9MediaTypes.INSTANCE.getAPPLICATION_XML())),
			new StaticFormat("xslt", singletonList( Kite9MediaTypes.INSTANCE.getXSLT())),
			new StaticFormat("css", singletonList( Kite9MediaTypes.INSTANCE.getCSS())),
			new StaticFormat("js", singletonList( Kite9MediaTypes.INSTANCE.getJS())),
			new StaticFormat("jpeg", singletonList( Kite9MediaTypes.INSTANCE.getJPEG())),
			new StaticFormat("jpg", singletonList( Kite9MediaTypes.INSTANCE.getJPEG())),
							
			// fonts
			new StaticFormat("woff", singletonList( Kite9MediaTypes.INSTANCE.getWOFF2())),
			new StaticFormat("woff2", singletonList( Kite9MediaTypes.INSTANCE.getWOFF())),
			new StaticFormat("eot", singletonList( Kite9MediaTypes.INSTANCE.getEOT())),
			new StaticFormat("otf", singletonList( Kite9MediaTypes.INSTANCE.getOTF())),
			new StaticFormat("ttf", singletonList( Kite9MediaTypes.INSTANCE.getTTF())),
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
