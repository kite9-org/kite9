package org.kite9.diagram.dom.css;

import java.io.IOException;
import java.util.List;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.SVG12CSSEngine;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.dom.cache.Cache;
import org.w3c.css.sac.InputSource;
import org.w3c.css.sac.SACMediaList;
import org.w3c.dom.Document;

/**
 * Provides functionality for caching loaded stylesheets
 * 
 * @author robmoffat
 *
 */
public class CachingCSSEngine extends SVG12CSSEngine {
	
	public class ScriptHandlingDocumentHandler extends StyleSheetDocumentHandler implements ScriptHandler {
		
		@Override
		public void importScript(String uri, SACMediaList ml) {
			styleSheet.append(new AtScriptRule(uri));
		}

		@Override
		public void setParam(String name, String value) {
			styleSheet.append(new AtParamsRule(name, value));
		}

		@Override
		public void setParam(String name, List<String> additionalValues) {
			styleSheet.append(new AtParamsRule(name, additionalValues));
		}
	}

	private Cache cache;

	public CachingCSSEngine(Document doc, ParsedURL uri, ExtendedParser p, ValueManager[] vms, ShorthandManager[] sms,
			CSSContext ctx, Cache cache) {
		super(doc, uri, p, vms, sms, ctx);
		this.cache = cache;
		this.styleSheetDocumentHandler = new ScriptHandlingDocumentHandler();
	}

	@Override
	protected void parseStyleSheet(StyleSheet ss, InputSource is, ParsedURL uri) throws IOException {
		String cssUri = is.getURI();
		StyleSheet existing = (StyleSheet) cache.get(cssUri);
		if (existing != null) {
			for (int i = 0; i < existing.getSize(); i++) {
				ss.append(existing.getRule(i));
			}
		} else {
			super.parseStyleSheet(ss, is, uri);
			cache.set(cssUri, ss);
		}
	}
	
	
}
