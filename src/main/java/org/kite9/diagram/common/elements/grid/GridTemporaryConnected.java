package org.kite9.diagram.common.elements.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.Connected;
import org.kite9.diagram.adl.Connection;
import org.kite9.diagram.adl.DiagramElement;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.impl.AbstractDiagramElement;

/**
 * A placeholder for spaces in a grid layout which are unoccupied.
 * 
 * @author robmoffat
 *
 */
public class GridTemporaryConnected extends AbstractDiagramElement implements Connected {

	private final String id;
	
	public GridTemporaryConnected(DiagramElement parent, int x, int y) {
		super(parent);
		this.id = parent.getID()+"-g-"+x+"-"+y;
	}

	@Override
	public String getID() {
		return id;
	}

	@Override
	public Value getCSSStyleProperty(String prop) {
		return null;
	}

	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		this.rri = (RectangleRenderingInformation) ri;
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public String getShapeName() {
		return null;
	}
	
	private Collection<Connection> links = new ArrayList<>();

	@Override
	public Collection<Connection> getLinks() {
		return links;
	}

	public Connection getConnectionTo(Connected c) {
		for (Connection link : getLinks()) {
			if (link.meets(c)) {
				return link;
			}
		}

		return null;
	}

	public boolean isConnectedDirectlyTo(Connected c) {
		return getConnectionTo(c) != null;
	}

	private RectangleRenderingInformation rri = new RectangleRenderingInformationImpl(null, null, null, null, false);
	
	@Override
	public RectangleRenderingInformation getRenderingInformation() {
		return rri;
	}
	
}
