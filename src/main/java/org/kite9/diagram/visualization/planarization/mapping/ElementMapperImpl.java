package org.kite9.diagram.visualization.planarization.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math.fraction.BigFraction;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.Diagram;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.BiDirectional;
import org.kite9.diagram.common.elements.ConnectedVertex;
import org.kite9.diagram.common.elements.MultiCornerVertex;
import org.kite9.diagram.common.elements.PlanarizationEdge;
import org.kite9.diagram.common.elements.Vertex;
import org.kite9.diagram.common.objects.OPair;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.Layout;
import org.kite9.diagram.style.BorderTraversal;
import org.kite9.diagram.visualization.planarization.grid.GridPositioner;
import org.kite9.framework.common.Kite9ProcessingException;
import org.kite9.framework.logging.LogicException;
import org.kite9.framework.serialization.CSSConstants;
import org.kite9.framework.serialization.EnumValue;

public class ElementMapperImpl implements ElementMapper {
	
	private GridPositioner gp;

	public ElementMapperImpl(GridPositioner gp) {
		super();
		this.gp = gp;
	}

	Map<DiagramElement, Vertex> singleVertices = new HashMap<DiagramElement, Vertex>();
	Map<DiagramElement, CornerVertices> cornerVertices = new HashMap<DiagramElement, CornerVertices>();
	Map<BiDirectional<Connected>, PlanarizationEdge> edges = new HashMap<BiDirectional<Connected>, PlanarizationEdge>();
	
	public boolean hasOuterCornerVertices(DiagramElement d) {
		return cornerVertices.containsKey(d);
	}
	
	public CornerVertices getOuterCornerVertices(final DiagramElement c) {
		CornerVertices v = cornerVertices.get(c);
		if (v == null) {
			if (hasParentGridLayout(c)) {
				CornerVertices parentCV = getOuterCornerVertices((Container)c.getParent());
				OPair<BigFraction> xspan = gp.getGridXPosition(c);
				OPair<BigFraction> yspan = gp.getGridYPosition(c);
				v = new SubwindowCornerVertices(c, xspan, yspan, parentCV);
				cornerVertices.put(c, v);
			} else {
				v = new IndependentCornerVertices(c);
				cornerVertices.put(c, v);
			}
		}

		return v;
	}

	private boolean hasParentGridLayout(DiagramElement c) {
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

	private Map<DiagramElement, Integer> containerDepths = new HashMap<DiagramElement, Integer>(100);

	@Override
	public int getContainerDepth(DiagramElement c) {
		DiagramElement parent = c.getParent();
		if (parent==null) {
			return 0;
		} else {
			Integer depth = containerDepths.get(c);
			if (depth != null) {
				return depth;
			} else {
				// we don't count nested grids, because these containers are parts of the whole.
				
				Layout l = ((Container) parent).getLayout();
				depth = getContainerDepth(parent) + ((l==Layout.GRID) ? 0 : 1);
				containerDepths.put(c, depth);
			}
			return depth;
		}
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
		Layout l = c.getParent() == null ? null : ((Container) c.getParent()).getLayout();
		return (l == Layout.GRID);
	}

	private boolean isElementTraversible(DiagramElement c) {
		return isElementTraversible(c, CSSConstants.TRAVERSAL_BOTTOM_PROPERTY) ||
				isElementTraversible(c, CSSConstants.TRAVERSAL_LEFT_PROPERTY) ||
				isElementTraversible(c, CSSConstants.TRAVERSAL_RIGHT_PROPERTY) ||
				isElementTraversible(c, CSSConstants.TRAVERSAL_TOP_PROPERTY);
	}

	private boolean isElementTraversible(DiagramElement c, String p) {
		EnumValue v = (EnumValue) c.getCSSStyleProperty(p);
		BorderTraversal bt = (BorderTraversal) v.getTheValue();
		return (bt == BorderTraversal.ALWAYS);
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
	
}
