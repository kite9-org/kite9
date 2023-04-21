package com.kite9.server.persistence.github.urls;

import java.util.function.Supplier;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClient.ResponseSpec;

import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;

import reactor.core.publisher.Mono;

/**
 * Handles creating URLs to map to github's raw access point, which looks like this:
 * 
 * <pre>https://raw.githubusercontent.com/&lt;owner&gt;/&lt;repo&gt;/&lt;ref&gt;/&lt;path&gt;</pre>

 * @author rob@kite9.com
 *
 */
public class RawGithub {

	private static final String RAW_GITHUB = "https://raw.githubusercontent.com";

	public static String assembleGithubURL(Kite9GithubPath k9p, Supplier<String> refSupplier) {
		String ref = k9p.getRef() == null ? refSupplier.get() : k9p.getRef();
		return assembleGithubURL(k9p.getOwner(), k9p.getReponame(), ref, k9p.getFilepath());
	}
	
	public static String assembleGithubURL(String owner, String reponame, String ref, String filepath) {
		String url = RAW_GITHUB + (!StringUtils.hasText(owner) ? "" : 
			 "/" +owner + (!StringUtils.hasText(reponame) ? "" : 
				 "/"+ reponame + (!StringUtils.hasText(ref)? "" : 
					 "/" + ref + (!StringUtils.hasText(filepath) ? "" :
						"/" + filepath))));
		return url;
	}
	
	/**
	 * Can load bytes from the URL, given a URL in the format above.
	 */
	public static ByteArrayResource loadBytesFromGithub(String token, String url) {
		WebClient webClient = WebClient.create(url);
				
		RequestHeadersSpec<?> spec = webClient.get()
			.header("Accept-Encoding", "identity")
			.header(HttpHeaders.ACCEPT, Kite9MediaTypes.ALL_VALUE);
		
		if (token != null) {
			spec = spec.header("Authorization", "token "+token);
		}
				
		ResponseSpec retrieve = spec.retrieve();
		Mono<ByteArrayResource> mono = retrieve.bodyToMono(ByteArrayResource.class);
		ByteArrayResource db = mono.block();
		return db;
	}
}
