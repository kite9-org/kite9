package com.kite9.server.persistence.github;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.function.Consumer;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHPermissionType;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTree;
import org.kohsuke.github.GHTreeBuilder;
import org.kohsuke.github.GitHub;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.server.pipeline.adl.holder.meta.Role;
import com.kite9.server.sources.ModifiableAPI;

public abstract class AbstractGithubModifiableFileAPI extends AbstractGithubFileAPI implements ModifiableAPI {
	
	private boolean isNew;
	protected final URI sourceURI;
	
	public AbstractGithubModifiableFileAPI(URI u, OAuth2AuthorizedClientRepository clientRepository, MediaType mt, boolean isNew) throws URISyntaxException {
		super(u.getPath(), clientRepository, mt);
		this.isNew = isNew;
		this.sourceURI = unmodifiedURI(u);
	}

	/**
	 * This simplifies the URI to remove any query parameters
	 * @throws URISyntaxException 
	 */
	private URI unmodifiedURI(URI u) throws URISyntaxException {
		URI out = new URI(u.getScheme(), u.getAuthority(), u.getPath(), null, null);
		return out;
	}

	protected void commitRevision(String message, Consumer<GHTreeBuilder> fn, Authentication by) {
		try {
			GHRepository repo = getGitHubAPI(by).getRepository(owner+"/"+reponame);
			String treeSha = repo.getTree(branchName).getSha();
			String branchSha = repo.getBranch(branchName).getSHA1();

			GHTreeBuilder treeBuilder = repo.createTree().baseTree(treeSha);
			fn.accept(treeBuilder);
			GHTree newTree = treeBuilder.create();
			
			GHCommit c = repo.createCommit()
					.committer(AbstractGithubModifiableFileAPI.getUserLogin(by), AbstractGithubModifiableFileAPI.getEmail(by), new Date())
					.message(message)
					.parent(branchSha)
					.tree(newTree.getSha())
					.create();
			
			repo.getRef("heads/"+branchName).updateTo(c.getSHA1());	
			isNew = false;
		} catch (IOException e) {
			throw new Kite9XMLProcessingException("Couldn't commit change to: "+path, e);
		}
	}

	@Override
	public void commitRevisionAsBytes(String message, Authentication by, byte[] bytes) {
		commitRevision(message, tb -> tb.add(filepath, bytes, false), by);
	}
	
	@Override
	public Type getType(Authentication a) {
		return getAuthenticatedRole(a) == Role.EDITOR ? (isNew ? Type.CREATABLE : Type.MODIFIABLE) : Type.VIEWONLY;
	}

	@Override
	public Role getAuthenticatedRole(Authentication a) {
		if (a != null) {
			try {
				GitHub api = getGitHubAPI(a);
				GHRepository repo = api.getRepository(owner+ "/"+reponame);
				GHPermissionType pt = repo.getPermission(api.getMyself());
				return translateRole(pt);
			} catch (Throwable e) {
				logger.error("Couldn't determine user's role", e);
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

	@Override
	public URI getSourceLocation() {
		return sourceURI;
	}

	public static String createTitle(URI u) {
		String fileNamePart = u.getPath().contains("/") ? 
			u.getPath().substring(u.getPath().lastIndexOf("/")+1) :
			u.getPath();
	
		String withoutExtension = fileNamePart.contains(".") ? 
			fileNamePart.substring(0, fileNamePart.indexOf(".")) :
			fileNamePart;
				
		
		return withoutExtension;
	}

	
}
