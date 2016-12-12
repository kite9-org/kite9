package org.kite9.diagram.visualization.compaction.insertion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.compaction.AbstractSegmentModifier;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.planarization.Face;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This step requires that all the subgraphs being inserted are already
 * rectangularized, as this removes any concave edges in the diagram.
 * 
 * This step effectively joins all the various inner and outer faces back
 * together again, to form a single diagram.
 * 
 * @author robmoffat
 * 
 */
public class SubGraphInsertionCompactionStep extends AbstractSegmentModifier implements CompactionStep, Logable {

	public SubGraphInsertionCompactionStep(CompleteDisplayer displayer) {
		super(displayer);
	}

	Kite9Log log = new Kite9Log(this);
	Tools t = new Tools();

	public void compactDiagram(Compaction c) {

		// builds a handy map of faces to dart faces
		Map<Face, DartFace> faceMap = new HashMap<Face, DartFace>();
		for (DartFace dartFace : c.getOrthogonalization().getFaces()) {
			Object underlying = dartFace.getUnderlying();
			if (underlying instanceof Face) {
				faceMap.put((Face) underlying, dartFace);
			}
		}

		List<Dart> newDarts = new ArrayList<Dart>();
		Collection<Face> done = new HashSet<>();

		// next, recurse through to go bottom up on the insertions
		for (DartFace dartFace : c.getOrthogonalization().getFaces()) {
			insertSubFaces(dartFace, faceMap, newDarts, done, c);
		}
	}

	private void insertSubFaces(DartFace dartFace, Map<Face, DartFace> faceMap, List<Dart> newDarts, Collection<Face> done, Compaction c) {
		if (dartFace == null) {
			throw new LogicException("Planarization error: dart face not present");
		}
		
		Face underlyingFace = dartFace.getUnderlying();

		if (underlyingFace.getContainedFaces().size() == 0) {
			return;
		}
		
		Segment[] border = c.getFaceSpace(dartFace);

		// get space for the darts to be inserted - this must be an empty
		// rectangle in the
		// rectangularization
		Set<Segment> topSeg = new LinkedHashSet<Segment>();
		Set<Segment> rightSeg = new LinkedHashSet<Segment>();
		Set<Segment> leftSeg = new LinkedHashSet<Segment>();
		Set<Segment> bottomSeg = new LinkedHashSet<Segment>();
		topSeg.add(border[0]);
		rightSeg.add(border[1]);
		bottomSeg.add(border[2]);
		leftSeg.add(border[3]);

		Direction directionOfInsertion = null;
		Map<Integer, DartFace> faceInsertionOrder = new HashMap<Integer, DartFace>();

		for (Face ef : underlyingFace.getContainedFaces()) {
			Direction returned = addLowestContainmentIndex(faceMap.get(ef), faceInsertionOrder);
			if (directionOfInsertion == null) {
				directionOfInsertion = returned;
			} else if (directionOfInsertion != returned) {
				throw new LogicException("Containment problem for " + ef);
			}
		}

		List<Integer> order = new ArrayList<Integer>(faceInsertionOrder.keySet());
		Collections.sort(order);
		boolean addedSomething = false;

		for (Integer i : order) {
			DartFace embeddedDartFace = faceInsertionOrder.get(i);
			if (!done.contains(embeddedDartFace.getUnderlying())) {
				log.send(log.go() ? null : "Inserting face: " + embeddedDartFace.getUnderlying().id + " into: " + dartFace.getUnderlying().id);
				log.send(log.go() ? null : "Inserting: \n\t\t " + embeddedDartFace + "\n     into: \n\t\t" + dartFace);
				
				insertSubFaces(embeddedDartFace, faceMap, newDarts, done, c);
	
				// find the segment border of the subgraph being inserted
				Set<Segment> lLimit = getLimits(embeddedDartFace, c.getVerticalSegments(), c.getVerticalVertexSegmentMap(),
						Direction.LEFT);
				Set<Segment> rLimit = getLimits(embeddedDartFace, c.getVerticalSegments(), c.getVerticalVertexSegmentMap(),
						Direction.RIGHT);
				Set<Segment> uLimit = getLimits(embeddedDartFace, c.getHorizontalSegments(), c
						.getHorizontalVertexSegmentMap(), Direction.UP);
				Set<Segment> dLimit = getLimits(embeddedDartFace, c.getHorizontalSegments(), c
						.getHorizontalVertexSegmentMap(), Direction.DOWN);
	
				if ((directionOfInsertion == null) || (directionOfInsertion == Direction.RIGHT)
						|| (directionOfInsertion == Direction.LEFT)) {
					separate(topSeg, uLimit, c.getVerticalVertexSegmentMap(), Direction.UP, c, newDarts);
					separate(bottomSeg, dLimit, c.getVerticalVertexSegmentMap(), Direction.DOWN, c, newDarts);
					separate(leftSeg, lLimit, c.getHorizontalVertexSegmentMap(), Direction.LEFT, c, newDarts);
					leftSeg = rLimit;
				} else {
					separate(topSeg, uLimit, c.getVerticalVertexSegmentMap(), Direction.UP, c, newDarts);
					separate(leftSeg, lLimit, c.getHorizontalVertexSegmentMap(), Direction.LEFT, c, newDarts);
					separate(rightSeg, rLimit, c.getHorizontalVertexSegmentMap(), Direction.RIGHT, c, newDarts);
					topSeg = dLimit;
				}
				
				addedSomething = true;
				done.add(embeddedDartFace.getUnderlying());
			}
		}

		if (addedSomething) {
			if ((directionOfInsertion == Direction.DOWN) || (directionOfInsertion == Direction.UP)) {
				separate(bottomSeg, topSeg, c.getVerticalVertexSegmentMap(), Direction.DOWN, c, newDarts);
			} else {
				separate(rightSeg, leftSeg, c.getHorizontalVertexSegmentMap(), Direction.RIGHT, c, newDarts);
			}
		}
	}

	/**
	 * Used for populating the faceInsertionOrder map, and working out the
	 * direction in which faces are inserted
	 */
	private Direction addLowestContainmentIndex(DartFace ef, Map<Integer, DartFace> faceInsertionOrder) {
		int out = Integer.MAX_VALUE;
		Direction outDir = null;

		for (DartDirection dd : ef.dartsInFace) {
			Object de = Tools.getUltimateElement(dd.getDart().getFrom());
			
			if (de instanceof Connected) {
				Container c = ((Connected)de).getContainer();
				
				if (c!=null) {
					List<DiagramElement> content = c.getContents();
					// since the collection is ordered, position is important
					int index = content.indexOf(de);
					if (index != -1) {
						out = Math.min(out, index);
						outDir = getDirectionOfInsertion(c.getLayout());
					} else {
						throw new LogicException("The contained object is not contained in the face or something?");
					}
				}
			}
		}

		faceInsertionOrder.put(out, ef);

		return outDir;
	}

	private Direction getDirectionOfInsertion(Layout layoutDirection) {
		if (layoutDirection==null)
			return null;
		
		switch (layoutDirection) {
		case HORIZONTAL: 
		case RIGHT:
		case LEFT:
			return Direction.RIGHT;
		case VERTICAL:
		case DOWN:
		case UP:
		case GRID:
			return Direction.DOWN;
			
		default:
			throw new LogicException("Wasn't expecting this direction: "+layoutDirection);
		}
	}

	public String getPrefix() {
		return "SGI ";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
