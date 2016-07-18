package org.kite9.diagram.visualization.display.style;

import org.kite9.diagram.style.StyledDiagramElement;

/**
 * Takes a base style defined in the Stylesheet and applies it to the current element.
 * @author robmoffat
 *
 */
public interface OverrideableAttributedStyle {

	public OverrideableAttributedStyle overrideWith(StyledDiagramElement sde);
	
}
