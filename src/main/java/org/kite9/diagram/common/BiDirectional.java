package org.kite9.diagram.common;

import org.kite9.diagram.position.Direction;

/**
 * This generic interface allows you to specify a bi-directional, optionally directed from-to relationships, where from and to are 
 * both objects of generic class X.
 * 
 * @author robmoffat
 *
 * @param <X>
 */
public interface BiDirectional<X> {

	public X getFrom();

	public X getTo();

	public void setFrom(X v);

	public void setTo(X v);

	/**
	 * Returns from, if to is the argument, or to if from is the argument.
	 * @param end
	 * @return
	 */
	public X otherEnd(X end);

	public boolean meets(BiDirectional<X> e);

	public boolean meets(X v);
	
	/**
	 * Indicates the layout of from/to for the bi-directional item.  If this is non-null, then it is describing
	 * the single direction it needs to flow in for the diagram.
	 */
	public Direction getDrawDirection();
	
	public Direction getDrawDirectionFrom(X from);
	
	public void setDrawDirection(Direction d);
	
	public void setDrawDirectionFrom(Direction d, X from);


}