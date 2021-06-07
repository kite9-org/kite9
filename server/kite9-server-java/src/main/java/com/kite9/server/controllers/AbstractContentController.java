package com.kite9.server.controllers;

import java.net.URI;
import java.util.List;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.adl.holder.meta.MetaHelper;
import com.kite9.server.uri.URIWrapper;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;

/**
 * Used for loading content from writable ContentAPIs, like Github.  Handles creation of initial versions
 * of content, and setting edit details in the metadata.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractContentController extends AbstractNegotiatingController {
	
	public AbstractContentController(FormatSupplier fs, SourceAPIFactory factory) {
		super(fs, factory);
	}

	@Override
	protected void handleDynamicMetadata(Authentication authentication, K9URI uri, MetaReadWrite adl, SourceAPI api, K9MediaType out) {
		super.handleDynamicMetadata(authentication, uri, adl, api, out);
		MetaHelper.setUser(adl);
		adl.setUri(uri);
		if (api instanceof ModifiableDiagramAPI) {
			((ModifiableDiagramAPI) api).addMeta(adl);
		}
		if (api instanceof ModifiableAPI) {
			adl.setRole(((ModifiableAPI) api).getAuthenticatedRole(authentication));
		}
	}

	@Override
	protected ResponseEntity<?> handleCreatableContent(RequestEntity<?> req, Authentication authentication, K9URI uri,
													   ModifiableDiagramAPI api, List<K9MediaType> putMediaType, HttpHeaders headers) throws Exception {
		MultiValueMap<String, String> params = UriComponentsBuilder.fromUri(URIWrapper.from(uri)).build().getQueryParams();
		String template = params.getFirst("templateUri");
		if (StringUtils.isEmpty(template)) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File doesn't exist and templateUri parameter not supplied");
		} else {
			// creating a new document
			Format f2 = fs.getFormatFor(template);
			if (f2 == null) {
				throw new Kite9ProcessingException("no format for "+template);
			}
			URI templateUri = new URI(template);
			if (f2 instanceof DiagramFileFormat) {
				ADLBase adl = ((DiagramFileFormat) f2).handleRead(URIWrapper.wrap(templateUri), headers);
				ADLDom dom = adl.parse();
				api.commitRevision("Created New Diagram in Kite9 from "+uri, authentication, dom);
				handleDynamicMetadata(authentication, uri, dom, api, getBestDiagramMediaType(putMediaType));
				return new ResponseEntity<ADLDom>(dom, HttpStatus.OK);
			} else {
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't load diagram from "+templateUri);
			}
		}
	}

}
