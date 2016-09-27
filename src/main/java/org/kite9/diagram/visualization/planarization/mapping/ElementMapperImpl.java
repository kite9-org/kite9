package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.Connected;
import org.kite9.diagram.common.elements.ConnectedVertex;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.framework.logging.LogicException;

public class ElementMapperImpl implements ElementMapper {
	
	private GridPositioner gp;

	public ElementMapperImpl(GridPositioner gp) {
		super();
		this.gp = gp;
	}

	Map<Connected, Vertex> vertices = new HashMap<Connected, Vertex>();
	Map<Container, ContainerVertices> containers = new HashMap<Container, ContainerVertices>();
	Map<BiDirectional<Connected>, PlanarizationEdge> edges = new HashMap<BiDirectional<Connected>, PlanarizationEdge>();

	public ContainerVertices getContainerVertices(final Container c) {
		ContainerVertices v = containers.get(c);
		if (v == null) {
			if (hasParentGridLayout(c)) {
				ContainerVertices parentCV = getContainerVertices((Container)c.getParent());
				OPair<BigFraction> xspan = gp.getGridXPosition(c);
				OPair<BigFraction> yspan = gp.getGridXPosition(c);
				return new SubwindowContainerVertices(c, xspan, yspan, parentCV);
			} else {
				v = new IndependentContainerVertices(c);
				containers.put(c, v);
			}
		}

		return v;
	}

	private boolean hasParentGridLayout(Container c) {
		DiagramElement parent = c.getParent();
		if (parent != null) {
			return ((Container)parent).getLayout()==Layout.GRID;
		}
		
		return false;
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
			for (ContainerVertex vertex : cv.getPerimeterVertices()) {
				out.add(vertex);
			}
		}
		
		return out;
	}

	private Map<DiagramElement, Integer> containerDepths = new HashMap<DiagramElement, Integer>(100);

	@Override
	public int getContainerDepth(DiagramElement c) {
		if (c.getParent()==null) {
			return 0;
		} else {
			Integer depth = containerDepths.get(c);
			if (depth != null) {
				return depth;
			} else {
				depth = getContainerDepth(c.getParent()) + 1;
				containerDepths.put(c, depth);
			}
			return depth;
		}
	}

}
