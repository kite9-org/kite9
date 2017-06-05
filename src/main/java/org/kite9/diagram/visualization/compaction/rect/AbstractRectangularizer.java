package org.kite9.diagram.visualization.compaction.rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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
import org.kite9.diagram.visualization.compaction.segment.Segment;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.DartFace;
import org.kite9.diagram.visualization.orthogonalization.DartFace.DartDirection;
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
public abstract class AbstractRectangularizer extends AbstractCompactionStep {

	public AbstractRectangularizer(CompleteDisplayer cd) {
		super(cd);
	}

	boolean failfast = false;
	
	/**
	 * This ties off any loose ends in the diagram by extending the segments to
	 * meet each other. This prevents overlapping of darts in the diagram.
	 * overlapping.
	 */
	@Override
	public void compact(Compaction c, Rectangular r, Compactor rc) {
		List<DartFace> faces = c.getOrthogonalization().getDartFacesForRectangular(r);
		
		if (faces == null) {
			return;
		}
		
		List<Dart> result = new ArrayList<Dart>();
		List<DartFace> orderedFaces = new ArrayList<DartFace>(faces);

		for (DartFace df : orderedFaces) {
			log.send(log.go() ? null : "FACE: " + df);
			// first, add all the segments to the stack in unrectangularized
			// form
			List<VertexTurn> theStack = new ArrayList<VertexTurn>();
			List<DartDirection> turns = df.getDartsInFace();
			Vertex from = df.getStartVertex();
			buildStack(df, theStack, turns, from, c);
			
			// ensures basic dart separation
			for (int i = 0; i < theStack.size(); i++) {
				fixSize(c, getIthElementRotating(theStack, i), 0, !df.outerFace);
			}

			performFaceRectangularization(c, result, theStack);

			if (!df.outerFace) {

				if (theStack.size() != 4) {
					throw new LogicException("Rectangularization did not complete properly - stack > 4, face = "+df);
				}
			}

			for (int i = 0; i < theStack.size(); i++) {
				fixSize(c, getIthElementRotating(theStack, i), 0, !df.outerFace);
			}
			
			setSlideableFaceRectangle(c, df, theStack, df.outerFace);
		}

	}

	private void buildStack(DartFace df, List<VertexTurn> theStack,
			List<DartDirection> turns, Vertex from, Compaction c) {
		if (df.getDartsInFace().size() > 2) {
			
			int startPoint = 0;
			for (int i = 0; i <  turns.size(); i++) {
				DartDirection last = turns.get((turns.size() -1 + i ) % turns.size());
				DartDirection current = turns.get(i);
				if (last.getDirection() != current.getDirection()) {
					startPoint = i;
					break;
				}
			}
			
			List<DartDirection> turnCopy = new ArrayList<>(turns);
			Collections.rotate(turnCopy, -startPoint);
			
			List<Segment> segments = turnCopy.stream().map(dd -> c.getSegmentForDart(dd.getDart())).collect(Collectors.toList());
			List<Direction> directions = turnCopy.stream().map(dd -> dd.getDirection()).collect(Collectors.toList());
					
			List<Segment> uniqueSegments = new ArrayList<>();
			List<Direction> uniqueDirections = new ArrayList<>();
			for (int i = 0; i < segments.size(); i++) {
				Segment segment = segments.get(i);
				Direction d = directions.get(i);
				if ((i == 0) ||  (segment != uniqueSegments.get(uniqueSegments.size()-1))) {
					uniqueSegments.add(segment);
					uniqueDirections.add(d);
				}
			}
			
			// convert to VertexTurns
			int us = uniqueSegments.size();
			for (int i = 0; i < us; i++) {
				
				Segment last = uniqueSegments.get((i-1+us) % us);
				Segment current = uniqueSegments.get(i);
				Segment next = uniqueSegments.get((i+1) % us);
				Direction d = uniqueDirections.get(i);
				
				VertexTurn t = new VertexTurn(c, current.getSlideable(), d, last.getSlideable(), next.getSlideable());
				theStack.add(t);
			}
		
			log.send("Stack for face "+df, theStack);
		}
	}

	protected abstract void performFaceRectangularization(Compaction c, List<Dart> result, List<VertexTurn> theStack);

	private void setSlideableFaceRectangle(Compaction c, DartFace df, List<VertexTurn> theStack, boolean outer) {
		Rectangle<Slideable<Segment>> r = new Rectangle<>(
				getSlideableInDirection(theStack, outer ? Direction.LEFT : Direction.RIGHT),
				getSlideableInDirection(theStack, outer ? Direction.UP : Direction.DOWN),
				getSlideableInDirection(theStack, outer ? Direction.RIGHT : Direction.LEFT), 
				getSlideableInDirection(theStack, outer ? Direction.DOWN : Direction.UP));
			

		c.createFaceSpace(df, r);
	}

	private Slideable<Segment> getSlideableInDirection(List<VertexTurn> vt, Direction d) {
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
		Slideable<Segment> first = ext.getEndsWith();
		Slideable<Segment> to = meets.getSlideable();
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
	protected void fixSize(Compaction c, VertexTurn link, double externalMin, boolean concave) {
		Slideable<Segment> early = link.getStartsWith();
		Slideable<Segment> late = link.getEndsWith();
		Segment early1 = (Segment) early.getUnderlying();
		Segment late1 = (Segment) late.getUnderlying();
		Direction d = link.getDirection();
		
		double minDistance = getMinimumDistance(Direction.isHorizontal(d), early1, late1,  link.getSegment(), concave);
		log.send(log.go() ? null : "Fixing: "+link+" min length "+minDistance);
		link.ensureLength(Math.max(minDistance, externalMin));
	
	}

	protected void performRectangularizationA(List<VertexTurn> stack, Compaction c, List<Dart> out, VertexTurn meets,
			VertexTurn link, VertexTurn par, VertexTurn ext) {
		logRectangularizationContext(meets, link, par, ext);
		Slideable<Segment> first = ext.getStartsWith();
		Slideable<Segment> to = meets.getSlideable();
		Direction d1 = Direction.reverse(ext.getDirection());
		Direction d2 = meets.getDirection();

		performRectangularization(c, out, meets, link, par, ext, first, to, d1, d2);
		cutRectangleCorner(stack, link, par);
	}
	

	protected void performRectangularization(Compaction c, List<Dart> out, VertexTurn meets, VertexTurn link,
			VertexTurn par, VertexTurn extender, Slideable<Segment> from, Slideable<Segment> to, Direction d1, Direction d2) {
		fixSize(c, link, 0, true);
		fixSize(c, par, 0, true);

		double newMeetsLength = calculateNewMeetsLength(meets, par);
		double extensionLength = extender.getMinimumLength() + link.getMinimumLength(); 

		if (extender.getStartsWith() == from) {
			extender.resetEndsWith(to);
		} else {
			extender.resetStartsWith(to);
		}
		
		// update meets
		if (meets.getStartsWith() == link.getSlideable()) {
			meets.resetStartsWith(extender.getSlideable());
		} else {
			meets.resetEndsWith(extender.getSlideable());
		}
		
		fixSize(c, meets, newMeetsLength, true);
		fixSize(c, extender, extensionLength, true);
	}

	protected double calculateNewMeetsLength(VertexTurn meets, VertexTurn par) {
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
