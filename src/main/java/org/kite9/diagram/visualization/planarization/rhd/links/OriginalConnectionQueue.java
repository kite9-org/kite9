package org.kite9.diagram.visualization.planarization.rhd.links;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.NoSuchElementException;

import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.BiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Connection;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;

/**
 * Creates a queue of edges to embed in the planarization.  Since at the time of writing, I 
 * don't know the best order to introduce the edges, I am going to keep 3 lists: X-directed,
 * Y-directed and undirected.  The queue will then work out which one to give the client
 * at any given time.
 * 
 * @author robmoffat
 *
 */
public class OriginalConnectionQueue implements ConnectionManager, Logable {
	
	Kite9Log log = new Kite9Log(this);
	
	boolean hasContradictions = false;
	
	protected Collection<BiDirectional<Connected>> x;
	protected Collection<BiDirectional<Connected>> y;
	protected Collection<BiDirectional<Connected>> u;

	@Override
	public Iterator<BiDirectional<Connected>> iterator() {
		
		return new Iterator<BiDirectional<Connected>>() {
			Iterator<BiDirectional<Connected>> xi = x.iterator();
			Iterator<BiDirectional<Connected>> yi = y.iterator();
			Iterator<BiDirectional<Connected>> ui = u.iterator();
			
			int current = 0;

			@Override
			public boolean hasNext() {
				return xi.hasNext() || yi.hasNext() || ui.hasNext();
			}

			@Override
			public BiDirectional<Connected> next() {
				BiDirectional<Connected> out;
				if (xi.hasNext()) {
					out = xi.next();
					current = 0;
				} else if (yi.hasNext()) {
					out = yi.next();
					current = 1;
				} else if (ui.hasNext()) {
					out = ui.next();
					current = 2;
				} else {
					throw new NoSuchElementException();
				}
				
				return out;
			}

			@Override
			public void remove() {
				if (current == 0) {
					xi.remove();
				} else if (current == 1) {
					yi.remove();
				} else if (current == 2) {
					ui.remove();
				}
			}
			
		};
		
	}
	
	protected OriginalConnectionQueue() {
	}
	
	public OriginalConnectionQueue(RoutableHandler2D rh) {
		super();
		this.x = new LinkedHashSet<BiDirectional<Connected>>();
		this.y = new LinkedHashSet<BiDirectional<Connected>>();
		this.u = new LinkedHashSet<BiDirectional<Connected>>();
	}
		
	@Override
	public void handleLinks(CompoundGroup cg) {
		if (cg.getInternalLinkA() != null) {
			
			for (BiDirectional<Connected> c : cg.getInternalLinkA().getConnections()) {
				if (considerThis(c, cg)) {
					//checkForPositionContradiction(c);
					add(c);
				}
			}
		}
	}

	protected boolean considerThis(BiDirectional<Connected> c, CompoundGroup cg) {
		if (c instanceof Connection) {
			Direction d = c.getDrawDirection();
			return (((d == null) && (!u.contains(c))) 
					|| (((d == Direction.LEFT) || d == Direction.RIGHT) && cg.getAxis().isHorizontal())
					|| (((d == Direction.UP) || d == Direction.DOWN) && cg.getAxis().isVertical()));			
		} else {
			return false;
		}
	}


	public boolean add(BiDirectional<Connected> c2) {
		//System.out.println("Admitting: "+c2);
		Direction d = c2.getDrawDirection();
		boolean contradiction = (c2 instanceof Connection) && Tools.isConnectionContradicting((Connection) c2);
		
		if (contradiction) {
			hasContradictions = true;
		}
		
		if ((d == null) || (contradiction)) {
			u.add(c2);
			return true;
		} else {
			switch (d) {
			case UP:
			case DOWN:
				y.add(c2);
				return true;
			case LEFT:
			case RIGHT:
				x.add(c2);
				return true;
			}
		}
		
		return false;
	}


	@Override
	public boolean addAll(Collection<? extends BiDirectional<Connected>> c) {
		throw new UnsupportedOperationException();
	}


	@Override
	public void clear() {
		u.clear();
		x.clear();
		y.clear();
	}


	@Override
	public boolean contains(Object o) {
		return u.contains(o) || x.contains(o) || y.contains(o);
	}


	@Override
	public boolean containsAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean isEmpty() {
		return x.isEmpty() && y.isEmpty() && u.isEmpty();
	}


	@Override
	public boolean remove(Object o) {
		return x.remove(o) || y.remove(o) || u.remove(o);
	}


	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}


	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}


	@Override
	public int size() {
		return x.size() + y.size() + u.size();
	}


	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}


	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String getPrefix() {
		return "CQ  ";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

	@Override
	public boolean hasContradictions() {
		return hasContradictions;
	}

	@Override
	public String toString() {
		return x.toString()+"/"+y.toString()+"/"+u.toString();
	}

	
}
