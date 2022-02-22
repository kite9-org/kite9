package com.kite9.server.persistence.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.ADLFactory;
import com.kite9.pipeline.uri.K9URI;
import com.kite9.server.persistence.queue.ChangeEventConsumerFactory;
import com.kite9.server.persistence.queue.ChangeQueue;
import com.kite9.server.persistence.queue.ChangeQueueImpl;
import com.kite9.server.persistence.queue.CommandQueueModifiableDiagramAPI;
import com.kite9.server.sources.ModifiableDiagramAPI;
import com.kite9.server.sources.SourceAPI;
import com.kite9.server.sources.SourceAPIFactory;

public abstract class CacheManagedAPIFactory implements SourceAPIFactory {

	private static Logger logger = LoggerFactory.getLogger(CacheManagedAPIFactory.class);
	
	private final Map<String, CachingModifiableDiagramAPI> cache = new HashMap<>();
	
	@Autowired
	ChangeEventConsumerFactory changeEventConsumerFactory;
	
	@Autowired
	ADLFactory factory;

	public CacheManagedAPIFactory() {
		super();
	}

	public SourceAPI createAPI(K9URI u, Authentication a) throws Exception {
		String path = u.getPath();
		if (!path.startsWith("/github")) {
			return null;
		}
		SourceAPI out = cache.get(path);
		
		if (out == null) {
			out = buildNewAPI(u, a);
			if (out instanceof CachingModifiableDiagramAPI) {
				cache.put(path, (CachingModifiableDiagramAPI) out);
			}
		} 
		return out;
	}

	private SourceAPI buildNewAPI(K9URI u, Authentication a) throws Exception {
		SourceAPI backingApi = createBackingAPI(u, a);
		if (backingApi == null) {
			return null;
		} else if (backingApi instanceof ModifiableDiagramAPI){
			String path = u.getPath();
			logger.info("Building Cache For: "+path);
			ChangeQueue cq = createChangeQueue(path);
			return new CommandQueueModifiableDiagramAPI(cq, (ModifiableDiagramAPI) backingApi, factory);
		} else {
			return backingApi;
		} 
	}

	protected ChangeQueue createChangeQueue(String path) {
		return new ChangeQueueImpl(10, createConsumer(path));
	}

	private Consumer<ChangeQueue.ChangeEvent> createConsumer(String path) {
		return changeEventConsumerFactory.createEventConsumer(path);
	}

	protected abstract SourceAPI createBackingAPI(K9URI u, Authentication a) throws Exception;


	@Scheduled(fixedDelay = 5000)
	public void cleanUp() {
		int startSize = cache.size();
		if (startSize > 0) {
			if (cache.entrySet().removeIf(e -> e.getValue().canEvict())) {
				logger.debug("Evicting apis: "+startSize+ " -> " + cache.size());
			}
		}
	}
	
	/**
	 * Update the cache every minute
	 */
	@Scheduled(fixedRate = 1 * 60 * 1000)
	public void update() {
		cache.values().stream()
			.filter(v -> !v.canEvict())
			.forEach(v -> v.update());
	}
}		
