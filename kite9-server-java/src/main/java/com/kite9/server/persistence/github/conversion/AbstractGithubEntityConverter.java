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
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GHTreeEntry;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.util.StringUtils;

import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.server.domain.Content;
import com.kite9.server.domain.Directory;
import com.kite9.server.domain.Document;
import com.kite9.server.domain.Organisation;
import com.kite9.server.domain.Repository;
import com.kite9.server.domain.RestEntity;
import com.kite9.server.domain.User;
import com.kite9.server.persistence.github.GithubSourceAPIFactory;
import com.kite9.server.persistence.github.config.Config;

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

	public Repository templateRepo(Link self, GHRepository r, Config c, List<Content> contents, List<RestEntity> parents) {
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

			@Override
			public Config getConfig() { return c; }
		};

		p.add(self);

		return p;
	}
	
	public List<Repository> templateChildRepos(LinkBuilder lb, List<GHRepository> repos) {
		List<Repository> repoList = repos.stream().map(r -> {
			return templateRepo(lb.slash(r.getName()).withSelfRel(), r, null, null, null);
		}).collect(Collectors.toList());
		return repoList;
	}

	public List<Organisation> templateChildOrganisations(LinkBuilder lb, List<GHOrganization> orgs) throws IOException {
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

			@Override
			public List<RestEntity> getParents() {
				return parents;
			}

		};
		out.add(l);
		return out;
	}

	public List<Content> templateContents(LinkBuilder lb, DirectoryDetails dd)
			throws IOException {
		
		String ref = dd.getPath().getRef() == null ? dd.getRepo().getDefaultBranch() : dd.getPath().getRef();
		
		return dd.getEntries().stream()
				.map(c -> {
					if (isTree(c)) {
						return templateDirectory(buildDiagramLink(lb, c, ref), c.getPath(), null, null);	
					} else if (fs.getFormatFor(c.getPath()) != null) {
						Document out = templateDocument(buildDiagramLink(lb, c, ref), c.getPath(), null);
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

	protected Link buildDiagramLink(LinkBuilder lb, GHTreeEntry c, String ref) {
		Link basic = lb.slash(c.getPath()).withSelfRel();
		Link withRef = Link.of(basic.getHref()+"?v="+ref, basic.getRel());
		return withRef;
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
	
	@Override
	public User getHomePage(HomeDetails hd) throws Exception {
		LinkBuilder lb = linkToRemappedURI();
		String name = hd.getUser().getLogin();
		List<Repository> repoList = templateChildRepos(lb.slash("github").slash(name), hd.getRepoList());
		List<Organisation> orgList = templateChildOrganisations(lb.slash("github"), hd.getOrganisations());
		User out = templateHome(lb.slash("github").withSelfRel(), hd.getUser(), repoList, orgList, null);
		return out;
	}
	
	protected abstract LinkBuilder linkToRemappedURI();

	public RestEntity createTopLevelParent() {
		LinkBuilder lb = linkToRemappedURI();
		return templateHome(lb.slash("github").withSelfRel());
	}

	@Override
	public Organisation getOrgPage(OrgDetails od) throws Exception {
		LinkBuilder lb = linkToRemappedURI();
		RestEntity gh = createTopLevelParent();
		String name = od.getUserOrOrg().getLogin();
		List<Repository> repoList = templateChildRepos(lb.slash("github").slash(name), od.getRepoList());
		return templateOrganisation(lb.slash("github").slash(name).withSelfRel(), od.getUserOrOrg(), repoList, Collections.singletonList(gh)); 
	}
		
	@Override
	public RestEntity getDirectoryPage(DirectoryDetails dd, Config c) throws Exception {
		
		try {
			LinkBuilder lb = linkToRemappedURI().slash("github");
			LinkBuilder lbUserOrg = lb.slash(dd.getPath().getOwner());
			LinkBuilder lbRepo = lbUserOrg.slash(dd.getPath().getReponame());
			LinkBuilder lbRelative = lbRepo.slash(dd.getPath().getFilepath());
			Repository repo = templateRepo(lbRepo.withSelfRel(), dd.getRepo(), c, null, null);
			Organisation owner = templateOrganisation(lbUserOrg.withSelfRel(), dd.getRepo().getOwner(), null, null);
			
			List<Content> childContent = templateContents(lbRepo, dd);
			RestEntity out;
			
			if (StringUtils.hasText(dd.getPath().getFilepath())) {
				// directory in repo
				String path = dd.getPath().getFilepath();
				String currentDir = path.substring(path.lastIndexOf("/")+1);
				List<RestEntity> parents = buildParents(owner, repo, dd.getPath().getFilepath(), lbRepo);
				out = templateDirectory(lbRelative.withSelfRel(), currentDir, parents, childContent);
			} else {
				List<RestEntity> parents = buildParents(owner, null, null, lbRepo);
				out = templateRepo(lbRelative.withSelfRel(), dd.getRepo(), c, childContent, parents);
			} 
			return out;
		} catch (Throwable e) {
			throw new Kite9XMLProcessingException("Couldn't format directory page "+dd.getPath(), e);
		}
	}

	private List<RestEntity> buildParents(Organisation owner, Repository repo,  String path, LinkBuilder lb) {
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
