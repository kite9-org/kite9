package org.kite9.diagram.visualization.compaction.position.optstep;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.adl.Label;
import org.kite9.diagram.adl.Leaf;
import org.kite9.diagram.common.algorithms.det.DetHashSet;
import org.kite9.diagram.common.algorithms.det.UnorderedSet;
import org.kite9.diagram.common.algorithms.so.OptimisationStep;
import org.kite9.diagram.common.algorithms.so.Slideable;
import org.kite9.diagram.common.elements.CornerVertex;
import org.kite9.diagram.common.elements.PositionAction;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.Rectangle;
import org.kite9.diagram.position.CostedDimension;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visualization.compaction.AbstractSegmentModifier;
import org.kite9.diagram.visualization.compaction.Compaction;
import org.kite9.diagram.visualization.compaction.Segment;
import org.kite9.diagram.visualization.compaction.Tools;
import org.kite9.diagram.visualization.compaction.position.SegmentSlackOptimisation;
import org.kite9.diagram.visualization.display.CompleteDisplayer;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.diagram.visualization.orthogonalization.Dart;
import org.kite9.diagram.visualization.orthogonalization.Orthogonalization;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * This considers the insertion of Link labels and container labels etc.
 * <p>
 * This approaches the problem by looking for a pair of perpendicular segments,
 * and a pair of directions, and working out how large the OrientedRectangle
 * could be from there.
 * </p>
 * <p>
 * Having done this, it works out the cost of inserting the label within this
 * region. It considers all the options of places to insert the label, and then
 * places it in the one with least cost.
 * </p>
 * 
 * 
 * @author robmoffat
 * 
 */
public class LabelInsertionOptimisationStep extends AbstractSegmentModifier implements OptimisationStep, Logable {

	int labelNumber = 0;

	private Kite9Log log = new Kite9Log(this);

	public LabelInsertionOptimisationStep(CompleteDisplayer displayer) {
		super(displayer);
	}

	public void optimise(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {
		xo.updatePositionalOrdering();
		yo.updatePositionalOrdering();
		processContainerLabels(c, xo, yo);
		processConnectionLabels(c, xo, yo);
	}

	/**
	 * Given a set of rectangles as options for where to place a label, this
	 * chooses the best one and puts it there.
	 */
	private void placeLabel(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo, Label l, List<Comb> options,
			boolean container) {
		Comb r = getBestOrientedRectangle(l, options, xo, yo, container);

		if (r != null) {
			Dimension2D space = r.getSpaceNeeded();
			Rectangle<Vertex> labelVertices = createOrthogonalShape(c.getOrthogonalization(), l, r.getPadding());
			insertSlideables(r, labelVertices, xo, yo, c, l, space);

			RectangleRenderingInformation rri = (RectangleRenderingInformation) l.getRenderingInformation();
			rri.setHorizontalJustification(r.getHPos());
			rri.setVerticalJustification(r.getVPos());

			xo.updateCanonicalOrdering();
			yo.updateCanonicalOrdering();

			xo.updatePositionalOrdering();
			yo.updatePositionalOrdering();
		}
	}
	
	public double getPadding(Slideable s, Direction d) {
		Segment seg = (Segment) s.getUnderlying();
		
		// needs to handle container internals and link width
		if (seg.getUnderlying() instanceof Container) {
			if (d == seg.getUnderlyingSide()) {
				return displayer.getPadding(seg.getUnderlying(), d);
			} else {
				return 0;
			}
		}
		
		if (seg.getUnderlying() instanceof Connection) {
			Connection c = (Connection) seg.getUnderlying();
			return displayer.getPadding(c, d); 
		}
		
		return 0;
	}

	public static final long DISTORT_GLYPH_COST = 10000;

	private Comb getBestOrientedRectangle(Label key, List<Comb> value, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo,
			boolean container) {

		Comb best = null;
		for (Comb orientedRectangle : value) {
			if (best == null) {
				best = orientedRectangle;
			} else if (best.getCost() > orientedRectangle.getCost()) {
				best = orientedRectangle;
			} else if (best.getCost() == orientedRectangle.getCost()) {
				if (!orientedRectangle.isAlignToZeroTyne()) {
					// pick the largest space
					if (best.getDartsCovered() < orientedRectangle.getDartsCovered()) {
						best = orientedRectangle;
					}
				} else {
					// pick the smallest space that will fit?
					if (best.getDartsCovered() > orientedRectangle.getDartsCovered()) {
						best = orientedRectangle;
					}
				}

			}
		}

		log.send(log.go() ? null : "Best rectangle: " + best + " " + best.getCost());
		return best;
	}

	/**
	 * Makes sure that the label will not overlap the top of a vertex.
	 */
	private boolean okUnderlying(Slideable a, Dart dart, Direction d) {
		if (dart == null) {
			return false; // no dart on side means we are not trying to label on
			// this side
		}

		if (dart.getUnderlying() == null)
			return true;

		if (((Segment) a.getUnderlying()).underlying instanceof Connected) {
			if (((Segment) a.getUnderlying()).underlyingSide != d) {
				return false;
			}
		}

		return true;
	}

	private void insertSlideables(Comb r, Rectangle<Vertex> labelVertices, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo,
			Compaction c, Label l, Dimension2D space) {

		Slideable baseline = r.getSpine();
		SegmentSlackOptimisation par = (SegmentSlackOptimisation) baseline.getSlackOptimisation();
		SegmentSlackOptimisation perp = xo == par ? yo : xo;

		// vertices added to the baseline
		Vertex baselineVertex1, baselineVertex2;

		// vertices added to the zero end of the comb
		Vertex zeroEndVertex1, zeroEndVertex2;

		// vertices added to the high end of the comb
		Vertex highEndVertex1, highEndVertex2;

		// vertices added on their own slideable
		Vertex slideableVertex1, slideableVertex2;

		boolean highEndIncreasingDirection = r.getTyneIncDirection() == Direction.DOWN
				|| r.getTyneIncDirection() == Direction.RIGHT;
		boolean slideableSideIncreasingDirection = r.getSpineDirection() == Direction.DOWN
				|| r.getSpineDirection() == Direction.RIGHT;

		int baselineOutDist;
		int tyneDist;

		switch (r.getSpineDirection()) {
		case DOWN:
			baselineVertex1 = labelVertices.getA();
			baselineVertex2 = labelVertices.getB();
			slideableVertex1 = labelVertices.getC();
			slideableVertex2 = labelVertices.getD();
			baselineOutDist = (int) space.getHeight();
			tyneDist = (int) space.getWidth();
			break;
		case UP:
			baselineVertex1 = labelVertices.getC();
			baselineVertex2 = labelVertices.getD();
			slideableVertex1 = labelVertices.getA();
			slideableVertex2 = labelVertices.getB();
			baselineOutDist = (int) space.getHeight();
			tyneDist = (int) space.getWidth();
			break;
		case LEFT:
			baselineVertex1 = labelVertices.getB();
			baselineVertex2 = labelVertices.getC();
			slideableVertex1 = labelVertices.getA();
			slideableVertex2 = labelVertices.getD();
			baselineOutDist = (int) space.getWidth();
			tyneDist = (int) space.getHeight();
			break;
		case RIGHT:
			baselineVertex1 = labelVertices.getA();
			baselineVertex2 = labelVertices.getD();
			slideableVertex1 = labelVertices.getB();
			slideableVertex2 = labelVertices.getC();
			baselineOutDist = (int) space.getWidth();
			tyneDist = (int) space.getHeight();
			break;
		default:
			throw new LogicException("Should always have baseline direction: " + r);
		}

		switch (r.getTyneIncDirection()) {
		case DOWN:
			zeroEndVertex1 = labelVertices.getA();
			zeroEndVertex2 = labelVertices.getB();
			highEndVertex1 = labelVertices.getC();
			highEndVertex2 = labelVertices.getD();
			break;
		case UP:
			zeroEndVertex1 = labelVertices.getC();
			zeroEndVertex2 = labelVertices.getD();
			highEndVertex1 = labelVertices.getA();
			highEndVertex2 = labelVertices.getB();
			break;
		case LEFT:
			zeroEndVertex1 = labelVertices.getB();
			zeroEndVertex2 = labelVertices.getC();
			highEndVertex1 = labelVertices.getA();
			highEndVertex2 = labelVertices.getD();
			break;
		case RIGHT:
			zeroEndVertex1 = labelVertices.getA();
			zeroEndVertex2 = labelVertices.getD();
			highEndVertex1 = labelVertices.getB();
			highEndVertex2 = labelVertices.getC();
			break;
		default:
			throw new LogicException("Should always have tyne direction: " + r);
		}

		// now we need to subdivide one way to add the label
		((Segment) baseline.getUnderlying()).addToSegment(baselineVertex1);
		((Segment) baseline.getUnderlying()).addToSegment(baselineVertex2);
		par.updateVertexSlidableMap(baseline);
		Slideable s2 = createSlideable(slideableVertex1, slideableVertex2, par, c, r.getHighEndSlideable(), r
				.getZeroEndSlideable(), l, r.getSpineDirection(), ((Segment) baseline.getUnderlying()).getDimension());

		if (slideableSideIncreasingDirection) {
			par.ensureMinimumDistance(baseline, s2, baselineOutDist, true);
		} else {
			par.ensureMinimumDistance(s2, baseline, baselineOutDist, true);
		}

		for (Slideable s : r.getOppositeSlideables()) {
			double padding = getPadding(s, Direction.reverse(r.getSpineDirection()));
			if (slideableSideIncreasingDirection) {
				par.ensureMinimumDistance(s2, s, (int) padding, true);
			} else {
				par.ensureMinimumDistance(s, s2, (int) padding, true);
			}
		}
		// now subdivide the other way
		((Segment) r.getZeroEndSlideable().getUnderlying()).addToSegment(zeroEndVertex1);
		((Segment) r.getZeroEndSlideable().getUnderlying()).addToSegment(zeroEndVertex2);
		perp.updateVertexSlidableMap(r.getZeroEndSlideable());
		if (r.isAlignToZeroTyne()) {
			// align to zero end
			Slideable sn = createSlideable(highEndVertex1, highEndVertex2, perp, c, s2, baseline, l, r
					.getTyneIncDirection(), ((Segment) r.getZeroEndSlideable().getUnderlying()).getDimension());

			if (highEndIncreasingDirection) {
				perp.ensureMinimumDistance(r.getZeroEndSlideable(), sn, tyneDist, true);
				perp.ensureMinimumDistance(sn, r.getHighEndSlideable(), 0, true);
			} else {
				perp.ensureMinimumDistance(r.getHighEndSlideable(), sn, 0, true);
				perp.ensureMinimumDistance(sn, r.getZeroEndSlideable(), tyneDist, true);
			}

		} else {
			// align in the middle
			((Segment) r.getHighEndSlideable().getUnderlying()).addToSegment(highEndVertex1);
			((Segment) r.getHighEndSlideable().getUnderlying()).addToSegment(highEndVertex2);
			perp.updateVertexSlidableMap(r.getHighEndSlideable());
			if (highEndIncreasingDirection) {
				perp.ensureMinimumDistance(r.getZeroEndSlideable(), r.getHighEndSlideable(), tyneDist, true);
			} else {
				perp.ensureMinimumDistance(r.getHighEndSlideable(), r.getZeroEndSlideable(), tyneDist, true);
			}
		}

	}

	private Slideable createSlideable(Vertex r1, Vertex r2, SegmentSlackOptimisation yo, Compaction c, Slideable end1,
			Slideable end2, Label l, Direction direction, PositionAction action) {
		Segment s = c.newSegment(action);
		s.addToSegment(r1);
		s.addToSegment(r2);

		Slideable s2 = new Slideable(yo, s, yo.getSegmentAlignStyle(s));
		yo.addSlideables(s2);
		s.setUnderlying(l);
		s.setUnderlyingSide(direction);

		c.createCompactionVertex((Segment) end1.getUnderlying(), s);
		c.createCompactionVertex((Segment) end2.getUnderlying(), s);
		log.send(log.go() ? null : "Created slideable: " + s2);
		return s2;
	}

	/**
	 * Creates a zero-size rectangle to eventually contain the label
	 */
	private Rectangle<Vertex> createOrthogonalShape(Orthogonalization o, Label l, DirectionalValues padding) {
		int ln = labelNumber;
		labelNumber++;
		LabelFrame lf = new LabelFrame(l, padding);
		CornerVertex tl = new CornerVertex("label_" + ln + "_tl", HPos.LEFT, VPos.UP, lf);
		CornerVertex tr = new CornerVertex("label_" + ln + "_tr", HPos.RIGHT, VPos.UP, lf);
		CornerVertex br = new CornerVertex("label_" + ln + "_br", HPos.RIGHT, VPos.DOWN, lf);
		CornerVertex bl = new CornerVertex("label_" + ln + "_bl", HPos.LEFT, VPos.DOWN, lf);
		o.getAllVertices().add(tl);
		o.getAllVertices().add(tr);
		o.getAllVertices().add(bl);
		o.getAllVertices().add(br);

		o.createDart(tl, tr, l, Direction.RIGHT, 0);
		o.createDart(bl, br, l, Direction.RIGHT, 0);
		o.createDart(tl, bl, l, Direction.DOWN, 0);
		o.createDart(tr, br, l, Direction.DOWN, 0);

		return new Rectangle<Vertex>(tl, tr, br, bl);
	}

	private void processContainerLabels(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {

		Map<Label, Set<Dart>> lowestDartsInContainer = new HashMap<Label, Set<Dart>>();
		Map<Label, Integer> lowestDartsInContainerLevel = new HashMap<Label, Integer>();

		for (Dart d : c.getOrthogonalization().getAllDarts()) {
			Object de = Tools.getUltimateElement(d);
			Vertex dFrom = d.getFrom();
			Vertex dTo = d.getTo();

			if (de instanceof Container) {
				Label l = ((Container) de).getLabel();
				if ((l != null) && (l.hasContent()) && ((Container)de).isBordered()) {
					Set<Dart> lowest = lowestDartsInContainer.get(l);
					Integer lowestPos = lowestDartsInContainerLevel.get(l);
					if (lowest == null) {
						lowest = new DetHashSet<Dart>();
						lowestDartsInContainer.put(l, lowest);
						lowestPos = Integer.MIN_VALUE;
					}

					Slideable s1 = yo.getVertexToSlidableMap().get(dFrom);
					Slideable s2 = yo.getVertexToSlidableMap().get(dTo);
					if (s2 == s1) {
						// we have a horizontal slideable
						Integer pos = s1.getPositionalOrder();
						if ((lowestPos == null) || (pos > lowestPos)) {
							lowest.clear();
							lowest.add(d);
							lowestDartsInContainerLevel.put(l, pos);
						} else if (pos == lowestPos) {
							lowest.add(d);
						}
					}
					RenderingInformation rri = (RenderingInformation) l.getRenderingInformation();
					rri.setRendered(true);
				} else if (l != null) {
					RenderingInformation rri = (RenderingInformation) l.getRenderingInformation();
					rri.setRendered(false);
				}
			}
		}

		// having established above the darts making the bottom edge of the
		// container, map
		for (Entry<Label, Set<Dart>> entry : lowestDartsInContainer.entrySet()) {
			List<Comb> possibles = new ArrayList<Comb>();
			for (Dart d : entry.getValue()) {
				Vertex dFrom = d.getFrom();
				Vertex dTo = d.getTo();
				if (d.getDrawDirectionFrom(dFrom) == Direction.RIGHT) {
					addContainerRectangles(possibles, entry.getKey(), dFrom, xo, yo);
				} else {
					addContainerRectangles(possibles, entry.getKey(), dTo, xo, yo);
				}
			}
			placeLabel(c, xo, yo, entry.getKey(), possibles, true);
		}

	}

	static class End {

		Connection de;
		Vertex point;

	}

	private void processConnectionLabels(Compaction c, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo) {

		// created to avoid concurrent mod problem
		List<Dart> existingDarts = new ArrayList<Dart>(c.getOrthogonalization().getAllDarts());

		for (Dart d : existingDarts) {
			Object de = Tools.getUltimateElement(d);
			Vertex dFrom = d.getFrom();
			Vertex dTo = d.getTo();
			
			boolean horizontal = (d.getDrawDirection()==Direction.LEFT) ||  (d.getDrawDirection()==Direction.RIGHT);

			if (de instanceof Connection) {
				processConnectionEnd(xo, yo, (Connection) de, dFrom, c, horizontal, displayer.getTerminatorReserved(((Connection) de).getFromDecoration(), (Connection) de));
				processConnectionEnd(xo, yo, (Connection) de, dTo, c, horizontal, displayer.getTerminatorReserved(((Connection) de).getToDecoration(), (Connection) de));
			}
		}

	}

	private void processConnectionEnd(SegmentSlackOptimisation xo, SegmentSlackOptimisation yo, Connection e, Vertex dartEnd, Compaction c, boolean aboveOnly, double terminatorReserve) {
		DiagramElement dartUnderlyer = dartEnd.getOriginalUnderlying();
		Label label = null;
		if (dartUnderlyer == e.getFrom()) {
			label = e.getFromLabel();
		}

		if (dartUnderlyer == e.getTo()) {
			label = e.getToLabel();
		}

		if ((label != null) && (label.hasContent())) {
			List<Comb> possibles = new ArrayList<Comb>();
			addILinkRectangles(possibles, label, dartEnd, xo, yo, !aboveOnly, terminatorReserve);
			placeLabel(c, xo, yo, label, possibles, false);
		}
	}

	private void addILinkRectangles(List<Comb> possibles, Label l, Vertex v, SegmentSlackOptimisation xo, SegmentSlackOptimisation yo, boolean allowBelow, double terminatorReserve) {

		Slideable sx = xo.getVertexToSlidableMap().get(v);
		Slideable sy = yo.getVertexToSlidableMap().get(v);

		Dart dUp = getIncidentDart(sx, sy, yo, false);
		Dart dDown = getIncidentDart(sx, sy, yo, true);
		Dart dLeft = getIncidentDart(sy, sx, xo, false);
		Dart dRight = getIncidentDart(sy, sx, xo, true);

		boolean includeTopRight = okUnderlying(sx, dUp, Direction.RIGHT) && okUnderlying(sy, dRight, Direction.UP);
		boolean includeTopLeft = okUnderlying(sx, dUp, Direction.LEFT) && okUnderlying(sy, dLeft, Direction.UP);
		boolean includeBottomRight = okUnderlying(sx, dDown, Direction.RIGHT) && okUnderlying(sy, dRight, Direction.DOWN) && allowBelow;
		boolean includeBottomLeft = okUnderlying(sx, dDown, Direction.LEFT) && okUnderlying(sy, dLeft, Direction.DOWN) && allowBelow;

		log.send(log.go() ? null : "Trying to place label: " + l);
		boolean horizontal = true;
		boolean vertical = true;

		// there are four possible positions for the label around the vertex.
		if (includeTopLeft) {
			if (horizontal) {
				List<Dart> leftU = getAbove(v, xo, yo, false);
				log.send(log.go() ? null : "From " + v + "going leftU: " + leftU);
				leftU.add(0, dUp);
				createCombs(possibles, sy, leftU, Direction.UP, Direction.LEFT, yo, xo, l, terminatorReserve);
			}

			if (vertical) {
				List<Dart> aboveL = getAbove(v, yo, xo, false);
				log.send(log.go() ? null : "From " + v + "going aboveL: " + aboveL);
				aboveL.add(0, dLeft);
				createCombs(possibles, sx, aboveL, Direction.LEFT, Direction.UP, xo, yo, l, terminatorReserve);
			}
		}

		if (includeTopRight) {
			if (horizontal) {
				List<Dart> rightU = getBelow(v, xo, yo, false);
				log.send(log.go() ? null : "From " + v + "going rightU: ", rightU);
				rightU.add(0, dUp);
				createCombs(possibles, sy, rightU, Direction.UP, Direction.RIGHT, yo, xo, l, terminatorReserve);
			}

			if (vertical) {
				List<Dart> aboveR = getAbove(v, yo, xo, true);
				log.send(log.go() ? null : "From " + v + "going aboveR: ", aboveR);
				aboveR.add(0, dRight);
				createCombs(possibles, sx, aboveR, Direction.RIGHT, Direction.UP, xo, yo, l, terminatorReserve);
			}
		}

		if (includeBottomRight) {
			if (horizontal) {
				List<Dart> rightD = getBelow(v, xo, yo, true);
				log.send(log.go() ? null : "From " + v + "going rightD: " + rightD);
				rightD.add(0, dDown);
				createCombs(possibles, sy, rightD, Direction.DOWN, Direction.RIGHT, yo, xo, l, terminatorReserve);
			}
			if (vertical) {
				List<Dart> belowR = getBelow(v, yo, xo, true);
				log.send(log.go() ? null : "From " + v + "going belowR: " + belowR);
				belowR.add(0, dRight);
				createCombs(possibles, sx, belowR, Direction.RIGHT, Direction.DOWN, xo, yo, l, terminatorReserve);
			}
		}

		if (includeBottomLeft) {
			if (horizontal) {
				List<Dart> leftD = getAbove(v, xo, yo, true);
				log.send(log.go() ? null : "From " + v + "going leftD: " + leftD);
				leftD.add(0, dDown);
				createCombs(possibles, sy, leftD, Direction.DOWN, Direction.LEFT, yo, xo, l, terminatorReserve);
			}
			if (vertical) {
				List<Dart> belowL = getBelow(v, yo, xo, false);
				log.send(log.go() ? null : "From " + v + "going belowL: " + belowL);
				belowL.add(0, dLeft);
				createCombs(possibles, sx, belowL, Direction.LEFT, Direction.DOWN, xo, yo, l, terminatorReserve);
			}
		}
	}

	private void createCombs(List<Comb> possibles, Slideable spine, List<Dart> tynes, Direction tyneDirection,
			Direction spineDirection, SegmentSlackOptimisation par, SegmentSlackOptimisation perp, Label l, double zeroEndReserved) {
		for (int i = 2; i <= tynes.size(); i++) {
			Comb c = new Comb(spine, tynes, i, tyneDirection, spineDirection, true, par, perp);
			costComb(c, l, zeroEndReserved);
			possibles.add(c);
		}
	}

	private void addContainerRectangles(List<Comb> possibles, Label l, Vertex v, SegmentSlackOptimisation xo,
			SegmentSlackOptimisation yo) {

		Slideable sx = xo.getVertexToSlidableMap().get(v);
		Slideable sy = yo.getVertexToSlidableMap().get(v);

		log.send(log.go() ? null : "Trying to place label: " + l);

		Dart startDart = getIncidentDart(sx, sy, yo, false);

		if (startDart != null) {

			List<Dart> rightU = getBelow(v, xo, yo, false);
			log.send(log.go() ? null : "From " + v + "going rightU: ", rightU);

			rightU.add(0, startDart);

			// since with the container we always want to take up the full
			// width, only the last dart is relevant
			Comb c = new Comb(sy, rightU, rightU.size(), Direction.UP, Direction.RIGHT, false, yo, xo);
			possibles.add(c);
			costComb(c, l, 0);
			log.send(log.go() ? null : "Added comb: " + c);
		}
	}

	private void costComb(Comb c, Label l, double zeroEndReserved) {
		// figure out size
		boolean horizComb = c.getSpineDirection() == Direction.UP | c.getSpineDirection() == Direction.DOWN;
		boolean backwards = c.getSpineDirection() == Direction.UP | c.getSpineDirection() == Direction.LEFT;

		// size of comb without moving anything
		double cx = Double.MAX_VALUE;
		double cy = Double.MAX_VALUE;
		boolean horizPenalty = false;
		boolean vertPenalty = false;
		
		double spinePadding = getPadding(c.getSpine(), Direction.reverse(c.getSpineDirection()));

		// figure out first dimension
		for (Slideable use : c.getOppositeSlideables()) {
			Integer d1n = !backwards ? c.getSpine().minimumDistanceTo(use) : use.minimumDistanceTo(c.getSpine());
			if (d1n == null) {
				d1n = 0; 
			} else {
				double oppositePadding = getPadding(use, c.getSpineDirection());
				d1n = Math.max(0, d1n - (int) spinePadding - (int) oppositePadding);
			}

			if (horizComb) {
				cy = Math.min(cy, d1n);
				vertPenalty = vertPenalty
						|| ((underlyingSide(use) == Direction.UP) && (c.spineDirection == Direction.DOWN));
				vertPenalty = vertPenalty
						|| ((underlyingSide(use) == Direction.DOWN) && (c.spineDirection == Direction.UP));
			} else {
				cx = Math.min(cx, d1n);
				horizPenalty = horizPenalty
						|| ((underlyingSide(use) == Direction.LEFT) && (c.spineDirection == Direction.RIGHT));
				horizPenalty = horizPenalty
						|| ((underlyingSide(use) == Direction.RIGHT) && (c.spineDirection == Direction.LEFT));
			}
		}

		// figure out second

		Slideable s1 = c.getZeroEndSlideable();
		double zeroEndPadding = getPadding(s1, Direction.reverse(c.getTyneIncDirection()));

		Slideable s2 = c.getHighEndSlideable();
		double highEndPadding = getPadding(s2, c.getTyneIncDirection());

		Slideable mins = s1.getPositionalOrder() < s2.getPositionalOrder() ? s1 : s2;
		Slideable maxs = s1.getPositionalOrder() < s2.getPositionalOrder() ? s2 : s1;
		Integer d2n = mins.minimumDistanceTo(maxs);
	
		if (d2n == null)
			// if two slideables are not related, then the min distance will
			// come back null.
			d2n = 0;

		if (horizComb) {
			cx = Math.max(0, d2n - zeroEndPadding - highEndPadding);
			horizPenalty = horizPenalty || (underlyingSide(mins) == Direction.LEFT);
			horizPenalty = horizPenalty || (underlyingSide(maxs) == Direction.RIGHT);
		} else {
			cy = Math.max(0, d2n - zeroEndPadding - highEndPadding);
			vertPenalty = vertPenalty || (underlyingSide(mins) == Direction.UP);
			vertPenalty = vertPenalty || (underlyingSide(maxs) == Direction.DOWN);
		}

		CostedDimension cost2 = displayer.size(l, new Dimension2D(cx, cy));

		// if we are expanding the shape, add any extra penalties from moving
		// slidables that we don't want to move
		if ((cost2.getWidth() > cx) && (horizPenalty)) {
			cost2.cost += 1000;
		}

		if ((cost2.getHeight() > cy) && (vertPenalty)) {
			cost2.cost += 1000;
		}

		c.setCost(cost2.cost);
	
		// this creates padding for the shape against the spine, zero and high-end slideables
		double[] paddings = new double[4];
		paddings[Direction.reverse(c.getSpineDirection()).ordinal()] = spinePadding;
		paddings[Direction.reverse(c.getTyneIncDirection()).ordinal()] = zeroEndPadding + zeroEndReserved;
		paddings[c.getTyneIncDirection().ordinal()] = highEndPadding;
		c.padding = new DirectionalValues(paddings);
		c.setSpaceNeeded(new Dimension2D(cost2.getWidth() + c.getPadding().getLeft() + c.padding.getRight(),
				cost2.getHeight() + c.getPadding().getTop() + c.getPadding().getBottom()));

	}

	private Direction underlyingSide(Slideable use) {
		if (((Segment) use.getUnderlying()).getUnderlying() instanceof Leaf) {
			return ((Segment) use.getUnderlying()).getUnderlyingSide();
		} else {
			return null;
		}
	}

	/**
	 * Returns darts intersecting with the position of line in the s direction,
	 * starting at the startSlideable
	 */
	private static List<Dart> getInDirection(Slideable startSlideable, Slideable line, SegmentSlackOptimisation s,
			SegmentSlackOptimisation perpDir, int increment, Boolean stopDir) {
		List<Dart> out = new ArrayList<Dart>();
		List<Slideable> canon = s.getPositionalOrder();
		int startIndex = canon.indexOf(startSlideable);
		int index = startIndex;
		do {

			index += increment;

			if (index == -1) {
				return out;
			}

			if (index == canon.size()) {
				return out;
			}

			Slideable possible = canon.get(index);
			Dart result = getIncidentDart(possible, line, perpDir, stopDir);
			if (result != null) {
				if (result.getUnderlying() == null) {
					out.add(result);
				} else {
					out.add(result);
					return out;

				}
			}

		} while (true);
	}

	/**
	 * Gets the Dart on slideable s which meets c. Returns dart with an
	 * underlying by preference, since these stop the positioner routine from
	 * looking further.
	 * 
	 * @param stopPos
	 * @return if a dart covers in such a way that progress is barred, stop.
	 */
	private static Dart getIncidentDart(Slideable s, Slideable c, SegmentSlackOptimisation perp, Boolean stopPos) {
		int cpos = c.getMinimumPosition();
		Dart result = null;

		for (Dart dart : ((Segment) s.getUnderlying()).getDartsInSegment()) {
			double spos = getPerpPosition(dart.getFrom(), perp);
			double epos = getPerpPosition(dart.getTo(), perp);
			double maxPos = Math.max(spos, epos);
			double minPos = Math.min(spos, epos);

			if (overlaps(minPos, maxPos, cpos) || hits(dart, c)) {
				if ((stopPos == null) || stopPos) {
					// dart going positive will stop
					if ((maxPos > cpos) || (dartDirectionFrom(dart, c) == 1)) {
						if (Tools.getUltimateElement(dart) != null) {
							result = dart;
							if (dart.getUnderlying() != null) {
								return dart;
							}
						}
					}
				}
				if ((stopPos == null) || (!stopPos)) {
					// dart going negative will stop
					if ((minPos < cpos) || (dartDirectionFrom(dart, c) == -1)) {
						if (Tools.getUltimateElement(dart) != null) {
							result = dart;
							if (dart.getUnderlying() != null) {
								return dart;
							}
						}
					}
				}
			}

		}

		return result;
	}

	private static boolean hits(Dart dart, Slideable c) {
		Set<Vertex> verticesInSegment = ((Segment) c.getUnderlying()).getVerticesInSegment();
		return verticesInSegment.contains(dart.getFrom()) || verticesInSegment.contains(dart.getTo());
	}

	/**
	 * Returns true if the dart starts one side of cpos and finishes the other
	 */
	private static boolean overlaps(double minPos, double maxPos, int cpos) {
		return (minPos < cpos) && (maxPos > cpos);
	}

	private static int dartDirectionFrom(Dart dart, Slideable c) {
		boolean fromInC = ((Segment)c.getUnderlying()).getVerticesInSegment().contains(dart.getFrom());
		boolean toInC = ((Segment) c.getUnderlying()).getVerticesInSegment().contains(dart.getTo());
		Direction d = null;
		if (fromInC) {
			d = dart.getDrawDirectionFrom(dart.getFrom());
		} else if (toInC) {
			d = dart.getDrawDirectionFrom(dart.getTo());
		} else {
			return 0;
		}
		if ((d == Direction.LEFT) || (d == Direction.UP)) {
			return -1;
		} else if ((d == Direction.RIGHT) || (d == Direction.DOWN)) {
			return 1;
		} else {
			throw new LogicException("Was expecting dart to have a direction");
		}
	}

	private static boolean occupiedBetween(Slideable slideable, Slideable p1, Slideable p2, SegmentSlackOptimisation perp) {
		double pos1 = Math.min(p1.getMinimumPosition(), p2.getMinimumPosition());
		double pos2 = Math.max(p2.getMinimumPosition(), p1.getMinimumPosition());

		for (Dart d : ((Segment) slideable.getUnderlying()).getDartsInSegment()) {
			if (d.getUnderlying() != null) {
				Vertex from = d.getFrom();
				Vertex to = d.getTo();
				Slideable sFrom = perp.getVertexToSlidableMap().get(from);
				double pfrom = sFrom.getMinimumPosition();
				Slideable sTo = perp.getVertexToSlidableMap().get(to);
				double pto = sTo.getMinimumPosition();
				double d1 = Math.min(pfrom, pto);
				double d2 = Math.max(pfrom, pto);

				boolean nooverlap = d2 <= pos1 || d1 >= pos2;

				if (!nooverlap) {
					return true;
				}
			}
		}

		return false;
	}

	private static int getPerpPosition(Vertex v, SegmentSlackOptimisation perp) {
		Slideable cs = perp.getVertexToSlidableMap().get(v);
		int cpos = cs.getMinimumPosition();

		return cpos;

	}

	private List<Dart> getBelow(Vertex v, SegmentSlackOptimisation s, SegmentSlackOptimisation perpDir, boolean stopPos) {
		return getInDirection(s.getVertexToSlidableMap().get(v), perpDir.getVertexToSlidableMap().get(v), s, perpDir,
				1, stopPos);
	}

	private List<Dart> getAbove(Vertex v, SegmentSlackOptimisation s, SegmentSlackOptimisation perpDir, boolean stopPos) {
		return getInDirection(s.getVertexToSlidableMap().get(v), perpDir.getVertexToSlidableMap().get(v), s, perpDir,
				-1, stopPos);
	}

	/**
	 * The comb models a space that a label can occupy.
	 */
	static class Comb {

		Slideable spine;

		public Slideable getSpine() {
			return spine;
		}

		List<Dart> tynes;

		int tyneCount = 0;

		Direction spineDirection;
		Direction tyneIncDirection;
		
		DirectionalValues padding;

		public DirectionalValues getPadding() {
			return padding;
		}

		public void setPadding(DirectionalValues padding) {
			this.padding = padding;
		}

		public Direction getTyneIncDirection() {
			return tyneIncDirection;
		}

		public Direction getSpineDirection() {
			return spineDirection;
		}

		public List<Dart> getTynes() {
			return tynes;
		}

		boolean alignToZeroTyne;

		public boolean isAlignToZeroTyne() {
			return alignToZeroTyne;
		}

		List<Slideable> opps;

		public List<Slideable> getOppositeSlideables() {
			if (opps != null)
				return opps;

			opps = new ArrayList<Slideable>(10);
			int slideableNo = spine.getPositionalOrder();
			int increment = (spineDirection == Direction.DOWN || spineDirection == Direction.RIGHT) ? 1 : -1;
			Set<Slideable> done = new UnorderedSet<Slideable>();

			do {
				slideableNo += increment;
				if ((slideableNo == -1) || (slideableNo == parallel.getPositionalOrder().size())) {
					break;
				}

				Slideable next = parallel.getPositionalOrder().get(slideableNo);
				if (!done.contains(next)) {
					if (occupiedBetween(next, getHighEndSlideable(), getZeroEndSlideable(), perp)) {
						opps.add(next);
						excludeAllDependents(next, done, increment);
					}
				}
			} while (true);

			return opps;
		}

		private void excludeAllDependents(Slideable next, Set<Slideable> done, int increment) {
			if (done.contains(next))
				return;

			done.add(next);

			List<Slideable> deps = increment == 1 ? next.getMinRight() : next.getMinLeft();
			for (Slideable slideable : deps) {
				excludeAllDependents(slideable, done, increment);
			}
		}

		public Slideable getZeroEndSlideable() {
			return perp.getVertexToSlidableMap().get(tynes.get(0).getFrom());
		}

		public Slideable getHighEndSlideable() {
			return perp.getVertexToSlidableMap().get(tynes.get(tyneCount - 1).getFrom());
		}

		private Comb(Slideable spine, List<Dart> tynes, int count, Direction spineDirection,
				Direction tyneIncDirection, boolean align, SegmentSlackOptimisation parallel, SegmentSlackOptimisation perp) {
			super();
			this.spine = spine;
			this.tynes = tynes;
			this.tyneCount = count;
			this.spineDirection = spineDirection;
			this.alignToZeroTyne = align;
			this.parallel = parallel;
			this.perp = perp;
			this.tyneIncDirection = tyneIncDirection;

			if (spine.getSlackOptimisation() != parallel) {
				throw new LogicException("Parallel direction not set correctly");
			}

		}

		SegmentSlackOptimisation parallel;
		SegmentSlackOptimisation perp;

		/**
		 * Cost associated with adding the label
		 */
		double cost;
		
		Dimension2D spaceNeeded;
		

		public Dimension2D getSpaceNeeded() {
			return spaceNeeded;
		}

		public void setSpaceNeeded(Dimension2D spaceNeeded) {
			this.spaceNeeded = spaceNeeded;
		}

		public int getDartsCovered() {
			return tyneCount;
		}

		public double getCost() {
			return cost;
		}

		public void setCost(double cost) {
			this.cost = cost;
		}

		@Override
		public String toString() {
			StringBuilder out = new StringBuilder();

			out.append("COMB:[");
			out.append(spineDirection);
			out.append(" ");
			out.append(spine);
			out.append("\n\t");
			for (int i = 0; i < tyneCount; i++) {
				out.append(tynes.get(i));
				out.append(" ");
			}
			return out.toString();
		}

		public HPos getHPos() {
			switch (spineDirection) {
			case UP:
			case DOWN:
				if (alignToZeroTyne) {
					if (tyneIncDirection == Direction.LEFT) {
						return HPos.RIGHT;
					} else if (tyneIncDirection == Direction.RIGHT) {
						return HPos.LEFT;
					} else {
						throw new LogicException("Unexpected tyne direction: " + tyneIncDirection);
					}
				} else {
					return null;
				}
			case LEFT:
				return HPos.RIGHT;
			case RIGHT:
				return HPos.LEFT;
			}

			throw new LogicException("No spine direction set");
		}

		public VPos getVPos() {
			switch (spineDirection) {
			case UP:
				return VPos.DOWN;
			case DOWN:
				return VPos.UP;
			case LEFT:
			case RIGHT:
				if (alignToZeroTyne) {
					if (tyneIncDirection == Direction.UP) {
						return VPos.DOWN;
					} else if (tyneIncDirection == Direction.DOWN) {
						return VPos.UP;
					} else {
						throw new LogicException("Unexpected tyne direction: " + tyneIncDirection);
					}
				} else {
					throw new LogicException("Wasn't expecting null for vpos");
				}
			}

			throw new LogicException("No spine direction set");
		}
	}

	public String getPrefix() {
		return "LABL";
	}

	public boolean isLoggingEnabled() {
		return true;
	}

}
