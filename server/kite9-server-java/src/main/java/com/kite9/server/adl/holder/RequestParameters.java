package com.kite9.server.adl.holder;

import java.net.URI;

import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.TranscodingHints.Key;
import org.kite9.diagram.batik.format.Kite9SVGTranscoder;
import org.kite9.diagram.format.Kite9Transcoder;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Handles converting details of the HTTP request parameters into transcoder configuration
 *
 * @author robmoffat
 *
 */
public class RequestParameters {

	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String PIXELS_PER_MM = "pixelsPerMM";
	public static final String MAX_WIDTH = "maxWidth";
	public static final String MAX_HEIGHT = "maxHeight";
	public static final String MEDIA = "media";
	public static final String TEMPLATE = "template";

	public static void configure(URI uri, Kite9Transcoder transcoder) {
		MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUri(uri).build().getQueryParams();
		setFloatIfPresent(MAX_HEIGHT, queryParams, transcoder, SVGAbstractTranscoder.KEY_MAX_HEIGHT);
		setFloatIfPresent(MAX_WIDTH, queryParams, transcoder, SVGAbstractTranscoder.KEY_MAX_WIDTH);
		setFloatIfPresent(PIXELS_PER_MM, queryParams, transcoder, SVGAbstractTranscoder.KEY_PIXEL_UNIT_TO_MILLIMETER);
		setFloatIfPresent(HEIGHT, queryParams, transcoder, SVGAbstractTranscoder.KEY_HEIGHT);
		setFloatIfPresent(WIDTH, queryParams, transcoder, SVGAbstractTranscoder.KEY_WIDTH);
		setStringIfPresent(MEDIA, queryParams, transcoder, SVGAbstractTranscoder.KEY_MEDIA);
		setStringIfPresent(TEMPLATE, queryParams, transcoder, Kite9SVGTranscoder.KEY_TEMPLATE);
	}

	private static void setStringIfPresent(String paramName, MultiValueMap<String, String> queryParams, Kite9Transcoder transcoder, Key key) {
		if (queryParams.containsKey(paramName)) {
			String paramValue = queryParams.getFirst(paramName);
			transcoder.addTranscodingHint(key, paramValue);
		}
	}

	private static void setFloatIfPresent(String paramName, MultiValueMap<String, String> queryParams, Kite9Transcoder transcoder, Key key) {
		if (queryParams.containsKey(paramName)) {
			String paramValue = queryParams.getFirst(paramName);
			float floatValue = Float.parseFloat(paramValue);
			transcoder.addTranscodingHint(key, floatValue);
		}
	}
}
