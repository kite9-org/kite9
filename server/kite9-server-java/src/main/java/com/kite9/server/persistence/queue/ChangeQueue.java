package com.kite9.server.persistence.queue;

import java.util.function.Consumer;

import org.springframework.security.core.Authentication;

import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.server.sources.ModifiableDiagramAPI;

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
		ADLDom payload;
		ModifiableDiagramAPI on;
		Authentication by;
		Consumer<?> done;
		
		public Change(ModifiableDiagramAPI on, String message, ADLDom payload, Authentication by) {
			super();
			this.on = on;
			this.message = message;
			this.payload = payload;
			this.by = by;
		}
		
		public void perform() {
			on.commitRevision(message, by, payload);
		}
	}
	
	


	public int getQueueSize();
	
	public void addItem(Change c);
}
