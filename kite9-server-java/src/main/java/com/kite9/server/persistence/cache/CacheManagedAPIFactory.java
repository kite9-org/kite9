package com.kite9.server.persistence.cache;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
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
	
	protected final ApplicationContext ctx;
	
	protected final ADLFactory factory;
	
	@Value("${cache.occupancy-time:30000}")
	private long cacheOccupancyTimeMs = 1000*30;

	public CacheManagedAPIFactory(ApplicationContext ctx, ADLFactory factory) {
		super();
		this.ctx = ctx;
		this.factory = factory;
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
			return new CommandQueueModifiableDiagramAPI(cq, (ModifiableDiagramAPI) backingApi, factory, cacheOccupancyTimeMs);
		} else {
			return backingApi;
		} 
	}

	protected ChangeQueue createChangeQueue(String path) {
		return new ChangeQueueImpl(10, createConsumer(path));
	}

	/**
	 * Builds a consumer that sends events to any beans returned from {@link ChangeEventConsumerFactory}s.
	 */
	protected Consumer<ChangeQueue.ChangeEvent> createConsumer(String path) {
		List<Consumer<ChangeQueue.ChangeEvent>> delegates = ctx.getBeansOfType(ChangeEventConsumerFactory.class).values().stream()
			.map(e -> e.createEventConsumer(path))
			.collect(Collectors.toList());
		return e -> delegates.forEach(d -> d.accept(e)); 
	}

	protected abstract SourceAPI createBackingAPI(K9URI u, Authentication a) throws Exception;


	@Scheduled(fixedDelay = 5000)
	public void cleanUp() {
		int startSize = cache.size();
		if (startSize > 0) {
			cache.entrySet().removeIf(e -> {
				boolean evict = e.getValue().canEvict();
				if (evict) {
					logger.debug("Evicting api: ()", e.getKey());
				}
				return evict;
			});
		}
	}
	
	/**
	 * Update the cache every minute
	 */
	@Scheduled(fixedRate = 1 * 60 * 1000)
	public void update() {
		cache.entrySet().stream()
			.filter(e -> !e.getValue().canEvict())
			.forEach(e -> {
				logger.debug("Updating apis: {} ",e.getKey());
				e.getValue().update();
			});
	}
}		
