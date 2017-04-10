package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.List;

import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.diagram.common.elements.edge.Edge;
import org.kite9.diagram.visualization.planarization.Face;

class NudgeItem {

	enum NudgeItemType { SINGLE_CORNER, SINGLE_FACE, MULTI_FACE }
	
	int id;
	private Route r;
	List<PortionNode> portionsClockwise;
	List<PortionNode> portionsAntiClockwise;
	public int faceCount;
	Node source;
	Node sink;
	public NudgeItemType type;
	private Edge last;
	private Face lastFace;

	public NudgeItem(int id, Route r) {
		this.id = id;
		this.r = r;
		source = new SimpleNode("source-"+id, 0, null);
		sink = new SimpleNode("sink-"+id, 0, null);
	}

	public void calculateType() {
		if (faceCount >1) {
			type = NudgeItemType.MULTI_FACE;
		} else if (isCornerPortion(portionsAntiClockwise) || isCornerPortion(portionsClockwise)) {
			type = NudgeItemType.SINGLE_CORNER;
		} else {
			type = NudgeItemType.SINGLE_FACE;
		}
		
	}
	
	private boolean isCornerPortion(List<PortionNode> lp) {
		if (lp.size()==1) {
			PortionNode p = lp.get(0);
			Edge ces = p.getConstrainedEdgeStart();
			Edge cee = p.getConstrainedEdgeEnd();
			if ((ces.meets(cee)) && (cee != null)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	
	public int portionCount() {
		return Math.min(portionsClockwise.size(), portionsAntiClockwise.size());
	}

	public String toString() {
		Edge se = getFirstEdge();
		Edge ee = getLastEdge();
		return "NI"+id + " " + ((portionsClockwise!=null) ? portionsClockwise.toString() : "")+" "+r+" "+type+" from="+se+"("+se.getOriginalUnderlying()+") to="+ee+"("+ee.getOriginalUnderlying()+")";
	}

	public Edge getLastEdge() {
		checkLastSet();
		return last;
	}

	private void checkLastSet() {
		if (last == null) {
			Route c = r;
			while (c.getRest() != null) {
				c = c.getRest();
			}
			
			last = c.getInEdge();
			lastFace = c.getFace();
		}
	}

	public Edge getFirstEdge() {
		return r.getOutEdge();
	}
	
	public Face getFirstFace() {
		return r.getFace();
	}	
	
	public Face getLastFace() {
		checkLastSet();
		return lastFace;
	}
}
