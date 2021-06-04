package com.kite9.server.controllers;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.kite9.server.domain.RestEntity;
import com.kite9.server.update.AbstractUpdateHandler;
import com.kite9.server.update.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.server.pipeline.adl.format.media.DiagramReadFormat;
import com.kite9.server.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.server.pipeline.adl.format.media.Format;
import com.kite9.server.pipeline.adl.format.media.NotKite9DiagramException;
import com.kite9.server.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.sources.DiagramFileAPI;
import com.kite9.server.sources.DirectoryAPI;
import com.kite9.server.sources.FileAPI;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;

/**
 * Handles conversion of source format into ADL.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractNegotiatingController extends AbstractUpdateHandler {
	
	private SourceAPIFactory factory;
	protected FormatSupplier fs;
	
	@Autowired
	ContentNegotiationStrategy negotiator;

	public AbstractNegotiatingController(FormatSupplier fs, SourceAPIFactory factory) {
		this.fs = fs;
		this.factory = factory;
	}

	public ResponseEntity<?> contentNegotiation(RequestEntity<?> req, 
			URI sourceUri, 
			URI rewrittenURI, 
			HttpHeaders headers, 
			List<MediaType> putMediaType,
			Authentication authentication) throws Exception {
		
		
		Update u = new Update(Collections.emptyList(), sourceUri, Update.Type.NEW);
		SourceAPI s = getSourceAPI(u, authentication);
		return contentNegotiation(req, s, rewrittenURI, headers, putMediaType, authentication);
	}
		
		
	protected ResponseEntity<?> contentNegotiation(
					RequestEntity<?> req, 
					SourceAPI s, 
					URI rewrittenURI, 
					HttpHeaders headers, 
					List<MediaType> putMediaType,
					Authentication authentication) throws Exception {
		
		if (s instanceof DirectoryAPI) {
			return outputDirectory(s, rewrittenURI, headers, getBestDiagramMediaType(putMediaType), authentication);
		} 

		if ((s instanceof ModifiableDiagramAPI) && (((ModifiableAPI) s).getType(authentication) == ModifiableAPI.Type.CREATABLE)) {
			return handleCreatableContent(req, authentication, rewrittenURI, (ModifiableDiagramAPI) s, putMediaType, headers);
		}
		
		if (s instanceof DiagramFileAPI) {
			DiagramFileAPI api = (DiagramFileAPI) s;
			if (putMediaType.contains(api.getMediaType())) {
				return unconvertedOutput(rewrittenURI, headers, authentication, api);
			} else {
				try {
					return convertDiagram(rewrittenURI, headers, getBestDiagramMediaType(putMediaType), authentication, api);
				} catch (Exception e) {
					LOG.debug("Couldn't convert diagram to "+putMediaType+ " from "+ api.getMediaType());
				}
			}
		}
			
		if (s instanceof FileAPI) {
			FileAPI api = (FileAPI) s;
			return unconvertedOutput(rewrittenURI, headers, authentication, api);
		}
		
		throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
	}

	protected MediaType getBestDiagramMediaType(List<MediaType> putMediaType) {
		return putMediaType.stream()
			.filter(mt -> fs.getFormatFor(mt) instanceof DiagramWriteFormat)
			.findFirst()
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Can't convert to any of "+putMediaType));
	}

	protected ResponseEntity<?> outputDirectory(SourceAPI s, URI rewrittenURI, HttpHeaders headers,
			MediaType putMediaType, Authentication authentication) throws Exception {
		RestEntity re = ((DirectoryAPI) s).getEntityRepresentation(authentication);
		return new ResponseEntity<RestEntity>(re, createResponseHeaders(rewrittenURI, headers, putMediaType, true), HttpStatus.OK);
	}

	protected ResponseEntity<ADLDom> convertDiagram(URI rewrittenURI, HttpHeaders headers, MediaType putMediaType,
			Authentication authentication, DiagramFileAPI api) throws Exception {
			// we always load and process the diagram here, adding dynamic metadata as we go.
		try {
			Format inFormat = fs.getFormatFor(api.getMediaType());
			if (inFormat instanceof DiagramReadFormat) {
				ADLBase in =  api.getCurrentRevisionContent(authentication, headers);
				ADLDom dom = in.parse();
				handleDynamicMetadata(authentication, rewrittenURI, dom, api, putMediaType);
				return new ResponseEntity<ADLDom>(dom, createResponseHeaders(rewrittenURI, headers, putMediaType, true), HttpStatus.OK);
			} else {
				throw new NotKite9DiagramException("Format for "+rewrittenURI+" was "+inFormat.getClass());
			}
		} catch (WebClientResponseException e) {
			throw convertToResponseStatusException(e);
		}
	}

	protected ResponseEntity<?> unconvertedOutput(URI rewrittenURI, HttpHeaders headers, Authentication authentication,
			FileAPI api) throws Exception {
		InputStreamResource ris = new InputStreamResource(api.getCurrentRevisionContentStream(authentication));
		return new ResponseEntity<InputStreamResource>(ris, createResponseHeaders(rewrittenURI, headers, api.getMediaType(), false), HttpStatus.OK);
	}
	
	private ResponseStatusException convertToResponseStatusException(WebClientResponseException e) {
		return new ResponseStatusException(e.getStatusCode(), e.getMessage(), e);
	}

	protected void handleDynamicMetadata(Authentication authentication, URI uri, MetaReadWrite adl, SourceAPI api, MediaType out) {
	
	}

	/**
	 * Headers that are independent of the content being returned.
	 */
	protected HttpHeaders createResponseHeaders(URI uri, HttpHeaders headers, MediaType mt, boolean modifiable) {
		HttpHeaders out = new HttpHeaders();
		out.setContentType(mt);
		return out;
	}

	protected ResponseEntity<?> handleCreatableContent(RequestEntity<?> req, Authentication authentication, URI uri, ModifiableDiagramAPI api, List<MediaType> putMediaType, HttpHeaders headers) throws Exception {
		throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}
	
	public SourceAPI getSourceAPI(Update u, Authentication a) throws Exception {
		return factory.createAPI(u, a);
	}
	

	protected DiagramWriteFormat getOutputFormat(NativeWebRequest req) {
		DiagramWriteFormat format;
		List<MediaType> accepted = Collections.emptyList();
		try {
			accepted = 	new ArrayList<>(negotiator.resolveMediaTypes(req));
			MediaType best = getBestDiagramMediaType(accepted);
			format = (DiagramWriteFormat) fs.getFormatFor(best);
		} catch (Exception e1) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Couldn't return diagram matching "+accepted, e1);
		}
		return format;
	}
	
	protected DiagramWriteFormat getOutputFormat(RequestEntity<?> req) {
		DiagramWriteFormat format;
		List<MediaType> accepted = Collections.emptyList();
		try {
			accepted = req.getHeaders().getAccept();
			MediaType best = getBestDiagramMediaType(accepted);
			format = (DiagramWriteFormat) fs.getFormatFor(best);
		} catch (Exception e1) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Couldn't return diagram matching "+accepted, e1);
		}
		return format;
	}

	protected List<MediaType> getMediaTypes(NativeWebRequest webRequest)
			throws HttpMediaTypeNotAcceptableException {
		List<MediaType> types = new ArrayList<>(negotiator.resolveMediaTypes(webRequest));
		types.replaceAll(mt -> mt == MediaType.ALL ? MediaType.TEXT_HTML : mt);
		return types;
	}


}