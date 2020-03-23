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

	public Object get(String key);
	
	public void set(String key, Object value);
	
	/**
	 * Means this is something we could store in the cache.
	 */
	boolean isValid(String key);

	public static Cache NO_CACHE = new Cache() {
		
		@Override
		public void set(String key, Object value) {
		}
		
		@Override
		public Object get(String key) {
			return null;
		}

		@Override
		public boolean isValid(String key) {
			return false;
		}
	};
}
