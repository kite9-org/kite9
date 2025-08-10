package org.kite9.diagram.dom.cache;

import org.apache.batik.css.engine.FontFaceRule;
import org.apache.batik.css.engine.StyleSheet;
import org.w3c.dom.Document;

import java.util.List;

/**
 * This stores {@link StyleSheet} and {@link Document} instances in memory to avoid excessive parsing.
 * 
 * @author robmoffat
 *
 */
public interface Cache {
	
	String STYLESHEET = "stylesheet";
	String DOCUMENT = "document";
	String FONT_FACE_RULES = "font-face-rules";
	
	Object get(String key, String type);

	default Document getDocument(String key) {
		return (Document) get(key, DOCUMENT);
	}
	
	default StyleSheet getStylesheet(String key) {
		return (StyleSheet) get(key, STYLESHEET);
	}

	default List<FontFaceRule> getFontFaceRules(String key) {
		return (List<FontFaceRule>) get(key, FONT_FACE_RULES);
	}

	default byte[] getBytes(String key, String type) {
		return (byte[]) get(key, type);
	}
		
	void set(String key, String type, Object value);
	
	/**
	 * Means this is something we could store in the cache.
	 */
	boolean isValid(String key);

	Cache NO_CACHE = new Cache() {
		
		@Override
		public void set(String key, String type, Object value) {
		}
		
		@Override
		public Object get(String key, String type) {
			return null;
		}

		@Override
		public boolean isValid(String key) {
			return false;
		}

		@Override
		public Document getDocument(String key) {
			return null;
		}

		@Override
		public StyleSheet getStylesheet(String key) {
			return null;
		}
	};
}
