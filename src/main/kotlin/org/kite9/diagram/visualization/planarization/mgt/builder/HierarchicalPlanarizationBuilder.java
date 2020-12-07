package org.kite9.diagram.visualization.planarization.mgt.builder;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.edge.BiDirectionalPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.mapping.ContainerLayoutEdge;
import org.kite9.diagram.common.elements.mapping.CornerVertices;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.Bounds;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.CrossingType;
import org.kite9.diagram.visualization.planarization.mgt.router.GeographyType;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.diagram.logging.LogicException;

/**
 * This handles the creation of edges for a diagram element border, and the
 * introduction of edges to respect the ordering / direction settings
 * provided on the element.
 * 
 * @author robmoffat
 * 
 */
public class HierarchicalPlanarizationBuilder extends DirectedEdgePlanarizationBuilder {

	public static String LAST_PLANARIZATION_DEBUG = null;
	
	public HierarchicalPlanarizationBuilder(ElementMapper em, GridPositioner gp) {
		super(em, gp);
		LAST_PLANARIZATION_DEBUG = null;
	}

	@Override
	protected void completeEmbedding(MGTPlanarization p) { 
		setupElementBorderEdges(p, p.getDiagram());
		super.completeEmbedding(p);
		if (!log.go()) {
			LAST_PLANARIZATION_DEBUG = p.toString();
		}
	}
	
	@Override
	protected int processCorrectDirectedConnections(MGTPlanarization p) {
		// does the container layout edges, which will also be directed.
		List<PlanarizationEdge> containerLayoutEdges = new LinkedList<PlanarizationEdge>();
		addContainerLayoutEdges(p.getDiagram(), p, containerLayoutEdges);
		log.send("Layout edges:", containerLayoutEdges);
		for (PlanarizationEdge edge : containerLayoutEdges) {
			getEdgeRouter().addPlanarizationEdge(p, edge, edge.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
		}

		int out = super.processCorrectDirectedConnections(p);
		
		return out;
	}
	
	/**
	 * Checks to see if a current edge links from this vertex to the previous
	 * one.   If it does, and the container is directed, then we use that edge to
	 * maintain the container direction
	 */
	private boolean checkIfNewBackEdgeNeeded(DiagramElement current, DiagramElement prev, MGTPlanarization pln, Container inside) {
		if (inside.getLayout() == Layout.GRID) {
			return false;		// back edges not needed as elements are connected together.
		}
		
		boolean needed = checkForInsertedBackEdge(current, prev, pln, inside);
		if (!needed) {
			return false;
		}
		
		needed = checkForUninsertedBackEdge(current, prev, pln, inside);	
		if (!needed) {
			return false;
		}
		
		return true;
	}


	private boolean checkForUninsertedBackEdge(DiagramElement vUnd, DiagramElement prevUnd, MGTPlanarization pln, Container inside) {
		if ((vUnd instanceof Connected) && (prevUnd instanceof Connected)) {
			for (Connection c : ((Connected)prevUnd).getLinks()) {
				if (c.meets((Connected) vUnd)) {
					Vertex prevUndVertex = getVertexFor(prevUnd);
					PlanarizationEdge e = getEdgeForConnection(c, pln);
					if (pln.getUninsertedConnections().contains(e)) {
						Direction d= getDirectionForLayout(inside);
						boolean setOk = setEdgeDirection(e, d, prevUndVertex, false);
						
						if (setOk) {
							pln.getUninsertedConnections().remove(e);
							getEdgeRouter().addPlanarizationEdge(pln, e, c.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
							return false;
						} 
					}
				}
			}
		}
		
		return true;
	}


	private boolean checkForInsertedBackEdge(DiagramElement vUnd, DiagramElement prevUnd, MGTPlanarization pln, Container inside) {
		Direction d= getDirectionForLayout(inside);

		if ((vUnd instanceof Connected) && (prevUnd instanceof Connected)) {
			EdgeOrdering vOrd = getRelevantEdgeOrdering(vUnd, pln);
			EdgeOrdering prevOrd = getRelevantEdgeOrdering(prevUnd, pln);
			
			if ((vOrd==null) || (vOrd.size()==0) || (prevOrd==null) || (prevOrd.size()==0)) {
				// can't be linked
				return true;
			}

			Set<DiagramElement> vSet = vOrd.getUnderlyingLeavers(); 
			for (Edge e : prevOrd.getEdgesAsList()) {
				if (e instanceof BiDirectionalPlanarizationEdge) {
					DiagramElement eUnd = ((BiDirectionalPlanarizationEdge) e).getOriginalUnderlying();
					if ((eUnd != null) && (vSet.contains(eUnd))) {
						EdgeMapping em = pln.getEdgeMappings().get(eUnd);
						Vertex start = em.getStartVertex();
						List<PlanarizationEdge> edges = em.getEdges();
						Route b = getRoute(vUnd, prevUnd, start, edges, d, inside);
						if (b!=null) {
							log.send("Using "+eUnd+" as a back edge from "+prevUnd+" to "+vUnd+", from="+start+" going="+d);
							paintRoute(b, edges, d);
							return false;
						}
					}
				}
			}
		}
		
		return true;  // back edge still needed
	}

	private Direction getDirectionForLayout(Container inside) {
		switch (inside.getLayout()) {
		case UP:
		case DOWN:
		case VERTICAL:
			return Direction.DOWN;
		case LEFT:
		case RIGHT:
		case HORIZONTAL:
			return Direction.RIGHT;
		}
		
		throw new LogicException("Unexpected layout: "+inside.getLayout());
	}

	private void paintRoute(Route b, List<PlanarizationEdge> edges, Direction d) {
		Vertex start = b.sv;
		int end = b.end == null ? edges.size()-1 : b.end;
		for (int i = b.start; i <= end; i++) {
			Edge e = edges.get(i);
			setEdgeDirection(e, d, start, b.reverse);
			start = e.otherEnd(start);
		}
	}

	class Route {
		
		int start;
		Vertex sv;
		Integer end = null;
		boolean reverse = false;
		
	}

	private Route getRoute(DiagramElement vUnd, DiagramElement prevUnd,
			Vertex start, List<PlanarizationEdge> edges, Direction d, Container inside) {
		throw new UnsupportedOperationException();
		
//		Route b = null;
//		for (int j = 0; j < edges.size(); j++) {
//			Edge edge = edges.get(j);
//			boolean metV = start.getOriginalUnderlying()==vUnd;
//			boolean metVPrev = start.getOriginalUnderlying()==prevUnd;
//			
//			if ((metV || metVPrev) && (b!=null)) {
//				b.end = j-1;
//				return b;
//			}  
//				
//			if ((metVPrev || metV) && (b==null)) {
//				b = new Route();
//				b.start = j;
//				b.sv = start;
//				b.reverse = metV;					
//			} 
//			
//	//			if ((!metV && !metVPrev) && (b!=null)) {
//	//				// we are in the route - make sure nothing is interferes
//	//				DiagramElement under = start.getOriginalUnderlying();
//	//				if (under!=null) {
//	//					return null;
//	//				}
//	//			}
//			
//			start = edge.otherEnd(start);
//		}
//		
//		return b;
	}
	
	/**
	 * Returns true if we were able to set the edge to the new direction
	 */
	private boolean setEdgeDirection(Edge e, Direction d, Vertex from, boolean reverse) {
		d = reverse ? Direction.reverse(d) : d;
		boolean wrongDirection = (e.getDrawDirection()!=null) && (e.getDrawDirectionFrom(from) != d);
		
		
		if (wrongDirection)  {
			log.error("Direction already set for "+e+" as "+e.getDrawDirectionFrom(from)+" wanted to set "+d);
			Tools.setUnderlyingContradiction(e, true);
			return false;
		}
		
		log.send("Setting "+e+" "+d);
		e.setDrawDirectionFrom(d, from);
		return true;
	}


	private EdgeOrdering getRelevantEdgeOrdering(DiagramElement vUnd, Planarization pln) {
		if (vUnd instanceof Container) {
			return pln.getEdgeOrderings().get(vUnd);
		} else {
			Vertex v = getVertexFor(vUnd);
			return pln.getEdgeOrderings().get(v);
		}
	}
	
	/**
	 * This ensures that the containers' vertices have edges outsideEdge and
	 * below the planarization line to contain their content vertices.
	 * 
	 * This creates an {@link EdgeMapping} in the planarization, which is an ordered list
	 * of edges.  So, we have to journey round the perimeter vertices of the container and
	 * create edges between them in order.
	 */
	protected void setupElementBorderEdges(MGTPlanarization p, DiagramElement outer) {
		if (em.hasOuterCornerVertices(outer)) {
			CornerVertices cv = em.getOuterCornerVertices(outer);
			String originalLabel = outer.getID();
					
			LinkedList<PlanarizationEdge> out = new LinkedList<PlanarizationEdge>();
			EdgeMapping em = new EdgeMapping(outer, out);
			p.getEdgeMappings().put(outer, em);
	
			int i = 0;
			MultiCornerVertex fromv, tov = null;
			List<MultiCornerVertex> perimeterVertices = gridHelp.getClockwiseOrderedContainerVertices(cv);
			Iterator<MultiCornerVertex> iterator = perimeterVertices.iterator();
			while (iterator.hasNext()) {
				fromv = tov;
				tov = iterator.next();
				if (fromv != null) {
					addEdgeBetween(p, outer, originalLabel, em, i, fromv, tov);
					i++;
				}
			}
			
			// join back into a circle
			addEdgeBetween(p, outer, originalLabel, em, i, 
					perimeterVertices.get(perimeterVertices.size()-1), 
					perimeterVertices.get(0));
			
			if (outer instanceof Container) {
				for (DiagramElement c : ((Container)outer).getContents()) {
					setupElementBorderEdges(p, c);
				}
			}
		}
	}

	private void addEdgeBetween(MGTPlanarization p, DiagramElement outer, String originalLabel, EdgeMapping em, int i, MultiCornerVertex fromv, MultiCornerVertex tov) {
		Edge newEdge = updateEdges(originalLabel, outer, fromv, tov, i, em);
		if (newEdge != null) {
			getEdgeRouter().addPlanarizationEdge(p, (PlanarizationEdge) newEdge, newEdge.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
		}
	}

	private Edge updateEdges(String l, DiagramElement c, Vertex from, Vertex to, int i, EdgeMapping em) {
		Direction d = null;
		Bounds ax = rh.getBoundsOf(from.getRoutingInfo(), true);
		Bounds ay = rh.getBoundsOf(from.getRoutingInfo(), false);
		Bounds bx = rh.getBoundsOf(to.getRoutingInfo(), true);
		Bounds by = rh.getBoundsOf(to.getRoutingInfo(), false);
		
		
		if (ax.compareTo(bx) == 0) {
			int comp = ay.compareTo(by);
			if (comp == -1) {
				d = Direction.DOWN;
			} else if (comp == 1) {
				d = Direction.UP;
			}
		} else if (ay.compareTo(by) == 0) {
			int comp = ax.compareTo(bx);
			if (comp == -1) {
				d = Direction.RIGHT;
			} else if (comp == 1) {
				d = Direction.LEFT;
			}
		} 
		
		if (d != null) {
			while (from != to) {
				PlanarizationEdge e = getLeaverInDirection(from, d);
				if (e==null) {
					Map<DiagramElement, Direction> elementMap = new HashMap<>();
					elementMap.put(c, Direction.rotateAntiClockwise(d));
					BorderEdge cbe = new BorderEdge((MultiCornerVertex) from, (MultiCornerVertex) to, l+d+i, d, elementMap);
					em.add(cbe);			
					return cbe;
				}
				
				if (!(e instanceof BorderEdge)) {
					throw new LogicException("What is this?");
				}
				
				((BorderEdge)e).getDiagramElements().put(c, Direction.rotateAntiClockwise(d));
				em.add(e);			
				from = e.otherEnd(from);
			}
			
		 	return null;
		} else {
			throw new LogicException("Not dealt with this ");
		}
	}

	private PlanarizationEdge getLeaverInDirection(Vertex from, Direction d) {
		for (Edge e : from.getEdges()) {
			if (e.getDrawDirectionFrom(from) == d) {
				return (PlanarizationEdge) e;
			}
		}
		
		return null;
	}

	protected void addContainerLayoutEdges(Container c, MGTPlanarization p, List<PlanarizationEdge> toAdd) {
		List<Connected> contents;
		boolean layingOut = c.getLayout() != null;
		if (layingOut) {
			contents = p.getContainerOrderingMap().get(c);
		} else {
			contents = getConnectedContainerContents(c.getContents());
		}
			
		if (contents != null) {
			DiagramElement prev = null;
			for (DiagramElement current : contents) {
				if ((prev != null) && (layingOut)) {
					log.send("Ensuring layout between " + current + " and " + prev);
					checkAddLayoutEdge(p, c, toAdd, prev, current);
				}
				prev = current;

				if (current instanceof Container) {
					addContainerLayoutEdges((Container) current, p, toAdd);
				}
			}
		}
		
	}
	
	


	private void checkAddLayoutEdge(MGTPlanarization p, Container c, List<PlanarizationEdge> newEdges, DiagramElement prev, DiagramElement current) {
		boolean needsDirectingBackEdge = checkIfNewBackEdgeNeeded(current, prev, p, c);
		// create a directing back edge
		if (needsDirectingBackEdge) {
			Direction d = getDirectionForLayout(c);
			ContainerLayoutEdge e = new ContainerLayoutEdge(getVertexFor(prev), getVertexFor(current), d, (Connected) prev, (Connected) current);
			DiagramElement und = e.getOriginalUnderlying();
			EdgeMapping em = new EdgeMapping(und, e);
			p.getEdgeMappings().put(und, em);
			log.send("Creating New Layout Edge: "+e+" going "+d);
			newEdges.add(e);
		}
	}

	private Vertex getVertexFor(DiagramElement c) {
		if (em.hasOuterCornerVertices(c)) {
			Collection<MultiCornerVertex> vertices = em.getOuterCornerVertices((Container) c).getPerimeterVertices();
			for (MultiCornerVertex cv : vertices) {
				if (cv.hasAnchorFor(c)) {
					return cv;
				}
			}
		} 
		
		if (c instanceof Connected) {
			return em.getPlanarizationVertex((Connected) c); 
		} 
		
		throw new LogicException("Can't get a vertex for "+c);
	}

	@Override
	protected PlanarizationEdge getEdgeForConnection(BiDirectional<Connected> c, MGTPlanarization p) {
		Connected from = c.getFrom();
		Connected to = c.getTo();
		Vertex fromv = getVertexFor(from), tov = getVertexFor(to);
		
		// make sure we keep the edge/connection mapping list up to date.
		PlanarizationEdge out = em.getEdge(from, fromv, to, tov, c);
		
		if (c instanceof DiagramElement) {
			EdgeMapping em = new EdgeMapping((DiagramElement) c, out);
			p.getEdgeMappings().put((DiagramElement) c, em);
		}
		return out;
	}
}