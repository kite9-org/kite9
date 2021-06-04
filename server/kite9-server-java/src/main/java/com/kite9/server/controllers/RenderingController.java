package com.kite9.server.controllers;

import static com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes.ADL_SVG;
import static com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes.ADL_SVG_VALUE;
import static com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes.EDITABLE_SVG_VALUE;
import static com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes.SVG;
import static com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes.SVG_VALUE;
import static org.springframework.http.MediaType.IMAGE_PNG;
import static org.springframework.http.MediaType.IMAGE_PNG_VALUE;
import static org.springframework.http.MediaType.TEXT_HTML_VALUE;

import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import com.kite9.server.update.Update;
import com.kite9.server.web.URIRewriter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.context.request.NativeWebRequest;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;

/**
 * Handles rendering of Kite9 ADL content.  A bit like a simpler version of the CommandController, 
 * except that it just renders the ADL.
 * 
 * @author robmoffat
 *
 */
@Controller
public class RenderingController extends AbstractNegotiatingController {
	
	public RenderingController(FormatSupplier fs, SourceAPIFactory factory) {
		super(fs, factory);
	}

	@GetMapping(path= {"/api/renderer", "api/renderer.svg"}, produces= {SVG_VALUE})
	public ResponseEntity<?> renderSVG(
			RequestEntity<?> request,
			@RequestParam("uri") String uri, 
			@RequestHeader HttpHeaders headers) throws Exception {
		return contentNegotiation(request, new URI(uri), URIRewriter.getCompleteCurrentRequestURI(), headers, Arrays.asList(SVG), null);
	}
	
	@GetMapping(path= {"/api/renderer", "api/renderer.adl"}, produces= {ADL_SVG_VALUE})
	public ResponseEntity<?> renderADL(
			RequestEntity<?> request,
			@RequestParam("uri") String uri, 
			@RequestHeader HttpHeaders headers) throws Exception {
		return contentNegotiation(request, new URI(uri), URIRewriter.getCompleteCurrentRequestURI(), headers, Arrays.asList(ADL_SVG), null);
	}
	
	@GetMapping(path= {"/api/renderer", "api/renderer.png"}, produces= {IMAGE_PNG_VALUE})
	public ResponseEntity<?> renderPNG(
			RequestEntity<?> request,
			@RequestParam("uri") String uri, 
			@RequestHeader HttpHeaders headers) throws Exception {
		return contentNegotiation(request, new URI(uri), URIRewriter.getCompleteCurrentRequestURI(), headers, Arrays.asList(IMAGE_PNG), null);
	}

	@PostMapping(path="/api/renderer", consumes= {ADL_SVG_VALUE}, 
		produces= {SVG_VALUE, EDITABLE_SVG_VALUE, IMAGE_PNG_VALUE, ADL_SVG_VALUE, TEXT_HTML_VALUE})
	public ResponseEntity<?> postEcho(
			@RequestBody byte[] adlSvg, 
			RequestEntity<?> request,
			NativeWebRequest webRequest,
			@RequestHeader HttpHeaders headers) throws Exception {
		String encoded = Base64.getEncoder().encodeToString(adlSvg);
		Update u = new Update(Collections.emptyList(), URIRewriter.getCompleteCurrentRequestURI(), encoded, Update.Type.NEW);
		SourceAPI s = getSourceAPI(u, null);
		List<MediaType> accepted = getMediaTypes(webRequest);
		return contentNegotiation(request, s, URIRewriter.getCompleteCurrentRequestURI(), headers, accepted, null);

	}

}
