package com.kite9.k9server.persistence.queue;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;

import org.kite9.diagram.common.Kite9XMLProcessingException;

public class ChangeQueueImpl implements ChangeQueue {
	
	private final BlockingQueue<Change> workQueue;
    private final ExecutorService service;
    private int size;
    private Consumer<ChangeQueue.ChangeEvent> consumer;

    public ChangeQueueImpl(int workQueueSize, Consumer<ChangeQueue.ChangeEvent> consumer) {
        this.workQueue = new LinkedBlockingQueue<>(workQueueSize);
        this.service = Executors.newFixedThreadPool(1);
        this.consumer = consumer;
    }


	@Override
	public int getQueueSize() {
		return size;
	}

	@Override
	public void addItem(Change c) {
		try {
			workQueue.put(c);
	        service.submit(createWorker());
	        size = workQueue.size();
		} catch (InterruptedException e) {
			throw new Kite9XMLProcessingException("Work Queue Put Interrupted "+c, e);
		}
	}
	
	public Runnable createWorker() {
		return () -> {
			while (workQueue.size() > 0) {
				Change c = workQueue.poll();
				try {
					c.perform();
					size = workQueue.size();
					consumer.accept(new ChangeEvent(size, null));
				} catch (Exception e) {
					consumer.accept(new ChangeEvent(size, e.getMessage()));
				}
			}
		};
	}

}
