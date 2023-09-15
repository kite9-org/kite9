package com.kite9.server.persistence.github;

import java.io.IOException;
import java.util.Date;
import java.util.function.Consumer;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPermissionType;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.pipeline.adl.holder.meta.Role;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.persistence.github.config.ConfigLoader;
import com.kite9.server.sources.ModifiableAPI;

/**
 * Augments the source api with methods to allow updating.
 * 
 * @author rob@kite9.com
 *
 */
public abstract class AbstractGithubModifiableAPI extends AbstractGithubSourceAPI implements ModifiableAPI {
		
	public AbstractGithubModifiableAPI(K9URI u, OAuth2AuthorizedClientRepository clientRepository, ConfigLoader configLoader) throws Exception  {
		super(u, clientRepository, configLoader);
	}

	protected void commitRevision(String message, Consumer<GHTreeBuilder> fn, Authentication by) {
		try {
			String token = getAccessToken(by, clientRepository);
			GHRepository repo = getRepo(token);
			String treeSha = repo.getTree(branchName).getSha();
			String branchSha = repo.getBranch(branchName).getSHA1();

			GHTreeBuilder treeBuilder = repo.createTree().baseTree(treeSha);
			fn.accept(treeBuilder);
			GHTree newTree = treeBuilder.create();
			
			GHCommit c = repo.createCommit()
					.committer(AbstractGithubModifiableAPI.getUserLogin(by), AbstractGithubModifiableAPI.getEmail(by), new Date())
					.message(message)
					.parent(branchSha)
					.tree(newTree.getSha())
					.create();
			
			repo.getRef("heads/"+branchName).updateTo(c.getSHA1());	
			
			clearContents();
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't commit change to: "+this.githubPath.getFilepath(), e);
		}
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
		commitRevision(message, tb -> tb.add(this.githubPath.getFilepath(), bytes, false), by);
	}
	
	
	
	@Override
	public ModificationType getModificationType(Authentication a) throws Exception {
		Object contents = initContents(a);
		return getAuthenticatedRole(a) == Role.EDITOR ? (contents == StaticPages.NO_FILE ? ModificationType.CREATABLE : ModificationType.MODIFIABLE) : ModificationType.VIEWONLY;
	}

	@Override
	public Role getAuthenticatedRole(Authentication a) {
		if (a != null) {
			try {
				String token = getAccessToken(a, clientRepository);
				GitHub api = getGitHubAPI(token);
				GHRepository repo = getRepo(token);
				GHPermissionType pt = repo.getPermission(api.getMyself());
				return translateRole(pt);
			} catch (Throwable e) {
				LOG.error("Couldn't determine user's role", e);
			}
		}

		return Role.VIEWER;
	}

	protected Role translateRole(GHPermissionType pt) {
		switch(pt) {
		case ADMIN:
		case WRITE:
			return Role.EDITOR;
		case READ:
			return Role.VIEWER;
		case NONE:
		default:
			return Role.NONE;
		}
	}

	public static String createTitle(K9URI u) {
		String fileNamePart = u.getPath().contains("/") ? 
			u.getPath().substring(u.getPath().lastIndexOf("/")+1) :
			u.getPath();
	
		String withoutExtension = fileNamePart.contains(".") ? 
			fileNamePart.substring(0, fileNamePart.indexOf(".")) :
			fileNamePart;
				
		
		return withoutExtension;
	}

}
