package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.model.Rectangular;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Turn;
import org.kite9.diagram.visualization.compaction.AbstractCompactionStep;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Compactor;
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.compaction.rect.PrioritizingRectangularizer.Match;
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.logging.LogicException;

/**
 * This class is responsible for 'completing' a dart diagram by ensuring that
 * each face is subdivided by darts into rectangles.
 * 
 * This is done because otherwise the algorithm for compaction cannot work out
 * exactly where to align the darts.
 * 
 * The way this works is by tracing around each face looking for LRR or RRL
 * patterns of angles between the segments in the face.
 * 
 * There are 5 {@link VertexTurn}s in a rectangularization. These are
 * labelled as follows:
 * 
 * <pre>
 *       |          <- (post - not changed)
 *        ------    <- (meets)
 *              |   <-  (link)
 *           ---    <-  (par)
 *          |       <- (extender)
 *          |
 * </pre>
 * 
 * The naming is because the extender is extended to the meets item. par is
 * clearly parallel to meets, while link is linking par to meets.
 * 
 * @author robmoffat
 * 
 */
public abstract class AbstractDartRectangularizer extends AbstractCompactionStep {

	public AbstractDartRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	boolean failfast = false;

	Tools t = new Tools();
	
	/**
	 * This ties off any loose ends in the diagram by extending the segments to
	 * meet each other. This prevents overlapping of darts in the diagram.
	 * overlapping.
	 */
	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		List<DartFace> faces = c.getDartFacesForRectangular(r);
		List<Dart> result = new ArrayList<Dart>();
		List<DartFace> orderedFaces = new ArrayList<DartFace>(faces);
		Collections.sort(orderedFaces);

		for (DartFace df : orderedFaces) {
			log.send(log.go() ? null : "FACE: " + df);
			// first, add all the segments to the stack in unrectangularized
			// form
			List<VertexTurn> theStack = new ArrayList<VertexTurn>();
			List<DartDirection> turns = df.dartsInFace;
			Vertex from = df.getStartVertex();
			buildStack(df, theStack, turns, from, c);

			performFaceRectangularization(c, result, theStack);
			rebuildFaceDarts(theStack, df);

			if (!df.outerFace) {

				if (theStack.size() != 4) {
					throw new LogicException("Rectangularization did not complete properly - stack > 4, face = "+df.getUnderlying());
				}
				
				for (int i = 0; i < theStack.size(); i++) {
//					fixDartSize(c, getIthElementRotating(theStack, i).getUnderlying());
				}
			}
			
			setSlideableFaceRectangle(c, df, theStack, df.outerFace);
		}

	}

	private void rebuildFaceDarts(List<VertexTurn> theStack, DartFace df) {
//		df.dartsInFace.clear();
//		for (VertexTurn vertexTurn : theStack) {
//			df.dartsInFace.add(new DartDirection(vertexTurn.getUnderlying(), vertexTurn.d));
//		}
	}

	private void buildStack(DartFace df, List<VertexTurn> theStack,
			List<DartDirection> turns, Vertex from, Compaction c) {
		Vertex origFrom = from;
		if (df.dartsInFace.size() > 2) {
			try {
				for (int i = 0; i < turns.size(); i++) {
					Dart dart = turns.get(i).getDart();
					Vertex to = dart.otherEnd(from);
					VertexTurn stackTop = theStack.size() > 0 ? theStack.get(theStack.size() - 1) : null;
					Segment segment = c.getSegmentForDart(dart);
					if (segment == null) {
						throw new LogicException("No segment for dart: " + dart);
					}
					if ((stackTop != null) && (segment == stackTop.getSegment())) {
						// we have two darts in the same segment
						VertexTurn vt = theStack.get(theStack.size() - 1);
						vt.resetEndsWith(c, to, dart.isChangeEarly(to), dart.getChangeCost());
					} else {
						VertexTurn vt = new VertexTurn(
							c, segment.getSlideable(), dart.getDrawDirectionFrom(from),
							dart.getChangeCost(), 
							from, 
							dart.otherEnd(from),
							dart.isChangeEarly(from), 
							dart.isChangeEarly(to));
						theStack.add(vt);
						log.send(log.go() ? null : "adding (" + (theStack.size()-1) + "): " + dart + "      " + vt);
					}
					from = to;
				}
			} catch (RuntimeException e) {
				log.send("DartFace Issue:"+df.dartsInFace);
				throw e;
			}

			// check first and last segments are not same
			while (theStack.get(0).getSegment() == theStack.get(theStack.size() - 1).getSegment()) {
				VertexTurn keep = theStack.get(0);
				VertexTurn lose = theStack.get(theStack.size() - 1);
//				keep.resetEndsWith(c, origFrom, changeEarly);startsWith = lose.startsWith;
//				keep.tempChangeCost = Math.max(keep.tempChangeCost, lose.tempChangeCost);
//				keep.setUnderlying(null);
				theStack.remove(theStack.size() - 1);
			}
		}
	}

	protected abstract void performFaceRectangularization(Compaction c, List<Dart> result, List<VertexTurn> theStack);

	private void setSlideableFaceRectangle(Compaction c, DartFace df, List<VertexTurn> theStack, boolean outer) {
		Rectangle<Slideable> r = new Rectangle<Slideable>(
				getSlideableInDirection(theStack, outer ? Direction.LEFT : Direction.RIGHT),
				getSlideableInDirection(theStack, outer ? Direction.UP : Direction.DOWN),
				getSlideableInDirection(theStack, outer ? Direction.RIGHT : Direction.LEFT), 
				getSlideableInDirection(theStack, outer ? Direction.DOWN : Direction.UP));
			

		c.createFaceSpace(df, r);
	}

	private Slideable getSlideableInDirection(List<VertexTurn> vt, Direction d) {
		for (int i = 0; i < vt.size(); i++) {
			VertexTurn prev = vt.get(( i + vt.size() -1 ) % vt.size());
			VertexTurn curr = vt.get(i);
			VertexTurn next = vt.get(( i + 1 ) % vt.size());
			
			if ((curr.getDirection() == d) && (prev.getDirection() != next.getDirection())) {
				return curr.getSlideable();
			}
		}		

		throw new LogicException("No turn in that direction");
	}

	/**
	 * Works out the direction of turn between one segment and the next
	 */
	public static Turn getTurn(VertexTurn t1, VertexTurn t2) {
		Direction thisDirection = t1.getDirection();
		Direction nextDirection = t2.getDirection();

		Turn change = thisDirection.getDirectionChange(nextDirection);
		return change;
	}

	public static <X> X getIthElementRotating(List<X> items, int i) {
		return items.get((i + items.size()) % items.size());
	}

	protected static VertexTurn getItemRotating(List<VertexTurn> stack, int index) {
		while (index < 0)
			index += stack.size();
		index = index % stack.size();
		return stack.get(index);
	}

	protected void performRectangularizationD(List<VertexTurn> stack, Compaction c, List<Dart> out, VertexTurn ext,
			VertexTurn par, VertexTurn link, VertexTurn meets) {
		logRectangularizationContext(ext, par, link, meets);
		Slideable first = ext.getEndsWith();
		Slideable to = meets.getStartsWith();
		Direction d2 = Direction.reverse(meets.getDirection());
		Direction d1 = ext.getDirection();

		performRectangularization(c, out, meets, link, par, ext, first, to, d1, d2);
		cutRectangleCorner(stack, par, link);
	}

	/**
	 * Given that fixing is in a rectangle, with sides of before and after,
	 * there may need to be a minimum length set on fixing.
	 * 
	 * This errs on the side of too large right now
	 */
	private void fixDartSize(Compaction c, Dart underlying) {
		Direction d = underlying.getDrawDirection();
		double newLength = Math.max(underlying.getLength(), getMinimumDistance(c, underlying.getFrom(), underlying.getTo(), d));
		underlying.setLength(newLength);
		log.send(log.go() ? null : "Fixing: "+underlying+" min length "+underlying.getLength());
	}

	protected void performRectangularizationA(List<VertexTurn> stack, Compaction c, List<Dart> out, VertexTurn meets,
			VertexTurn link, VertexTurn par, VertexTurn ext) {
		logRectangularizationContext(meets, link, par, ext);
		Vertex first = ext.startsWith;
		Vertex to = meets.endsWith;
		Direction d1 = Direction.reverse(ext.d);
		Direction d2 = meets.d;

		performRectangularization(c, out, meets, link, par, ext, first, to, d1, d2);
		cutRectangleCorner(stack, link, par);
	}
	

	/**
	 * A pop-out is where meets and par both need to be kept minimal. So, we
	 * introduce 3 new segments, replacing meets, par, link with a three sides
	 * of a new rectangle for which there are no edge limits
	 * 
	 * @param c
	 */
	protected VertexTurn performPopOut(Compaction c, List<Dart> out, VertexTurn meets, VertexTurn link, VertexTurn par,
			VertexTurn ext, Slideable parFrom, Slideable meetsFrom, List<VertexTurn> stack, Match m) {

		fixDartSize(c, link.getUnderlying());

		Orthogonalization o = c.getOrthogonalization();

		// create the replacement for link
		Segment newLinkSeg = c.newSegment(link.getSegment().getDimension());
		Vertex parV = c.createCompactionVertex(newLinkSeg, par.getSegment());
		Vertex meetsV = c.createCompactionVertex(newLinkSeg, meets.getSegment());
		Vertex startsWith = m == Match.A ? parV : meetsV;
		Vertex endsWith = m == Match.A ? meetsV : parV;
		Direction d = link.getDirection();
		VertexTurn newLinkTurn = new VertexTurn(newLinkSeg, c, link.getDirection(), );
		Dart dLink = o.createDart(parV, meetsV, null, m == Match.A ? link.d : Direction.reverse(link.d), link.getUnderlying().getLength());
		// dLink.setChangeCostChangeEarlyBothEnds(link.getUnderlying().getChangeCost());
		newLinkTurn.setUnderlying(dLink);
		newLinkTurn.d = link.d;

		int ci = stack.indexOf(link);
		stack.set(ci, newLinkTurn);

		// reverse the par segment
		Dart dPar = o.createDart(parFrom, parV, null, m == Match.A ? meets.d : Direction.reverse(meets.d), 0);
		fixDartSize(c, dPar);
		dPar.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
		par.d = Direction.reverse(par.d);
		if (m == Match.A) {
			par.endsWith = parV;
		} else {
			par.startsWith = parV;
		}
		par.setUnderlying(dPar);

		// reverse the meets segment
		Dart dMeets = o.createDart(meetsFrom, meetsV, null, m == Match.A ? meets.d : Direction.reverse(meets.d), 0);
		fixDartSize(c, dMeets);
		dMeets.setChangeCost(Dart.EXTEND_IF_NEEDED, null);
		meets.d = Direction.reverse(meets.d);
		if (m == Match.A) {
			meets.startsWith = meetsV;
		} else {
			meets.endsWith = meetsV;
		}
		meets.setUnderlying(dMeets);
		log.send(log.go() ? null : "Updated par: " + par);
		log.send(log.go() ? null : "Updated link: " + newLinkTurn);
		log.send(log.go() ? null : "Updated meets: " + meets);

		return newLinkTurn;
	}

	private void performRectangularization(Compaction c, List<Dart> out, VertexTurn meets, VertexTurn link,
			VertexTurn par, VertexTurn ext, Vertex first, Vertex to, Direction d1, Direction d2) {
		fixDartSize(c, link.getUnderlying());

		double newMeetsLength = calculateNewMeetsLength(meets, par);
		int newMeetsChangeCost = meets.getChangeCost();
		boolean meetsLengthKnown = meets.isLengthKnown() && par.isLengthKnown();
		//boolean meetsLengthKnown = false;
		
		double extensionLength = ext.getMinimumLength() + link.getMinimumLength(); 
		extendSegment(ext, meets, c, first, to, d1, out, d2, extensionLength, newMeetsLength, newMeetsChangeCost, meetsLengthKnown);
	}

	private double calculateNewMeetsLength(VertexTurn meets, VertexTurn par) {
		System.out.println("fix this");
		if (par.isLengthKnown()) {
			return Math.max(0, calculateLength(meets) - calculateLength(par));
		} else {
			return 0;
		}
	}

	private int calculateLength(VertexTurn vt) {
		return vt.getMinimumLength();
	}

	private void logRectangularizationContext(VertexTurn vt4, VertexTurn vt3, VertexTurn vt2, VertexTurn vt1) {
		log.send(log.go() ? null : "Context:");
		log.send(log.go() ? null : "vt4 " + vt4);
		log.send(log.go() ? null : "vt3 " + vt3);
		log.send(log.go() ? null : "vt2 " + vt2);
		log.send(log.go() ? null : "vt1 " + vt1);
	}

	/**
	 * After the new darts are added to create the rectangle, this snips off the
	 * old rectangle from the stack
	 */
	private void cutRectangleCorner(List<VertexTurn> stack, VertexTurn remove1, VertexTurn remove2) {
		stack.remove(remove1);
		stack.remove(remove2);
		log.send(log.go() ? null : "Removed: " + remove1);
		log.send(log.go() ? null : "Removed: " + remove2);

	}

	/**
	 * Extends the end of one segment so that it meets up with the end of the
	 * other. Returns the vertex that was added, which will be met by 3 darts
	 * 
	 * @param out
	 */
	private Vertex extendSegment(VertexTurn extender, VertexTurn meets, Compaction c, Vertex extendFrom,
			Vertex meetsFrom, Direction d, List<Dart> out, Direction segmentDirection, double extenderLength,
			double meetsDartLength, int meetsChangeCost, boolean meetsLengthKnwon) {
		// check that these two segments don't already meet
		checkSeparated(extender.getSegment(), meets.getSegment());

		Vertex newVertex = c.createCompactionVertex(extender.getSegment(), meets.getSegment());
		Orthogonalization o = c.getOrthogonalization();

		Dart dx = o.createDart(newVertex, extendFrom, null, d, extenderLength);
		Vertex changeEarlyEnd = extender.getUnderlying().isChangeEarly(extendFrom) ? extendFrom : null;
		dx.setChangeCost(extender.getUnderlying().getChangeCost(), changeEarlyEnd);
		log.send(log.go() ? null : "New Dart (extender) : " + dx + " extended : " + extender + " into " + meets + " with length "
				+ dx.getLength());
		extender.setUnderlying(dx);
		out.add(dx);

		Dart dm = o.createDart(newVertex, meetsFrom, null, segmentDirection, meetsDartLength);
		dm.setChangeCost(meetsChangeCost, null);
		dm.setVertexLengthKnown(meetsLengthKnwon);
		meets.setUnderlying(dm);
		log.send(log.go() ? null : "New Dart (meets) : " + dm + " with length " + dm.getLength());

		if (extender.startsWith == extendFrom) {
			extender.endsWith = newVertex;
		} else {
			extender.startsWith = newVertex;
		}

		// update vertex turns
		if (meets.startsWith != meetsFrom) {
			meets.startsWith = newVertex;
		} else {
			meets.endsWith = newVertex;
		}

		return newVertex;
	}

	private boolean checkSeparated(Segment extender, Segment meets) {
		for (Vertex v : extender.getVerticesInSegment()) {
			if (meets.getVerticesInSegment().contains(v)) {
				log.send(log.go() ? null : "These already meet " + extender + " /// " + meets);
				return true;
			}
		}

		return false;
	}

	public static List<Turn> patternA = createList(Turn.LEFT, Turn.RIGHT, Turn.RIGHT);
	public static List<Turn> patternD = createList(Turn.RIGHT, Turn.RIGHT, Turn.LEFT);

	private static List<Turn> createList(Turn... turns) {
		List<Turn> out = new ArrayList<Turn>();
		for (Turn t : turns) {
			out.add(t);
		}
		return out;
	}

	public String getPrefix() {
		return "RECT";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
