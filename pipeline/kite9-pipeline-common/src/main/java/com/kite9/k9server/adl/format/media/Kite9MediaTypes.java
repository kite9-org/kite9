package com.kite9.k9server.adl.format.media;

import sun.jvm.hotspot.oops.MethodData;

/**
 * Defines many media types used by kite9.
 *
 * @author robmoffat
 *
 */
public class Kite9MediaTypes {

	// json
	public static final String HAL_JSON_VALUE = "application/hal+json";
	public static final String APPLICATION_JSON_VALUE = "application/json";
	public static final MediaType HAL_JSON;
	public static final MediaType APPLICATION_JSON;

	public static final String TEXT_XML_VALUE = "text/xml";
	public static final String APPLICATION_XML_VALUE = "application/xml";
	public static final MediaType TEXT_XML;
	public static final MediaType APPLICATION_XML;

	// diagram formats
	public static final String ADL_SVG_VALUE = "text/adl-svg+xml";	// input ADL
	public static final String EDITABLE_SVG_VALUE = "image/svg+xml;purpose=editable";	// editable SVG, with references to fonts and stylesheets.
	public static final String PNG_VALUE = "image/png";
	public static final String SVG_VALUE = "image/svg+xml";			// output SVG, all styles/images encapsulated
	public static final String PDF_VALUE = "application/pdf";			// output PDF
	public static final String CLIENT_SIDE_IMAGE_MAP_VALUE = "text/html-image-map";  // output html-fragment
	public static final String JAVASCRIPT_VALUE = "text/javascript";			// JS
	public static final String CSS_VALUE = "text/css";			// CSS
	public static final String XSLT_VALUE = "application/xslt+xml";
	public static final String JPEG_VALUE = "image/jpeg";

	public static final MediaType SVG;
	public static final MediaType XSLT;
	public static final MediaType ESVG;
	public static final MediaType PNG;
	public static final MediaType JPEG;
	public static final MediaType PDF;
	public static final MediaType ADL_SVG;
	public static final MediaType CLIENT_SIDE_IMAGE_MAP;
	public static final MediaType JS;
	public static final MediaType CSS;



	// fonts
	public static final String WOFF_VALUE = "font/woff";
	public static final String WOFF2_VALUE = "font/woff2";
	public static final String TTF_VALUE = "font/ttf";
	public static final String OTF_VALUE = "font/otf";
	public static final String EOT_VALUE = "application/vnd.ms-fontobject";


	// fonts
	public static final MediaType WOFF;
	public static final MediaType WOFF2;
	public static final MediaType TTF;
	public static final MediaType EOT;
	public static final MediaType OTF;

	
	static {
		SVG = MediaType.parseMediaType(SVG_VALUE);
		ESVG = MediaType.parseMediaType(EDITABLE_SVG_VALUE);
		
		PDF = MediaType.parseMediaType(PDF_VALUE);
		PNG = MediaType.parseMediaType(PNG_VALUE);
		JPEG = MediaType.parseMediaType(JPEG_VALUE);
		ADL_SVG = MediaType.parseMediaType(ADL_SVG_VALUE);
		CLIENT_SIDE_IMAGE_MAP = MediaType.parseMediaType(CLIENT_SIDE_IMAGE_MAP_VALUE);
		JS = MediaType.parseMediaType(JAVASCRIPT_VALUE);
		CSS = MediaType.parseMediaType(CSS_VALUE);

		WOFF =  MediaType.parseMediaType(WOFF_VALUE);
		WOFF2 =  MediaType.parseMediaType(WOFF2_VALUE);
		TTF =  MediaType.parseMediaType(TTF_VALUE);
		EOT =  MediaType.parseMediaType(EOT_VALUE);
		OTF =  MediaType.parseMediaType(OTF_VALUE);
		XSLT =  MediaType.parseMediaType(XSLT_VALUE);

		APPLICATION_JSON = MediaType.parseMediaType(APPLICATION_JSON_VALUE);
		HAL_JSON = MediaType.parseMediaType(HAL_JSON_VALUE);

		TEXT_XML = MediaType.parseMediaType(APPLICATION_XML_VALUE);
		APPLICATION_XML = MediaType.parseMediaType(TEXT_XML_VALUE);

	}
}
