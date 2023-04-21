package com.kite9.server.persistence.github;

import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;

import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.holder.meta.MetaReadWrite;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.format.media.DiagramFileFormat;
import com.kite9.server.persistence.github.config.ConfigLoader;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.topic.WebSocketConfig;

public abstract class AbstractGithubModifiableDiagramAPI extends AbstractGithubModifiableAPI
		implements ModifiableDiagramAPI {

	private final DiagramFileFormat dff;
	protected K9MediaType mediaType;

	public AbstractGithubModifiableDiagramAPI(K9URI u, OAuth2AuthorizedClientRepository clientRepository, ConfigLoader configLoader,
			DiagramFileFormat dff, K9MediaType mt) throws Exception {
		super(u, clientRepository, configLoader);
		this.dff = dff;
		this.mediaType = mt;
	}

	@Override
	public K9MediaType getMediaType() {
		return mediaType;
	}

	@Override
	public ADLBase getCurrentRevisionContent(Authentication authentication, HttpHeaders headers) throws Exception {
		ADLBase base = dff.handleRead(getCurrentRevisionContentStream(authentication), getUnderlyingResourceURI(), headers);
		return base;
	}

	@Override
	public void commitRevision(String message, Authentication by, ADLDom dom) {
		ADLOutput out = dom.process(getUnderlyingResourceURI(), dff);
		commitRevision(message, tb -> tb.add(this.githubPath.getFilepath(), out.getAsBytes(), false), by);
	}

	@Override
	public void addMeta(MetaReadWrite adl) {
		K9URI u = adl.getUri();
		String uPath = u.getPath();
		String closePath = uPath.substring(0, uPath.lastIndexOf("/"));
		K9URI close = u.resolve(closePath);
		adl.setCloseUri(close);

		String socketScheme = u.getScheme().equals("http") ? "ws" : "wss";
		K9URI topic = u.changeScheme(socketScheme, WebSocketConfig.TOPIC_PREFIX + uPath);
		adl.setTopicUri(topic);

		String[] parts = uPath.split("/");

		if (parts.length >= 3) {

			adl.setUploadsPath("/" + GithubContentController.GITHUB + "/" + parts[2] // org
					+ "/" + parts[3] // repo
					+ GithubContentController.DEFAULT_GITHUB_UPLOADS);

		}

		adl.setTitle(AbstractGithubModifiableAPI.createTitle(u));
	}
}
