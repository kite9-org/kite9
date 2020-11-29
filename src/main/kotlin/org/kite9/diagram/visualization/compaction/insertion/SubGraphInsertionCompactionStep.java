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

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.grid.GridTemporaryConnected;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.CompactionStep;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Embedding;
import org.kite9.diagram.visualization.compaction.FaceSide;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
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
public class SubGraphInsertionCompactionStep extends AbstractCompactionStep implements CompactionStep, Logable {

	public SubGraphInsertionCompactionStep(CompleteDisplayer cd) {
		super(cd);
	}

	Kite9Log log = new Kite9Log(this);


	@Override
	public void compact(Compaction c, Embedding r, Compactor rc) {
		log.send("Subgraph Insertion: "+r);
		Collection<DartFace> done = new HashSet<>();

		// next, recurse through to go bottom up on the insertions
		for (DartFace dartFace : r.getDartFaces()) {
			insertSubFaces(dartFace, done, c);
		}
	}

	private void insertSubFaces(DartFace dartFace, Collection<DartFace> done, Compaction c) {
		if (dartFace == null) {
			throw new LogicException("Planarization error: dart face not present");
		}
		
		if (dartFace.getContainedFaces().size() == 0) {
			return;  // nothing embedded
		}
		
		Rectangle<FaceSide> border = c.getFaceSpace(dartFace);
		
		if ((border == null) || (border == Compaction.DONE)) {
			// not been rectangularized or already done.
			return;
		}

		// get space for the darts to be inserted - this must be an empty
		// rectangle in the
		// rectangularization
		Slideable<Segment> top =border.getA().getMain();
		Slideable<Segment> right = border.getB().getMain();
		Slideable<Segment> bottom = border.getC().getMain();
		Slideable<Segment> left = border.getD().getMain();

		Direction directionOfInsertion = null;
		Map<Integer, DartFace> faceInsertionOrder = new HashMap<Integer, DartFace>();

		for (DartFace df : dartFace.getContainedFaces()) {
			Direction returned = addLowestContainmentIndex(df, faceInsertionOrder);
			if (directionOfInsertion == null) {
				directionOfInsertion = returned;
			} else if (directionOfInsertion != returned) {
				throw new LogicException("Containment problem for " + df);
			}
		}

		List<Integer> order = new ArrayList<Integer>(faceInsertionOrder.keySet());
		Collections.sort(order);
		if ((directionOfInsertion == Direction.LEFT) || (directionOfInsertion == Direction.UP)) {
			Collections.reverse(order);
		}
		boolean addedSomething = false;

		for (Integer i : order) {
			DartFace embeddedDartFace = faceInsertionOrder.get(i);
			if (!done.contains(embeddedDartFace)) {
				log.send(log.go() ? null : "Inserting: \n\t\t " + embeddedDartFace + "\n     into: \n\t\t" + dartFace);
					
				// find the segment border of the subgraph being inserted
				Rectangle<FaceSide> limits = c.getFaceSpace(embeddedDartFace);
				
				FaceSide uLimit = limits.getA();
				FaceSide rLimit = limits.getB();
				FaceSide dLimit = limits.getC();
				FaceSide lLimit = limits.getD();
				
				if ((directionOfInsertion == null) || (directionOfInsertion == Direction.RIGHT)
						|| (directionOfInsertion == Direction.LEFT)) {
					separate(top, uLimit);
					separate(dLimit, bottom);
					separate(left, lLimit);
					left = rLimit.getMain();
				} else {
					separate(top, uLimit);
					separate(left, lLimit);
					separate(rLimit, right);
					top = dLimit.getMain();
				}
				
				addedSomething = true;
				done.add(embeddedDartFace);
			}
		}

		if (addedSomething) {
			if ((directionOfInsertion == Direction.DOWN) || (directionOfInsertion == Direction.UP)) {
				separate(top, bottom);
			} else {
				separate(left, right);
			}
		}
		
		c.setFaceSpaceToDone(dartFace);
	}



	/**
	 * Used for populating the faceInsertionOrder map, and working out the
	 * direction in which faces are inserted
	 */
	private Direction addLowestContainmentIndex(DartFace ef, Map<Integer, DartFace> faceInsertionOrder) {
		int out = Integer.MAX_VALUE;
		Direction outDir = null;

		for (DartDirection dd : ef.getDartsInFace()) {
			for (DiagramElement de : dd.getDart().getDiagramElements().keySet()) {
				if (de instanceof Connected) {
					Container c = ((Rectangular)de).getContainer();
					
					if (c!=null) {
						List<DiagramElement> content = c.getContents();
						// since the collection is ordered, position is important
						int index = content.indexOf(de);
						if (index != -1) {
							out = Math.min(out, index);
							outDir = getDirectionOfInsertion(c.getLayout());
						} else if (de instanceof GridTemporaryConnected) {
							out = 1;
							outDir = Direction.DOWN;	// doesn't matter since there will only be one.
						} else {
							throw new LogicException("The contained object is not contained in the face or something?");
						}
					}
				}
			}
		}

		faceInsertionOrder.put(out, ef);

		return outDir;
	}
	
	/**
	 * Returns all segments at the extreme <direction> edge within the face.
	 */
	protected Set<Slideable<Segment>> getLimits(DartFace df, Map<Vertex, Slideable<Segment>> map, Direction direction) {
		Set<Slideable<Segment>> out = new LinkedHashSet<>(4);
		for (DartDirection dd : df.getDartsInFace()) {
			Dart d = dd.getDart();
			Vertex from = d.getFrom();
			Vertex to = d.getTo();
			Slideable<Segment> fs = map.get(from);
			Slideable<Segment> ts = map.get(to);
			if ((!out.contains(fs)) && (testSegment(direction, fs.getUnderlying()))) {
				out.add(fs);
			}
			if ((!out.contains(ts)) && (testSegment(direction, ts.getUnderlying()))) {
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
	protected boolean testSegment(Direction dir, Segment possible) {
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

	private Direction getDirectionOfInsertion(Layout layoutDirection) {
		if (layoutDirection==null)
			return null;
		
		switch (layoutDirection) {
		case HORIZONTAL: 
		case RIGHT:
			return Direction.RIGHT;
		case LEFT:
			return Direction.LEFT;
		case VERTICAL:
		case DOWN:
		case GRID:
			return Direction.DOWN;
		case UP:
			return Direction.UP;

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
