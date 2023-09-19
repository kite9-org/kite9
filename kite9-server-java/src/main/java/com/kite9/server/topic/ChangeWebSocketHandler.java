package com.kite9.server.topic;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.kite9.diagram.dom.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.w3c.dom.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kite9.pipeline.adl.format.FormatSupplier;
import com.kite9.pipeline.adl.format.media.DiagramWriteFormat;
import com.kite9.pipeline.adl.format.media.Format;
import com.kite9.pipeline.adl.format.media.K9MediaType;
import com.kite9.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.pipeline.adl.holder.meta.BasicMeta;
import com.kite9.pipeline.adl.holder.meta.MetaRead;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.adl.holder.meta.MetaHelper;
import com.kite9.server.adl.holder.meta.Payload;
import com.kite9.server.persistence.queue.ChangeEventConsumerFactory;
import com.kite9.server.persistence.queue.ChangeQueue.ChangeEvent;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.update.AbstractUpdateHandler;
import com.kite9.server.update.Update;
import com.kite9.server.update.UpdateHandler;
import com.kite9.server.uri.URIWrapper;

/**
 * This keeps track of which users have subscribed to events via websockets.
 * 
 * @author robmoffat
 *
 */
@Component
public class ChangeWebSocketHandler extends TextWebSocketHandler implements ChangeBroadcaster, ChangeEventConsumerFactory, InitializingBean, ApplicationContextAware {

	private static final Logger LOG = LoggerFactory.getLogger(ChangeWebSocketHandler.class);
	
	static class TopicDetails {
		
		final K9URI url;
		final String contentType;
		
		public TopicDetails(String url, String contentType) {
			super();
			this.url = URIWrapper.wrap(URI.create(url));
			this.contentType = contentType;
		}

		@Override
		public String toString() {
			return "TopicDetails [url=" + url + ", contentType=" + contentType + "]";
		}
		
	}
	
	protected final Map<String, List<WebSocketSession>> sessions = new HashMap<>();
	protected final Map<WebSocketSession, TopicDetails> topics = new HashMap<>();
	protected final ObjectMapper objectMapper;
	protected final XMLHelper xmlHelper = new XMLHelper();
	protected final SourceAPIFactory apiFactory;
	protected final FormatSupplier formatSupplier;
	protected UpdateHandler updateHandler;
	protected ApplicationContext ctx;
	
	public ChangeWebSocketHandler(ObjectMapper objectMapper, SourceAPIFactory apiFactory, FormatSupplier formatSupplier) {
		this.objectMapper = objectMapper;
		this.apiFactory = apiFactory;
		this.formatSupplier = formatSupplier;
	}	
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		K9URI k9uri = URIWrapper.wrap(session.getUri());
		String topic = getTopicFromUri(k9uri);
		String contentType = getContentTypeFromURI(k9uri);
		List<WebSocketSession> sessionList = sessions.getOrDefault(topic, new ArrayList<WebSocketSession>());
		sessionList.add(session);
		sessions.put(topic, sessionList);
		topics.put(session, new TopicDetails(topic, contentType));
		
		if (sessionList.size() > 1) {
			BasicMeta out = new BasicMeta(new HashMap<>(), null);
			UserMeta joiner = getUserFromSession(session);
			out.setNotification(joiner.getDisplayName()+" has joined");
			out.setCollaborators(getCurrentSubscribers(topic));
			metaUpdate(out, topic);
		}
		
		LOG.debug("Handled Subscription to "+topic);
	}
	

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		TopicDetails topic = topics.get(session);
		LOG.info("Received message: "+message.getPayload()+" on topic: "+topic);
		Update u = objectMapper.readValue(message.getPayload(), Update.class);
		Authentication principal = (Authentication) session.getPrincipal();

		try {
			updateHandler.performDiagramUpdate(u, principal);
		} catch (Exception e) {
			LOG.error("Problem with command", e);
			respondWithError(session, u, principal, e);
		}
	}

	protected void respondWithError(WebSocketSession session, Update u, Authentication principal, Exception e) throws Exception, IOException {
		ModifiableDiagramAPI api = (ModifiableDiagramAPI) updateHandler.getModifiableAPI(u, principal);
		ADLBase base = api.getCurrentRevisionContent(principal, u.getHeaders());
		String cause = updateHandler.getProperCause(e);
		ADLDom dom = base.parse();
		dom.setError(cause);
		LOG.warn("Responding with error: ", e);
		session.sendMessage(new TextMessage(dom.getAsString()));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		LOG.error("Transport Error ", exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		String topic = getTopicFromUri(URIWrapper.wrap(session.getUri()));
		List<WebSocketSession> existingSessions = sessions.getOrDefault(topic, Collections.emptyList());
		existingSessions.remove(session);
		
		if (existingSessions.size() > 0) {
			BasicMeta out = new BasicMeta(new HashMap<>(), null);
			UserMeta leaver = getUserFromSession(session);
			out.setNotification(leaver.getDisplayName()+" has left");
			out.setCollaborators(getCurrentSubscribers(topic));
			metaUpdate(out, topic);
		}
		
		LOG.debug("Removed Session", session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
	
	private String getTopicFromUri(K9URI u) {
		if (u != null) {
			return u.getPath().replaceFirst(WebSocketConfig.TOPIC_PREFIX, "");
		} else {
			return null;
		}
	}
	
	private final static String DEFAULT_CONTENT_TYPE = Kite9MediaTypes.EDITABLE_ADL_XML_VALUE;
	
	private String getContentTypeFromURI(K9URI u) {
		if (u != null) {
			List<String> values = u.param("contentType");
			
			if (values.size() > 0) {
				return values.get(0);
			}
		}
		
		return DEFAULT_CONTENT_TYPE;
	}

	@Scheduled(fixedDelay = 60000) 
	public void tidyUp() {
		sessions.values().forEach(l -> l.removeIf(ws -> !ws.isOpen()));
		for (Map.Entry<String, List<WebSocketSession>> e : sessions.entrySet()) {
			if (e.getValue().isEmpty()) {
				sessions.remove(e.getKey());
			}
		}
	}
	
	

	@Override
	public void broadcast(ADLDom dom) {
		String topic = getTopicFromUri(dom.getUri());
		broadcastInternal(topic, dom, (adl, contentType) -> {
			K9MediaType mediaType = K9MediaType.Companion.parseMediaType(contentType);
			Format f = formatSupplier.getFormatFor(mediaType);
			if (f instanceof DiagramWriteFormat) {
				ADLOutput out = adl.process(adl.getUri(), (DiagramWriteFormat) f);
				String strOut = new String(out.getAsBytes());
				TextMessage bm = new TextMessage(strOut);
				return bm;
			}
				
			return null;
		});
	}

	public <X> void broadcastInternal(String topic, X adl, BiFunction<X, String, TextMessage> converter) {
		List<WebSocketSession> broadcastTo = sessions.getOrDefault(topic, Collections.emptyList());
		LOG.info("Sending message on "+topic+" to "+broadcastTo.size()+" recipients "+adl.getClass());

		broadcastTo
			.parallelStream()
			.forEach(wss -> {
				try {
					TopicDetails td = topics.get(wss);
					if (td != null) {
						TextMessage tm = converter.apply(adl, td.contentType);
						LOG.info("Sending message on topic "+topic+"to "+wss.getId());
						wss.sendMessage(tm);	
					}
				} catch (IOException e) {
					LOG.error("Couldn't send message: ", e);
				}	
			});
	}
	
	protected void metaUpdate(MetaRead meta, String topic) {
		Document d = Payload.createMetaDocument(meta);
		String text = xmlHelper.toXML(d, true);
		TextMessage bm = new TextMessage(text);
		broadcastInternal(topic, bm, (x, contentType) -> {
			return x;
		});
	}

	@Override
	public List<UserMeta> getCurrentSubscribers(K9URI topicUri) {
		if (topicUri == null) {
			return Collections.emptyList();
		}
		
		String t = getTopicFromUri(topicUri);
		return getCurrentSubscribers(t);
	}

	protected List<UserMeta> getCurrentSubscribers(String topic) {
		List<WebSocketSession> list = sessions.get(topic);
		return list == null ? Collections.emptyList() : list.stream()
			.map(session -> getUserFromSession(session))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	protected UserMeta getUserFromSession(WebSocketSession session) {
		if (session.getPrincipal() instanceof OAuth2AuthenticationToken) {
			return MetaHelper.createUser(((OAuth2AuthenticationToken) session.getPrincipal()).getPrincipal());
		} else {
			return null;
		}
	}

	@Override
	public Consumer<ChangeEvent> createEventConsumer(String path) {
		return x -> metaUpdate(createMetaFromChange(x), path);
	}

	private MetaRead createMetaFromChange(ChangeEvent x) {
		BasicMeta bm = new BasicMeta(new HashMap<>(), null);
		if (x.errorMessage != null) {
			bm.setError(x.errorMessage);
		}
		
		bm.setCommitCount(x.queueSize);
		return bm;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		updateHandler = new AbstractUpdateHandler() {
			
			@Override
			public SourceAPI getSourceAPI(Update u, Authentication a) throws Exception {
				return apiFactory.createAPI(u, a);
			}
		};
		
		((ApplicationContextAware) updateHandler).setApplicationContext(ctx);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx = applicationContext;
	}

}
