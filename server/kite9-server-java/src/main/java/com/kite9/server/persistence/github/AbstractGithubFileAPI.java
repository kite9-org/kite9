package com.kite9.server.persistence.github;

import static com.kite9.server.persistence.PathUtils.FILEPATH;
import static com.kite9.server.persistence.PathUtils.OWNER;
import static com.kite9.server.persistence.PathUtils.REPONAME;
import static com.kite9.server.persistence.PathUtils.getPathSegment;

import java.io.InputStream;

import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.server.sources.FileAPI;
import com.kite9.server.web.URIRewriter;

import reactor.core.publisher.Mono;

public abstract class AbstractGithubFileAPI implements FileAPI {

	protected static Logger logger = LoggerFactory.getLogger(FileAPI.class);
	
	protected String path;
	protected String owner;
	protected String reponame;
	protected String filepath;
	protected String branchName;
	protected OAuth2AuthorizedClientRepository clientRepository;
	protected K9MediaType mediaType;
	
	public AbstractGithubFileAPI(String path, OAuth2AuthorizedClientRepository clientRepository, K9MediaType mt) {
		this.path = path;
		this.owner = getPathSegment(OWNER, path);
		this.reponame = getPathSegment(REPONAME, path);
		this.filepath = getPathSegment(FILEPATH, path);
		this.branchName = "master";
		this.clientRepository = clientRepository;
		this.mediaType = mt;
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


	public static <X> X getGHObject(String pathAndQuery, Class<X> cls, String oauthToken) {
		WebClient c = createWebClient();
		RequestHeadersSpec<?> m = c.get().uri(pathAndQuery);
		if (oauthToken != null) {
			m = m.header("Authorization", "token " + oauthToken);
		}
		
		Mono<X> mono = m.retrieve().bodyToMono(cls);
	
		X content = mono.block();
		return content;
	}

	protected GHContent getGHContent(String oauthToken) {
		String uri = formatGithubPath(owner, reponame, filepath);
		return getGHObject(uri, GHContent.class, oauthToken);
	}

	public static Object getGHContent(Authentication authentication, OAuth2AuthorizedClientRepository clientRepository, 
			String owner, String reponame, String filepath) {
		Object o;
		String githubPath = formatGithubPath(owner, reponame, filepath);
		o = AbstractGithubFileAPI.getGHObject(githubPath, Object.class, getAccessToken(authentication, clientRepository));
		return o;
	}

	public static String formatGithubPath(String owner, String reponame, String filepath) {
		return "/repos/" + owner+"/"+reponame+"/contents/" + filepath;
	}


	public abstract GitHub getGitHubAPI(Authentication a);

	public static WebClient createWebClient() {
		return WebClient.create("https://api.github.com");
	}

	@Override
	public K9MediaType getMediaType() {
		return mediaType;
	}

	@Override
	public InputStream getCurrentRevisionContentStream(Authentication authentication) throws Exception {
		GHContent content = getGHContent(getAccessToken(authentication, clientRepository));
		return content.read();
	}

	
}
