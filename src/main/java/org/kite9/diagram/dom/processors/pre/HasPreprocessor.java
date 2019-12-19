package org.kite9.diagram.dom.processors.pre;

import org.kite9.diagram.dom.processors.XMLProcessor;

public interface HasPreprocessor {

	/**
	 * Allows an XML element to store the pre-processor used to create it, for onward pre-processing.
	 */
	public void setPreprocessor(XMLProcessor p);
	
	public XMLProcessor getPreprocessor();
}
