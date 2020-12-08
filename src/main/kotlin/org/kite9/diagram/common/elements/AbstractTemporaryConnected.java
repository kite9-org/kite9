package org.kite9.diagram.common.elements;

import org.kite9.diagram.common.elements.factory.AbstractDiagramElement;
import org.kite9.diagram.common.elements.factory.TemporaryConnected;
import org.kite9.diagram.model.*;

import java.util.ArrayList;
import java.util.Collection;

public abstract class AbstractTemporaryConnected extends AbstractDiagramElement implements TemporaryConnected {

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