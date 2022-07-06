package com.kite9.server.persistence.github;

import static com.kite9.server.persistence.PathUtils.FILEPATH;
import static com.kite9.server.persistence.PathUtils.OWNER;
import static com.kite9.server.persistence.PathUtils.REPONAME;
import static com.kite9.server.persistence.PathUtils.getPathSegment;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.security.LoginRequiredException;
import com.kite9.server.security.LoginRequiredException.Type;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.web.URIRewriter;

import reactor.core.publisher.Mono;

/**
 * Represents files or directories loaded from Github.
 * 
 * @author rob@kite9.com
 *
 */
public abstract class AbstractGithubSourceAPI implements SourceAPI {

	private static final String RAW_GITHUB = "https://raw.githubusercontent.com/";
	private static final String HEAD_REF = "HEAD";
	protected static final Object NO_FILE = new Object();
	protected static final Object ORG_PAGE = new Object();
	
	protected static Logger LOG = LoggerFactory.getLogger(SourceAPI.class);
	
	private final K9URI resourceUri;
	protected String path;
	protected String owner;
	protected String reponame;
	protected String filepath;
	protected String ref;
	protected OAuth2AuthorizedClientRepository clientRepository;
	protected Set<String> validTokens;
	protected Object contents;
	
	public AbstractGithubSourceAPI(K9URI u, OAuth2AuthorizedClientRepository clientRepository) {
		this.path = u.getPath();
		this.ref = getRef(u);
		this.owner = getPathSegment(OWNER, path);
		this.reponame = getPathSegment(REPONAME, path);
		this.filepath = getPathSegment(FILEPATH, path);
		this.clientRepository = clientRepository;
		this.resourceUri = unmodifiedURI(u);
	}

	/**
	 * This simplifies the URI to remove any query parameters
	 */
	private static K9URI unmodifiedURI(K9URI u) {
		K9URI out = u.withoutQueryParameters();
		return out;
	}
	public static String getRef(K9URI u) {
		List<String> param = u.param("v");
		if ((param == null) || (param.isEmpty())) {
			return null;
		} else {
			return param.get(0);
		}
	}

	public static String getAccessToken(Authentication p, OAuth2AuthorizedClientRepository clientRepository) {
		OAuth2AuthorizedClient client = clientRepository.loadAuthorizedClient("github", p, URIRewriter.getCurrentRequest());
		if (client == null) {
			// alternatively, use a token from the authorization headers.
			String authorization = URIRewriter.getCurrentRequest().getHeader(HttpHeaders.AUTHORIZATION);
			if (authorization != null) {
				if (authorization.toLowerCase().startsWith("token ")) {
					authorization = authorization.substring(6);
				}
				if (authorization.toLowerCase().startsWith("bearer ")) {
					authorization = authorization.substring(7);
				}
			}
			return authorization;
		} else {
			return client.getAccessToken().getTokenValue();
		}
	}

	public static String getEmail(Authentication authentication) {
		if (authentication instanceof OAuth2AuthenticationToken) {
			return authentication.getDetails().toString();
		} else {
			throw new UnsupportedOperationException("Couldn't get user email " + authentication);
		}
	}

	public static String getUserLogin(Authentication p) {
		try {
			OAuth2User user = (OAuth2User) p.getPrincipal();
			String login = user.getAttribute("login");
			return login;
		} catch (Exception e) {
			throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Could not determine login");
		}
	}

	public abstract GitHub getGitHubAPI(Authentication a);

	public String getRef() {
		return ref;
	}
	

	protected void initContents(Authentication auth) throws Exception {
		testOrg();
		testFile(auth);
		testDirectory(auth);
		testMissing(auth);
	}

	private void testDirectory(Authentication auth) throws Exception {
		if ((contents == null) && (auth != null)) {
			GitHub api = getGitHubAPI(auth);
			try {
				contents = api.getRepository(owner+"/"+reponame).getDirectoryContent(filepath, ref);
			} catch (IOException e) {
				LOG.debug("Not a directory");
			}
		}
	}

	private void testMissing(Authentication auth) {
		if (contents == null) {
			if (auth == null) {
				throw new LoginRequiredException(Type.GITHUB, getKite9ResourceURI());
			} else {
				contents = NO_FILE;
			}
		}
	}

	private void testOrg() {
		if (contents == null) {
			// easy one first - no repo, so use org page.
			if (reponame == null) {
				contents = ORG_PAGE;
			}
		}
	}

	private void testFile(Authentication auth) throws Exception {
		if (contents == null) {
			try {
				// first, check to see if there is a raw entry
				String ref = this.ref == null ? HEAD_REF : this.ref;
				String url = RAW_GITHUB + owner + "/" + reponame + "/" + ref + "/" + filepath;
				
				WebClient webClient = WebClient.create(url);
						
				RequestHeadersSpec<?> spec = webClient.get()
					.header("Accept-Encoding", "identity")
					.header(HttpHeaders.ACCEPT, Kite9MediaTypes.ALL_VALUE);
				
				if (auth instanceof OAuth2AuthenticationToken) {
					String token = getAccessToken(auth, clientRepository);
					spec = spec.header("Authorization", "token "+token);
				}
						
				ResponseSpec retrieve = spec.retrieve();
				Mono<ByteArrayResource> mono = retrieve.bodyToMono(ByteArrayResource.class);
				ByteArrayResource db = mono.block();
				
				if (db != null) {
					contents =  db.getByteArray();
				}
			} catch (NotFound e) {
				if (auth == null) {
					return;
				} else {
					throw e;
				}
			} catch (Exception e) {
				LOG.debug("Not file: "+e.getMessage());
			}
		}
	}
	
	@Override
	public SourceType getSourceType(Authentication auth) throws Exception {
		initContents(auth);
		if ((contents instanceof byte[]) || (contents == NO_FILE)) {
			return SourceType.FILE;
		} else {
			return SourceType.DIRECTORY;
		}
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		initContents(authentication);
		if (contents instanceof byte[]) {
			return new ByteArrayInputStream((byte[]) contents);
		} else {
			throw new Kite9ProcessingException("not a file: "+getKite9ResourceURI());
		}
	}

	@Override
	public K9URI getKite9ResourceURI() {
		return resourceUri;
	}
	
	
}
