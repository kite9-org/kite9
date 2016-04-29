package org.kite9.diagram.visualization.display.complete;

import java.util.ArrayList;
import java.util.List;

import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.DiagramElement;
import org.kite9.diagram.visualization.display.ComponentDisplayer;
import org.kite9.diagram.visualization.display.style.Stylesheet;

/**
 * Extends the basic displayer to allow ordering of component drawing, via a command buffer.
 * This ensures that some component displayers display output in front of others.
 * 
 * @author robmoffat
 *
 */
public abstract class AbstractOrderedDisplayer extends AbstractGraphicsSourceRendererCompleteDisplayer {


	public AbstractOrderedDisplayer(Stylesheet ss, boolean buffer) {
		super(ss, buffer);
	}

	class DrawItem {
		DiagramElement de;
		RenderingInformation ri;
	}

	private List<DrawItem> toDraw = new ArrayList<DrawItem>(100);

	public void finish() {
		paintComponents();
	}

	protected void paintComponents() {
		for (ComponentDisplayer disp : displayers) {
			int idx = 0;
			while (idx < toDraw.size()) {
				DrawItem d = toDraw.get(idx);
				idx++;
				if (disp.canDisplay(d.de)) {
					disp.getLayer().startElement(d.de);
					disp.draw(d.de, d.ri);
					disp.getLayer().endElement();
				}
			}
		}
	}

	public void draw(DiagramElement element, RenderingInformation ri) {
		if (isVisibleElement(element)) {
			DrawItem di = new DrawItem();
			di.de = element;
			di.ri = ri;
			toDraw.add(di);
		}
	}

}
