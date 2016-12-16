package org.kite9.diagram.visualization.planarization.rhd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.functional.TestingEngine;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.HPos;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.position.VPos;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.grid.FracMapper;
import org.kite9.diagram.visualization.planarization.grid.FracMapperImpl;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.diagram.visualization.planarization.mapping.CornerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.router.RoutableReader;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.LeafGroup;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupResult;
import org.kite9.diagram.visualization.planarization.rhd.grouping.GroupingStrategy;
import org.kite9.diagram.visualization.planarization.rhd.grouping.directed.DirectedGroupAxis;
import org.kite9.diagram.visualization.planarization.rhd.grouping.generators.GeneratorBasedGroupingStrategyImpl;
import org.kite9.diagram.visualization.planarization.rhd.layout.DirectionLayoutStrategy;
import org.kite9.diagram.visualization.planarization.rhd.layout.LayoutStrategy;
import org.kite9.diagram.visualization.planarization.rhd.layout.MostNetworkedFirstLayoutQueue;
import org.kite9.diagram.visualization.planarization.rhd.links.BasicContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.ConnectionManager;
import org.kite9.diagram.visualization.planarization.rhd.links.ContradictionHandler;
import org.kite9.diagram.visualization.planarization.rhd.links.RankBasedConnectionQueue;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.PositionRoutingInfo;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Rob's Hierarchical Decomposition Planarization Builder is a 4-phase process:
 * <ul>
 * <li>grouping
 * <li>layout
 * <li>positioning 
 * <li>vertex ordering
 * </ul>
 * 
 * The grouping phase divides the orderables at each level into hierarchical groups
 * based on how many connections they have with other groups.
 * 
 * The layout phase takes a group and works out whether the two merge groups
 * within it should be placed horizontally or vertically within their container.
 * This means there are 4^c options for this merge where c is the number of
 * containers with parts of both groups.
 * 
 * Positioning is the process of assigning x/y positions to the leaf groups and containers.
 * 
 * Finally, the results of this are used to create the list of vertices to
 * return, with their positions set by the layout strategy.  We also add vertices for any container corners at this stage.
 * 
 * @author moffatr
 * 
 */
public abstract class RHDPlanarizationBuilder implements PlanarizationBuilder, Logable {


	private Kite9Log log = new Kite9Log(this);

	protected ElementMapper em;
	protected GridPositioner gridHelp;
	protected RoutableHandler2D rh;
	
	// temp workspace
	private double borderTrimAreaX = .25d; 
	private double borderTrimAreaY = .25d; 
	private FracMapper fracMapper = new FracMapperImpl();

	public RHDPlanarizationBuilder(ElementMapper em, GridPositioner gridHelp) {
		super();
		this.em = em;
		this.gridHelp = gridHelp;
	}
	
	public RoutableReader getRoutableReader() {
		return rh;
	}

	protected abstract Planarization buildPlanarization(Diagram c, List<Vertex> vertexOrder, Collection<BiDirectional<Connected>> initialUninsertedConnections, Map<Container, List<DiagramElement>> sortedContainerContents);

	public Planarization planarize(Diagram c) {
		final int[] elements = new int[1];

		new DiagramElementVisitor().visit(c, new VisitorAction() {

			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Connected) {
					elements[0]++;
				}
			}
		});
		boolean firstGo = true, redo = false;
		List<Vertex> out = new ArrayList<Vertex>(elements[0] * 2);
		ConnectionManager connections = null;
		Map<Container, List<DiagramElement>> sortedContainerContents = null;
		try {
			
			while (firstGo || redo) {
				redo = false;
				rh =  new PositionRoutableHandler2D();
				ContradictionHandler ch = new BasicContradictionHandler(em);
				GroupingStrategy strategy = new GeneratorBasedGroupingStrategyImpl(ch);
				
				// Grouping
				GroupPhase gp = new GroupPhase(log, c, elements[0], strategy, ch, gridHelp);
				GroupResult mr = strategy.group(gp);
				
				if (!log.go()) {
					log.send("Created Groups:", mr.groups());
				}

				if (mr.groups().size() > 1) {
					throw new LogicException("Should end up with a single group");
				} 
				
				Group topGroup = mr.groups().iterator().next();
					
				if (!log.go()) {
					outputGroupInfo(topGroup, 0);
				}
				
				// Layout
				LayoutStrategy layout = new DirectionLayoutStrategy(rh);
				layout.layout(gp, mr, new MostNetworkedFirstLayoutQueue(gp.groupCount));
				
				// positioning
				connections = new RankBasedConnectionQueue(rh);
				buildPositionMap(topGroup, connections);
				if (!log.go()) {
					outputGroupInfo(topGroup, 0);
				}
				if (connections.hasContradictions()) {
					if (!checkLayoutIsConsistent(c)) {
						if (firstGo) {
							log.send("Contradiction forces regroup");
							redo = true;
							continue;
						}
					}
				}
					
				// vertex ordering
				sortedContainerContents = new HashMap<Container, List<DiagramElement>>(gp.containerCount * 2);
				instantiateContainerVertices(c);
				buildVertexList(null, c, null, out, sortedContainerContents);
				sortContents(out, rh.getTopLevelBounds(true), rh.getTopLevelBounds(false));
				
				firstGo = false;
			}
		} finally {
			if (!log.go()) {
				TestingEngine.drawPositions(out, RHDPlanarization.class, "positions", "vertex.png");
			}
		}

		Planarization planOut = buildPlanarization(c, out, connections, sortedContainerContents);
		((RHDPlanarizationImpl)planOut).setRoutableReader(rh);
		
		return planOut;
	}

	/**
	 * This makes sure all the container vertices have the correct anchors before we position them.
	 */
	private void instantiateContainerVertices(DiagramElement c) {
		if (requiresCornerVertices(c)) {
			em.getCornerVertices(c);
			
			if (c instanceof Container) {
				for (DiagramElement de : ((Container)c).getContents()) {
					instantiateContainerVertices(de);
				}
			}
		}
	}

	/**
	 * Potentially expensive, but checks to make sure that none of the positions overlap.
	 */
	private boolean checkLayoutIsConsistent(Container c) {
		List<DiagramElement> contents = c.getContents();
		for (int i = 0; i < contents.size(); i++) {
			DiagramElement ci = contents.get(i);
			for (int j = 0; j < i; j++) {
				DiagramElement cj = contents.get(j);
				if (overlaps(ci, cj)) {
					log.error("Overlap in positions of: "+ci+"  "+cj);
					return false;
				}
			}
			
			if (ci instanceof Container) {
				if (!checkLayoutIsConsistent((Container) ci)) {
					return false;
				}
			}
		}		
		
		return true;
	}

	private boolean overlaps(DiagramElement a, DiagramElement b) {
		RoutingInfo ria = rh.getPlacedPosition(a);
		RoutingInfo rib = rh.getPlacedPosition(b);
		return (rh.overlaps(ria, rib));
	}


	public void outputGroupInfo(Group g, int spc) {
		StringBuilder sb = new StringBuilder(spc);
		for (int i = 0; i < spc; i++) {
			sb.append(" ");
		}
		DirectedGroupAxis axis = DirectedGroupAxis.getType(g);
		Layout l = g.getLayout();
		log.send(sb.toString() + g.getGroupNumber() +
				" "+ ((g instanceof LeafGroup) ? g.toString() : axis) 
				+ "   " + rh.getPlacedPosition(g)+"  "+l+" "+(g.getAxis().isLayoutRequired() ? "LR " : " ")
				+ ((g instanceof CompoundGroup) ? (((CompoundGroup)g).getA().groupNumber)+" "+(((CompoundGroup)g).getB().groupNumber) : "" ));
		
		if (g instanceof CompoundGroup) {		
//			if ((l==Layout.UP) || (l==Layout.LEFT)) {
//				outputGroupInfo(((CompoundGroup) g).getB(), spc+1);
//				outputGroupInfo(((CompoundGroup) g).getA(), spc+1);
//			} else {
				outputGroupInfo(((CompoundGroup) g).getA(), spc+1);
				outputGroupInfo(((CompoundGroup) g).getB(), spc+1);
//			}
		}
	}	
	
	
	public void explain(Group g, int spc) {
		StringBuilder sb = new StringBuilder(spc);
		for (int i = 0; i < spc; i++) {
			sb.append(" ");
		}
		DirectedGroupAxis axis = DirectedGroupAxis.getType(g);
		log.send(sb.toString() + g.getGroupNumber() + "   " + rh.getPlacedPosition(g));

		Group hParent = axis.horizParentGroup;
		Group vParent = axis.vertParentGroup;
		if (vParent != hParent) {
			if (hParent != null) {
				log.send(sb.toString()
						+ "horiz: "
						+ ((g == ((CompoundGroup) hParent).getB()) ? hParent.getLayout() : Layout.reverse(hParent
								.getLayout())));
				explain(hParent, spc + 1);
			}
			if (vParent != null) {
				log.send(sb.toString()
						+ "vert: "
						+ ((g == ((CompoundGroup) vParent).getB()) ? vParent.getLayout() : Layout.reverse(vParent
								.getLayout())));
				explain(vParent, spc + 1);
			}

		} else {
			if (vParent != null) {
				log.send(sb.toString()
						+ "pos: "
						+ ((g == ((CompoundGroup) vParent).getB()) ? vParent.getLayout() : Layout.reverse(vParent
								.getLayout())));
				explain(hParent, spc + 1);
			}
		}

	}	
	
	private void buildPositionMap(Group start, ConnectionManager connections) {
		if (start instanceof CompoundGroup) {
			CompoundGroup cg = (CompoundGroup) start;
			log.send("Processing Group: "+start.getGroupNumber());
			buildPositionMap(cg.getA(), connections);
			buildPositionMap(cg.getB(), connections);
			connections.handleLinks(cg);
		} else {
			LeafGroup lg = (LeafGroup) start;
			log.send("Processing Group: " + lg);
			// g is a leaf group.  can we place it?
			Connected l = lg.getContained();
			Container c = lg.getContainer();
			
			// sizing
			RoutingInfo ri = lg.getAxis().getPosition(rh, false);
			checkMinimumGridSizes(ri);
			if (l != null) {
				rh.setPlacedPosition(l, ri);
			}
			ensureContainerBoundsAreLargeEnough(ri, c, lg);
		}	
	}
	
	private void checkMinimumGridSizes(RoutingInfo ri) {
		if (ri instanceof PositionRoutingInfo) {
			PositionRoutingInfo pri = (PositionRoutingInfo) ri;
			borderTrimAreaX = Math.min(borderTrimAreaX, pri.getWidth() /4d);
			borderTrimAreaY = Math.min(borderTrimAreaY, pri.getHeight() /4d);
		}
	}

	private void ensureContainerBoundsAreLargeEnough(RoutingInfo ri, Container c, LeafGroup lg) {
		Connected l;
		while (c != null) {
			// make sure container bounds are big enough for the contents
			RoutingInfo cri = rh.getPlacedPosition(c);
			if (cri == null) {
				cri = rh.emptyBounds();
			}

			RoutingInfo cri2 = rh.increaseBounds(cri, ri);
			if (!cri2.equals(cri)) {
				log.send("Increased bounds of "+c+" to "+cri2+" due to "+lg);
			}
			rh.setPlacedPosition(c, cri2);
			l = (Connected) c;
			c = l.getContainer();
		}
	}

	/**
	 * Constructs the list of vertices in no particular order.
	 */
	private void buildVertexList(Connected before, DiagramElement c, Connected after, List<Vertex> out, Map<Container, List<DiagramElement>> sortedContainerContents) {
		if (requiresCornerVertices(c)) {
			CornerVertices cvs = em.getCornerVertices(c);
			RoutingInfo bounds = rh.getPlacedPosition(c);
			log.send("Placed position of container: "+c+" is "+bounds);
			setCornerVertexPositions(before, c, after, cvs, out);

			if (c instanceof Container) {
				Container container = (Container) c;
				if (container.getContents().size() > 0) {
					buildVertexListForContainerContents(out, container, sortedContainerContents);
				}
			}

		} else {
			RoutingInfo bounds = rh.getPlacedPosition(c);
			log.send("Placed position: "+c+" is "+bounds);
			Vertex v = em.getVertex((Connected) c);
			out.add(v);
			bounds = rh.narrow(bounds, borderTrimAreaX, borderTrimAreaY);
			v.setRoutingInfo(bounds);
			setPlanarizationHints(c, bounds);
		}
		return;
	}

	Map<DiagramElement, Boolean> hasConnections = new HashMap<>();
	
	private boolean hasConnections(DiagramElement c) {
		if (hasConnections.containsKey(c)) {
			return hasConnections.get(c);
		} 
		
		boolean has = false;
		
		if (c instanceof Connected) {
			has = ((Connected)c).getLinks().size() > 0;
		}
		
		if ((has == false) && (c instanceof Container)) {
			for (DiagramElement de : ((Container)c).getContents()) {
				if (hasConnections(de)) {
					has = true;
					break;
				}
			}
		}
		
		hasConnections.put(c, has);
		return has;
	}
	
	private boolean requiresCornerVertices(DiagramElement c) {
		if (c instanceof Diagram) {
			return true;
		}
		// does anything inside it have connections?
		if (c instanceof Container) {
			for (DiagramElement de : ((Container) c).getContents()) {
				if (hasConnections(de)) {
					return true;
				}
			}
		}
		
		// is it embedded in a grid?  If yes, use corners
		Layout l = c.getParent() == null ? null : ((Container) c.getParent()).getLayout();
		return (l == Layout.GRID);
	}


	public static final boolean CHANGE_CONTAINER_ORDER = true;

	private void buildVertexListForContainerContents(List<Vertex> out, Container container, Map<Container, List<DiagramElement>> sortedContainerContents) {
		boolean layingOut = container.getLayout() != null;
		List<DiagramElement> contents = container.getContents();
		
		if (layingOut) {
			// sort the contents so that we can connect the right elements together
			contents = getContainerContentsHolder(container.getLayout(), contents);
			
			Collections.sort(contents, new Comparator<DiagramElement>() {
				@Override
				public int compare(DiagramElement arg0, DiagramElement arg1) {
					return compareDiagramElements(arg0, arg1);
				}
			});
			
			sortedContainerContents.put(container, contents);
		} 
		
		Connected conBefore = null, current = null, conAfter = null;
		boolean start =true;
		Iterator<DiagramElement> iterator = contents.iterator();
		while (start || (current != null)) {
			conBefore = current;
			current = conAfter;
			conAfter = getNextConnected(iterator);
			if (current != null) {
				buildVertexList(conBefore, current, conAfter, out, sortedContainerContents);	
				start = false;
			}
		}
	}

	private Connected getNextConnected(Iterator<DiagramElement> iterator) {
		while (iterator.hasNext()) {
			DiagramElement de= iterator.next();
			if (de instanceof Connected) {
				return (Connected) de;
			}
		}
		
		return null;
	}

	private List<DiagramElement> getContainerContentsHolder(Layout ld, List<DiagramElement> contents) {
//		if (CHANGE_CONTAINER_ORDER) {
//			if ((ld == Layout.HORIZONTAL) || (ld == Layout.VERTICAL)) {
//				// we are going to modify the original diagram
//				return contents;
//			}
//		} 
		
		// leave original intact
		return new ArrayList<DiagramElement>(contents);
	} 

	@Deprecated
	private void setPlanarizationHints(DiagramElement c, RoutingInfo bounds) {
//		HintMap hints = c.getPositioningHints();
//		if (hints == null) {
//			hints = new HintMap();
//			c.setPositioningHints(hints);
//		}
//		rh.setHints(hints, bounds);
	}
	
	private void setCornerVertexPositions(Connected before, DiagramElement c, Connected after, CornerVertices cvs, List<Vertex> out) {
		Container within = c.getContainer();
		
		Layout l = within == null ? null : within.getLayout();
		
		int depth = em.getContainerDepth(c);
		double xs = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 1));
		double xe = borderTrimAreaX - (borderTrimAreaX / (double) (depth + 2));
		double ys = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 1));
		double ye = borderTrimAreaY - (borderTrimAreaY / (double) (depth + 2));
		
		RoutingInfo bounds;
		Bounds bx, by;
		if (l==Layout.GRID) {
			// use the bounds of the non-grid parent container.
			Container containerWithNonGridParent = MultiCornerVertex.getRootGridContainer(c);
			bounds = rh.getPlacedPosition(containerWithNonGridParent);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);				
		} else {
			bounds =  rh.getPlacedPosition(c);
			bx = rh.getBoundsOf(bounds, true);
			by = rh.getBoundsOf(bounds, false);
		}

		// set up frac maps to control where the vertices will be positioned
		OPair<Map<BigFraction, Double>> fracMaps = fracMapper.getFracMapForGrid(c, rh, em.getCornerVertices(c), bounds);
		Map<BigFraction, Double> fracMapX = fracMaps.getA();
		Map<BigFraction, Double> fracMapY = fracMaps.getB();
		
		if (c instanceof Connected) {
			
			// add extra vertices for connections to keep the layout
			if (l != null) {
				switch (l) {
				case UP:
				case DOWN:
				case VERTICAL:
					Direction d1 = compareDiagramElements((Connected) c, before) == 1 ? Direction.UP : Direction.DOWN;
					Direction d2 = compareDiagramElements((Connected) c, after) == 1 ? Direction.UP : Direction.DOWN;
					addExtraSideVertex((Connected) c, d1, before, cvs, bx, by, out, xs, xe, ys, ye, fracMapX, fracMapY);
					addExtraSideVertex((Connected) c, d2, after, cvs, bx, by, out, xs, xe, ys, ye, fracMapX, fracMapY);
					break;
				case LEFT:
				case RIGHT:
				case HORIZONTAL:
					d1 = compareDiagramElements((Connected) c, before) == 1 ? Direction.LEFT : Direction.RIGHT;
					d2 = compareDiagramElements((Connected) c, after) == 1 ? Direction.LEFT : Direction.RIGHT;
					addExtraSideVertex((Connected) c, d1, before, cvs, bx, by, out, xs, xe, ys, ye, fracMapX, fracMapY);
					addExtraSideVertex((Connected) c, d2, after, cvs, bx, by, out, xs, xe, ys, ye, fracMapX, fracMapY);
					break;	
				default:
					// do nothing
				}
			}
		
			// add border vertices for directed edges.
			for (Connection conn : ((Connected) c).getLinks()) {
				if ((conn.getDrawDirection() != null) && (!conn.getRenderingInformation().isContradicting())) {
					addExtraSideVertex((Connected) c, conn.getDrawDirectionFrom((Connected) c), conn.otherEnd((Connected) c), cvs, bx, by, out, xs, xe, ys, ye, fracMapX, fracMapY);
				}
			}
		}
	
		for (MultiCornerVertex cv : cvs.getVerticesAtThisLevel()) {
			setRouting(cvs, cv, bx, by, xs, xe, ys, ye, out, fracMapX, fracMapY);
		}
		setPlanarizationHints(c, bounds);
	}
	
	private void addExtraSideVertex(Connected c, Direction d, Connected to, CornerVertices cvs, Bounds x, Bounds y, List<Vertex> out, double xs, double xe, double ys, double ye, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (to != null) {
			RoutingInfo toBounds = rh.getPlacedPosition(to);
			if (!(requiresCornerVertices(to))) {
				toBounds = rh.narrow(toBounds, borderTrimAreaX, borderTrimAreaY);
			}
			BigFraction xOrd = null, yOrd = null;
			Bounds xNew = null, yNew = null;
			double fracX = 0d, fracY = 0d;
			HPos hpos = null;
			VPos vpos = null;
			MultiCornerVertex cvNew = null;	

			// set position
			switch (d) {
			case UP:
			case DOWN:
				x = x.narrow(rh.getBoundsOf(toBounds, true));
				double containerWidth = x.getDistanceMax() - x.getDistanceMin();
				int denom = Math.round((float) (containerWidth / (xe-xs)));
				denom = (denom % 2 == 1) ? denom + 1 : denom;  // make sure it's even
				fracX = ((x.getDistanceCenter() - x.getDistanceMin()) / containerWidth);
				double numerd = fracX * (double) denom;
				int numer = Math.round((float) numerd);
				yOrd = MultiCornerVertex.getOrdForYDirection(d);
				xOrd = BigFraction.getReducedFraction(numer, denom);
				cvNew = cvs.createVertex(xOrd, yOrd);	
				yOrd = cvNew.getYOrdinal();
				fracY = fracMapY.get(yOrd);
				
				if (to instanceof Container) {
					xNew = x.keep(xs, xe-xs, fracX);	// we are connecting to a container vertex
				} else {
					xNew = x;
				}
				yNew = y.keep(ys, ye-ys, fracY);
				vpos = d == Direction.UP ? VPos.UP : VPos.DOWN;
				break;
			case LEFT:
			case RIGHT:
				y = y.narrow(rh.getBoundsOf(toBounds, false));
				double containerHeight = y.getDistanceMax() - y.getDistanceMin();
				denom = Math.round((float) (containerHeight / (ye-ys)));
				denom = (denom % 2 == 1) ? denom + 1 : denom;  // make sure it's even
				fracY = ((y.getDistanceCenter() - y.getDistanceMin()) / containerHeight);
				numerd = fracY * (double) denom;
				numer = Math.round((float) numerd);
				xOrd = MultiCornerVertex.getOrdForXDirection(d);
				yOrd = BigFraction.getReducedFraction(numer, denom);
				cvNew = cvs.createVertex(xOrd, yOrd);	
				xOrd = cvNew.getXOrdinal();
				fracX = fracMapX.get(xOrd);
				
				if (requiresCornerVertices(to)) {
					yNew = y.keep(ys, ye-ys, fracY);
				} else {
					yNew = y;
				}
				xNew = x.keep(xs, xe-xs, fracX);
				hpos = d == Direction.LEFT ? HPos.LEFT: HPos.RIGHT;
				break;
			}
			
			
			if (cvNew.getRoutingInfo() == null) {
				// new vertex				
				cvNew.setRoutingInfo(rh.createRouting(xNew, yNew));
				cvNew.addAnchor(hpos, vpos, c);
				out.add(cvNew);
			}
			
		}
	}

	private void setRouting(CornerVertices cvs, MultiCornerVertex cv, Bounds bx, Bounds by, double xs, double xe, double ys, double ye, List<Vertex> out, Map<BigFraction, Double> fracMapX, Map<BigFraction, Double> fracMapY) {
		if (cv.getRoutingInfo() == null) {
			BigFraction xOrdinal = cv.getXOrdinal();
			BigFraction yOrdinal = cv.getYOrdinal();
			
			double xfrac = fracMapX.get(xOrdinal);
			double yfrac = fracMapY.get(yOrdinal);
			bx = bx.keep(xs, xe - xs, xfrac);
			by = by.keep(ys, ye - ys, yfrac);
			cv.setRoutingInfo(rh.createRouting(bx,by));
			cv = cvs.mergeDuplicates(cv, rh);
			
			if (cv != null) {
				out.add(cv);
				log.send("Setting routing info: "+cv+" "+bx+" "+by);
			}
		}
	}

	
	/**
	 * This implements a kind of quad sort, where we look at each vertex and ascribe it to TL, TR, BL, BR
	 * quadrants.  We do that over and over until there's only a single vertex in each quadrant.
	 */
	private void sortContents(List<Vertex> in, Bounds x, Bounds y) {
		if (in.size() > 1) {
			log.send("Sorting: "+in);
			Bounds left = rh.narrow(Layout.LEFT, x, true, false); 
			Bounds right =rh.narrow(Layout.RIGHT, x, true, false);
			Bounds top =rh.narrow(Layout.UP, y, false, false);
			Bounds bottom = rh.narrow(Layout.DOWN, y, false, false);
			
			List<Vertex> topRight = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> topLeft = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> bottomRight = new ArrayList<Vertex>(in.size()/2);
			List<Vertex> bottomLeft = new ArrayList<Vertex>(in.size()/2);
			
			boolean mergeTopAndBottom = false;
			boolean mergeLeftAndRight = false;
			
			for (Vertex vertex : in) {
				RoutingInfo vri = vertex.getRoutingInfo();
				Bounds vx = rh.getBoundsOf(vri, true);
				Bounds vy = rh.getBoundsOf(vri, false);
				boolean inLeft = rh.compareBounds(vx, left) == DPos.OVERLAP;
				boolean inRight = rh.compareBounds(vx, right) == DPos.OVERLAP;
				boolean inTop = rh.compareBounds(vy, top) == DPos.OVERLAP;
				boolean inBottom = rh.compareBounds(vy, bottom) == DPos.OVERLAP;
				
				if (inTop && inBottom) {
					mergeTopAndBottom = true;
				}
				
				if (inLeft && inRight) {
					mergeLeftAndRight = true;
				}
				
				if ((!inTop && !inBottom) || (!inLeft && !inRight)) {
					throw new LogicException("Vertex not within either bounds"+vri+" "+vertex);
				}
				
				if (inLeft) {
					if (inTop) {
						topLeft.add(vertex);
					} else {
						bottomLeft.add(vertex);
					}  
				} else {
					if (inTop) {
						topRight.add(vertex);
					} else {
						bottomRight.add(vertex);
					}  
				}
			}
			
			if (mergeLeftAndRight && mergeTopAndBottom) {
				// we should have a single vertex, so you need to sort the old fashioned way
				sortContentsOldStyle(in);
			} else if (mergeLeftAndRight) {
				in.clear();
				topRight.addAll(topLeft);
				bottomRight.addAll(bottomLeft);
				sortContents(topRight, x, top);
				sortContents(bottomRight, x, bottom);
				in.addAll(topRight);
				in.addAll(bottomRight);
			} else if (mergeTopAndBottom) {
				in.clear();
				topRight.addAll(bottomRight);
				topLeft.addAll(bottomLeft);
				sortContents(topRight, right, y);
				sortContents(topLeft, left, y);
				in.addAll(topLeft);
				in.addAll(topRight);
			} else {
				in.clear();
				sortContents(topLeft, left, top);
				sortContents(topRight, right, top);
				sortContents(bottomLeft, left, bottom);
				sortContents(bottomRight, right, bottom);
				in.addAll(topLeft);
				in.addAll(topRight);
				in.addAll(bottomLeft);
				in.addAll(bottomRight);
			}
		
			
		}
		
		
	}

	
	/**
	 * Simple Y,X sort, where Y has priority.
	 * 
	 * This is insufficient on it's own, because we end up with situations where in order to "get around" one large vertex we
	 * move out-of-position with respect to another vertex.  Because of the difference in vertex sizes, you basically
	 * can't just sort on middle position and hope things are in the right order.
	 */
	private void sortContentsOldStyle(List<Vertex> c) {
		Collections.sort(c, new Comparator<Vertex>() {

			@Override
			public int compare(Vertex arg0, Vertex arg1) {
				RoutingInfo ri0 = arg0.getRoutingInfo();
				RoutingInfo ri1 = arg1.getRoutingInfo();
				int out = 0;

				DPos yc = rh.compare(ri0, ri1, false);
				if (yc == DPos.BEFORE) {
					out = -1;
				} else if (yc == DPos.AFTER) {
					out = 1;
				}

				if (out == 0) {
					DPos xc = rh.compare(ri0, ri1, true);
					if (xc == DPos.BEFORE) {
						out = -1;
					} else if (xc == DPos.AFTER) {
						out = 1;
					}
				}

				if (out == 0) {
					// return 0;
					throw new LogicException("Contents overlap: "+arg0+" "+arg1);
				}

				//System.out.println("Comparing: " + arg0 + " " + arg1 + " " + out);
				return out;
			}
		});
	}

	@Override
	public String getPrefix() {
		return "GRPW";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}
	
	/**
	 * Returns the sort order for the two elements according to their groupwise position
	 * within a container.
	 */
	protected int compareDiagramElements(DiagramElement a, DiagramElement b) {
		DiagramElement parent =a.getParent();
		if (b.getParent() != parent) {
			parent = getCommonContainer(a, b);
		}
		
		Layout l = ((Container) parent).getLayout();
		
		if (l == null) {
			return 0;
		} else {
			DPos dp = null;
			switch (l) {
				case UP:
				case DOWN:
				case VERTICAL:
					dp  = rh.compare(a, b, false);
					break;
				case LEFT:
				case RIGHT:
				case HORIZONTAL:
					dp = rh.compare(a, b, true);
					break;
				case GRID:
					dp = rh.compare(a, b, false);
					dp = (dp == DPos.OVERLAP) ? rh.compare(a, b, true) : dp;
			}
			
			if (dp == DPos.BEFORE) {
				return -1;
			} else if (dp == DPos.AFTER) {
				return 1; 
			} else {
				return 0;
			//	throw new LogicException("Elements within a container shouldn't overlap");
			}
		}
		
	}
	
	protected Container getCommonContainer(DiagramElement from, DiagramElement to) {
		while (from != to) {
			int depthFrom = em.getContainerDepth(from);
			int depthTo = em.getContainerDepth(to);
			if (depthFrom < depthTo) {
				to = to.getParent();
			} else if (depthFrom > depthTo) {
				from = from.getParent();
			} else {
				to = to.getParent();
				from = from.getParent();
			}
		}
		
		return (Container) from;
	}
}
