package org.kite9.diagram.visualization.planarization.mgt.builder;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.kite9.diagram.common.elements.Edge;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.primitives.BiDirectional;
import org.kite9.diagram.primitives.Connected;
import org.kite9.diagram.primitives.Connection;
import org.kite9.diagram.primitives.Contained;
import org.kite9.diagram.primitives.Container;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.planarization.EdgeMapping;
import org.kite9.diagram.visualization.planarization.Planarization;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mapping.ContainerLayoutEdge;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertex;
import org.kite9.diagram.visualization.planarization.mapping.ContainerVertices;
import org.kite9.diagram.visualization.planarization.mapping.ElementMapper;
import org.kite9.diagram.visualization.planarization.mgt.ContainerBorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.router.CrossingType;
import org.kite9.diagram.visualization.planarization.mgt.router.GeographyType;
import org.kite9.diagram.visualization.planarization.ordering.EdgeOrdering;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.framework.logging.LogicException;

/**
 * This handles the creation of edges for the container boundary, and the
 * introduction of edges to respect the container ordering / direction settings
 * provided on the container.
 * 
 * @author robmoffat
 * 
 */
public class HierarchicalPlanarizationBuilder extends DirectedEdgePlanarizationBuilder {

	public HierarchicalPlanarizationBuilder(RoutableHandler2D rh, ElementMapper em) {
		super(em);
	}

	@Override
	protected void completeEmbedding(MGTPlanarization p) { 
		setupContainerBoundaryEdges(p, ((ContainerVertex) p.getVertexOrder().get(0)).getOriginalUnderlying());
		super.completeEmbedding(p);
		if (!log.go()) {
			try {
				FileWriter fw = new FileWriter("plan.txt", true);
				fw.write(p.toString());
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected int processCorrectDirectedConnections(MGTPlanarization p) {
		// does the container layout edges, which will also be directed.
		List<Edge> containerLayoutEdges = new LinkedList<Edge>();
		addContainerLayoutEdges(p.getDiagram(), p, containerLayoutEdges);
		log.send("Layout edges:", containerLayoutEdges);
		for (Edge edge : containerLayoutEdges) {
			getEdgeRouter().addEdgeToPlanarization(p, edge, edge.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
		}

		int out = super.processCorrectDirectedConnections(p);
		
		return out;
	}
	
	/**
	 * Checks to see if a current edge links from this vertex to the previous
	 * one.   If it does, and the container is directed, then we use that edge to
	 * maintain the container direction
	 */
	private boolean checkIfNewBackEdgeNeeded(Contained current, Contained prev, MGTPlanarization pln, Container inside) {
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


	private boolean checkForUninsertedBackEdge(Contained vUnd, Contained prevUnd, MGTPlanarization pln, Container inside) {
		if ((vUnd instanceof Connected) && (prevUnd instanceof Connected)) {
			for (Connection c : ((Connected)prevUnd).getLinks()) {
				if (c.meets((Connected) vUnd)) {
					Vertex prevUndVertex = getVertexFor(prevUnd);
					Edge e = getEdgeForConnection(c, pln);
					if (pln.getUninsertedConnections().contains(e)) {
						Direction d= getDirectionForLayout(inside);
						boolean setOk = setEdgeDirection(e, d, prevUndVertex, false);
						
						if (setOk) {
							pln.getUninsertedConnections().remove(e);
							getEdgeRouter().addEdgeToPlanarization(pln, e, c.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
							return false;
						} 
					}
				}
			}
		}
		
		return true;
	}


	private boolean checkForInsertedBackEdge(Contained vUnd, Contained prevUnd, MGTPlanarization pln, Container inside) {
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
				DiagramElement eUnd = e.getOriginalUnderlying();
				if ((eUnd != null) && (vSet.contains(eUnd))) {
					EdgeMapping em = pln.getEdgeMappings().get(eUnd);
					Vertex start = em.getStartVertex();
					List<Edge> edges = em.getEdges();
					Bounds b = getRoute(vUnd, prevUnd, start, edges, d, inside);
					if (b!=null) {
						log.send("Using "+eUnd+" as a back edge from "+prevUnd+" to "+vUnd+", from="+start+" going="+d);
						paintRoute(b, edges, d);
						return false;
					}
				}
			}
		}
		
		return true;  // back edge still needed
	}

	private Direction getDirectionForLayout(Container inside) {
		switch (inside.getLayoutDirection()) {
		case UP:
		case DOWN:
		case VERTICAL:
			return Direction.DOWN;
		case LEFT:
		case RIGHT:
		case HORIZONTAL:
			return Direction.RIGHT;
		}
		
		throw new LogicException("Unexpected layout: "+inside.getLayoutDirection());
	}

	private void paintRoute(Bounds b, List<Edge> edges, Direction d) {
		Vertex start = b.sv;
		int end = b.end == null ? edges.size()-1 : b.end;
		for (int i = b.start; i <= end; i++) {
			Edge e = edges.get(i);
			setEdgeDirection(e, d, start, b.reverse);
			start = e.otherEnd(start);
		}
	}

	class Bounds {
		
		int start;
		Vertex sv;
		Integer end = null;
		boolean reverse = false;
		
	}

	private Bounds getRoute(DiagramElement vUnd, DiagramElement prevUnd,
			Vertex start, List<Edge> edges, Direction d, Container inside) {
		Bounds b = null;
		for (int j = 0; j < edges.size(); j++) {
			Edge edge = edges.get(j);
			boolean metV = start.getOriginalUnderlying()==vUnd;
			boolean metVPrev = start.getOriginalUnderlying()==prevUnd;
			
			if ((metV || metVPrev) && (b!=null)) {
				b.end = j-1;
				return b;
			}  
				
			if ((metVPrev || metV) && (b==null)) {
				b = new Bounds();
				b.start = j;
				b.sv = start;
				b.reverse = metV;					
			} 
			
//			if ((!metV && !metVPrev) && (b!=null)) {
//				// we are in the route - make sure nothing is interferes
//				DiagramElement under = start.getOriginalUnderlying();
//				if (under!=null) {
//					return null;
//				}
//			}
			
			start = edge.otherEnd(start);
		}
		
		return b;
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
	 * This ensures that the containers' terminal vertices have edges outsideEdge and
	 * below the planarization line to contain their content vertices.
	 */
	protected void setupContainerBoundaryEdges(MGTPlanarization p, Container outer) {
		for (Contained c : outer.getContents()) {
			if (c instanceof Container) {
				setupContainerBoundaryEdges(p, (Container)c);
			}
		}
		
		ContainerVertices cv = em.getContainerVertices(outer);
		String originalLabel = outer.getID();
				
		LinkedList<Edge> out = new LinkedList<Edge>();
		EdgeMapping em = new EdgeMapping(outer, out);
		p.getEdgeMappings().put(outer, em);

		int i = 0;
		ContainerVertex fromv, tov = null;
		Iterator<ContainerVertex> iterator = cv.getVertices().listIterator();
		while (iterator.hasNext()) {
			fromv = tov;
			tov = iterator.next();
			if (fromv != null) {
				addEdgeBetween(p, outer, originalLabel, em, i, fromv, tov);
				i++;
			}
		}
		
		// join back into a circle
		addEdgeBetween(p, outer, originalLabel, em, i, cv.getVertices().getLast(), cv.getVertices().getFirst());
	}

	private void addEdgeBetween(MGTPlanarization p, Container outer, String originalLabel, EdgeMapping em, int i,
			ContainerVertex fromv, ContainerVertex tov) {
		Edge edge = getEdge(originalLabel, outer, fromv, tov, fromv.getXOrdinal(), fromv.getYOrdinal(), tov.getXOrdinal(), tov.getYOrdinal(), i);
		em.add(edge);			
		getEdgeRouter().addEdgeToPlanarization(p, edge, edge.getDrawDirection(), CrossingType.STRICT, GeographyType.STRICT);
	}

	private Edge getEdge(String l, Container c, ContainerVertex from, ContainerVertex to, int ax, int ay, int bx, int by, int i) {
		if ((ay == LOWEST_ORD) && (by == LOWEST_ORD)) {
			return new ContainerBorderEdge(from, to, l+"-top-"+i, c, Direction.RIGHT);
		} else if ((ay == HIGHEST_ORD) && (by == HIGHEST_ORD)) {
			return new ContainerBorderEdge(from, to, l+"-bottom-"+i, c, Direction.LEFT);
		} else if ((ax == LOWEST_ORD) && (bx == LOWEST_ORD)) {
			return new ContainerBorderEdge(from, to, l+"-left-"+i, c, Direction.UP);
		} else if ((ax == HIGHEST_ORD) && (bx == HIGHEST_ORD)) {
			return new ContainerBorderEdge(from, to, l+"-right-"+i, c, Direction.DOWN);
		}
	
		throw new LogicException("Not dealt with this");
	}

	protected void addContainerLayoutEdges(Container c, MGTPlanarization p, List<Edge> toAdd) {
		List<Contained> contents;
		boolean layingOut = c.getLayoutDirection() != null;
		if (layingOut) {
			contents = p.getContainerOrderingMap().get(c);
		} else {
			contents = c.getContents();
		}
			
		if (contents != null) {
			Contained prev = null;
			for (Contained current : contents) {
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
	
	


	private void checkAddLayoutEdge(MGTPlanarization p, Container c, List<Edge> newEdges, Contained prev, Contained current) {
		boolean needsDirectingBackEdge = checkIfNewBackEdgeNeeded(current, prev, p, c);
		// create a directing back edge
		if (needsDirectingBackEdge) {
			Direction d = getDirectionForLayout(c);
			Edge e = new ContainerLayoutEdge(getVertexFor(prev), getVertexFor(current), d, (Connected) prev, (Connected) current);
			DiagramElement und = e.getOriginalUnderlying();
			EdgeMapping em = new EdgeMapping(und, e);
			p.getEdgeMappings().put(und, em);
			log.send("Creating New Layout Edge: "+e+" going "+d);
			newEdges.add(e);
		}
	}

	private Vertex getVertexFor(DiagramElement c) {
		if (c instanceof Container) {
			return em.getContainerVertices((Container) c).getVertices().getFirst();  // any one will do
		} else if (c instanceof Connected) {
			return em.getVertex((Connected) c); 
		} else {
			throw new LogicException("Can't get a vertex for "+c);
		}
	}

	@Override
	protected Edge getEdgeForConnection(BiDirectional<Connected> c, MGTPlanarization p) {
		Connected from = c.getFrom();
		Connected to = c.getTo();
		Vertex fromv = getVertexFor(from), tov = getVertexFor(to);
		
		// get nearest vertices
		int fromi = p.getVertexIndex(fromv);
		int toi = p.getVertexIndex(tov);
		
		// make sure we keep the edge/connection mapping list up to date.
		PlanarizationEdge out = em.getEdge(from, fromv, to, tov, c);
		if (fromi > toi) {
			out.reverseDirection();
		}
		
		if (c instanceof DiagramElement) {
			EdgeMapping em = new EdgeMapping((DiagramElement) c, out);
			p.getEdgeMappings().put((DiagramElement) c, em);
		}
		return out;
	}
}