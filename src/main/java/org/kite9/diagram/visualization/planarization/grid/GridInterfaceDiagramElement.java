package org.kite9.diagram.visualization.planarization.grid;

import java.util.Arrays;
import java.util.List;

import org.apache.batik.css.engine.value.Value;
import org.kite9.diagram.adl.Container;
import org.kite9.diagram.adl.HintMap;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.style.AbstractDiagramElement;
import org.kite9.diagram.visualization.planarization.mgt.ContainerBorderEdge;
import org.kite9.framework.common.Kite9ProcessingException;

/**
 * This is used where two containers meet one another, and share vertices.
 * @See {@link ContainerBorderEdge}
 * 
 * @author robmoffat
 *
 */
public class GridInterfaceDiagramElement extends AbstractDiagramElement {
	
	private List<Container> containers;
	private String id; 
	
	public GridInterfaceDiagramElement(Container a, Container b) {
		this.containers = Arrays.asList(a, b);
		this.id = a.getID()+"-|-"+b.getID();
	}

	@Override
	public String getID() {
		return id;
	}
	
	public String toString() {
		return id;
	}

	@Override
	public RenderingInformation getRenderingInformation() {
		return null;
		//throw new Kite9ProcessingException();
	}

	@Override
	public Value getCSSStyleProperty(String prop) {
		return null;
	}

	@Override
	public void setRenderingInformation(RenderingInformation ri) {
		throw new Kite9ProcessingException();
	}

	@Override
	public HintMap getPositioningHints() {
		return null;
	}

	@Override
	public String getShapeName() {
		return null;
	}

	public List<Container> getContainers() {
		return containers;
	}


}