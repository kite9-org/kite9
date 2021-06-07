package com.kite9.server.persistence.github;

import java.net.URI;

import javax.servlet.http.HttpServletRequest;

import com.kite9.pipeline.uri.K9URI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.server.controllers.AbstractContentController;
import com.kite9.server.sources.ModifiableAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.web.URIRewriter;

/**
 * Allows uploading of files into github.  Used for images.
 * 
 * @author robmoffat
 *
 */
@RestController
public class GithubContentController extends AbstractContentController {
	
	public GithubContentController(FormatSupplier fs, SourceAPIFactory factory) {
		super(fs, factory); 
	}

	public static final String GITHUB = "github";
		
	public static final String DEFAULT_GITHUB_UPLOADS = "/.kite9/uploads";
	
	public static final String DEFAULT_GITHUB_TEMPLATES = ".kite9/templates";
	
	@Autowired
	protected GithubSourceAPIFactory apiFactory;
	
	@PostMapping(path = { "/"+GITHUB+"/**" }, consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Void> upload(@RequestHeader HttpHeaders headers, HttpServletRequest req,
			@RequestParam("file") MultipartFile file, Authentication authentication) throws Exception {
		K9URI uri = URIRewriter.getCompleteCurrentRequestURI();
		SourceAPI api = apiFactory.createAPI(uri,authentication);
		if (api instanceof ModifiableAPI) {
			byte[] bytes = StreamUtils.copyToByteArray(file.getInputStream());
			((ModifiableAPI) api).commitRevisionAsBytes("File Upload "+uri, authentication, bytes);
			return new ResponseEntity<Void>(HttpStatus.OK);
		} else {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Can't  modify this resource: "+uri);
		}
	}
	
}