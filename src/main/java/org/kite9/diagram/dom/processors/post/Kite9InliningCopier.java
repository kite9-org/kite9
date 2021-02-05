package org.kite9.diagram.dom.processors.post;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.batik.anim.dom.SVGOMScriptElement;
import org.apache.batik.anim.dom.SVGOMStyleElement;
import org.apache.batik.bridge.UserAgent;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.ImportRule;
import org.apache.batik.css.engine.Rule;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.dom.AbstractNode;
import org.apache.batik.util.Base64EncoderStream;
import org.apache.batik.util.ParsedURL;
import org.apache.batik.util.SVGConstants;
import org.kite9.diagram.dom.XMLHelper;
import org.kite9.diagram.dom.elements.ADLDocument;
import org.kite9.diagram.dom.processors.xpath.ValueReplacer;
import org.kite9.diagram.common.StreamHelp;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGImageElement;

/**
 * - Removes script tags
 * - Inlines CSS
 * - Inlines Images using data: url
 * 
 * @author robmoffat
 *
 */
public class Kite9InliningCopier extends Kite9ExpandingCopier {
	
	private UserAgent ua;

	public Kite9InliningCopier(String newPrefix, Document destination, ValueReplacer vr, UserAgent ua) {
		super(newPrefix, destination, vr);
		this.ua = ua;
	}
	
	
	
	@Override
	protected Element processTag(Element from) {
		if (from instanceof SVGOMStyleElement) {
			Element out = getDestinationDocument().createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_STYLE_TAG);
			
			CSSEngine cssEngine = ((ADLDocument) from.getOwnerDocument()).getCSSEngine();
			SVGOMStyleElement style = (SVGOMStyleElement) from;
			StyleSheet ss = (StyleSheet) style.getCSSStyleSheet();
			StringBuilder rules = new StringBuilder();
			collateRules(ss, rules, cssEngine);
			
			String contents = rules.toString();
			
			out.setTextContent(contents);
			return out;
		} else if (from instanceof SVGOMScriptElement) {
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
				String out = new XMLHelper().toXML(d);
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
	protected void processAttributes(Element from, Element context) {
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
