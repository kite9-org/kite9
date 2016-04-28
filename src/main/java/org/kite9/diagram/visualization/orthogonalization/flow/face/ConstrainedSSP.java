package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.List;

import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.FlowGraph;
import org.kite9.diagram.common.algorithms.fg.FlowGraphSPP;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.Path;
import org.kite9.diagram.common.objects.Pair;
import org.kite9.diagram.visualization.orthogonalization.flow.MappedFlowGraph;

public class ConstrainedSSP extends FlowGraphSPP<MappedFlowGraph> {
	
	Node source;
	Node sink;
	List<Pair<SubdivisionNode>> splits;
	
	
	public ConstrainedSSP(Node source, Node sink, List<Pair<SubdivisionNode>> splits) {
		super();
		this.sink = sink;
		this.source = source;
		this.splits = splits;
	}

	/**
	 * This implementation makes sure that if the path completes, then
	 * it is only allowed to complete at a node which won't break the constraints
	 */
	@Override
	protected Path generateNewPath(Path p, boolean reversed, Arc a) {
		Node beginNode = p.getEndNode();
		Node endNode = a.otherEnd(beginNode);
		
		if ((beginNode==source) || (beginNode==sink)) {
			if (endNode instanceof SubdivisionNode) {
				// ensure it is in splits
				for (int i = 0; i < splits.size(); i++) {
					Pair<SubdivisionNode> psn = splits.get(i);
					if ((psn.getA()==endNode) || (psn.getB()==endNode)) {
						return super.generateNewPath(p, reversed, a);	
					}
				}
				
				return null;
			}
		}
		
		
		if ((endNode==source) || (endNode==sink)) {
			Node splitA = beginNode;
			Node splitB = getStartNodeButOne(p);
		
			for (Pair<SubdivisionNode> pair : splits) {
				if (pair.getA()==splitA) {
					if (pair.getB()==splitB) {
						return super.generateNewPath(p, reversed, a);
					}
				} else if (pair.getA()==splitB) {
					if (pair.getB()==splitA) {
						return super.generateNewPath(p, reversed, a);
					}
				}
			}
			
			return null;
		}
		
		
	
		return super.generateNewPath(p, reversed, a);
	}

	
	/**
	 * Since the path is also dependent on the split you are in, this must be part of the location
	 * object.  Otherwise, lower cost splits will override higher cost, different splits.
	 */
	@Override
	protected Object getLocation(Path path) {
		Node splitB = getStartNodeButOne(path);
		if (splitB==null) {
			return path.getLocation();
		}
		
		return new SplitLocation(splitB, path.getLocation());
	}

	static class SplitLocation {
		
		Node split;
		Object location;
		
		public SplitLocation(Node split, Object location) {
			this.split = split;
			this.location = location;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			result = prime * result + ((split == null) ? 0 : split.hashCode());
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
			SplitLocation other = (SplitLocation) obj;
			if (location == null) {
				if (other.location != null)
					return false;
			} else if (!location.equals(other.location))
				return false;
			if (split == null) {
				if (other.split != null)
					return false;
			} else if (!split.equals(other.split))
				return false;
			return true;
		}
		
		
	}
	
	public Node getStartNodeButOne(Path p) {
		if (p.getNextPathItem()!=null) {
			if (p.getNextPathItem().getNextPathItem()==null) {
				return p.getEndNode();
			}
			
			return getStartNodeButOne(p.getNextPathItem());
		} else {
			return null;
		}
	}


	@Override
	public void displayFlowInformation(FlowGraph fg) {
		// do nothing to reduce logging
	}

	
}
