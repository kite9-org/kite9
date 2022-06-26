package com.kite9.server.persistence.github;

import static com.kite9.server.persistence.PathUtils.FILEPATH;
import static com.kite9.server.persistence.PathUtils.OWNER;
import static com.kite9.server.persistence.PathUtils.REPONAME;
import static com.kite9.server.persistence.PathUtils.getPathSegment;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.persistence.PathUtils;
import com.kite9.server.persistence.RelativeHostLinkBuilder;
import com.kite9.server.persistence.cache.CacheManagedAPIFactory;
import com.kite9.server.security.LoginRequiredException;
import com.kite9.server.security.LoginRequiredException.Type;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.topic.ChangeBroadcaster;
import com.kite9.server.update.Update;

public final class GithubSourceAPIFactory extends CacheManagedAPIFactory implements InitializingBean {

	protected OAuth2AuthorizedClientRepository clientRepository;
	
	private FormatSupplier formatSupplier;
	
	private AbstractGithubEntityConverter ec;
	
	public GithubSourceAPIFactory(
			ApplicationContext ctx,
			ADLFactory factory,
			OAuth2AuthorizedClientRepository clientRepository, 
			FormatSupplier formatSupplier) {
		super(ctx, factory);
		this.clientRepository = clientRepository;
		this.formatSupplier = formatSupplier;
	}
	
	protected GHContent getFileContent(Authentication auth, String owner, String reponame, String filepath, String ref) throws IOException {
		GitHub api = createGitHub(auth);
		return api.getRepository(owner+"/"+reponame)
				.getFileContent(filepath, ref);
	}
	
	protected List<GHContent> getDirectoryContent(Authentication auth, String owner, String reponame, String filepath, String ref) throws IOException {
		GitHub api = createGitHub(auth);
		return api.getRepository(owner+"/"+reponame).getDirectoryContent(filepath, ref);
	}

	public SourceAPI createBackingAPI(K9URI u, Authentication authentication) throws Exception {
		String path = u.getPath();
		String ref = AbstractGithubFileAPI.getRef(u);
		if (GithubContentController.GITHUB.equals(PathUtils.getPathSegment(PathUtils.TYPE, path))) {
			String owner = getPathSegment(OWNER, path);
			String reponame = getPathSegment(REPONAME, path);
			String filepath = getPathSegment(FILEPATH, path);
			
			if (owner == null) {
				return new GithubDirectoryAPI(path, ref, ec.getHomePage(authentication));
			} else if (reponame == null) {
				return new GithubDirectoryAPI(path, ref, ec.getOrgPage(owner, authentication));
			} else {
				// try as content.
				Format f = formatSupplier.getFormatFor(path);
				if (f != null) {
					try {
						GHContent o = getFileContent(authentication, owner, reponame, filepath, ref);
						
						if (f instanceof DiagramFileFormat) {
							return createDiagramApi(u, f, false, o);
						} else {
							return createRegularFileApI(u, f, false, o);
						}
					} catch (IOException e) {
						return ensureLogin(u, authentication, e);
					} catch (WebClientResponseException e) {
						if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
							return createDiagramApi(u,f, true, null);
						} else {
							throw new ResponseStatusException(e.getStatusCode(), e.getMessage());
						}
					} 
				} else {
					try {
						// ok, try as directory
						List<GHContent> go = getDirectoryContent(authentication, owner, reponame, filepath, ref);
						return new GithubDirectoryAPI(path, ref, ec.getDirectoryPage(owner, reponame, filepath, go,  authentication));
					} catch (IOException e) {
						return ensureLogin(u, authentication, e);
					}
				}
				
			}
		} else {
			return null;
		}	
	}

	/**
	 * If the user gets a 404, it could be due to lack of authentication, 
	 * so redirect to the auth page.
	 */
	private SourceAPI ensureLogin(K9URI u, Authentication authentication, IOException e)
			throws LoginRequiredException, IOException {
		if (authentication == null) {
			throw new LoginRequiredException(Type.GITHUB, u);
		} else {
			throw e;
		}
	}

	protected SourceAPI createRegularFileApI(K9URI u, Format f2, boolean isNew, GHContent o) throws URISyntaxException {
		K9MediaType mainMediaType = f2.getMediaTypes().get(0);
		return new AbstractGithubModifiableFileAPI(u, o, clientRepository, mainMediaType, isNew) {
			
			@Override
			public GitHub getGitHubAPI(Authentication a) {
				return createGitHub(getAccessToken(a, clientRepository));
			}
			
			@Override
			public String getUserId(Authentication a) {
				return AbstractGithubFileAPI.getEmail(a);
			}
		};
	}

	protected SourceAPI createDiagramApi(K9URI u, Format f2, boolean isNew, GHContent o) throws URISyntaxException {
		K9MediaType mainMediaType = f2.getMediaTypes().get(0);
		return new GithubDiagramFileAPI(u, o, clientRepository, (DiagramFileFormat) f2, mainMediaType, isNew) {
			
			@Override
			public GitHub getGitHubAPI(Authentication a) {
				return createGitHub(getAccessToken(a, clientRepository));
			}

			@Override
			public String getUserId(Authentication a) {
				return getEmail(a);
			}

			@Override
			public void addMeta(MetaReadWrite adl) {
				super.addMeta(adl);
				List<UserMeta> subscribers = getSubscribers(adl.getTopicUri());
				adl.setCollaborators(subscribers);
			}
			
		};
	}

	public GitHub createGitHub(String token) {
		try {
			GitHubBuilder gb = new GitHubBuilder();
			if (token != null) {
				return gb.withOAuthToken(token).build();
			} else {
				return gb.build();
			}
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't get handle to github", e);
		}
	}
	
	public GitHub createGitHub(Authentication p) {
		String token = AbstractGithubFileAPI.getAccessToken(p, clientRepository);
		return createGitHub(token);
	}
	
	@Override
	public SourceAPI createAPI(Update u, Authentication a) throws Exception {
		return createAPI(u.getUri(), a);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ec = new AbstractGithubEntityConverter(formatSupplier) {

			@Override
			public LinkBuilder linkToRemappedURI() {
				return RelativeHostLinkBuilder.linkToCurrentMapping();
			}

			@Override
			protected GitHub getGithubApi(Authentication authentication) throws Exception {
				return GithubSourceAPIFactory.this.createGitHub(authentication);
			}
			
			
		};
	}

	protected List<UserMeta> getSubscribers(K9URI topicUri) {
		Set<UserMeta> out = new HashSet<UserMeta>();
		ctx.getBeansOfType(ChangeBroadcaster.class).values()
			.forEach(b -> out.addAll(b.getCurrentSubscribers(topicUri)));
		return new ArrayList<UserMeta>(out);
	}

}