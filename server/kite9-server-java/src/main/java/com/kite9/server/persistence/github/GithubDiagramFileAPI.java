package com.kite9.server.persistence.github;


import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.topic.WebSocketConfig;
import org.kohsuke.github.GHContent;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.sources.ModifiableDiagramAPI;

public abstract class GithubDiagramFileAPI extends AbstractGithubModifiableFileAPI implements ModifiableDiagramAPI {
	
	private final DiagramFileFormat dff;
	
	public GithubDiagramFileAPI(K9URI u, OAuth2AuthorizedClientRepository clientRepository, DiagramFileFormat dff, K9MediaType mt, boolean isNew) {
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
		ADLOutput out = dom.process(sourceURI, dff);
		if (dff.isBinaryFormat()) {
			commitRevision(message, tb -> tb.add(filepath, out.getAsBytes(), false), by);
		} else {
			commitRevision(message, tb -> tb.add(filepath, out.getAsString(), false), by);
		}
	}
	

	@Override
	public void addMeta(MetaReadWrite adl) {
		K9URI u = adl.getUri();
		String uPath = u.getPath();
		String closePath = uPath.substring(0, uPath.lastIndexOf("/"));
		K9URI close = u.resolve(closePath);
		adl.setCloseUri(close);

		String socketScheme = u.getScheme().equals("http") ? "ws" : "wss";
		K9URI topic = u.changeScheme(socketScheme, WebSocketConfig.TOPIC_PREFIX);
		adl.setTopicUri(topic);

		String[] parts = uPath.split("/");

		adl.setUploadsPath("/"+GithubContentController.GITHUB
				+"/"+parts[2]	// org
				+"/"+parts[3]	// repo
				+GithubContentController.DEFAULT_GITHUB_UPLOADS);

		adl.setTitle(AbstractGithubModifiableFileAPI.createTitle(u));
	}

}
