package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.Label;
import org.kite9.diagram.position.Direction;
import org.kite9.diagram.position.RouteRenderingInformation;
import org.kite9.diagram.position.RouteRenderingInformationImpl;
import org.kite9.framework.logging.LogicException;

/**
 * This is an edge created in the planarization process.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractPlanarizationEdge extends AbstractEdge implements LabelledEdge, PlanarizationEdge {

	private static final String EDGE_CONT_MSG = "Edges don't have contradiction flags set";
	private static final long serialVersionUID = 3701310306763145747L;

	public int getBendCost() {
		return 1;
	}

	protected Object fromDecoration, toDecoration;
	protected Label fromLabel, toLabel;
	protected boolean reversed = false;
		
	
    public AbstractPlanarizationEdge(Vertex from, Vertex to, Object fromDecoration, Label fromLabel,
			Object toDecoration, Label toLabel, Direction d) {
    	super(from, to , d);
		this.fromDecoration = fromDecoration;
		this.toDecoration = toDecoration;
		if (fromLabel!=null) {
			this.fromLabel = fromLabel;
			this.fromLabel.setParent(this);
		}
		if (toLabel!=null) {
			this.toLabel = toLabel;
			this.toLabel.setParent(this);
		}
		
		from.addEdge(this);
		to.addEdge(this);
	}

	public int getUnderlyingPart() {
		throw new UnsupportedOperationException("Not defined for this class");
	}
	
	@Override
	public String toString() {
		return "["+getID()+"/"+from+"-"+to+"]";
	}

	public void reverseDirection() {
		Vertex temp = from;
		from = to;
		to = temp;
		
		Object dec = fromDecoration;
		fromDecoration = toDecoration;
		toDecoration = dec;
		
		Label dec2 = fromLabel;
		fromLabel = toLabel;
		toLabel = dec2;
	
		if (drawDirection!=null)
			drawDirection = Direction.reverse(drawDirection);
	
		reversed = !reversed;
	}

	public Object getFromDecoration() {
		return fromDecoration;
	}

	public Object getToDecoration() {
		return toDecoration;
	}

	protected Label toText = null;

	public Label getFromLabel() {
		return fromLabel;
	}

	public Label getToLabel() {
		return toLabel;
	}
	
	private RouteRenderingInformation rri;

	public RouteRenderingInformation getRenderingInformation() {
		if (rri==null) {
			rri = new RouteRenderingInformationImpl() {

				private static final long serialVersionUID = 1L;

				@Override
				public boolean isContradicting() {
					throw new LogicException(EDGE_CONT_MSG);
				}

				@Override
				public void setContradicting(boolean b) {
					throw new LogicException(EDGE_CONT_MSG);
				}
			
			};
		}
		
		return rri;
	}
	
	public void remove() {
		from.removeEdge(this);
		to.removeEdge(this);
	}
	
	public abstract RemovalType removeBeforeOrthogonalization();
	
	public abstract int getCrossCost();

	public boolean isReversed() {
		return reversed;
	}
	
	protected boolean straight = true;

	public boolean isStraightInPlanarization() {
		return straight;
	}

	public void setStraight(boolean straight) {
		this.straight = straight;
	}

}
