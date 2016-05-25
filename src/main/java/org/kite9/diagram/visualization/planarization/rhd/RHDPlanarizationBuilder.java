package org.kite9.diagram.visualization.planarization.rhd;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.common.elements.RoutingInfo;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.BasicBounds;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.primitives.BiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.primitives.HintMap;
import org.kite9.diagram.primitives.PositionableDiagramElement;
import org.kite9.diagram.visitors.DiagramElementVisitor;
import org.kite9.diagram.visitors.VisitorAction;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.PlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertices;
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
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D.DPos;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Rob's Hierarchical Decomposition vertex orderer is a 3-phase process: grouping, layout and ordering. The
 * grouping phase divides the orderables at each level into hierarchical groups
 * based on how many connections they have with other groups.
 * 
 * The layout phase takes a group and works out whether the two merge groups
 * within it should be placed horizontally or vertically within their container.
 * This means there are 4^c options for this merge where c is the number of
 * containers with parts of both groups.
 * 
 * Finally, the results of this are used to create the list of vertices to
 * return, with their positions set by the layout strategy.
 * 
 * @author moffatr
 * 
 */
public abstract class RHDPlanarizationBuilder implements PlanarizationBuilder, Logable {

	public static final double CONTAINER_VERTEX_SIZE = 1E-10; 

	private Kite9Log log = new Kite9Log(this);

	protected ElementMapper em;
	private RoutableHandler2D rh;

	public RHDPlanarizationBuilder(ElementMapper em) {
		super();
		this.em = em;
	}
	
	public RoutableReader getRoutableReader() {
		return rh;
	}

	protected abstract Planarization buildPlanarization(Diagram c, List<Vertex> vertexOrder, Collection<BiDirectional<Connected>> initialUninsertedConnections, Map<Container, List<Contained>> sortedContainerContents);

	public Planarization planarize(Diagram c) {
		final int[] elements = new int[1];

		new DiagramElementVisitor().visit(c, new VisitorAction() {

			@Override
			public void visit(DiagramElement de) {
				if (de instanceof Contained) {
					elements[0]++;
				}
			}
		});
		boolean firstGo = true, redo = false;
		List<Vertex> out = new ArrayList<Vertex>(elements[0] * 2);
		ConnectionManager connections = null;
		Map<Container, List<Contained>> sortedContainerContents = null;
		
		while (firstGo || redo) {
			redo = false;
			rh =  new PositionRoutableHandler2D();
			ContradictionHandler ch = new BasicContradictionHandler(em);
			GroupingStrategy strategy = new GeneratorBasedGroupingStrategyImpl(ch);
			LayoutStrategy layout = new DirectionLayoutStrategy(rh);
			GroupPhase gp = new GroupPhase(log, c, elements[0], strategy, ch);
			GroupResult mr = strategy.group(gp);
			
			if (!log.go()) {
				log.send("Created Groups:", mr.groups());
			}
	
			if (mr.groups().size() > 1) {
				throw new LogicException("Should end up with a single group");
			} else {
				Group topGroup = mr.groups().iterator().next();
				
				if (!log.go()) {
					outputGroupInfo(topGroup, 0);
				}
				
				
				layout.layout(gp, mr, new MostNetworkedFirstLayoutQueue(gp.groupCount));
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
				
				sortedContainerContents = new HashMap<Container, List<Contained>>(gp.containerCount * 2);
				buildVertexList(null, c, null, out, sortedContainerContents);
				sortContents(out, rh.getTopLevelBounds(true), rh.getTopLevelBounds(false));
			}
			
			firstGo = false;
		}

		Planarization planOut = buildPlanarization(c, out, connections, sortedContainerContents);
		((RHDPlanarizationImpl)planOut).setRoutableReader(rh);
		
		return planOut;
	}

	/**
	 * Potentially expensive, but checks to make sure that none of the positions overlap.
	 */
	private boolean checkLayoutIsConsistent(Container c) {
		List<Contained> contents = c.getContents();
		for (int i = 0; i < contents.size(); i++) {
			Contained ci = contents.get(i);
			for (int j = 0; j < i; j++) {
				Contained cj = contents.get(j);
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

	private boolean overlaps(Contained a, Contained b) {
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
		log.send(sb.toString() + g.getGroupNumber() + " "+ ((g instanceof LeafGroup) ? g.toString() : axis) + "   " + rh.getPlacedPosition(g)+"  "+l+" "+(g.getAxis().isLayoutRequired() ? "LR" : ""));
		
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
			Contained l = lg.getContained();
			Container c = lg.getContainer();
			
			// sizing
			RoutingInfo ri = lg.getAxis().getPosition(rh, false);
			if (l != null) {
				rh.setPlacedPosition(l, ri);
			}
			ensureContainerBoundsAreLargeEnough(ri, c);
		}	
	
	}
	
	private void ensureContainerBoundsAreLargeEnough(RoutingInfo ri, Container c) {
		Contained l;
		while (c != null) {
			// make sure container bounds are big enough for the contents
			RoutingInfo cri = rh.getPlacedPosition(c);
			if (cri == null) {
				cri = rh.emptyBounds();
			}

			cri = rh.increaseBounds(cri, ri);
			rh.setPlacedPosition(c, cri);
			l = (Contained) c;
			c = l.getContainer();
		}
	}

	/**
	 * Constructs the list of vertices in no particular order.
	 */
	private void buildVertexList(Contained before, Contained c, Contained after, List<Vertex> out, Map<Container, List<Contained>> sortedContainerContents) {
		if (c instanceof Container) {
			Container container = (Container) c;
			
			if (container.getContents().size() > 0) {
				buildVertexListForContainerContents(out, container, sortedContainerContents);
			}
			
			ContainerVertices cvs = em.getContainerVertices(container);
			setContainerVertexPositions(before, container, after, cvs);
			for (Vertex vertex : cvs.getVertices()) {
				out.add(vertex);
			}
		} else {
			RoutingInfo bounds = rh.getPlacedPosition(c);
			Vertex v = em.getVertex((Connected) c);
			out.add(v);
			bounds = rh.narrow(bounds, CONTAINER_VERTEX_SIZE);
			v.setRoutingInfo(bounds);
			setPlanarizationHints(c, bounds);
		}
		return;
	}
	
	public static final boolean CHANGE_CONTAINER_ORDER = true;

	private void buildVertexListForContainerContents(List<Vertex> out, Container container, Map<Container, List<Contained>> sortedContainerContents) {
		boolean layingOut = container.getLayoutDirection() != null;
		List<Contained> contents = container.getContents();
		if (layingOut) {
			// sort the contents so that we can connect the right elements together
			contents = getContainerContentsHolder(container.getLayoutDirection(), contents);
			
			Collections.sort(contents, new Comparator<Contained>() {
				@Override
				public int compare(Contained arg0, Contained arg1) {
					return compareContained(arg0, arg1);
				}
			});
			
			sortedContainerContents.put(container, contents);
		} 
		
		Contained conBefore = null, current = null, conAfter = null;
		boolean start =true;
		Iterator<Contained> iterator = contents.iterator();
		while (start || (current != null)) {
			conBefore = current;
			current = conAfter;
			conAfter = iterator.hasNext() ? iterator.next() : null;
			if (current != null) {
				buildVertexList(conBefore, current, conAfter, out, sortedContainerContents);	
				start = false;
			}
		}
	}

	private List<Contained> getContainerContentsHolder(Layout ld, List<Contained> contents) {
		if (CHANGE_CONTAINER_ORDER) {
			if ((ld == Layout.HORIZONTAL) || (ld == Layout.VERTICAL)) {
				// we are going to modify the original diagram
				return contents;
			}
		} 
		
		// leave original intact
		return new ArrayList<Contained>(contents);
	} 

	private void setPlanarizationHints(DiagramElement c, RoutingInfo bounds) {
		if (c instanceof PositionableDiagramElement) {
			HintMap hints = ((PositionableDiagramElement)c).getPositioningHints();
			if (hints == null) {
				hints = new HintMap();
				((PositionableDiagramElement)c).setPositioningHints(hints);
			}
			rh.setHints(hints, bounds);
		}
	}
	
	private void setContainerVertexPositions(Contained before, Container c, Contained after, ContainerVertices cvs) {
		Container within = (c instanceof Contained) ? ((Contained)c).getContainer():  null;
		
		Layout l = within == null ? null : within.getLayoutDirection();
		if (l != null) {
			switch (l) {
			case UP:
			case DOWN:
			case VERTICAL:
				expandContainerSpace(c, within, true);
				addExtraContainerVertex(c, Direction.DOWN, before, cvs);
				addExtraContainerVertex(c, Direction.DOWN, after, cvs);
				break;
			case LEFT:
			case RIGHT:
			case HORIZONTAL:
				expandContainerSpace(c, within, false);
				addExtraContainerVertex(c, Direction.RIGHT, before, cvs);
				addExtraContainerVertex(c, Direction.RIGHT, after, cvs);
				break;			
			}
		}
		
		RoutingInfo bounds = rh.getPlacedPosition(c);
		Bounds bx = rh.getBoundsOf(bounds, true);
		Bounds by = rh.getBoundsOf(bounds, false);
		int depth = em.getContainerDepth(c);
		double b1 = CONTAINER_VERTEX_SIZE - (CONTAINER_VERTEX_SIZE / (double) (depth + 1));
		double b2 = CONTAINER_VERTEX_SIZE - (CONTAINER_VERTEX_SIZE / (double) (depth + 2));
	
		for (ContainerVertex cv : cvs.getVertices()) {
			setRouting(cv, bx, by, b1, b2);
		}
		setPlanarizationHints(c, bounds);
	}
	
	private void expandContainerSpace(Container c, Container within, boolean horiz) {
		Bounds nx, ny;
		if (horiz) {
			nx = rh.getBoundsOf(rh.getPlacedPosition(within), true);
			ny = rh.getBoundsOf(rh.getPlacedPosition(c), false);
		} else {
			ny = rh.getBoundsOf(rh.getPlacedPosition(within), false);
			nx = rh.getBoundsOf(rh.getPlacedPosition(c), true);			
		}
		rh.setPlacedPosition(c, rh.createRouting(nx, ny));
	}

	private void addExtraContainerVertex(Container c, Direction d, Contained to, ContainerVertices cvs) {
		if (to != null) {
			int comp = compareContained((Contained) c, to);
			if (comp == 1) {
				d = Direction.reverse(d);
			}
			RoutingInfo cbounds = rh.getPlacedPosition(c);
			RoutingInfo toBounds = rh.getPlacedPosition(to);
			Bounds x = rh.getBoundsOf(cbounds, true);
			Bounds y = rh.getBoundsOf(cbounds, false);
			ContainerVertex cvNew = cvs.getCentralVertexOnSide(d);
			// set position
			switch (d) {
			case UP:
			case DOWN:
				Bounds newX = x.narrow(rh.getBoundsOf(toBounds, true));
				x = newX == BasicBounds.EMPTY_BOUNDS ? x: newX;
				x = x.keepMid(CONTAINER_VERTEX_SIZE);
				break;
			case LEFT:
			case RIGHT:
				Bounds newY = y.narrow(rh.getBoundsOf(toBounds, false));
				y = newY == BasicBounds.EMPTY_BOUNDS ? y : newY;
				y = y.keepMid(CONTAINER_VERTEX_SIZE);
				break;
			}
			cvNew.setRoutingInfo(rh.createRouting(x, y));
		}
	}

	private void setRouting(ContainerVertex cv, Bounds bx, Bounds by, double b1, double b2) {
		if (cv.getXOrdinal() == LOWEST_ORD) {
			bx = bx.keepMin(b1, b2);
		} else if (cv.getXOrdinal() == HIGHEST_ORD) {
			bx = bx.keepMax(b1, b2);
		} else {
			bx = rh.getBoundsOf(cv.getRoutingInfo(), true);  // whole side, narrowed later
		}
		
		if (cv.getYOrdinal() == LOWEST_ORD) {
			by = by.keepMin(b1, b2);
		} else if (cv.getYOrdinal() == HIGHEST_ORD) {
			by = by.keepMax(b1, b2);
		} else {
			by = rh.getBoundsOf(cv.getRoutingInfo(), false);  // whole side, narrowed later
		}
		cv.setRoutingInfo(rh.createRouting(bx,by));
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
				} else if (yc==DPos.AFTER) {
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
					throw new LogicException("Contents overlap: "+arg0+" "+arg1);
				}
					
				//System.out.println("Comparing: "+arg0+" "+arg1+" "+out);
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
	protected int compareContained(Contained a, Contained b) {
		Container parent =a.getContainer();
		if (b.getContainer() != parent) {
			throw new LogicException("a and b must share a container");
		}
		
		Layout l = parent.getLayoutDirection();
		
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
			}
			
			if (dp == DPos.BEFORE) {
				return -1;
			} else if (dp == DPos.AFTER) {
				return 1; 
			} else {
				throw new LogicException("Elements within a container shouldn't overlap");
			}
		}
		
	}
}
