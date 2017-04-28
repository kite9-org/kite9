package org.kite9.diagram.common.elements.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.kite9.diagram.batik.element.AbstractDiagramElement;
import org.kite9.diagram.common.HintMap;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.position.RectangleRenderingInformation;
import org.kite9.diagram.model.position.RectangleRenderingInformationImpl;
import org.kite9.diagram.model.position.RenderingInformation;
import org.kite9.diagram.model.style.ContainerPosition;
import org.kite9.diagram.model.style.DiagramElementSizing;

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

	@Override
	public double getMargin(Direction d) {
		return 0;
	}

	@Override
	public ContainerPosition getContainerPosition() {
		return null;
	}

	@Override
	public double getPadding(Direction d) {
		return 0;
	}

	@Override
	public DiagramElementSizing getSizing() {
		return DiagramElementSizing.UNSPECIFIED;
	}

}
