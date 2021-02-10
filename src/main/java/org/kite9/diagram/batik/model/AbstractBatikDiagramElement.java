package org.kite9.diagram.batik.model;

import org.kite9.diagram.common.Kite9XMLProcessingException;
import org.kite9.diagram.dom.bridge.ElementContext;
import org.kite9.diagram.dom.css.CSSConstants;
import org.kite9.diagram.dom.elements.Kite9XMLElement;
import org.kite9.diagram.dom.model.AbstractDOMDiagramElement;
import org.kite9.diagram.dom.painter.Painter;
import org.kite9.diagram.dom.processors.XMLProcessor;
import org.kite9.diagram.dom.transform.SVGTransformer;
import org.kite9.diagram.dom.transform.TransformFactory;
import org.kite9.diagram.model.Connection;
import org.kite9.diagram.model.DiagramElement;
import org.kite9.diagram.model.position.CostedDimension2D;
import org.kite9.diagram.model.position.Direction;
import org.kite9.diagram.model.style.ContentTransform;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents {@link DiagramElement}s that contain SVG that will need rendering, and the method to render them
 * (delegating to Painter and Transform implementations).
 *  
 * @author robmoffat
 *
 */
public abstract class AbstractBatikDiagramElement extends AbstractDOMDiagramElement {
	
	public AbstractBatikDiagramElement(Element el, DiagramElement parent, ElementContext ctx, Painter p, ContentTransform t) {
		super(el, parent);
		this.p = p;
		this.p.setDiagramElement(this);
		this.ctx = ctx;
		this.defaultTransform = t;
	}

	protected Painter p;
	protected ElementContext ctx;
	
	private ContentTransform defaultTransform;
	protected SVGTransformer transformer;
	protected double margin[] = new double[4];
	protected double padding[] = new double[4];
	
	protected void initialize() {
		initializeDirectionalCssValues(padding, CSSConstants.KITE9_CSS_PADDING_PROPERTY_PREFIX);
		initializeDirectionalCssValues(margin, CSSConstants.KITE9_CSS_MARGIN_PROPERTY_PREFIX);
		initTransform();
	}

	protected double getCssDoubleValue(String prop) {
		return ctx.getCssDoubleValue(prop,  getTheElement());
	}

	protected Element paintElementToDocument(Document d, XMLProcessor postProcessor) {
		return transformer.postProcess(p, d, postProcessor);
	}

	private void initTransform() {
		ContentTransform t = (ContentTransform) ctx.getCSSStyleProperty(CSSConstants.CONTENT_TRANSFORM, getTheElement());
		this.transformer = TransformFactory.INSTANCE.initializeTransformer(this, t, this.defaultTransform);
	}

	@Override
	public Painter getPainter() {
		return p;
	}

	public double getMargin(Direction d) {
		ensureInitialized();
		return margin[d.ordinal()];
	}

	public double getPadding(Direction d) {
		ensureInitialized();
		return padding[d.ordinal()];
	}

	protected void initializeDirectionalCssValues(double[] vals, String prefix) {
		vals[Direction.UP.ordinal()] = getCssDoubleValue(prefix+CSSConstants.TOP);
		vals[Direction.DOWN.ordinal()] = getCssDoubleValue(prefix+CSSConstants.BOTTOM);
		vals[Direction.LEFT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.LEFT);
		vals[Direction.RIGHT.ordinal()] = getCssDoubleValue(prefix+CSSConstants.RIGHT);	
	}

	protected CostedDimension2D getSizeBasedOnPadding() {
		double left = getPadding(Direction.LEFT);
		double right = getPadding(Direction.RIGHT);
		double up = getPadding(Direction.UP);
		double down = getPadding(Direction.DOWN);
		return new CostedDimension2D(left + right, up + down, CostedDimension2D.Companion.getUNBOUNDED());
	}


	/*protected void ensureNoChildKite9Elements(Element e) {
		if (e instanceof Kite9XMLElement) {
			if (((Kite9XMLElement) e).iterator().hasNext()) {
				throw new Kite9XMLProcessingException(e+" shouldn't have nested Kite9 elements - it's supposed to be a leaf (svg elements only). ", e);
			}
		} else {
			throw new Kite9XMLProcessingException("How is "+e+" not a Kite9 element? ", e);
		}
	}*/

	protected Kite9XMLProcessingException contextualException(String reason, Throwable t) {
		return new Kite9XMLProcessingException(reason, t, getTheElement());
	}

	protected Kite9XMLProcessingException contextualException(String reason) {
		return contextualException(reason, null);
	}

	@Override
	protected List<DiagramElement> initContents() {
		List<DiagramElement> contents = new ArrayList<>();
		for (DiagramElement de : ctx.getChildDiagramElements(getTheElement(), this)) {
			if (de instanceof Connection) {
				registerConnection((Connection) de);
			} else if (de != null) {
				contents.add(de);
			}
		}

		return contents;
	}
}
