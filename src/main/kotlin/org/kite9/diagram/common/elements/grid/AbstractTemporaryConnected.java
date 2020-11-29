package org.kite9.diagram.common.elements.grid;

import java.util.ArrayList;
import java.util.Collection;

import org.kite9.diagram.dom.model.AbstractDiagramElement;
import org.kite9.diagram.model.Connected;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.Container;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Temporary;

public abstract class AbstractTemporaryConnected extends AbstractDiagramElement implements Connected, Temporary {

	protected String id;
	private Collection<Connection> links = new ArrayList<>();

	public AbstractTemporaryConnected(DiagramElement parent) {
		super(parent);
	}

	@Override
	public String getID() {
		return id;
	}

	public AbstractTemporaryConnected() {
		super();
	}

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

	@Override
	public Container getContainer() {
		return (Container) getParent();
	}

}