package com.kite9.server.persistence.queue;

import java.util.function.Consumer;

import org.springframework.security.core.Authentication;

import com.kite9.server.sources.ModifiableAPI;

public interface ChangeQueue {
	
	public class ChangeEvent {
		
		public int queueSize;
		public String errorMessage;
		
		public ChangeEvent(int queueSize, String errorMessage) {
			super();
			this.queueSize = queueSize;
			this.errorMessage = errorMessage;
		}
		
	}
		
	public class Change {

		String message;
		byte[] payload;
		ModifiableAPI on;
		Authentication by;
		Consumer<?> done;
		
		public Change(ModifiableAPI on, String message, byte[] payload, Authentication by) {
			super();
			this.on = on;
			this.message = message;
			this.payload = payload;
			this.by = by;
		}
		
		public void perform() throws Exception {
			on.commitRevisionAsBytes(message, by, payload);
		}
	}
	
	


	public int getQueueSize();
	
	public void addItem(Change c);
}
