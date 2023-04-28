package com.kite9.server.persistence.local.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.LinkBuilder;
import org.springframework.security.core.Authentication;

import com.kite9.server.domain.Content;
import com.kite9.server.domain.Directory;
import com.kite9.server.domain.Document;
import com.kite9.server.domain.RestEntity;

public abstract class AbstractPublicEntityConverter {

	String pathStem;
	String resourceStem;
	ResourcePatternResolver rl;
	ApplicationContext ctx;
	
	
	public AbstractPublicEntityConverter(String pathStem, String resourceStem, ResourcePatternResolver rl) {
		this.pathStem = pathStem;
		this.resourceStem = resourceStem;
		this.rl = rl;
	}

	public RestEntity handleEntityContent(Authentication authentication, String path) throws Exception {
		String resourcePattern = path.replace(pathStem, resourceStem) + "/*";
		Resource[] resources = rl.getResources(resourcePattern);
		LinkBuilder lb = linkToRemappedURI().slash(path);
		Directory out = new Directory() {
			
			@Override
			public String getTitle() {
				return path.substring(path.lastIndexOf("/")+1);
			}
			
			@Override
			public List<RestEntity> getParents() {
				return buildPathEntities(authentication, path, linkToRemappedURI());
			}
			
			@Override
			public String getDescription() {
				return "directory";
			}
			
			public List<Content> getContents() {
				return Arrays.asList(resources).stream()
					.map(d -> {
						if (isDirectory(d)) {
							return createDirectory(d, lb);
						} else if (isDocument(d)) {
							return createDocument(d, lb);
						} else {
							return null;
						}
					})
					.filter(d -> d != null)
					.collect(Collectors.toList());
						
			}

			protected Directory createDirectory(Resource d, LinkBuilder lb) {
				Directory out = new Directory() {
					
					@Override
					public String getTitle() {
						return d.getFilename();
					}
					
					@Override
					public List<RestEntity> getParents() {
						return Collections.emptyList();
					}
					
					@Override
					public String getDescription() {
						return "directory";
					}
					
					@Override
					public List<Content> getContents() {
						return Collections.emptyList();
					}
				};
				
				out.add(lb.slash(d.getFilename()).withSelfRel());
				return out;
			}

			protected Document createDocument(Resource d, LinkBuilder lb) {
				Document out = new Document() {
					
					@Override
					public String getTitle() {
						return d.getFilename();
					}
					
					@Override
					public List<RestEntity> getParents() {
						return Collections.emptyList();
					}
					
					public String getDescription() {
						return getExtension(d.getFilename())+" file";
					}
				};
				
				out.add(lb.slash(d.getFilename()).withSelfRel());
				return out;
			}

			protected boolean isDocument(Resource d) {
				return d.exists() && d.isReadable();
			}
			
			protected boolean isDirectory(Resource d) {
				return d.exists() && !d.isReadable();
			}
		};
		
		Link l = linkToRemappedURI().slash(path).withSelfRel();
		out.add(l);
		
		return out;
		
	}
	
	protected abstract LinkBuilder linkToRemappedURI();

	private List<RestEntity> buildPathEntities(Authentication a, String path, LinkBuilder lb) {
		String[] parts = path.split("/");
		List<RestEntity> out = new ArrayList<RestEntity>();
		for (int i = 1; i < parts.length-1; i++) {
			String pathSoFar = String.join("/", Arrays.copyOfRange(parts, 1, i+1)); 
			Link dirLink = lb.slash(pathSoFar).withSelfRel();
			out.add(templateDirectory(dirLink, parts[i]));
		}
		
		return out;
	}

	public Directory templateDirectory(Link self, String name) {
		Directory out = new Directory() {

			@Override
			public List<Content> getContents() {
				return Collections.emptyList();
			}

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
				return null;
			}
			
		};
		
		out.add(self);
		
		return out;
	}
 
}
