package com.kite9.k9server.controllers;

import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;

import com.kite9.k9server.adl.format.FormatSupplier;
import com.kite9.k9server.adl.format.media.DiagramWriteFormat;
import com.kite9.k9server.adl.format.media.Kite9MediaTypes;
import com.kite9.k9server.adl.holder.pipeline.ADLDom;
import com.kite9.k9server.adl.holder.pipeline.ADLOutput;
import com.kite9.k9server.sources.ModifiableDiagramAPI;
import com.kite9.k9server.sources.SourceAPIFactory;
import com.kite9.k9server.update.Update;
import com.kite9.k9server.web.URIRewriter;

/**
 * Returns content held within the public folder of the project, which anyone
 * can access, and also the homepage at /.
 * 
 * This avoids responding to .js or .css files by only working for specific file extensions.
 * 
 * @author robmoffat
 *
 */
@Controller
public class PathContentController extends AbstractContentController {

	/**
	 * If we add extra SourceAPIFactory implementations, we'll probably need to change this.
	 * However, don't wildcard it, otherwise it breaks normal resource loading and the 
	 * websockets interface.
	 */
	public static final String MAPPED_PATHS = "/{type:github|public}/**";

	public PathContentController(FormatSupplier fs, SourceAPIFactory factory) {
		super(fs, factory);

	}

	@Value("${kite9.caching:true}")
	boolean caching;
	
	@Value("${kite9.home.path:/public/templates/admin/index.adl}")
	String homePagePath;
	

	CacheControl cc = CacheControl.maxAge(1, TimeUnit.DAYS);
	
	/**
	 * This is the basic mapping for resources, diagrams, directory contents etc. in any format
	 */
	@GetMapping(path = MAPPED_PATHS , produces = MediaType.ALL_VALUE)
	public ResponseEntity<?> load(
			RequestEntity<?> httpRequest, 
			NativeWebRequest webRequest,
			@RequestHeader HttpHeaders headers, 
			Authentication a) throws Exception {
		URI rewrittenURI = URIRewriter.getCompleteCurrentRequestURI();
		List<MediaType> accepted = getMediaTypes(webRequest);
		return contentNegotiation(httpRequest, rewrittenURI, rewrittenURI, headers, accepted, a);
	}
	
	/**
	 * For updates fired without websockets. 
	 */
	@PostMapping(path = MAPPED_PATHS, 
			produces = MediaType.ALL_VALUE, 
			consumes = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ADLOutput<?> updateViaPost(@RequestHeader HttpHeaders headers, NativeWebRequest req, @RequestBody Update update,
			Authentication authentication) throws Exception {
		URI uri = URIRewriter.getCompleteCurrentRequestURI();

		DiagramWriteFormat format = getOutputFormat(req);
		
		try {
			update.setUri(uri);
			update.addHeaders(headers);
			ADLOutput<?> adl = performDiagramUpdate(update, authentication, format);
			return adl;
		} catch (Exception e) {
			String properCause = getProperCause(e);
			LOG.info("User error {}", properCause);
			ModifiableDiagramAPI api = (ModifiableDiagramAPI) getSourceAPI(update, authentication);
			ADLDom dom = api.getCurrentRevisionContent(authentication, headers).parse();
			dom.setError(properCause);
			return dom.process(uri, format);
		}
	}

	
	/**
	 * Anything in the /public area should be cached in production.
	 */
	@Override
	protected HttpHeaders createResponseHeaders(URI uri, HttpHeaders headers, MediaType mt, boolean modifiable) {
		HttpHeaders out = super.createResponseHeaders(uri, headers, mt, modifiable);
		if ((!modifiable) && (caching)) {
			out.setCacheControl(cc);
		}
		return out;
	}

	/**
	 * Home page mapping, any given format.
	 */
	@GetMapping(path = "/", 
		produces = { 
				Kite9MediaTypes.ADL_SVG_VALUE, 
				MediaType.APPLICATION_XML_VALUE, 
				MediaType.TEXT_XML_VALUE, 
				MediaType.IMAGE_PNG_VALUE, 
				Kite9MediaTypes.SVG_VALUE })
	public ResponseEntity<?> homePage(
			RequestEntity<?> request, 
			NativeWebRequest webRequest,
			@RequestHeader HttpHeaders headers, 
			Authentication a)
			throws Exception {
		URI rewrittenURI = URIRewriter.getCompleteCurrentRequestURI();
		URI sourceUri = URIRewriter.resolve(homePagePath);
		List<MediaType> accepted = getMediaTypes(webRequest);
		return contentNegotiation(request, sourceUri, rewrittenURI, request.getHeaders(), accepted, a);
	}

}
