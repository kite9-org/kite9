package com.kite9.k9server.adl.holder.pipeline;

import java.net.URI;

import org.w3c.dom.Document;

import com.kite9.k9server.adl.holder.meta.MetaReadWrite;

/**
 * Second point in the pipeline, the DOM can be worked on by commands.
 * 
 * @author robmoffat
 *
 */
public interface XMLDom<T extends Document> extends XMLBase, MetaReadWrite {

	T getDocument();
	
	/**
	 * For parsing a referenced document, provided in content.
	 */
	T parseDocument(String content, URI uri);
	
	
	/**
	 * For loading up a referenced document.
	 */
	T parseDocument(URI uri);

}
