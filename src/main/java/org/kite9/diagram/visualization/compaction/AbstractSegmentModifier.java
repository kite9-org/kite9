package org.kite9.diagram.visualization.compaction;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.elements.DirectionEnforcingElement;
import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.xml.Context;
import org.kite9.diagram.xml.Link;
import org.kite9.diagram.xml.LinkLineStyle;
import org.kite9.framework.logging.LogicException;


/**
 * This contains utility methods to deal with insertion of sub-graphs within the overall graph
 *
 */
public class AbstractSegmentModifier {

	protected CompleteDisplayer displayer;

	public AbstractSegmentModifier(CompleteDisplayer displayer) {
		super();
		this.displayer = displayer;
	}
		
	protected double getMinimumDistance(Compaction c, Vertex from, Vertex to, Direction d) {
		boolean horizontalDart =  (d==Direction.LEFT) || (d==Direction.RIGHT);
		Map<Vertex, Segment> map = horizontalDart ? c.getVerticalVertexSegmentMap() : c.getHorizontalVertexSegmentMap();
		
		
		Segment froms = map.get(from);
		Segment tos = map.get(to);
		if (d==Direction.UP || d==Direction.LEFT) {
			// swap them round
			Segment temp = froms;
			froms = tos;
			tos = temp;
		}
		
		return getMinimumDistance(horizontalDart, froms, tos);
		
	}

	protected double getMinimumDistance(boolean horizontalDart, Segment froms,
			Segment tos) {
		DiagramElement fromde = froms.underlying;
		DiagramElement tode = tos.underlying;
		Direction fromUnderlyingSide = froms.underlyingSide;
		Direction toUnderlyingSide = tos.underlyingSide;
		
		if (!needsLength(fromde, tode)) {
			return 0;
		}
		
		// side checking
		if ((fromde instanceof Contained) && (tode instanceof Contained)) {
			if ((fromUnderlyingSide!=null) && (toUnderlyingSide!=null)) {
				if (fromUnderlyingSide==toUnderlyingSide) {
					// check whether there is containment
					boolean containment =  contains(fromde, tode);
					
					if (!containment) {
						return 0;
					}
					
				}
			}
		}
		
		return displayer.getMinimumDistanceBetween(fromde, fromUnderlyingSide, tode, toUnderlyingSide, horizontalDart ? Direction.RIGHT : Direction.DOWN);
	}
	
	private int getDepth(DiagramElement de) {
		if (de instanceof Contained) {
			 Container c = ((Contained)de).getContainer();
			 if (c==null) {
				 return 0;
			 } else {
				 return getDepth(c)+1;
			 }
		} else {
			return 0;
		}
	}
	
	private DiagramElement moveUp(DiagramElement move, int toDepth, int cDepth) {
		while (cDepth > toDepth) {
			move = ((Contained)move).getContainer();
			cDepth--;
		}
		
		return move;
	}
 
	private boolean contains(DiagramElement a, DiagramElement b) {
		int ad = getDepth(a);
		int bd = getDepth(b);
		
		if ((ad < bd) && (a instanceof Container)) {
			// b might be in a
			b = moveUp(b, ad+1, bd);
			return ((Container)a).getContents().contains(b);
		} else if ((ad > bd) && (b instanceof Container)) {
			// a might be in b
			a = moveUp(a, bd+1, ad);
			return ((Container)b).getContents().contains(a);
		} else {
			return false;
		}
	}

	private boolean needsLength(DiagramElement a, DiagramElement b) {
		if ((a instanceof DirectionEnforcingElement) || (b instanceof DirectionEnforcingElement)) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Returns all segments at the extreme <direction> edge within the face.
	 */
	protected Set<Segment> getLimits(DartFace df, List<Segment> segs, Map<Vertex, Segment> map, Direction direction) {
		Set<Segment> out = new LinkedHashSet<Segment>();
		for (DartDirection dd : df.dartsInFace) {
			Dart d = dd.getDart();
			Vertex from = d.getFrom();
			Vertex to = d.getTo();
			Segment fs = map.get(from);
			Segment ts = map.get(to);
			if ((!out.contains(fs)) && (testSegment(direction, fs))) {
				out.add(fs);
			}
			if ((!out.contains(ts)) && (testSegment(direction, ts))) {
				out.add(ts);
			}
		}
		
		if (out.size()==0) {
			throw new LogicException("Could not find far-edge segment?? ");
		}
		
		return out;
	}

	/** 
	 * Tests that the segment has no darts in the direction given.
	 */
	private boolean testSegment(Direction dir, Segment possible) {
		for (Vertex v : possible.getVerticesInSegment()) {
			for (Edge e : v.getEdges()) {
				if (e instanceof Dart) {
					Dart d = (Dart) e;
					if (d.getDrawDirectionFrom(v)==dir) {
						return false;
					}
				}
			}
		}
		
		return true;
	}
	
	
	/**
	 * Makes sure that 2 sets of segments definitely wont be crossing each other, by adding darts to ensure a separation.
	 * To ensure rectangularization, both ends of the inside segments are linked to the outside. 
	 */
	protected void separate(Set<Segment> outside, Set<Segment> inside, Map<Vertex, Segment> map, Direction d, Compaction c, List<Dart> newDarts) {
		for (Segment s1 : outside) {
			for (Segment s2 : inside) {
				Direction oneWay = Direction.rotateClockwise(d);
				Vertex toExtend = getExtremeEnd(s2, oneWay);
				Segment transverse = map.get(toExtend);
				separate(toExtend, s1, transverse, d, newDarts, c);
				
				Direction otherWay = Direction.rotateAntiClockwise(d);
				Vertex toExtend2 = getExtremeEnd(s2, otherWay);
				if (toExtend2!=toExtend) { 
					Segment transverse2 = map.get(toExtend2);
					separate(toExtend2, s1, transverse2, d, newDarts, c);
				}
			}
		}
	}

	private Vertex getExtremeEnd(Segment s2, Direction oneWay) {
		for (Vertex v : s2.getVerticesInSegment()) {
			if (isExtremeVertex(oneWay, v))
				return v; 
		}
		
		throw new LogicException("For some reason, no extreme "+oneWay+" end on "+s2);
	}

	private boolean isExtremeVertex(Direction oneWay, Vertex v) {
		for (Edge e : v.getEdges()) {
			if (e instanceof Dart) {
				Dart d = (Dart) e;
				
				if (d.getDrawDirectionFrom(v)==oneWay) {
					//System.out.println(v+" has "+oneWay+" in "+d);
					return false;
				}
			}
		}
		
		//System.out.println(v+" is extreme "+oneWay);
		return true;
	}

	
	protected void separate(Vertex a, Segment to, Segment extend, Direction d, List<Dart> result, Compaction c) {
		if (to.getVerticesInSegment().contains(a)) {
			return;
		}
		
		Vertex rv = c.createCompactionVertex(to, extend);
		double length = getMinimumDistance(c, a, rv, d);
		Dart da = c.getOrthogonalization().createDart(a, rv, null, d, length);
		da.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
		result.add(da);
	}
	
	protected void mergeSegments(Segment s, Segment with, Compaction c) {
		for (Vertex v : s.getVerticesInSegment()) {
			with.addToSegment(v);
			c.getHorizontalVertexSegmentMap().put(v, with);
			c.getVerticalVertexSegmentMap().put(v, with);
			for (Iterator<Edge> iterator = v.getEdges().iterator(); iterator.hasNext();) {
				Edge e = (Edge) iterator.next();
				if ((e instanceof Dart) && (with.getVerticesInSegment().contains(e.otherEnd(v)))) {
					// remove the dart
					iterator.remove();
				}
			}
		}
	}
	
	protected boolean checkVisibility(Segment current) {
		DiagramElement underlying = current.getUnderlying();
		
		if (underlying == null) {
			return false;
		}
		
		if (underlying instanceof Link) {
			if (((Link)underlying).getStyle()==LinkLineStyle.INVISIBLE) {
				return false;
			}
		}
		
		if (underlying instanceof Context) {
			if (!((Context)underlying).isBordered()) {
				return false;
			}
		}
			
		return true;
	}
	
}
