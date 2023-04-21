package com.kite9.server.persistence.github.conversion;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kohsuke.github.GHContent;
import org.kohsuke.github.GHOrganization;
import org.kohsuke.github.GHPerson;
import org.kohsuke.github.GHPersonSet;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;
import org.kohsuke.github.GHUser;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.domain.Content;
import com.kite9.server.domain.Directory;
import com.kite9.server.domain.Document;
import com.kite9.server.domain.Organisation;
import com.kite9.server.domain.Ref;
import com.kite9.server.domain.Repository;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.domain.User;
import com.kite9.server.persistence.github.AbstractGithubModifiableDiagramAPI;
import com.kite9.server.persistence.github.GithubSourceAPIFactory;

public abstract class AbstractGithubEntityConverter implements GithubEntityConverter {
	
	FormatSupplier fs;
	GithubSourceAPIFactory apiFactory;
	
	public AbstractGithubEntityConverter(FormatSupplier fs) {
		this.fs = fs;
	}

	public User templateHome(Link self, GHPerson user, List<Repository> repoList, List<Organisation> orgList,
							 RestEntity parent) {
		User out = new User() {

			@Override
			public String getTitle() {
				return "GitHub";
			}

			@Override
			public String getIcon() {
				return "/public/templates/admin/icons/github.png";
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public List<Repository> getRepositories() {
				return repoList;
			}

			@Override
			public List<Organisation> getOrganisations() {
				return orgList;
			}

			@Override
			public List<RestEntity> getParents() {
				return null;
			}
		};

		out.add(self);
		return out;
	}

	public RestEntity templateHome(Link self) {
		User out = new User() {

			@Override
			public String getTitle() {
				return "GitHub";
			}

			@Override
			public String getIcon() {
				return "/public/templates/admin/icons/github.png";
			}

			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public List<Repository> getRepositories() {
				return null;
			}

			@Override
			public List<Organisation> getOrganisations() {
				return null;
			}

			@Override
			public List<RestEntity> getParents() {
				return null;
			}
		};

		out.add(self);
		return out;
	}

	public String safeGetName(GHPerson o) {
		String n;
		try {
			n = o.getName();
		} catch (IOException e) {
			throw new UnsupportedOperationException("eh?");
		}
		return n;
	}

	public Organisation templateOrganisation(Link link, GHPerson o, List<Repository> repos, List<RestEntity> parents) {
		Organisation out = new Organisation() {

			@Override
			public String getTitle() {
				return o.getLogin();
			}

			@Override
			public String getDescription() {
				return safeGetName(o);
			}

			@Override
			public String getIcon() {
				return o.getAvatarUrl();
			}

			@Override
			public List<Repository> getRepositories() {
				return repos;
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}

		};

		out.add(link);
		return out;
	}

	public Directory templateDirectory(Link l, String name, List<RestEntity> parents, List<Content> contents) {
		Directory out = new Directory() {

			@Override
			public String getTitle() {
				return name;
			}

			@Override
			public String getDescription() {
				return "";
			}

			@Override
			public List<Content> getContents() {
				return contents;
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}
		};

		out.add(l);

		return out;
	}

	public Repository templateRepo(Link self, GHRepository r, List<Content> contents, List<RestEntity> parents) {
		Repository p = new Repository() {

			@Override
			public String getTitle() {
				return r.getName();
			}

			@Override
			public String getDescription() {
				return r.getDescription();
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}

			@Override
			public List<Content> getContents() {
				return contents;
			}

		};

		p.add(self);

		return p;
	}
	
	public Ref templateRef(Link self, String name, List<Content> contents, List<RestEntity> parents) {
		Ref p = new Ref() {

			@Override
			public String getTitle() {
				return name;
			}

			@Override
			public String getDescription() {
				return "";
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}

			@Override
			public List<Content> getContents() {
				return contents;
			}

		};

		p.add(self);

		return p;
	}

	
	public List<Repository> templateChildRepos(LinkBuilder lb, GHPerson user) {
		PagedIterable<GHRepository> repos = user.listRepositories();

		@SuppressWarnings("deprecation")
		List<Repository> repoList = repos.asList().stream().map(r -> {
			return templateRepo(lb.slash(r.getName()).withSelfRel(), r, null, null);
		}).collect(Collectors.toList());
		return repoList;
	}

	public List<Organisation> templateChildOrganisations(LinkBuilder lb, GHUser user) throws IOException {
		GHPersonSet<GHOrganization> orgs = user.getOrganizations();

		List<Organisation> orgList = orgs.stream().map(o -> {

			Organisation out = templateOrganisation(lb.slash(o.getLogin()).withSelfRel(), o, null, null);

			return out;
		}).collect(Collectors.toList());
		return orgList;
	}

	public Document templateDocument(Link l, String name, List<RestEntity> parents) {
		Document out = new Document() {

			@Override
			public String getTitle() {
				return name;
			}

			@Override
			public String getDescription() {
				return "";
			}
			

			private String getExtension(String name) {
				return name.substring(name.lastIndexOf(".")+1);
			}
			
			@Override
			public String getIcon() {
				if (hasIcon(name)) {
					return "/public/templates/admin/icons/"+getExtension(name)+".svg";
				} else {
					return "/public/templates/admin/icons/unknown.svg";
				}
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}

		};
		out.add(l);
		return out;
	}

	public List<Content> templateContents(LinkBuilder lb, List<GHTreeEntry> contents)
			throws IOException {
		return contents.stream()
				.map(c -> {
					if (isTree(c)) {
						return templateDirectory(buildDiagramLink(lb, c), c.getPath(), null, null);	
					} else if (fs.getFormatFor(c.getPath()) != null) {
						Document out = templateDocument(buildDiagramLink(lb, c), c.getPath(), null);
						return out;
					} else {
						return null;
					}
				})
				.filter(c -> c != null)
				.collect(Collectors.toList());
	}
	
	private boolean isTree(GHTreeEntry c) {
		return "tree".equals(c.getType());
	}

	protected Link buildDiagramLink(LinkBuilder lb, GHTreeEntry c) {
		return lb.slash(c.getPath()).withSelfRel();
	}

	public String sanitize(GHContent c) {
		try {
			String url = c.getDownloadUrl();
			if ((url.endsWith(".svg")) || (url.endsWith(".adl"))) {
				return "/api/renderer.png?uri=" + URLEncoder.encode(url, "UTF-8");
			} else {
				return url;
			}

		} catch (IOException e) {
			return "";
		}
	}

	protected abstract GitHub getGithubApi(Authentication authentication) throws Exception;

	public static GHPerson getUserOrOrg(String userorg, GitHub github) throws IOException {
		GHPerson p = github.getUser(userorg);
		return p;
	}
	
	
	@Override
	public User getHomePage(Authentication authentication) throws Exception {
		GitHub github = getGithubApi(authentication);
		String name = AbstractGithubModifiableDiagramAPI.getUserLogin(authentication);
		LinkBuilder lb = linkToRemappedURI();
		GHUser user = github.getUser(name);
		List<Repository> repoList = templateChildRepos(lb.slash("github").slash(name), user);
		List<Organisation> orgList = templateChildOrganisations(lb.slash("github"), user);
		User out = templateHome(lb.slash("github").withSelfRel(), user, repoList, orgList, null);
		return out;
	}
	
	protected abstract LinkBuilder linkToRemappedURI();

	public RestEntity createTopLevelParent() {
		LinkBuilder lb = linkToRemappedURI();
		return templateHome(lb.slash("github").withSelfRel());
	}

	@Override
	public Organisation getOrgPage(
			@PathVariable(name = "userorg") String userOrg, 
			Authentication authentication) throws Exception {
		GitHub github = getGithubApi(authentication);
		LinkBuilder lb = linkToRemappedURI();
		GHPerson org = getUserOrOrg(userOrg, github);
		RestEntity gh = createTopLevelParent();
		List<Repository> repoList = templateChildRepos(lb.slash("github").slash(userOrg), org);
		return templateOrganisation(lb.slash("github").slash(userOrg).withSelfRel(), org, repoList, Collections.singletonList(gh)); 
	}
		
	@Override
	public RestEntity getDirectoryPage(
			DirectoryDetails dd, Authentication authentication) throws Exception {
		
		try {
			LinkBuilder lb = linkToRemappedURI().slash("github");
			LinkBuilder lbUserOrg = lb.slash(dd.getPath().getOwner());
			LinkBuilder lbRepo = lbUserOrg.slash(dd.getPath().getReponame());
			LinkBuilder lbRef = lbRepo.slash(dd.getPath().getRef());
			LinkBuilder lbRelative = lbRef.slash(dd.getPath().getFilepath());
			Repository repo = templateRepo(lbRepo.withSelfRel(), dd.getRepo(), null, null);
			Organisation owner = templateOrganisation(lbUserOrg.withSelfRel(), dd.getRepo().getOwner(), null, null);
			Ref ref = templateRef(lbRef.withSelfRel(), dd.getPath().getRef(), null, null);
			
			List<Content> childContent = templateContents(lbRelative, dd.getEntries());
			
			if (StringUtils.hasText(dd.getPath().getFilepath())) {
				// directory in repo
				String path = dd.getPath().getFilepath();
				String currentDir = path.substring(path.lastIndexOf("/")+1);
				List<RestEntity> parents = buildParents(authentication, owner, repo, ref, dd.getPath().getFilepath(), lbRef);
				return templateDirectory(lbRelative.withSelfRel(), currentDir, parents, childContent);
			} else if (StringUtils.hasText(dd.getPath().getRef())) {
				// top of branch
				List<RestEntity> parents = buildParents(authentication, owner, repo, ref, null, lbRef);
				return templateRef(lbRelative.withSelfRel(), dd.getPath().getRef(), childContent, parents);
			} else {
				// repo (no ref?)
				List<RestEntity> parents = buildParents(authentication, owner, null, null, null, lbRepo);
				return templateRepo(lbRelative.withSelfRel(), dd.getRepo(), childContent, parents);
			} 
		} catch (Throwable e) {
			throw new Kite9XMLProcessingException("Couldn't format directory page "+dd.getPath(), e);
		}
	}

	private List<RestEntity> buildParents(Authentication a, Organisation owner, Repository repo, Ref ref, String path, LinkBuilder lb) {
		List<RestEntity> out = new ArrayList<RestEntity>();
		RestEntity github = createTopLevelParent();
		out.add(github);
		out.add(owner);
		if (repo != null) {
			out.add(repo);
		}
		if (path != null) {
			String[] pathParts = path.split("/");
			for (int i = 0; i < pathParts.length-1; i++) {
				String curPart = pathParts[i];
				lb = lb.slash(curPart);
				out.add(templateDirectory(lb.withSelfRel(), curPart, null, null));
			}
		}
		return out;
	}

}
