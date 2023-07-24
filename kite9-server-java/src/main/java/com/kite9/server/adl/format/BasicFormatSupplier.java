package com.kite9.server.adl.format;

import static java.util.Collections.singletonList;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.logging.Kite9ProcessingException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.StaticFormat;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.ADLFormat;
import com.kite9.server.adl.format.media.EditableADLFormat;
import com.kite9.server.adl.format.media.EditableSVGFormat;
import com.kite9.server.adl.format.media.EntityFormat;
import com.kite9.server.adl.format.media.HTMLFormat;
import com.kite9.server.adl.format.media.PNGFormat;
import com.kite9.server.adl.format.media.SVGFormat;

public class BasicFormatSupplier implements FormatSupplier {

	private final Format[] formats;
	private final K9MediaType[] mediaTypes;

	public BasicFormatSupplier(ADLFactory adlFactory, XMLHelper xmlHelper) {
		super();
		this.formats = new Format[] { 
			// things that could be diagrams
			new ADLFormat(adlFactory, xmlHelper),
			new SVGFormat(adlFactory, xmlHelper),
			new PNGFormat(adlFactory, xmlHelper),
			new EditableADLFormat(adlFactory, xmlHelper),
			new EditableSVGFormat(xmlHelper),

			
			// entities
			new EntityFormat("hal", singletonList(Kite9MediaTypes.INSTANCE.getHAL_JSON()), xmlHelper),
			new EntityFormat("json", singletonList(Kite9MediaTypes.INSTANCE.getAPPLICATION_JSON()), xmlHelper),
			
			// static stuff
			new HTMLFormat(xmlHelper),
			new StaticFormat("xml", Arrays.asList(
					Kite9MediaTypes.INSTANCE.getTEXT_XML(),
					Kite9MediaTypes.INSTANCE.getAPPLICATION_XML())),
			new StaticFormat("xslt", singletonList( Kite9MediaTypes.INSTANCE.getXSLT())),
			new StaticFormat("xsl", singletonList( Kite9MediaTypes.INSTANCE.getXSLT())),
			new StaticFormat("css", singletonList( Kite9MediaTypes.INSTANCE.getCSS())),
			new StaticFormat("js", singletonList( Kite9MediaTypes.INSTANCE.getJS())),
			new StaticFormat("map", singletonList( Kite9MediaTypes.INSTANCE.getAPPLICATION_JSON())),
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
				.toArray(new K9MediaType[] {});
	}

	@Override
	public Format getFormatFor(K9MediaType mt) {
		// look for exact match first
		for (Format format : formats) {
			for (K9MediaType m : format.getMediaTypes()) {
				if (m.equals(mt)) {
					return format;
				}
			}
		}
		
		// return anything suitable
		for (Format format : formats) {
			for (K9MediaType mtProvided : format.getMediaTypes()) {
				if (mtProvided.isCompatibleWith(mt)) {
					return format;
				}
			}
		}

		return null;
	}

	public List<K9MediaType> getMediaTypes() {
		return Arrays.asList(mediaTypes);
	}

	@Override
	public Map<String, K9MediaType> getMediaTypeMap() {
		return Arrays.stream(formats)
				.collect(Collectors.toMap(Format::getExtension, f -> f.getMediaTypes().get(0)));
	}

	@Override
	public Format getFormatFor(String path) {
		for (Format format : formats) {
			if (path.endsWith(format.getExtension())) {
				return format;
			}
		}

		return null;
	}

	@Override
	public Format getFormatFor(K9URI u) {
		return getFormatFor(u.getPath());
	}
	

	public K9MediaType getMediaTypeFor(K9URI sourceUri) {
		String path = sourceUri.getPath();
		return getMediaTypeFor(path);
	}

	public K9MediaType getMediaTypeFor(String path) {
		if (path.contains(".")) {
			String ext = path.substring(path.lastIndexOf(".")+1);
			K9MediaType out = getMediaTypeMap().get(ext);
			if (out == null) {
				throw new Kite9ProcessingException("Don't know media type for "+ext);
			} else {
				return out;
			}
		} else {
			// assume renderable directory?
			return Kite9MediaTypes.INSTANCE.getHTML();
		}
	}

	@Override
	public Format[] getPriorityOrderedFormats() {
		return formats;
	}	

}
