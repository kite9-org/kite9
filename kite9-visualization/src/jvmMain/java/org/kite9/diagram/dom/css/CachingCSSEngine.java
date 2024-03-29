package org.kite9.diagram.dom.css;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.batik.css.engine.CSSContext;
import org.apache.batik.css.engine.FontFaceRule;
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
 * Provides functionality for caching loaded stylesheets.
 *
 * @author robmoffat
 *
 */
public class CachingCSSEngine extends SVG12CSSEngine {


	private Cache cache;

	public CachingCSSEngine(Document doc, ParsedURL uri, ExtendedParser p, ValueManager[] vms, ShorthandManager[] sms,
			CSSContext ctx, Cache cache) {
		super(doc, uri, p, vms, sms, ctx);
		this.cache = cache;
	}

	@Override
	protected void parseStyleSheet(StyleSheet ss, InputSource is, ParsedURL uri) throws IOException {
		String cssUri = is.getURI();
		StyleSheet existing = cssUri != null ? cache.getStylesheet(cssUri) : null;
		if (existing != null) {
			for (int i = 0; i < existing.getSize(); i++) {
				ss.append(existing.getRule(i));
			}

			List<FontFaceRule> existingRules = cache.getFontFaceRules(cssUri);
			this.fontFaces.addAll(existingRules);

		} else {
			if (cssUri == null) {
				is.setURI(uri.toString());
			}

			int oldFonts = this.fontFaces.size();
			super.parseStyleSheet(ss, is, uri);
			int newFonts = this.fontFaces.size();
			cache.set(cssUri, Cache.STYLESHEET, ss);
			cache.set(cssUri, Cache.FONT_FACE_RULES, new ArrayList<>(this.fontFaces.subList(oldFonts, newFonts)));
		}
	}
	
}
