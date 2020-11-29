package org.kite9.diagram.visualization.planarization.rhd.layout;

import org.kite9.diagram.model.position.Layout;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.CompoundGroup;
import org.kite9.diagram.visualization.planarization.rhd.GroupPhase.Group;
import org.kite9.diagram.visualization.planarization.rhd.position.RoutableHandler2D;
import org.kite9.diagram.logging.Kite9Log;

public class AbstractPlacementApproach {

	protected double score;
	protected boolean natural;

	public double getScore() {
		return score;
	}

	protected Layout aDirection;

	public boolean isNatural() {
		return natural;
	}
	
	protected CompoundGroup overall;
	protected GroupPhase gp;
	protected Kite9Log log;
	protected RoutableHandler2D rh;
	protected boolean setHoriz;
	protected boolean setVert;

	protected AbstractPlacementApproach(Kite9Log log, GroupPhase gp, Layout aDirection, CompoundGroup overall, 
				RoutableHandler2D rh, boolean setHoriz, boolean setVert, boolean natural) {
		super();
		this.aDirection = aDirection;
		this.overall = overall;
		this.gp = gp;
		this.log = log;
		this.rh = rh;
		this.setHoriz = setHoriz;
		this.setVert = setVert;
		this.natural = natural;
	}

	public void choose() {
		overall.setLayout(aDirection);
		Group before = ((aDirection == Layout.LEFT) || (aDirection == Layout.UP)) ? overall.getB() : overall.getA();
		Group after = ((aDirection == Layout.LEFT) || (aDirection == Layout.UP)) ? overall.getA() : overall.getB();
		log.send(log.go() ? null : "Placement " + aDirection + " chosen for --- " + before + "   ---     " + after);		
	}

}