package org.kite9.diagram.common.elements.vertex;

import java.util.HashSet;
import java.util.Set;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.visualization.planarization.mgt.ContainerConnectionTransform2;

/**
 * This is used by {@link ContainerConnectionTransform2}, which splits edges off from MultiCornerVertex points
 * onto their own vertices.
 * 
 * @author robmoffat
 */
public class ContainerSideVertex extends AbstractVertex implements MultiElementVertex {

	Set<DiagramElement> underlyings = new HashSet<>();	

	public ContainerSideVertex(String name) {
		super(name);
	}
	
	public ContainerSideVertex(String name, Set<DiagramElement> underlyings) {
		super(name);
		this.underlyings = underlyings;
	}

	@Override
	public boolean hasDimension() {
		return false;
	}

	@Override
	public boolean isPartOf(DiagramElement de) {
		return underlyings.contains(de);
	}
	
	public void addUnderlying(DiagramElement de) {
		if (de != null) {
			underlyings.add(de);
		}
	}

	@Override
	public Set<DiagramElement> getDiagramElements() {
		return underlyings;
	}
}