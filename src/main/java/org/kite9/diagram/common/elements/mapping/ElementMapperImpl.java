package org.kite9.diagram.common.elements.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.edge.PlanarizationEdge;
import org.kite9.diagram.common.elements.grid.GridPositioner;
import org.kite9.diagram.common.elements.vertex.ConnectedVertex;
import org.kite9.diagram.common.elements.vertex.MultiCornerVertex;
import org.kite9.diagram.common.elements.vertex.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.Diagram;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.model.style.BorderTraversal;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;

public class ElementMapperImpl implements ElementMapper {
	
	private GridPositioner gp;

	public ElementMapperImpl(GridPositioner gp) {
		super();
		this.gp = gp;
	}

	Map<DiagramElement, Vertex> singleVertices = new HashMap<DiagramElement, Vertex>();
	Map<DiagramElement, CornerVertices> cornerVertices = new HashMap<DiagramElement, CornerVertices>();
	Map<DiagramElement, BaseGridCornerVertices> baseGrids = new HashMap<>();
	Map<BiDirectional<Connected>, PlanarizationEdge> edges = new HashMap<BiDirectional<Connected>, PlanarizationEdge>();
	
	public boolean hasOuterCornerVertices(DiagramElement d) {
		return cornerVertices.containsKey(d);
	}
	
	public CornerVertices getOuterCornerVertices(final DiagramElement c) {
		CornerVertices v = cornerVertices.get(c);
		if (v == null) {
			if (isEmbeddedWithinGrid(c)) {
				BaseGridCornerVertices parentCV = getBaseGridCornerVertices((Container)c.getParent());
				v = createSubGridCornerVertices(c, parentCV);
			} else {
				v = new IndependentCornerVertices(c, c.getDepth());
				cornerVertices.put(c, v);
			}
		}

		return v;
	}

	private SubGridCornerVertices createSubGridCornerVertices(final DiagramElement c, BaseGridCornerVertices parentCV) {
		OPair<BigFraction> xspan = gp.getGridXPosition(c);
		OPair<BigFraction> yspan = gp.getGridYPosition(c);
		SubGridCornerVertices v = new SubGridCornerVertices(c, xspan, yspan, parentCV, c.getDepth());
		cornerVertices.put(c, v);
		return v;
	}

	private BaseGridCornerVertices getBaseGridCornerVertices(Container c) {
		BaseGridCornerVertices bgcv = baseGrids.get(c);
		if (bgcv == null) {
			bgcv = new BaseGridCornerVertices(c, c.getDepth()+1);
			baseGrids.put(c, bgcv);
		}
		return bgcv;
		
	}

	private boolean isEmbeddedWithinGrid(DiagramElement c) {
		DiagramElement parent = c.getParent();
		if ((parent != null) && (parent instanceof Container)) {
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
			} else if (element instanceof GeneratedLayoutConnection) {
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

	public Vertex getPlanarizationVertex(DiagramElement c) {
		Vertex v = singleVertices.get(c);
		if (v == null) {
			
			if (c instanceof Connected) {
				v = new ConnectedVertex(c.getID(), (Connected) c);
				singleVertices.put(c, v);
			} else {
				throw new Kite9ProcessingException("Not sure how to create vertex for "+c);
			}
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
		Collection<Vertex> out = new ArrayList<Vertex>(singleVertices.values());
		for (CornerVertices cv : cornerVertices.values()) {
			for (MultiCornerVertex vertex : cv.getVerticesAtThisLevel()) {
				out.add(vertex);
			}
		}
		
		return out;
	}
	
	public boolean requiresPlanarizationCornerVertices(DiagramElement c) {
		if (c instanceof Diagram) {
			return true;
		}
		// does anything inside it have connections?
		if (c instanceof Container) {
			for (DiagramElement de : ((Container) c).getContents()) {
				if (hasNestedConnections(de)) {
					return true;
				}
			}
			
			// are connections allowed to pass through it?
			boolean canTraverse = isElementTraversible(c);
			if (canTraverse && hasNestedConnections(c)) {
				return true;
			}
		}
		
		// is it embedded in a grid?  If yes, use corners
		if (c instanceof Connected) {
			Layout l = c.getParent() == null ? null : ((Container) c.getParent()).getLayout();
			return (l == Layout.GRID);
		}
		
		return false;
	}

	private boolean isElementTraversible(DiagramElement c) {
		return isElementTraversible(c, Direction.UP) ||
				isElementTraversible(c,  Direction.DOWN) ||
				isElementTraversible(c,  Direction.LEFT) ||
				isElementTraversible(c, Direction.RIGHT);
	}

	private boolean isElementTraversible(DiagramElement c, Direction d) {
		if (c instanceof Container) {
			return ((Container) c).getTraversalRule(d) == BorderTraversal.ALWAYS;
		}
		
		return false;
	}

	Map<DiagramElement, Boolean> hasConnections = new HashMap<>();
	
	public boolean hasNestedConnections(DiagramElement c) {
		if (hasConnections.containsKey(c)) {
			return hasConnections.get(c);
		} 
		
		boolean has = false;
		
		if (c instanceof Connected) {
			has = ((Connected)c).getLinks().size() > 0;
		}
		
		if ((has == false) && (c instanceof Container)) {
			for (DiagramElement de : ((Container)c).getContents()) {
				if (hasNestedConnections(de)) {
					has = true;
					break;
				}
			}
		}
		
		hasConnections.put(c, has);
		return has;
	}

	@Override
	public GridPositioner getGridPositioner() {
		return gp;
	}
	
}
