package org.kite9.diagram.batik.format;

import org.apache.batik.svggen.SVGGeneratorContext;
import org.w3c.dom.Document;

public class ExtendedSVGGeneratorContext extends SVGGeneratorContext {

	private ResourceReferencer resourceReferencer;
	private ElementNodeMapper elementNodeMapper;
	
	protected ExtendedSVGGeneratorContext(Document domFactory) {
		super(domFactory);
	}
	
	public ResourceReferencer getResourceReferencer() {
		return resourceReferencer;
	}


	public void setResourceReferencer(ResourceReferencer resourceReferencer) {
		this.resourceReferencer = resourceReferencer;
	}


	public ElementNodeMapper getElementNodeMapper() {
		return elementNodeMapper;
	}


	public void setElementNodeMapper(ElementNodeMapper elementNodeMapper) {
		this.elementNodeMapper = elementNodeMapper;
	}


}
