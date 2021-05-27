package com.kite9.k9server.adl.holder.pipeline;

import java.net.URI;

import org.kite9.diagram.dom.elements.ADLDocument;

import com.kite9.k9server.adl.format.media.DiagramFormat;

/**
 * Second point in the pipeline, the DOM can be worked on by commands.
 * 
 * @author robmoffat
 *
 */
public interface ADLDom extends XMLDom<ADLDocument> {

	/** 
	 * Does the actual processing
	 */
	<X extends DiagramFormat> ADLOutput<X> process(URI forLocation, X format);
	

	void ensureCssEngine(ADLDocument doc);
	
}
