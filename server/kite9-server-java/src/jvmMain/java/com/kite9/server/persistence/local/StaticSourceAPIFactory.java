package com.kite9.server.persistence.local;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.concurrent.TimeUnit;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StreamUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.format.media.NotKite9DiagramException;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.MediaTypeHelper;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.persistence.RelativeHostLinkBuilder;
import com.kite9.server.persistence.local.conversion.AbstractPublicEntityConverter;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.update.Update;
import com.kite9.server.web.URIRewriter;

/**
 * Handles content that cannot be changed - either referenced from a classpath resource
 * or loaded from a random URL on the internet.
 * 
 * @author robmoffat
 */
public class StaticSourceAPIFactory implements SourceAPIFactory {
	
	protected final Logger LOG = LoggerFactory.getLogger(StaticSourceAPIFactory.class);
	
	protected Cache publicCache;	
	protected ResourcePatternResolver rl;
	protected AbstractPublicEntityConverter ec;
	protected FormatSupplier fs;

	public StaticSourceAPIFactory(Cache publicCache, ResourcePatternResolver rl, FormatSupplier fs) {
		super();
		this.publicCache = publicCache;
		this.rl = rl;
		this.fs = fs;
		this.ec = new AbstractPublicEntityConverter("/public", "classpath:/static/public", rl) {

			@Override
			protected LinkBuilder linkToRemappedURI() {
				return RelativeHostLinkBuilder.linkToCurrentMapping();
			}
			
		};
	}
	
	@Override
	public SourceAPI createAPI(Update update, Authentication a) throws Exception {
		K9URI sourceUri = update.getUri();
		if (update.getBase64adl()!=null) {
			// update contains the ADL we're going to use
			Decoder d = Base64.getDecoder();
			byte[] bytes = d.decode(update.getBase64adl());
			return createAPIFromBytes(sourceUri, update.getHeaders(), bytes, Kite9MediaTypes.INSTANCE.getADL());
		} else {
			// we need to load from URL
			if (sourceUri == null) { 
				throw new Kite9ProcessingException("No URI or ADL provided in request");
			}

			K9MediaType underlying = fs.getMediaTypeFor(sourceUri);
			
			if (URIRewriter.localPublicContent(sourceUri)) {
				// first, check local cache
				byte[] cacheContent = publicCache.getBytes(sourceUri.toString(), underlying.toString());
				if (cacheContent != null) {
					return createAPIFromBytes(sourceUri, update.getHeaders(), cacheContent, underlying);
				} else {
					String uStr = sourceUri.getPath();
					String resourceStub = "classpath:/static" + uStr;
					Resource r = rl.getResource(resourceStub);
					
					if (r.exists()) {
						if (r.isReadable()) {
							byte[] bytes = StreamUtils.copyToByteArray(r.getInputStream());
							return createAPIFromBytes(sourceUri, update.getHeaders(), bytes, underlying);	
						} 
					} 
					
					if (r instanceof ClassPathResource) {
						
						//Resource[] res = rl.getResources(((ClassPathResource)r).getURI()+"/*");
						return createAPIFromResourceListing(sourceUri, update.getHeaders(), underlying);
					}
					
					return null;	
				}			
			} else {
				// loading from the internet somewhere...
				return createAPIFromRemoteUrl(sourceUri);
			}
		}	
	}

	private SourceAPI createAPIFromResourceListing(K9URI sourceUri, HttpHeaders headers,
			K9MediaType underlying) {
		return new AbstractStaticSourceAPI(underlying, sourceUri) {

			@Override
			public RestEntity getEntityRepresentation(Authentication a) throws Exception {
				return ec.handleEntityContent(a, sourceUri.getPath());
			}
			
		};
	}

	protected SourceAPI createAPIFromBytes(K9URI uri, HttpHeaders headers, byte[] bytes, K9MediaType mt) throws Exception {
		Format f = fs.getFormatFor(mt);
		try {
			if (f instanceof DiagramFileFormat) {
				DiagramFileFormat dff = (DiagramFileFormat) f;
				ADLBase base = dff.handleRead(new ByteArrayInputStream(bytes), uri, headers);
				return new TransientDiagramAPI(mt, base);
			}
		} catch (NotKite9DiagramException e) {
			LOG.debug("Couldn't find kite9 diagram in: "+uri);
		} 
		
		return new AbstractStaticSourceAPI(mt, bytes, uri) {

			@Override
			public RestEntity getEntityRepresentation(Authentication a) throws Exception {
				throw new UnsupportedOperationException("Can't show directory for file");
			}
		};
	}
	
	public static MultiValueMap<String, String> createCachingHeaders() {
		CacheControl cc = CacheControl.maxAge(5, TimeUnit.DAYS);
		HttpHeaders h = new HttpHeaders();
		h.add(HttpHeaders.CACHE_CONTROL, cc.getHeaderValue());
		return h;
	}

	/**
	 * We accept "ALL" media type here since github doesn't return the correct one
	 */
	protected SourceAPI createAPIFromRemoteUrl(K9URI u) {
		try {
			WebClient webClient = WebClient.create(u.toString());
			ClientResponse cr = webClient.get()
					.header("Accept-Encoding", "identity")
					.header(HttpHeaders.ACCEPT, Kite9MediaTypes.ALL_VALUE)
					.exchange().block();
			DataBuffer db = cr.bodyToMono(DataBuffer.class).block();
			return createAPIFromBytes(u, HttpHeaders.EMPTY, db.asByteBuffer().array(), getContentType(cr, u));
		} catch (Exception e) {
			throw new Kite9XMLProcessingException("Couldn't request data from: " + u, e, null, null);
		}
	}

	private K9MediaType getContentType(ClientResponse cr, K9URI u) {
		return cr.headers()
				.contentType()
				.map(mt -> MediaTypeHelper.getKite9MediaType(mt))
				.orElseThrow(() ->
				new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Media type not provided for "+u));
	}
}
