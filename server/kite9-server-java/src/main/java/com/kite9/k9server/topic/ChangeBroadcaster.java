package com.kite9.k9server.topic;

import java.net.URI;
import java.util.List;

import com.kite9.k9server.adl.format.media.EditableSVGFormat;
import com.kite9.k9server.adl.holder.meta.MetaRead;
import com.kite9.k9server.adl.holder.meta.UserMeta;
import com.kite9.k9server.adl.holder.pipeline.ADLOutput;

public interface ChangeBroadcaster {

	/**
	 * When a version of a document is updated, broadcast the contents of it like this,
	 * so that people can edit the new version.
	 */
	public void broadcast(URI topicUri, ADLOutput<EditableSVGFormat> adl);
	
	/**
	 * Broadcast status changes for a given topic.
	 */
	public void broadcastMeta(URI topicUri, MetaRead meta);
	
	public List<UserMeta> getCurrentSubscribers(URI topicUri);
		
}
