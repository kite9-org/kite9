package org.kite9.diagram.batik.format;

import org.apache.batik.svggen.DefaultErrorHandler;
import org.apache.batik.svggen.DefaultStyleHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGIDGenerator;
import org.kite9.diagram.batik.bridge.images.ResourceReferencerImageHandler;
import org.w3c.dom.Document;

public class ExtendedSVGGeneratorContext extends SVGGeneratorContext {

	private ResourceReferencer resourceReferencer;
	private ElementNodeMapper elementNodeMapper;
	
	public ExtendedSVGGeneratorContext(Document domFactory) {
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

	/**
	 * Helper method to create an <code>SVGGeneratorContext</code> from the
	 * constructor parameters.
	 */
	public static ExtendedSVGGeneratorContext buildSVGGeneratorContext(Document domFactory, ResourceReferencer rr, ElementNodeMapper enm) {
		ExtendedSVGGeneratorContext generatorCtx = new ExtendedSVGGeneratorContext(domFactory);
		generatorCtx.setIDGenerator(new SVGIDGenerator());
		generatorCtx.setExtensionHandler(new BatikPaintExtensionHandler());
		generatorCtx.setImageHandler(new ResourceReferencerImageHandler(rr));
		generatorCtx.setStyleHandler(new DefaultStyleHandler());
		generatorCtx.setComment("Generated by the Batik Graphics2D SVG Generator");
		generatorCtx.setErrorHandler(new DefaultErrorHandler());
		// generatorCtx.setResourceReferencer(rr);
		// generatorCtx.setElementNodeMapper(enm);

		return generatorCtx;
	}
}
