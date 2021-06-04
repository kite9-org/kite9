package com.kite9.server.persistence.github;

import java.net.URI;
import java.net.URISyntaxException;

import com.kite9.server.topic.WebSocketConfig;
import org.kohsuke.github.GHContent;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.server.pipeline.adl.format.media.DiagramReadFormat;
import com.kite9.server.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.sources.ModifiableDiagramAPI;

public abstract class GithubDiagramFileAPI extends AbstractGithubModifiableFileAPI implements ModifiableDiagramAPI {
	
	private DiagramReadFormat dff;
	
	public GithubDiagramFileAPI(URI u, OAuth2AuthorizedClientRepository clientRepository, DiagramReadFormat dff, MediaType mt, boolean isNew)
			throws URISyntaxException {
		super(u, clientRepository, mt, isNew);
		this.dff = dff;
	}

	@Override
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders headers) throws Exception {
		GHContent content = getGHContent(getAccessToken(authentication, clientRepository));
		ADLBase base = dff.handleRead(content.read(), sourceURI, headers);
		return base;
	}


	@Override
	public void commitRevision(String message, Authentication by, ADLDom dom) {
		ADLOutput<?> out = dom.process(sourceURI, (DiagramReadFormat) dff);
		if (dff.isBinaryFormat()) {
			commitRevision(message, tb -> tb.add(filepath, out.getAsBytes(), false), by);
		} else {
			commitRevision(message, tb -> tb.add(filepath, out.getAsString(), false), by);
		}
	}
	

	@Override
	public void addMeta(MetaReadWrite adl) {
		URI u = adl.getUri();
		try {
			String uPath = u.getPath();
			String closePath = uPath.substring(0, uPath.lastIndexOf("/"));
			URI close = new URI(u.getScheme(), u.getUserInfo(), u.getHost(), u.getPort(), closePath, null, null);
			adl.setCloseUri(close);
	
			String socketScheme = u.getScheme().equals("http") ? "ws" : "wss";
			URI topic = new URI(socketScheme, u.getUserInfo(), u.getHost(), u.getPort(),
					WebSocketConfig.TOPIC_PREFIX + u.getPath(), u.getQuery(), u.getFragment());
			adl.setTopicUri(topic);
			
			String[] parts = uPath.split("/");
			
			adl.setUploadsPath("/"+GithubContentController.GITHUB
					+"/"+parts[2]	// org
					+"/"+parts[3]	// repo
					+GithubContentController.DEFAULT_GITHUB_UPLOADS);
						
			adl.setTitle(AbstractGithubModifiableFileAPI.createTitle(u));
		} catch (URISyntaxException e) {
			logger.error("Couldn't create uri from " + u, e);
		}
	}

}
