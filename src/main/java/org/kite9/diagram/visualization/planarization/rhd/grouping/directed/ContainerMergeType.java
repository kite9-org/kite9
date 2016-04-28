package org.kite9.diagram.visualization.planarization.rhd.grouping.directed;

public enum ContainerMergeType { 
	
	WITHIN_LIVE_CONTAINER(0), JOINING_LIVE_CONTAINERS(20), NO_LIVE_CONTAINER(30); 

	
	private int pa;
	
	private ContainerMergeType(int priorityAdjustment) {
		this.pa = priorityAdjustment;
	}

	public int getPriorityAdjustment() {
		return pa;
	}

}