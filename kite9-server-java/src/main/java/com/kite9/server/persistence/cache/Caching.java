package com.kite9.server.persistence.cache;

/**
 * Marks the api as something held in memory that we can evict 
 * if necessary
 * 
 * @author robmoffat
 *
 */
public interface Caching {

	/**
	 * Updates the cache from the backing store
	 */
	public void update();
	
	/**
	 * Returns true if the cache hasn't been used recently.
	 */
	public boolean canEvict();
	
	/**
	 * Returns the number of commits still to make in local store.
	 */
	public int getCommitCount();
	
	/**
	 * An ID that can be used 
	 */
}
