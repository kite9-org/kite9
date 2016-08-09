package org.kite9.diagram.common.elements;

import org.kite9.diagram.adl.Label;

public interface LabelledEdge extends Edge {

	public Object getFromDecoration();
	public Object getToDecoration();

	public Label getFromLabel();
	public Label getToLabel();
//	
//	public void setFromDecoration(Object fd);
//	public void setToDecoration(Object td);
//	
//	public void setFromLabel(Label f);
//	public void setToLabel(Label t);
	

}
