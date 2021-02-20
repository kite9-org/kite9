package org.kite9.diagram.dom;

import java.net.URL;
import java.util.HashMap;

import org.apache.batik.anim.dom.SVG12DOMImplementation;
import org.apache.batik.css.dom.CSSOMSVGViewCSS;
import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.CSSEngine;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.css.parser.ExtendedParserWrapper;
import org.apache.batik.dom.AbstractStylableDocument;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.dom.cache.Cache;
import org.kite9.diagram.dom.css.CachingCSSEngine;
import org.kite9.diagram.dom.css.Kite9CSSParser;
import org.kite9.diagram.logging.Kite9Log;
import org.kite9.diagram.logging.Logable;
import org.w3c.css.sac.CSSException;
import org.w3c.css.sac.CSSParseException;
import org.w3c.css.sac.ErrorHandler;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Node;
import org.w3c.dom.css.CSSStyleSheet;
import org.w3c.dom.css.ViewCSS;
import org.w3c.dom.stylesheets.StyleSheet;

/**
 * Allows the use of caching when processing CSS.
 * 
 * @author robmoffat
 *
 */
public class CachingSVGDOMImplementation extends SVG12DOMImplementation  implements Logable {

	protected final Kite9Log log = Kite9Log.Companion.instance(this);
	protected final Cache cache;

	public CachingSVGDOMImplementation(Cache c) {
		super();
		this.cache = c;
	}

	@Override
	public String getPrefix() {
		return "ADOM";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	

	/**
	 * Allows us to carry on in the face of invalid css - happens a lot with noun project files.
	 */
	public CSSEngine createCSSEngine(AbstractStylableDocument doc, CSSContext ctx, ExtendedParser ep, ValueManager[] vms, ShorthandManager[] sms) {
		if (doc.getCSSEngine() != null) {
			return doc.getCSSEngine();
		}
		
		ParsedURL durl = null; // ((ADLDocument)doc).getParsedURL();
		
		ep = ExtendedParserWrapper.wrap(new Kite9CSSParser());
		
		CSSEngine result = new CachingCSSEngine(doc, durl, ep, vms, sms, ctx, cache);
		
		ep.setErrorHandler(new ErrorHandler() {
			
			private String getLocation(CSSParseException arg0) {
				return "("+arg0.getLineNumber()+","+arg0.getColumnNumber()+")";
			}
			
			@Override
			public void warning(CSSParseException arg0) throws CSSException {
				sendLogMessage("Warning:", arg0);
			}

			protected void sendLogMessage(String prefix, CSSParseException arg0) {
				String out = prefix+getLocation(arg0)+" "+arg0.getLocalizedMessage()+" "+arg0.getURI();
				log.send(out);
				System.err.println(out);
			}
			
			@Override
			public void fatalError(CSSParseException arg0) throws CSSException {
				sendLogMessage("Fatal:", arg0);
			}
			
			@Override
			public void error(CSSParseException arg0) throws CSSException {
				sendLogMessage("Error: ", arg0);
			}
		});

		URL url = getClass().getResource("resources/UserAgentStyleSheet.css");
		if (url != null) {
			ParsedURL purl = new ParsedURL(url);
			InputSource is = new InputSource(purl.toString());
			result.setUserAgentStyleSheet(result.parseStyleSheet(is, purl, "all"));
		}

		return result;
	}
	

	@Override
	public ViewCSS createViewCSS(AbstractStylableDocument doc) {
        return new CSSOMSVGViewCSS(doc.getCSSEngine());
	}
	

	public CSSStyleSheet createCSSStyleSheet(String title, String media) throws DOMException {
        throw new UnsupportedOperationException("StyleSheetFactory.createCSSStyleSheet is not implemented"); // XXX
	}


	public StyleSheet createStyleSheet(Node node, HashMap<String, String> attrs) {
        throw new UnsupportedOperationException("StyleSheetFactory.createStyleSheet is not implemented"); // XXX
	}
	

}