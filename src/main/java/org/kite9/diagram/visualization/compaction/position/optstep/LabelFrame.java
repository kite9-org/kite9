package org.kite9.diagram.visualization.compaction.position.optstep;

import org.kite9.diagram.position.BasicRenderingInformation;
import org.kite9.diagram.position.Dimension2D;
import org.kite9.diagram.position.RectangleRenderingInformation;
import org.kite9.diagram.position.RenderingInformation;
import org.kite9.diagram.primitives.AbstractIdentifiableDiagramElement;
import org.kite9.diagram.primitives.Label;
import org.kite9.diagram.visualization.display.style.DirectionalValues;
import org.kite9.framework.logging.LogicException;
import org.w3c.dom.Node;

/**
 * A container around a label which manages the padding of the label away from the things that
 * surround it.
 * 
 * @author robmoffat
 *
 */
public class LabelFrame extends AbstractIdentifiableDiagramElement {

	private static final long serialVersionUID = 3287011609590439600L;
	DirectionalValues padding;
	RectangleRenderingInformation rri;
	Label label;
	RectangleRenderingInformation label_rri;
	
	public Label getLabel() {
		return label;
	}

	public LabelFrame(Label l, DirectionalValues p) {
		super();
		this.label = l;
		this.label_rri = (RectangleRenderingInformation) l.getRenderingInformation();
		this.rri = new BasicRenderingInformation() {

			private static final long serialVersionUID = 1L;

			@Override
			public void setPosition(Dimension2D position) {
				super.setPosition(position);
				label_rri.setPosition(new Dimension2D(position.x() + padding.getLeft(), position.y() + padding.getTop()));
			}

			@Override
			public void setSize(Dimension2D size) {
				super.setSize(size);
				label_rri.setSize(new Dimension2D(size.x() - padding.getRight() - padding.getLeft(), size.y() - padding.getTop() - padding.getBottom()));
			}
			
		};
		this.padding = p;
	}

	public RectangleRenderingInformation getRenderingInformation() {
		return rri;
	}

	public void setRenderingInformation(RenderingInformation ri) {
		throw new LogicException("You can't directly set the rendering information on a label frame");
	}

	public DirectionalValues getPadding() {
		return padding;
	}

	@Override
	public String getShapeName() {
		return null;
	}

	@Override
	protected Node newNode() {
		// TODO Auto-generated method stub
		return null;
	}
}
