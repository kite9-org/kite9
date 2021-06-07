package com.kite9.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.kite9.pipeline.adl.format.media.*;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.update.AbstractUpdateHandler;
import com.kite9.server.update.Update;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.accept.ContentNegotiationStrategy;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
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
												K9URI sourceUri,
												K9URI rewrittenURI,
												HttpHeaders headers,
												List<K9MediaType> putMediaType,
												Authentication authentication) throws Exception {
		
		
		Update u = new Update(Collections.emptyList(), sourceUri, Update.Type.NEW);
		SourceAPI s = getSourceAPI(u, authentication);
		return contentNegotiation(req, s, rewrittenURI, headers, putMediaType, authentication);
	}
		
		
	protected ResponseEntity<?> contentNegotiation(
			RequestEntity<?> req,
			SourceAPI s,
			K9URI rewrittenURI,
			HttpHeaders headers,
			List<K9MediaType> putMediaType,
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

	protected K9MediaType getBestDiagramMediaType(List<K9MediaType> putMediaType) {
		return putMediaType.stream()
			.filter(mt -> fs.getFormatFor(mt) instanceof DiagramWriteFormat)
			.findFirst()
			.orElseThrow(() -> new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Can't convert to any of "+putMediaType));
	}

	protected ResponseEntity<?> outputDirectory(SourceAPI s, K9URI rewrittenURI, HttpHeaders headers,
												K9MediaType putMediaType, Authentication authentication) throws Exception {
		RestEntity re = ((DirectoryAPI) s).getEntityRepresentation(authentication);
		return new ResponseEntity<RestEntity>(re, createResponseHeaders(rewrittenURI, headers, putMediaType, true), HttpStatus.OK);
	}

	protected ResponseEntity<ADLDom> convertDiagram(K9URI rewrittenURI, HttpHeaders headers, K9MediaType putMediaType,
													Authentication authentication, DiagramFileAPI api) throws Exception {
			// we always load and process the diagram here, adding dynamic metadata as we go.
		try {
			Format inFormat = fs.getFormatFor(api.getMediaType());
			if (inFormat instanceof DiagramFileFormat) {
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

	protected ResponseEntity<?> unconvertedOutput(K9URI rewrittenURI, HttpHeaders headers, Authentication authentication,
												  FileAPI api) throws Exception {
		InputStreamResource ris = new InputStreamResource(api.getCurrentRevisionContentStream(authentication));
		return new ResponseEntity<InputStreamResource>(ris, createResponseHeaders(rewrittenURI, headers, api.getMediaType(), false), HttpStatus.OK);
	}
	
	private ResponseStatusException convertToResponseStatusException(WebClientResponseException e) {
		return new ResponseStatusException(e.getStatusCode(), e.getMessage(), e);
	}

	protected void handleDynamicMetadata(Authentication authentication, K9URI uri, MetaReadWrite adl, SourceAPI api, K9MediaType out) {
	
	}

	/**
	 * Headers that are independent of the content being returned.
	 */
	protected HttpHeaders createResponseHeaders(K9URI uri, HttpHeaders headers, K9MediaType mt, boolean modifiable) {
		HttpHeaders out = new HttpHeaders();
		out.set(HttpHeaders.CONTENT_TYPE, mt.toString());
		return out;
	}

	protected ResponseEntity<?> handleCreatableContent(RequestEntity<?> req, Authentication authentication, K9URI uri, ModifiableDiagramAPI api, List<K9MediaType> putMediaType, HttpHeaders headers) throws Exception {
		throw new ResponseStatusException(HttpStatus.NOT_FOUND);
	}
	
	public SourceAPI getSourceAPI(Update u, Authentication a) throws Exception {
		return factory.createAPI(u, a);
	}
	

	protected DiagramWriteFormat getOutputFormat(NativeWebRequest req) {
		DiagramWriteFormat format;
		List<K9MediaType> accepted = Collections.emptyList();
		try {
			accepted = getMediaTypes(req);
			K9MediaType best = getBestDiagramMediaType(accepted);
			format = (DiagramWriteFormat) fs.getFormatFor(best);
		} catch (Exception e1) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Couldn't return diagram matching "+accepted, e1);
		}
		return format;
	}
	
	protected DiagramWriteFormat getOutputFormat(RequestEntity<?> req) {
		DiagramWriteFormat format;
		List<K9MediaType> accepted = Collections.emptyList();
		try {
			accepted = req.getHeaders().get(HttpHeaders.ACCEPT).stream()
					.map(s -> K9MediaType.Companion.parseMediaType(s))
					.collect(Collectors.toList());
			K9MediaType best = getBestDiagramMediaType(accepted);
			format = (DiagramWriteFormat) fs.getFormatFor(best);
		} catch (Exception e1) {
			throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Couldn't return diagram matching "+accepted, e1);
		}
		return format;
	}

	protected List<K9MediaType> getMediaTypes(NativeWebRequest webRequest)
			throws HttpMediaTypeNotAcceptableException {
		List<K9MediaType> types = negotiator.resolveMediaTypes(webRequest).stream()
				.map(m -> K9MediaType.Companion.parseMediaType(m.toString()))
				.collect(Collectors.toList());
		types.replaceAll(mt -> mt == Kite9MediaTypes.INSTANCE.getALL() ? Kite9MediaTypes.INSTANCE.getHTML() : mt);
		return types;
	}


}