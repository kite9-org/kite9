package com.kite9.server.persistence.github;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GHUser;
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
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.persistence.github.config.Config;
import com.kite9.server.persistence.github.config.ConfigLoader;
import com.kite9.server.persistence.github.conversion.DirectoryDetails;
import com.kite9.server.persistence.github.conversion.HomeDetails;
import com.kite9.server.persistence.github.conversion.OrgDetails;
import com.kite9.server.persistence.github.urls.Kite9GithubPath;
import com.kite9.server.persistence.github.urls.RawGithub;
import com.kite9.server.security.LoginRequiredException;
import com.kite9.server.security.LoginRequiredException.Type;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.web.URIRewriter;

/**
 * Represents files or directories loaded from Github.
 * 
 * @author rob@kite9.com
 *
 */
public abstract class AbstractGithubSourceAPI implements SourceAPI {
	
	enum StaticPages {
		NO_FILE
	}
	
	protected static Logger LOG = LoggerFactory.getLogger(SourceAPI.class);
	
	protected final K9URI u;
	protected final Kite9GithubPath githubPath;
	protected final OAuth2AuthorizedClientRepository clientRepository;
	protected final ConfigLoader configLoader;
	
	// caches, set once in this class only by initContents()
	// and then cleared after we commitRevision
	private Object contents;
	protected Config config;
	protected String branchName;

	public AbstractGithubSourceAPI(K9URI u, OAuth2AuthorizedClientRepository clientRepository, ConfigLoader configLoader) throws Exception {
		this.githubPath = Kite9GithubPath.create(u.getPath(), getProvidedVersionParameter(u));
		this.clientRepository = clientRepository;
		this.configLoader = configLoader;
		this.u = u;
	}
		
	public static String getProvidedVersionParameter(K9URI u) {
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

	public abstract GitHub getGitHubAPI(String token);
	
	protected GHRepository getRepo(String token) throws IOException {
		return getGitHubAPI(token).getRepository(githubPath.getOwner()+"/"+githubPath.getReponame());
	}


	protected Object initContents(Authentication auth) throws Exception {
		String token = getAccessToken(auth, clientRepository);
		testHomePage(token, auth);
		testOrg(token);
		RepoSupplier repo = lazyRepo(token);
		ensureConfigAndDefaultBranch(auth, repo);
		testFile(token);
		testDirectory(token, repo);
		testMissing(token);
		return contents;
	}
	
	protected void clearContents() {
		this.contents = null;
	}
	
	interface RepoSupplier {
		
		public GHRepository get() throws Exception;
	}
	
	/**
	 * Some operations don't require this, so only load it if they do
	 */
	private RepoSupplier lazyRepo(String token) {
		return new RepoSupplier() {

			GHRepository out = null;

			@Override
			public GHRepository get() throws Exception {
				if (out == null) {
					GitHub api = getGitHubAPI(token);
					out = api.getRepository(githubPath.getOwner()+"/"+githubPath.getReponame());
				}
				return out;
			}
		};
	}

	private void ensureConfigAndDefaultBranch(Authentication a, RepoSupplier repo) throws Exception {
		if ((contents == null) && (a != null)) {
			if (branchName == null) {
				if (githubPath.getRef() != null) {
					branchName = githubPath.getRef();
				} else {
					branchName = repo.get().getDefaultBranch();
				}
			} 
			
			if ((config == null) && (!githubPath.getFilepath().equals(ConfigLoader.KITE9_CONFIG_FILE))) {
				this.config = configLoader.loadConfig(a, githubPath.getOwner(), githubPath.getReponame(), branchName);
			}
		}
	}

	private void testDirectory(String token, RepoSupplier repo) throws Exception {		
		if ((contents == null) && (token != null)) {
			GHTree tree = repo.get().getTreeRecursive(branchName,1);
			if (!checkDirectoryExists(tree)) {
				LOG.debug("Not a directory");
				return;
			}
			List<GHTreeEntry> treeEntries = getFilteredTreeList(tree, config, githubPath.getFilepath());
			contents = new DirectoryDetails(repo.get(), treeEntries, this.githubPath, config);
		}
	}

	private boolean checkDirectoryExists(GHTree tree) {
		if (!StringUtils.hasText(this.githubPath.getFilepath())) {
			return true;
		}
		
		GHTreeEntry entry = tree.getEntry(this.githubPath.getFilepath());
		return (entry != null) && ("tree".equals(entry.getType()));
	}

	private List<GHTreeEntry> getFilteredTreeList(GHTree tree, Config config, String filePath) throws IOException {
		return tree.getTree().stream()
			.filter(config)
			.filter(te -> te.getPath().startsWith(filePath) && (!filePath.equals(te.getPath())))
			.collect(Collectors.toList());
	}

	private void testMissing(String token) {
		if (contents == null) {
			if (token == null) {
				throw new LoginRequiredException(Type.GITHUB, u);
			} else {
				contents = StaticPages.NO_FILE;
			}
		}
	}
	
	private void testHomePage(String token, Authentication authentication) {
		if (contents == null) {
			if ((githubPath.getOwner() == null) && (token != null)) {
				try {
					GitHub github = getGitHubAPI(token);
					String name = getUserLogin(authentication);
					GHUser user = github.getUser(name); 
					List<GHRepository> repos = user.getRepositories().values().stream().collect(Collectors.toList());
					List<GHOrganization> orgs = user.getOrganizations().stream()
						.filter(o -> o instanceof GHOrganization)
						.map(o -> (GHOrganization) o)
						.collect(Collectors.toList());
					contents = new HomeDetails(this.githubPath, repos, orgs, user);
				} catch (IOException e) {
					LOG.debug("No token home page returned {} for {}", e.getMessage(), githubPath.toString());
				}
			}
		}
	}

	private void testOrg(String token) {
		if (contents == null) {
			// easy one first - no repo, so use org page.
			if ((githubPath.getReponame() == null)  && (token != null)) {
				// only applies for logged-in users
				try {
					GitHub github = getGitHubAPI(token);
					String org = githubPath.getOwner();
					GHPerson userOrg = github.getUser(org);
					List<GHRepository> repos = userOrg.getRepositories().values().stream().collect(Collectors.toList());
					contents = new OrgDetails(githubPath, repos, userOrg);
				} catch (IOException e) {
					LOG.debug("No token org page returned {} for {}", e.getMessage(), githubPath.toString());
				}
			}
		}
	}

	private void testFile(String token) throws Exception {
		if (contents == null) {
			try {
				String url = RawGithub.assembleGithubURL(githubPath, branchName);
				ByteArrayResource db = RawGithub.loadBytesFromGithub(token, url);
				
				if (db != null) {
					contents =  db.getByteArray();
				}
			} catch (NotFound e) {
				if (token != null) {
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
		if ((contents instanceof byte[]) || (contents == StaticPages.NO_FILE)) {
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
		} else if (contents == StaticPages.NO_FILE) {
			throw new FileNotFoundException();
		} else {
			throw new Kite9ProcessingException("not a file: "+getUnderlyingResourceURI(authentication));
		}
	}

	private K9URI resourceUri;
	
	@Override
	public K9URI getUnderlyingResourceURI(Authentication a) throws Exception {
		if (resourceUri == null) {
			resourceUri = u.filterQueryParameters(k -> "v".equals(k));
			if (resourceUri.param("v").isEmpty()) {
	 			resourceUri = resourceUri.withQueryParameter("v", Collections.singletonList(branchName));
			}
		}
		return resourceUri;
	}

	@Override
	public String toString() {
		return this.getClass().getName()+" [u=" + u + "]";
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
		if (config != null) {
			K9URI uploads = u.resolve(config.getUploadsPath());
			adl.setUploadsPath(uploads.toString());
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
}
