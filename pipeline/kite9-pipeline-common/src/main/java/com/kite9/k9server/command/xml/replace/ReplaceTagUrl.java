package com.kite9.k9server.command.xml.replace;

import org.w3c.dom.Element;

import com.kite9.k9server.adl.holder.pipeline.ADLDom;

/**
 * Allows the to field to be loaded from a url.
 * 
 * @author robmoffat
 *
 */
public class ReplaceTagUrl extends ReplaceTag {

	@Override
	protected Element getToContent(ADLDom adl) {
		Element out = getForeignElementCopy(adl.getDocument(), adl.getUri(), to, false, adl);
		return out;
	}

	
}
