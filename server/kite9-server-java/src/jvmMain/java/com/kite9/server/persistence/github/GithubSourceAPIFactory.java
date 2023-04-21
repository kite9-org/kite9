package com.kite9.server.persistence.github;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.logging.Kite9ProcessingException;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.persistence.RelativeHostLinkBuilder;
import com.kite9.server.persistence.cache.CacheManagedAPIFactory;
import com.kite9.server.persistence.github.config.ConfigLoader;
import com.kite9.server.persistence.github.conversion.AbstractGithubEntityConverter;
import com.kite9.server.persistence.github.conversion.DirectoryDetails;
import com.kite9.server.persistence.github.conversion.GithubEntityConverter;
import com.kite9.server.persistence.github.urls.Kite9GithubPath;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.topic.ChangeBroadcaster;
import com.kite9.server.update.Update;

public final class GithubSourceAPIFactory extends CacheManagedAPIFactory implements InitializingBean {

	protected OAuth2AuthorizedClientRepository clientRepository;
	
	protected ConfigLoader configLoader;
	
	private FormatSupplier formatSupplier;
	
	private GithubEntityConverter ec;
	
	private String githubApiKey;
	
	public GithubSourceAPIFactory(
			ApplicationContext ctx,
			ADLFactory factory,
			OAuth2AuthorizedClientRepository clientRepository, 
			ConfigLoader configLoader,
			FormatSupplier formatSupplier, String githubApiKey) {
		super(ctx, factory);
		this.clientRepository = clientRepository;
		this.configLoader = configLoader;
		this.formatSupplier = formatSupplier;
		this.githubApiKey = githubApiKey;
	}
	
	public SourceAPI createBackingAPI(K9URI u, Authentication authentication) throws Exception {
		String path = u.getPath();
		if (Kite9GithubPath.isGithubPath(path)) {
			Format f = formatSupplier.getFormatFor(path);
			if (f instanceof DiagramFileFormat) {
				return createDiagramApi(u, f);
			} else {
				return createNonDiagramApi(u, f);
			}
		} else {
			return null;
		}	
	}

	protected SourceAPI createNonDiagramApi(K9URI u, Format f) throws Exception {
		
		return new AbstractGithubModifiableAPI(u, clientRepository, configLoader) {		
			
			@Override
			public GitHub getGitHubAPI(String token) {
				return createGitHub(token);
			}
			
			@Override
			public String getUserId(Authentication a) {
				return getEmail(a);
			}

			@SuppressWarnings("unchecked")
			@Override
			public RestEntity getEntityRepresentation(Authentication a) throws Exception {
				if (contents == StaticPages.HOME_PAGE) {
					return ec.getHomePage(a);
				} else if (contents == StaticPages.ORG_PAGE) {
					return ec.getOrgPage(this.githubPath.getOwner(), a);
				} else if (contents instanceof DirectoryDetails) {		
					return ec.getDirectoryPage((DirectoryDetails) contents, a);
				} else {
					throw new Kite9ProcessingException("can't currently produce an entity representation here");
				}
			}

			@Override
			public K9MediaType getMediaType() {
				return formatSupplier.getMediaTypeFor(u.getPath());
			}
		};
	}

	protected SourceAPI createDiagramApi(K9URI u, Format f2) throws Exception {
		K9MediaType mainMediaType = f2.getMediaTypes().get(0);
		return new AbstractGithubModifiableDiagramAPI(u, clientRepository, configLoader, (DiagramFileFormat) f2, mainMediaType) {
			
			@Override
			public GitHub getGitHubAPI(String token) {
				return createGitHub(token);
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

			@Override
			public RestEntity getEntityRepresentation(Authentication a) throws Exception {
				throw new UnsupportedOperationException();
			}
			
		};
	}

	public GitHub createGitHub(String token) {
		try {
			GitHubBuilder gb = new GitHubBuilder();
			if (token != null) {
				return gb.withOAuthToken(token).build();
			} else {
				return gb.withOAuthToken(githubApiKey).build();
			}
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't get handle to github", e);
		}
	}
	
	public GitHub createGitHub(Authentication p) {
		String token = AbstractGithubSourceAPI.getAccessToken(p, clientRepository);
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