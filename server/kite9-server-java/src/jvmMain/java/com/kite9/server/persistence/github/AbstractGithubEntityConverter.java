package com.kite9.server.persistence.github;

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
import com.kite9.server.domain.Directory;
import com.kite9.server.domain.Document;
import com.kite9.server.domain.Organisation;
import com.kite9.server.domain.Repository;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.domain.User;

public abstract class AbstractGithubEntityConverter {
	
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
				return "/github/kite9-org/kite9/templates/admin/icons/github.png?v=v0.15";
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
				return "/github/kite9-org/kite9/templates/admin/icons/github.png?v=v0.15";
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

	public Directory templateDirectory(Link l, String name, List<RestEntity> parents, List<Document> contents,
									   List<Directory> subdirectories) {
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
			public List<Document> getDocuments() {
				return contents;
			}

			@Override
			public List<Directory> getSubDirectories() {
				return subdirectories;
			}

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}
		};

		out.add(l);

		return out;
	}

	public Repository templateRepo(Link self, GHRepository r, List<Document> documents, List<Directory> subdirs,
			List<RestEntity> parents) {
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
			public List<Document> getDocuments() {
				return documents;
			}

			@Override
			public List<Directory> getSubDirectories() {
				return subdirs;
			}

		};

		p.add(self);

		return p;
	}

	public List<Repository> templateChildRepos(LinkBuilder lb, GHPerson user) {
		PagedIterable<GHRepository> repos = user.listRepositories();

		@SuppressWarnings("deprecation")
		List<Repository> repoList = repos.asList().stream().map(r -> {
			return templateRepo(lb.slash(r.getName()).withSelfRel(), r, null, null, null);
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

	public Document templateDocument(Link l, GHContent c, List<RestEntity> parents) {
		Document out = new Document() {

			@Override
			public String getTitle() {
				return c.getName();
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
				return getIconUrl(l, c);
			}

			private String getIconUrl(Link l, GHContent c) {
				if (fs.getFormatFor(c.getName()) instanceof DiagramFileFormat) {
					return l.getHref();	
				} else if (hasIcon(c.getName())) {
					return "/github/kite9-org/kite9/templates/admin/icons/"+getExtension(c.getName())+".svg?v=v0.15";
				} else {
					return "/github/kite9-org/kite9/templates/admin/icons/unknown.svg?v=v0.15";
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

	public List<Document> templateChildDiagrams(LinkBuilder lb, List<GHContent> contents)
			throws IOException {
		return contents.stream().filter(c -> c.isFile())
				.filter(c -> fs.getFormatFor(c.getName()) != null).map(c -> {
					Document out = templateDocument(buildDiagramLink(lb, c), c, null);
					return out;
				}).collect(Collectors.toList());
	}

	protected Link buildDiagramLink(LinkBuilder lb, GHContent c) {
		return lb.slash(c.getName()).withSelfRel();
	}

	public List<Directory> templateChildDirectories(LinkBuilder lb, List<GHContent> contents) throws IOException {
		return contents.stream().filter(c -> c.isDirectory())
				.map(c -> templateDirectory(buildDiagramLink(lb, c), c.getName(), null, null, null))
				.collect(Collectors.toList());
		// .map(c -> EntityConverter.templateDirectory(, safeGetName(c), null, null,
		// null))
		// .collect(Collectors.toList());
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
		
	public Directory getDirectoryPage(
			String userorg,
			String reponame, 
			String path, 
			List<GHContent> contents, Authentication authentication) throws Exception {
		
		try {
			LinkBuilder lb = linkToRemappedURI().slash("github");
			LinkBuilder lbUserOrg = lb.slash(userorg);
			LinkBuilder lbRepo = lbUserOrg.slash(reponame);
			LinkBuilder lbRelative = lbRepo.slash(path);
			LinkBuilder contentRelative = linkToRemappedURI().slash(GithubContentController.GITHUB).slash(userorg).slash(reponame).slash(path);
			GitHub github = getGithubApi(authentication);
			GHRepository repo = github.getRepository(userorg+"/"+reponame);
			Repository repoParent = templateRepo(lbRepo.withSelfRel(), repo, null, null, null);
			Organisation owner = templateOrganisation(lbUserOrg.withSelfRel(), repo.getOwner(), null, null);
			List<Directory> childDirectories = templateChildDirectories(lbRelative, contents);
			List<Document> childDiagrams = templateChildDiagrams(contentRelative, contents);
			
			if (!StringUtils.hasText(path)) {
				List<RestEntity> parents = buildParents(authentication, owner, null, null, lbRepo);
				return templateRepo(lbRelative.withSelfRel(), repo, childDiagrams, childDirectories, parents);
			} else {
				String currentDir = path.substring(path.lastIndexOf("/")+1);
				List<RestEntity> parents = buildParents(authentication, owner, repoParent, path, lbRepo);
				return templateDirectory(lbRelative.withSelfRel(), 
						currentDir, parents, childDiagrams, childDirectories);
			}
		} catch (Throwable e) {
			throw new Kite9XMLProcessingException("Couldn't format directory page "+path, e);
		}
	}

	private List<RestEntity> buildParents(Authentication a, Organisation owner, Repository repo, String path, LinkBuilder lb) {
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
				out.add(templateDirectory(lb.withSelfRel(), curPart, null, null, null));
			}
		}
		return out;
	}

}
