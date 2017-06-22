package org.kite9.diagram.visualization.compaction.segment;

import java.util.Set;
import java.util.stream.Collectors;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.Rectangular;

public class UnderlyingInfo {
	
	private final DiagramElement de;
	private final Side side;
	
	public DiagramElement getDiagramElement() {
		return de;
	}
	
	public Side getSide() {
		return side;
	}
	
	public UnderlyingInfo(DiagramElement de, Side start) {
		super();
		this.de = de;
		this.side = start;
	}

	@Override
	public String toString() {
		return de+"/"+side;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((de == null) ? 0 : de.hashCode());
		result = prime * result + ((side == null) ? 0 : side.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnderlyingInfo other = (UnderlyingInfo) obj;
		if (de == null) {
			if (other.de != null)
				return false;
		} else if (!de.equals(other.de))
			return false;
		if (side != other.side)
			return false;
		return true;
	}
	
}