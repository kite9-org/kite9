package com.kite9.server.persistence.queue;

import java.util.function.Consumer;

import com.kite9.server.persistence.queue.ChangeQueue.ChangeEvent;

public interface ChangeEventConsumerFactory {
	
	public Consumer<ChangeEvent> createEventConsumer(String path);

}
