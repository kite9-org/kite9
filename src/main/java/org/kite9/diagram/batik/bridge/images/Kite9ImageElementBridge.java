package org.kite9.diagram.batik.bridge.images;

import java.awt.Graphics2D;

import org.apache.batik.bridge.BridgeContext;
import org.apache.batik.bridge.SVGImageElementBridge;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.gvt.ImageNode;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.batik.format.ExtendedSVG;
import org.kite9.diagram.batik.format.ExtendedSVGGraphics2D;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedString;
import org.w3c.dom.svg.SVGImageElement;

/**
 * Adds the url of the image to the graphics node so we can reference it later.
 * @author robmoffat
 *
 */
public class Kite9ImageElementBridge extends SVGImageElementBridge {

	/**
     * Sets the url on the GraphicsNode.
     */
	  public GraphicsNode createGraphicsNode(BridgeContext ctx, Element e) {
		URLEmbeddedImageNode out = (URLEmbeddedImageNode) super.createGraphicsNode(ctx, e);
        SVGImageElement ie = (SVGImageElement) e;
        SVGAnimatedString href = ie.getHref();
        if (href != null) {
			String uriStr = href.getAnimVal();
	        ParsedURL parsedURL = getParsedUrl(e, uriStr);
	        out.setUrl(parsedURL);
        }
        return out;
    }

	private ParsedURL getParsedUrl(Element e, String uriStr) {
        // Build the URL.
        String baseURI = AbstractNode.getBaseURI(e);
        ParsedURL purl;
        if (uriStr == null) {
        	return null;
        } else if (baseURI == null) {
            purl = new ParsedURL(uriStr);
        } else {
            purl = new ParsedURL(baseURI, uriStr);
        }
        return purl;
	}

	/**
	 * Sets the URL for the image as it goes.
	 * 
	 * This relies on the fact that the ImageNode represents (and contains) a single image.
	 * Even though it's technically a compound node.
	 *
	 */
	public static class URLEmbeddedImageNode extends ImageNode {
		
		private ParsedURL url;

		public ParsedURL getUrl() {
			return url;
		}

		public void setUrl(ParsedURL url) {
			this.url = url;
		}

		@Override
		public void primitivePaint(Graphics2D g2d) {
			if (g2d instanceof ExtendedSVG) {
				ExtendedSVGGraphics2D eg2d = (ExtendedSVGGraphics2D) g2d;
				((ResourceReferencerImageHandler) eg2d.getGeneratorContext().getImageHandler()).setLastImageURL(url);
			}
			
			super.primitivePaint(g2d);
		}
		
		
		
	}
	
	@Override
	protected GraphicsNode instantiateGraphicsNode() {
		return new URLEmbeddedImageNode();
	}

	
}
