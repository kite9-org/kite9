package com.kite9.server.topic;

import java.util.List;

import com.kite9.pipeline.adl.holder.meta.MetaRead;
import com.kite9.pipeline.adl.holder.meta.UserMeta;
import com.kite9.pipeline.adl.holder.pipeline.ADLDom;
import com.kite9.pipeline.uri.K9URI;

public interface ChangeBroadcaster {

	/**
	 * When a version of a document is updated, broadcast the contents of it like this,
	 * so that people can edit the new version.
	 */
	public void broadcast(K9URI topicUri, ADLDom adl);
	
	/**
	 * Broadcast status changes for a given topic.
	 */
	public void broadcastMeta(K9URI topicUri, MetaRead meta);
	
	public List<UserMeta> getCurrentSubscribers(K9URI topicUri);
		
}
