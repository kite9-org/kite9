package org.kite9.diagram.dom.processors.post;

import org.apache.batik.anim.dom.*;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.ImportRule;
import org.apache.batik.css.engine.Rule;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.gvt.GraphicsNode;
import org.apache.batik.util.Base64EncoderStream;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.batik.bridge.Kite9BridgeContext;
import org.kite9.diagram.batik.text.ExtendedSVGGeneratorContext;
import org.kite9.diagram.batik.text.ExtendedSVGGraphics2D;
import org.kite9.diagram.common.StreamHelp;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.ns.Kite9Namespaces;
import org.kite9.diagram.dom.processors.AbstractInlineProcessor;
import org.kite9.diagram.dom.processors.DiagramPositionProcessor;
import org.kite9.diagram.dom.processors.xpath.PatternValueReplacer;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGImageElement;

import java.awt.geom.AffineTransform;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.apache.batik.util.SVGConstants.SVG_G_TAG;
import static org.apache.batik.util.SVGConstants.SVG_NAMESPACE_URI;

/**
 * - Removes script tags
 * - Inlines CSS
 * - Inlines Images using data: url
 * 
 * @author robmoffat
 *
 */
public class Kite9InliningProcessor extends DiagramPositionProcessor {
	
	private final UserAgent ua;
	private final Kite9BridgeContext bridge;
	private final XMLHelper xmlHelper;

	public Kite9InliningProcessor(Kite9BridgeContext ec, PatternValueReplacer vr, UserAgent ua, XMLHelper xmlHelper) {
		super(ec, vr);
		this.ua = ua;
		this.bridge = ec;
		this.xmlHelper = xmlHelper;
	}
	
	@Override
	protected Element processTag(Element from) {
		if (from instanceof SVGOMTextElement) {
			// text elements are getting replaced with glyphs.
			Document d = from.getOwnerDocument();
			Element groupElem = d.createElementNS(SVG_NAMESPACE_URI, SVG_G_TAG);
			groupElem.setAttribute("rendered", "true");
			ExtendedSVGGeneratorContext genCtx = ExtendedSVGGeneratorContext.buildSVGGeneratorContext(d);
			ExtendedSVGGraphics2D g2d = new ExtendedSVGGraphics2D(genCtx, groupElem);
			GraphicsNode gn = bridge.getGraphicsNode(from);
			gn.paint(g2d);
			gn.setTransform(new AffineTransform());
			from.getParentNode().insertBefore(groupElem, from);
			from.getParentNode().removeChild(from);
			return groupElem;
		}
		if (from instanceof SVGOMStyleElement) {
			// the first style tag in the document will get replaced with all the styles
			Element out = from.getOwnerDocument().createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_STYLE_TAG);
			CSSEngine cssEngine = ((SVGOMDocument) from.getOwnerDocument()).getCSSEngine();
			SVGOMStyleElement style = (SVGOMStyleElement) from;
			StyleSheet ss = (StyleSheet) style.getCSSStyleSheet();
			StringBuilder rules = new StringBuilder();
			collateRules(ss, rules, cssEngine);
			
			String contents = rules.toString();
			
			out.setTextContent(contents);
			from.getParentNode().insertBefore(out, from);
			from.getParentNode().removeChild(from);
			return out;
		} else if (from instanceof SVGOMScriptElement) {
			from.getParentNode().removeChild(from);
			return null;
		} else {
			return super.processTag(from);
		}
	}

	private String encodeDataUrl(String uriStr, Element e)  {
		try {
			String baseURI = AbstractNode.getBaseURI(e);
			ParsedURL purl;
			if (baseURI == null) {
			    purl = new ParsedURL(uriStr);
			} else {
			    purl = new ParsedURL(baseURI, uriStr);
			}
			String mediaType = purl.getContentTypeMediaType();
			InputStream is = purl.openStream();
			return encodeContents(mediaType, is);
		} catch (IOException e1) {
			try {
				Document d = ua.getBrokenLinkDocument(e, uriStr, e1.getMessage());
				String out = xmlHelper.toXML(d, true);
				return encodeContents("image/svg+xml", new ByteArrayInputStream(out.getBytes()));
			} catch (IOException e2) {
				// not really sure what to do here...
				return e2.getMessage();
			}
		}
	}



	protected String encodeContents(String mediaType, InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		Base64EncoderStream b64e = new Base64EncoderStream(baos);
		StreamHelp.streamCopy(is, b64e, true);
		return "data:"+mediaType+";base64,"+new String(baos.toByteArray());
	}

	private void collateRules(Rule ss, StringBuilder rules, CSSEngine cssEngine) {
		if (ss instanceof ImportRule) {
			// make sure we expand imports
			collateRules((StyleSheet) ss, rules, cssEngine);
		} else {
			rules.append(ss.toString(cssEngine));
			rules.append("\n");
		}
	}

	private void collateRules(StyleSheet ss, StringBuilder rules, CSSEngine cssEngine) {
		for (int i = 0; i < ((StyleSheet) ss).getSize(); i++) {
			collateRules(((StyleSheet)ss).getRule(i), rules, cssEngine);
		} 
	}

	/**
	 * Here, we handle conversion of xlink:href images, to make sure they are data: 
	 * URLs.
	 */
	@Override
	public void processAttributes(Element from, Element context) {
		super.processAttributes(from, context);
		
		if (from instanceof SVGImageElement) {
			String source = ((SVGImageElement) from).getHref().getAnimVal();
			if (source.startsWith("data:")) {
				return;
			}
			
			
			String encodedData = encodeDataUrl(source, context);
			from.setAttributeNS(SVGConstants.XLINK_NAMESPACE_URI, SVGConstants.XLINK_HREF_ATTRIBUTE, encodedData);
		}
		
	}


	
}
