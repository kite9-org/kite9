package org.kite9.diagram.dom.css;

import java.io.IOException;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.SVG12CSSEngine;
import org.apache.batik.css.engine.StyleSheet;
import org.apache.batik.css.engine.value.ShorthandManager;
import org.apache.batik.css.engine.value.ValueManager;
import org.apache.batik.css.parser.ExtendedParser;
import org.apache.batik.util.ParsedURL;
import org.kite9.diagram.dom.cache.Cache;
import org.w3c.css.sac.InputSource;
import org.w3c.dom.Document;

/**
 * Provides functionality for caching loaded stylesheets
 * 
 * @author robmoffat
 *
 */
public class CachingCSSEngine extends SVG12CSSEngine {
	
	private Cache cache;

	public CachingCSSEngine(Document doc, ParsedURL uri, ExtendedParser p, ValueManager[] vms, ShorthandManager[] sms,
			CSSContext ctx, Cache cache) {
		super(doc, uri, p, vms, sms, ctx);
	}

	@Override
	protected void parseStyleSheet(StyleSheet ss, InputSource is, ParsedURL uri) throws IOException {
		// TODO Auto-generated method stub
		super.parseStyleSheet(ss, is, uri);
	}



}
