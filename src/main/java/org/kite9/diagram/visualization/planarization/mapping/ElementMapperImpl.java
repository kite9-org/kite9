package org.kite9.diagram.visualization.planarization.mapping;

import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.HIGHEST_ORD;
import static org.kite9.diagram.visualization.planarization.mapping.ContainerVertex.LOWEST_ORD;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Map;

import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Contained;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.ConnectedVertex;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.position.Direction;
import org.kite9.framework.logging.LogicException;

public class ElementMapperImpl implements ElementMapper {

	Map<Connected, Vertex> vertices = new HashMap<Connected, Vertex>();
	Map<Container, ContainerVertices> containers = new HashMap<Container, ContainerVertices>();
		Map<BiDirectional<Connected>, PlanarizationEdge> edges = new HashMap<BiDirectional<Connected>, PlanarizationEdge>();

	public ContainerVertices getContainerVertices(final Container c) {
		ContainerVertices v = containers.get(c);
		if (v == null) {
			v = new ContainerVertices() {
				
				public static final int MID_ORD = (LOWEST_ORD + HIGHEST_ORD) /2;
				LinkedList<ContainerVertex> elements = buildInitialList();
				
				ContainerVertex[] sideVertices = new ContainerVertex[4];
				
				private LinkedList<ContainerVertex> buildInitialList() {
					LinkedList<ContainerVertex> out = new LinkedList<ContainerVertex>();
					out.add(new ContainerVertex(c, LOWEST_ORD, LOWEST_ORD));
					out.add(new ContainerVertex(c, HIGHEST_ORD, LOWEST_ORD));
					out.add(new ContainerVertex(c, HIGHEST_ORD, HIGHEST_ORD));
					out.add(new ContainerVertex(c, LOWEST_ORD, HIGHEST_ORD));
					return out;
				};
				
				@Override
				public LinkedList<ContainerVertex> getVertices() {
					return elements;
				}

				@Override
				public ContainerVertex getCentralVertexOnSide(Direction d) {
					switch (d) {
					case UP:
						return addAfter(LOWEST_ORD, LOWEST_ORD, c, MID_ORD, LOWEST_ORD, d);
					case DOWN:
						return addAfter(HIGHEST_ORD, HIGHEST_ORD, c, MID_ORD, HIGHEST_ORD,d);
					case LEFT:
						return addAfter(LOWEST_ORD, HIGHEST_ORD, c, LOWEST_ORD, MID_ORD, d);
					case RIGHT:
						return addAfter(HIGHEST_ORD, LOWEST_ORD, c, HIGHEST_ORD, MID_ORD, d);
					}
					
					throw new LogicException("Direction not specified");
				}

				private ContainerVertex addAfter(int xBefore, int yBefore, Container c, int newXOrd, int newYOrd, Direction d) {
					ContainerVertex existing = sideVertices[d.ordinal()];
					if (existing != null) {
						return existing;
					}
					
					for (ListIterator<ContainerVertex> iterator = elements.listIterator(); iterator.hasNext();) {
						ContainerVertex before = iterator.next();
						if ((before.getXOrdinal() == xBefore) && (before.getYOrdinal() == yBefore)) {
							ContainerVertex result = new ContainerVertex(c, newXOrd, newYOrd);
							iterator.add(result);
							sideVertices[d.ordinal()] = result;
							return result;
						}
					}
					
					throw new LogicException("Couldn't find the element to add after!");
				}
			};
			containers.put(c, v);
		}

		return v;
	}

	public PlanarizationEdge getEdge(Connected from, Vertex vfrom, Connected to, Vertex vto,
			BiDirectional<Connected> element) {
		PlanarizationEdge e = edges.get(element);

		Direction dd = element.getDrawDirectionFrom(from);

		if (e == null) {
			if (element instanceof Connection) {
				e = new ConnectionEdge(vfrom, vto, (Connection) element, dd);
			} else if (element instanceof GeneratedLayoutElement) {
				e = new ContainerLayoutEdge(vfrom, vto, dd, from, to);
			} else {
				throw new LogicException("Unknown BiDirectional type: "+element);
			}

			if (element != null) {
				edges.put(element, e);
			}

			return e;
		} else {
			Vertex oldFrom = e.getFrom();
			oldFrom.removeEdge(e);
			vfrom.addEdge(e);
			e.setFrom(vfrom);

			Vertex oldTo = e.getTo();
			oldTo.removeEdge(e);
			vto.addEdge(e);
			e.setTo(vto);

			e.setDrawDirectionFrom(dd, vfrom);
			return e;
		}
	}

	public Vertex getVertex(Connected c) {
		Vertex v = vertices.get(c);
		if (v == null) {
			v = new ConnectedVertex(c.getID(), c);
			vertices.put(c, v);
		}

		return v;
	}

	public boolean hasCreated(Connection element) {
		return edges.get(element) != null;
	}

	/**
	 * Debug only - very slow
	 */
	@Override
	public Collection<Vertex> allVertices() {
		Collection<Vertex> out = new ArrayList<Vertex>(vertices.values());
		for (ContainerVertices cv : containers.values()) {
			for (ContainerVertex vertex : cv.getVertices()) {
				out.add(vertex);
			}
		}
		
		return out;
	}

	private Map<DiagramElement, Integer> containerDepths = new HashMap<DiagramElement, Integer>(100);

	@Override
	public int getContainerDepth(DiagramElement c) {
		if (((!(c instanceof Contained)) || ((Contained) c).getContainer() == null)) {
			return 0;
		} else {
			Integer depth = containerDepths.get(c);
			if (depth != null) {
				return depth;
			} else {
				depth = getContainerDepth(((Contained) c).getContainer()) + 1;
				containerDepths.put(c, depth);
			}
			return depth;
		}
	}

}
