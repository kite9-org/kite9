package org.kite9.diagram.visualization.display;

import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.RenderingInformation;

public interface Displayer {

	void draw(DiagramElement element, RenderingInformation ri);
	
}