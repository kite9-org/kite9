package org.kite9.diagram.dom.cache;

import org.apache.batik.css.engine.StyleSheet;
import org.w3c.dom.Document;

/**
 * This stores {@link StyleSheet} and {@link Document} instances in memory to avoid excessive parsing.
 * 
 * @author robmoffat
 *
 */
public interface Cache {
	
	public static final String STYLESHEET = "stylesheet";
	public static final String DOCUMENT = "document";
	
	public Object get(String key, String type);

	public default Document getDocument(String key) {
		return (Document) get(key, DOCUMENT);
	}
	
	public default StyleSheet getStylesheet(String key) {
		return (StyleSheet) get(key, STYLESHEET);
	}
	
	public default byte[] getBytes(String key, String type) {
		return (byte[]) get(key, type);
	}
		
	public void set(String key, String type, Object value);
	
	/**
	 * Means this is something we could store in the cache.
	 */
	boolean isValid(String key);

	public static Cache NO_CACHE = new Cache() {
		
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
