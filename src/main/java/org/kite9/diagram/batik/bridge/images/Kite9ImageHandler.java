package org.kite9.diagram.batik.bridge.images;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;

import org.apache.batik.svggen.DefaultImageHandler;
import org.apache.batik.svggen.SVGGeneratorContext;
import org.apache.batik.svggen.SVGGraphics2DIOException;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.batik.format.ResourceReferencer;
import org.kite9.diagram.batik.format.ResourceReferencer.Reference;
import org.w3c.dom.Element;

public class Kite9ImageHandler extends DefaultImageHandler {
	
	private ResourceReferencer rr;
	
	private ParsedURL lastImageURL;
	
	public Kite9ImageHandler(ResourceReferencer rr) {
		this.rr = rr;
	}

	@Override
	protected void handleHREF(Image image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
		handleHREFInternal(imageElement);
	}

	@Override
	protected void handleHREF(RenderedImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
		handleHREFInternal(imageElement);
	}

	@Override
	protected void handleHREF(RenderableImage image, Element imageElement, SVGGeneratorContext generatorContext) throws SVGGraphics2DIOException {
		handleHREFInternal(imageElement);
	}

	private void handleHREFInternal(Element imageElement) {
		Reference ref = rr.getReference(lastImageURL);
		imageElement.setAttributeNS(XLINK_NAMESPACE_URI,
                XLINK_HREF_QNAME,ref.getUrl());
		this.lastImageURL = null;
	}

	public void setLastImageURL(ParsedURL lastImageURL) {
		this.lastImageURL = lastImageURL;
	}

}
