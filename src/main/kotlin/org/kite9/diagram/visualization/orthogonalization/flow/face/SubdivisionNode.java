package org.kite9.diagram.visualization.orthogonalization.flow.face;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.kite9.diagram.common.algorithms.fg.Arc;
import org.kite9.diagram.common.algorithms.fg.Node;
import org.kite9.diagram.common.algorithms.fg.SimpleNode;
import org.kite9.framework.logging.LogicException;

/**
 * Used for modelling faces and parts of faces.  A subdivision node represents a corner constraint in the following ways:
 * <ol>
 * <li>To begin with, it is used to represent a face.  A face has 4 corners leaving it, and consists of 1 or more portions 
 * determined by the constraint system.
 * <li>In the nudging phase, the sources and sinks are linked to subdivisions.  The subdivision ensures that each face still has
 * only four corners leaving it, but these faces can be subdivided so that you could say, portion 1 + 2 must have only 2 corners
 * leaving them.  
 * <li>As more constrains are added to the system, the existing face constraints are subdivided further and further, until
 * all constraints are added.  
 * </ol>
 *
 * 
 * @author robmoffat
 *
 */
class SubdivisionNode extends SimpleNode {
	
	
	private String subdivision = "";
	
	
	
	public String getSubdivision() {
		return subdivision;
	}

	public void setSubdivision(String subdivision) {
		this.subdivision = subdivision;
	}

	public SubdivisionNode(String id, int supply) {
		super(id, supply, null);
		setType(ConstrainedFaceFlowOrthogonalizer.FACE_SUBDIVISION_NODE);
	}

	/**
	 * Splits the face in two, with the different arcs leading to the portions described in
	 * a and b.
	 */
	public SubdivisionNode split(List<PortionNode> aParts, List<PortionNode> bParts, int constraintNo) {
		int aflow = 0;
		int bflow = 0;
		
		SubdivisionNode nodeb = new SubdivisionNode(getId()+":"+constraintNo, 0);
	
		for (Iterator<Arc> iterator = this.getArcs().iterator(); iterator.hasNext();) {
			Arc	a = iterator.next();
			Node to = a.otherEnd(this);
			if (bParts.contains(to)) {
				// need to migrate
				bflow += a.getFlowFrom(this);
				
				if (a.getFrom()==this) {
					a.setFrom(nodeb);
				} else if (a.getTo()==this) {
					a.setTo(nodeb);
				} else {
					throw new LogicException("Arc doesn't meet with");
				}
				
				nodeb.getArcs().add(a);
				iterator.remove();
				
			} else if (!aParts.contains(to)) {
					throw new LogicException("A parts should contain this!");
			} else {
				aflow += a.getFlowFrom(this);
			}
		}
		
		
		// fix up flow info
		this.setSupply(-aflow);
		this.setFlow(aflow);
		
		nodeb.setSupply(-bflow);
		nodeb.setFlow(bflow);
		
		return nodeb;
	}
	
	public boolean meets(Collection<PortionNode> portions) {
		for (Arc a : getArcs()) {
			Node otherEnd = a.otherEnd(this);
			if (portions.contains(otherEnd))
				return true;
		}
		
		return false;
	}
}
