package org.kite9.diagram.common.algorithms.ssp;

import java.util.NoSuchElementException;

import org.kite9.diagram.logging.Logable;

/**
 * Abstract implementation of the Dijkstra Successive Shortest Path algorithm.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractSSP<P extends PathLocation<P>> implements Logable {

	protected Object getLocation(P path) {
		return path.getLocation();
	}

	public P createShortestPath() throws NoFurtherPathException {
		State<P> s = createState();
		lastState = s;
		try {
			createInitialPaths(s);
			while (true) {
				P r = null; 
				do {
					r = s.remove();
				} while (!r.isActive());
				
				if (pathComplete(r)) {
					return r;
				}

				generateSuccessivePaths(r, s);
			}
		} catch (NoSuchElementException e) {
			
			throw new NoFurtherPathException();
		}
	}

	protected State<P> createState() {
		return new State<P>(this);
	}

	/**
	 * Returns true if the path arrives at its destination
	 */
	protected abstract boolean pathComplete(P r);

	/**
	 * Generates successive valid paths from r, and adds them to the list of
	 * paths.
	 */
	protected abstract void generateSuccessivePaths(P r, State<P> s);

	protected abstract void createInitialPaths(State<P> s);

	public String getPrefix() {
		return "SSP ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}
	
	private State<P> lastState;
	
	public State<P> getLastState() {
		return lastState;
	}
	

}
