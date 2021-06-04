package com.kite9.server.topic;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import com.kite9.server.update.AbstractUpdateHandler;
import com.kite9.server.update.Update;
import org.kite9.diagram.dom.XMLHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.kite9.server.adl.format.media.EditableSVGFormat;
import com.kite9.server.pipeline.adl.format.media.Kite9MediaTypes;
import com.kite9.server.adl.holder.meta.BasicMeta;
import com.kite9.server.pipeline.adl.holder.meta.MetaRead;
import com.kite9.server.adl.holder.meta.Payload;
import com.kite9.server.pipeline.adl.holder.meta.UserMeta;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLBase;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.pipeline.adl.holder.pipeline.ADLOutput;
import com.kite9.server.persistence.queue.ChangeEventConsumerFactory;
import com.kite9.server.persistence.queue.ChangeQueue.ChangeEvent;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;
import com.kite9.server.update.UpdateHandler;

/**
 * This keeps track of which users have subscribed to events via websockets.
 * 
 * @author robmoffat
 *
 */
@Component
public class ChangeWebSocketHandler extends TextWebSocketHandler implements ChangeBroadcaster, ChangeEventConsumerFactory, InitializingBean {
	
	private static final Logger LOG = LoggerFactory.getLogger(ChangeWebSocketHandler.class);


	private Map<String, List<WebSocketSession>> sessions = new HashMap<>();
	private Map<WebSocketSession, String> topics = new HashMap<>();
	
	@Autowired
	private ObjectMapper objectMapper;
	
	private XMLHelper xmlHelper = new XMLHelper();
	
	@Autowired
	protected SourceAPIFactory apiFactory;
	
	@Autowired
	protected FormatSupplier formatSupplier;
	
	protected UpdateHandler updateHandler;
	
	protected EditableSVGFormat updateFormat;

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		String topic = getTopicFromUri(session.getUri());
		List<WebSocketSession> sessionList = sessions.getOrDefault(topic, new ArrayList<WebSocketSession>());
		sessionList.add(session);
		sessions.put(topic, sessionList);
		topics.put(session, topic);
		
		if (sessionList.size() > 1) {
			BasicMeta out = new BasicMeta(new HashMap<>());
			UserMeta joiner = getUserFromSession(session);
			out.setNotification(joiner.getDisplayName()+" has joined");
			out.setCollaborators(getCurrentSubscribers(topic));
			metaUpdate(out, topic);
		}
		
		LOG.debug("Handled Subscription to "+topic);
	}
	

	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		String topic = topics.get(session);
		LOG.info("Received message "+message+" on topic "+topic);
		Update u = objectMapper.readValue(message.getPayload(), Update.class);
		Authentication principal = (Authentication) session.getPrincipal();

		try {
			ADLOutput<EditableSVGFormat> out = updateHandler.performDiagramUpdate(u, principal, updateFormat);
			broadcastToTopic(out.getAsBytes(), topic);
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
		ADLOutput<EditableSVGFormat> svg = dom.process(u.getUri(), updateFormat);
		session.sendMessage(new TextMessage(svg.getAsBytes()));
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		LOG.error("Transport Error ", exception);
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		String topic = getTopicFromUri(session.getUri());
		List<WebSocketSession> existingSessions = sessions.getOrDefault(topic, Collections.emptyList());
		existingSessions.remove(session);
		
		if (existingSessions.size() > 0) {
			BasicMeta out = new BasicMeta(new HashMap<>());
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
	
	private String getTopicFromUri(java.net.URI u) {
		if (u != null) {
			return u.getPath().replaceFirst(WebSocketConfig.TOPIC_PREFIX, "");
		} else {
			return null;
		}
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
	public void broadcast(URI topicUri, ADLOutput<EditableSVGFormat> adl) {
		String topic = getTopicFromUri(topicUri);
		broadcastToTopic(adl.getAsBytes(), topic);
	}
	
	protected void metaUpdate(MetaRead meta, String topic) {
		Document d = Payload.createMetaDocument(meta);
		String text = xmlHelper.toXML(d);
		TextMessage bm = new TextMessage(text);
		broadcastTextMessage(topic, bm);
	}

	protected void broadcastTextMessage(String topic, TextMessage bm) {
		sessions.getOrDefault(topic, Collections.emptyList())
			.parallelStream()
			.forEach(wss -> {
				try {
					wss.sendMessage(bm);	
				} catch (IOException e) {
					LOG.error("Couldn't send message: ", e);
				}	
			});
	}
	

	protected void broadcastToTopic(byte[] contents, String topic) {
		TextMessage bm = new TextMessage(contents);
		broadcastTextMessage(topic, bm);
	}

	@Override
	public List<UserMeta> getCurrentSubscribers(URI topicUri) {
		if (topicUri == null) {
			return null;
		}
		
		String t = getTopicFromUri(topicUri);
		return getCurrentSubscribers(t);
	}

	protected List<UserMeta> getCurrentSubscribers(String topic) {
		List<WebSocketSession> list = sessions.get(topic);
		return list == null ? null : list.stream()
			.map(session -> getUserFromSession(session))
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}
	
	protected UserMeta getUserFromSession(WebSocketSession session) {
		if (session.getPrincipal() instanceof OAuth2AuthenticationToken) {
			return BasicMeta.createUser(((OAuth2AuthenticationToken) session.getPrincipal()).getPrincipal()); 
		} else {
			return null;
		}
	}

	@Override
	public void broadcastMeta(URI topicUri, MetaRead meta) {
		String topic = getTopicFromUri(topicUri);
		metaUpdate(meta, topic);
	}

	@Override
	public Consumer<ChangeEvent> createEventConsumer(String path) {
		return x -> metaUpdate(createMetaFromChange(x), path);
	}

	private MetaRead createMetaFromChange(ChangeEvent x) {
		BasicMeta bm = new BasicMeta(new HashMap<>());
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
		
		updateFormat = (EditableSVGFormat) formatSupplier.getFormatFor(Kite9MediaTypes.ESVG);
	}

}
