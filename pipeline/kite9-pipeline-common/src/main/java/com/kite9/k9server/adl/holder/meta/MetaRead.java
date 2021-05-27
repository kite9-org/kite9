package com.kite9.k9server.adl.holder.meta;

import java.net.URI;
import java.util.Map;

public interface MetaRead {
	
	String getTitle();

	URI getTopicUri();

	URI getUri();

	/**
	 * Information about the editing, creation etc. of the document.
	 */
	Map<String, Object> getMetaData();

}