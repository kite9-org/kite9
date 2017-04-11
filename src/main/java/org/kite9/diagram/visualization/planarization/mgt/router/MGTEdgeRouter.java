package org.kite9.diagram.visualization.planarization.mgt.router;

import java.util.List;
import java.util.Stack;

import org.kite9.diagram.common.algorithms.ssp.NoFurtherPathException;
import org.kite9.diagram.common.elements.edge.AbstractPlanarizationEdge;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.mapping.ConnectionEdge;
import org.kite9.diagram.common.elements.mapping.ContainerLayoutEdge;
import org.kite9.diagram.common.elements.mapping.ElementMapper;
import org.kite9.diagram.common.elements.vertex.EdgeCrossingVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.visualization.planarization.Tools;
import org.kite9.diagram.visualization.planarization.mgt.BorderEdge;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarization;
import org.kite9.diagram.visualization.planarization.mgt.MGTPlanarizationImpl;
import org.kite9.diagram.visualization.planarization.mgt.builder.MGTPlanarizationBuilder;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.EdgeCrossPath;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.EdgePath;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.FinishPath;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.Going;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.PlanarizationCrossPath;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.PlanarizationSide;
import org.kite9.diagram.visualization.planarization.mgt.router.AbstractRouteFinder.StartPath;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.Kite9Log;
import org.kite9.framework.logging.Logable;
import org.kite9.framework.logging.LogicException;

/**
 * Contains the logic needed to insert a route generated by the {@link AbstractRouteFinder} class into an
 * {@link MGTPlanarization}.
 * 
 * @author robmoffat
 * 
 */
public class MGTEdgeRouter implements EdgeRouter, Logable {
 
	private RoutableReader rh;
	private ElementMapper em;

	public MGTEdgeRouter(RoutableReader rh, ElementMapper em) {
		super();
		this.em = em;
		this.rh = rh;
	}

	Tools t = new Tools();
	int vertexIntro = 0;

	Kite9Log log = new Kite9Log(this);

	private boolean applyRoute(PlanarizationEdge ci, EdgePath best, EdgePath ep, MGTPlanarization p) {
		
		boolean currentAbove = true;
		Vertex lastVertexTemp = null;
	
		try {
		
		while (ep != null) {
			log.send(log.go() ? null : "Handling: " + ep);
			
			
			if (ep instanceof StartPath) {
				Vertex start = ((StartPath)ep).l.getVertex();
				
				if (start != ci.getFrom()) {
					// this occurs when we route container-meeting vertices.
					ci.getFrom().removeEdge(ci);
					ci.setFrom(start);
					start.addEdge(ci);
				}
				
				insertEdge(currentAbove, ci, p, ((StartPath)ep).getOutsideEdge());
			} else if (ep instanceof EdgeCrossPath) {
				// edge crossing
				EdgeCrossPath ec = (EdgeCrossPath) ep;
				PlanarizationEdge crossedEdge = ec.getCrossing();

				Vertex crossingVertex = createCrossingVertex(crossedEdge, ci);
				PlanarizationEdge[] brokenCross = t.splitEdge((PlanarizationEdge) crossedEdge, crossingVertex, p);
				int vertexTo = p.getVertexIndex(lastVertexTemp);
				int crossToPosition= p.getVertexIndex(crossedEdge.getTo());
				int crossFromPosition = p.getVertexIndex(crossedEdge.getFrom());

				boolean currentlyOutsideEdge = (vertexTo > crossToPosition) || (vertexTo < crossFromPosition);
				boolean crossingSideAbove = p.getAboveLineEdges().contains(crossedEdge);

				int place;
				
				if (crossingSideAbove != currentAbove) {
					throw new LogicException("Next edge should be on the same side as the crossing edge");
				}

				if (!currentlyOutsideEdge) {
					// vertexTo is coming up from below ground, we are breaking outside of a rainbow
					place = safeSiteInside(best, crossedEdge, vertexTo, crossFromPosition, crossToPosition, ep.getGoing() == Going.FORWARDS, p, crossingSideAbove);
					currentAbove = crossingSideAbove;
				} else {
					// vertexTo is outside the rainbow, wanting to push into it 
					place = safeSiteOutside(best, currentAbove, crossedEdge, crossToPosition, crossFromPosition,
							ep.getGoing() == Going.FORWARDS, p, crossingSideAbove);
				}
				
				PlanarizationEdge[] parts = t.splitEdge((PlanarizationEdge) ci, crossingVertex, p);
				insertVertices(place, ep.prev, p, crossingVertex);
				ci = parts[0];
				
				removeEdge(crossedEdge, p, crossingSideAbove);

				
				if (crossingSideAbove) {
					insertEdge(true, brokenCross[0], p, null);
					insertEdge(true, brokenCross[1], p, null);
				} else {
					insertEdge(false, brokenCross[0], p, null);
					insertEdge(false, brokenCross[1], p, null);
				}

				insertEdge(currentAbove, parts[1], p, crossToPosition > vertexTo ? brokenCross[1] : null);

				replaceReferences(crossedEdge, brokenCross[0], brokenCross[1], best);

				currentAbove = crossingSideAbove;
				lastVertexTemp = crossingVertex;
			} else if (ep instanceof PlanarizationCrossPath) {
				Vertex crossing = ((PlanarizationCrossPath)ep).getCrossingPoint();
				// side cross
				PlanarizationEdge[] parts = t.splitEdge((PlanarizationEdge) ci, crossing, p);
				ci = parts[0];
				if (currentAbove) {
					insertEdge(true, parts[1], p, null);
				} else {
					insertEdge(false, parts[1], p, null);
				}
				lastVertexTemp = crossing;
				
			
				currentAbove = !currentAbove;

			} else if (ep instanceof FinishPath) {
				Vertex end = ((FinishPath)ep).l.getVertex();
				if (ci.getTo() !=end) {
					// this occurs when we route container-meeting vertices.
					ci.getTo().removeEdge(ci);
					ci.setTo(end);
					end.addEdge(ci);
				}
				
				currentAbove = ep.getSide() == PlanarizationSide.ENDING_ABOVE;
				lastVertexTemp = end;
			} else {
				throw new LogicException("Simple Edge Path?," + ep);
			}

			ep = ep.prev;

		}
		
		} finally {
			log.send(log.go() ? null : "Inserted: "+ci);
			log.send(log.go() ? null : ((MGTPlanarizationImpl)p).getTextualRepresentation(ci.getDiagramElements().keySet()).toString());
		}
		return true;
	}
	
	

	private int safeSiteInside(EdgePath best, PlanarizationEdge cross, int vertexTo, int startVertex, int endVertex,
			boolean forwards, MGTPlanarization p, boolean crossingSideAbove) {
		if (forwards) {
			return safeSiteForwards(cross,
					AbstractRouteFinder.getCorrectEdgeSet(vertexTo, startVertex, crossingSideAbove, cross.getFrom(), p),
					startVertex, p.getVertexOrder().get(startVertex), crossingSideAbove, best, p);
		} else {
			return safeSiteBackwards(cross,
					AbstractRouteFinder.getCorrectEdgeSet(vertexTo, endVertex, crossingSideAbove, cross.getTo(), p),
					endVertex, p.getVertexOrder().get(endVertex), crossingSideAbove, best, p);
		}
	}

	private int safeSiteOutside(EdgePath best, boolean currentAbove, PlanarizationEdge cross, int endVertex, int startVertex,
			boolean forwards, MGTPlanarization p, boolean crossingAbove) {
		if (forwards) {
			return safeSiteBackwards(cross,
					crossingAbove ? p.getAboveBackwardLinks(cross.getTo()) : p.getBelowBackwardLinks(cross.getTo()),
					endVertex,  p.getVertexOrder().get(endVertex), currentAbove, best, p);
		} else {
			return safeSiteForwards(cross,
					crossingAbove ? p.getAboveForwardLinks(cross.getFrom()) : p.getBelowForwardLinks(cross.getFrom()),
					startVertex,  p.getVertexOrder().get(startVertex), currentAbove, best, p);
		}
	}

	private void insertVertices(int at, EdgePath ep, MGTPlanarization p, Vertex...vs) {
		for (Vertex crossing : vs) {
			p.addVertexToOrder(at, crossing);
			log.send("Inserted vertex " + at + "   " + crossing);
		}
	}

	private Vertex createCrossingVertex(Edge e2, Edge e1) {
		// crossing vertices for container edges should belong to their container
		DiagramElement underlying = null;
		DiagramElement e1Under = e1.getOriginalUnderlying();
		DiagramElement e2Under = e2.getOriginalUnderlying();
		if (e1Under instanceof Container) {
			underlying = e1Under;
		} else if (e2Under instanceof Container) {
			underlying = e2Under;
		}
		
		return new EdgeCrossingVertex("ecv" + vertexIntro++, underlying);
	}

	/**
	 * Ensures that we can add a vertex after vertex index without crashing any existing edges.
	 */
	private int safeSiteForwards(PlanarizationEdge cross, List<PlanarizationEdge> list, int vertexIndex, Vertex v, boolean aboveEdges, EdgePath ep, MGTPlanarization p) {
		int index = list.indexOf(cross);
		if (index == -1) {
			throw new LogicException("Was expecting " + cross + " in " + list + " at index " + vertexIndex);
		}
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				PlanarizationEdge around = list.get(i);
				Vertex crossing = new PlanarizationCrossingVertex("x" + vertexIntro++);
				PlanarizationEdge[] parts = t.splitEdge((PlanarizationEdge) around, crossing, p);
				insertVertices(vertexIndex, ep.prev, p, crossing);
				removeEdge(around, p, aboveEdges);
				replaceReferences(around, parts[1], parts[0], ep);

				if (aboveEdges) {
					insertEdge(false, parts[0], p, null);
					insertEdge(true, parts[1], p, null);
				} else {
					insertEdge(true, parts[0], p, null);
					insertEdge(false, parts[1], p, null);
				}
				i = i - 1;
				index--;
			}
		}

		return vertexIndex;

	}

	/**
	 * Ensures that we can add a vertex before vertex index without crashing any existing edges.
	 */
	private int safeSiteBackwards(PlanarizationEdge cross, List<PlanarizationEdge> list, int vertexIndex, Vertex v, boolean aboveEdges, EdgePath ep,
			MGTPlanarization p) {
		int index = list.indexOf(cross);
		int outIndex = vertexIndex - 1;
		if (index == -1) {
			throw new LogicException("Was expecting " + cross + " in " + list + " at index " + vertexIndex);
		}
		if (index > 0) {
			for (int i = 0; i < index; i++) {
				PlanarizationEdge around = list.get(i);
				Vertex crossing = new PlanarizationCrossingVertex("x" + vertexIntro++);
				PlanarizationEdge[] parts = t.splitEdge((PlanarizationEdge) around, crossing, p);
				insertVertices(outIndex, ep.prev, p, crossing);
				removeEdge(around, p, aboveEdges);
				replaceReferences(around, parts[0], parts[1], ep);
				if (aboveEdges) {
					insertEdge(true, parts[0], p, null);
					insertEdge(false, parts[1], p, null);
				} else {
					insertEdge(false, parts[0], p, null);
					insertEdge(true, parts[1], p, null);
				}
				i = i - 1; // removing the edge means we should go back a
				// place in the array
				index--;
				outIndex++;
			}
		}

		return outIndex;
	}

	private void replaceReferences(PlanarizationEdge from, PlanarizationEdge to, PlanarizationEdge other, EdgePath ep) {
		while (ep != null) {
			if (ep instanceof EdgeCrossPath) {
				if (((EdgeCrossPath) ep).crossing == from) {
					((EdgeCrossPath) ep).crossing = to;
				}
			} else if (ep instanceof StartPath) {
				PlanarizationEdge outsideOf = ((StartPath) ep).getOutsideEdge();
				Vertex start = ((StartPath) ep).l.getVertex();
				if (outsideOf == from) {
					if (to.meets(start)) {
						((StartPath) ep).outsideEdge = to;
					} else if (other.meets(start)) {
						((StartPath) ep).outsideEdge = other;
					} else {
						throw new LogicException("Can't fix up start path");
					}
					
				}
			}
			ep = ep.prev;
		}
	}

	@Override
	public boolean addEdgeToPlanarization(MGTPlanarization p, PlanarizationEdge ci, Direction d, CrossingType it, GeographyType gt) {
		try {	
			MGTPlanarizationBuilder.logPlanarEmbeddingDetails(p, log);
			AbstractRouteFinder f = 
				ci instanceof ContainerLayoutEdge ? new LayoutEdgeRouteFinder(p, rh, (ContainerLayoutEdge) ci, em, d) : 
				ci instanceof BorderEdge ? new ContainerEdgeRouteFinder(p, rh, (BorderEdge) ci) :
				new ConnectionEdgeRouteFinder(p, rh, (ConnectionEdge) ci, em, d, it, gt);
			log.send("Routing "+ci+" from "+ci.getFrom()+" to "+ci.getTo()+"d="+ci.getDrawDirection());
			EdgePath ep = f.createShortestPath();		
			
			if (ep.costing.illegalEdgeCrossings>0) {
				if (it==CrossingType.STRICT) {
					return false;
				} 
			}

			log.send(log.go() ? null : "Routed " + ci + ": " + ep);
			removeDuplicates(ep, p);
			createPlaneCrossingVertices(ep, p);
			
			if ((ep.costing.minimumExpensiveAxisDistance > 0) && (ci instanceof PlanarizationEdge)) {
				((AbstractPlanarizationEdge)ci).setStraight(false);
			}

			applyRoute(ci, ep, ep, p);
			
			return true;
		} catch (NoFurtherPathException nfe) {
			log.error("Could not route " + ci+" in direction "+ci.getDrawDirection());
			return false;
		} catch (EdgeRoutingException ere) {
			log.error("Could not route "+ci, ere);
			return false;
		}

	}

	private void removeDuplicates(EdgePath start, MGTPlanarization p) {
		Stack<EdgePath> onStack = new Stack<AbstractRouteFinder.EdgePath>();
		
		while (start != null) {
			if (onStack.size() > 0) {
				EdgePath top = onStack.peek();
				while (top.sameCrossing(start)) {
					onStack.pop();
					log.send("Removing duplicates: "+top+" and "+start);
					top = onStack.peek();
					top.prev = start.prev;
					start = start.prev;
				}
			}
			
			onStack.add(start);
			start = start.prev;
			
			
		}
	}
		
	private void createPlaneCrossingVertices(EdgePath ep, MGTPlanarization p) {
		// create vertices in the planarization for the edge crossings
		if (ep instanceof PlanarizationCrossPath) {
			PlanarizationCrossPath pcp = (PlanarizationCrossPath) ep;
			Vertex beforeStart = pcp.getBeforeV();
			Vertex before = beforeStart;
			int beforeI = p.getVertexIndex(before);
			// insert the vertex after beforeI
			Vertex crossing = new PlanarizationCrossingVertex("x" + vertexIntro++);
			insertVertices(beforeI, ep.prev, p, crossing);
			((PlanarizationCrossPath)ep).setCrossingPoint(crossing);
			log.send("Crossing Vertex created: "+crossing+" for "+ep);
				
		} 
		
		if (ep.prev != null) {
			createPlaneCrossingVertices(ep.prev, p);
		}
	}

	private void insertEdge(boolean above, PlanarizationEdge edge, MGTPlanarization p, PlanarizationEdge outsideOf) {
		if (!p.crosses(edge, above)) {
			p.addEdge(edge, above, outsideOf);
			log.send("Inserted edge: " + edge+" going "+edge.getDrawDirection());
		} else {
			throw new LogicException("Could not route edge: "+edge);
		}
	}

	private void removeEdge(Edge cross, MGTPlanarization p, boolean above) {
		p.removeEdge(cross);
	}

	@Override
	public String getPrefix() {
		return "MGER";
	}

	@Override
	public boolean isLoggingEnabled() {
		return true;
	}

}
